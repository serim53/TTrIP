package org.sfy.ttrip.presentation.chat

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString
import org.sfy.ttrip.ApplicationClass
import org.sfy.ttrip.MainActivity
import org.sfy.ttrip.R
import org.sfy.ttrip.common.util.BindingAdapters.setProfileImg
import org.sfy.ttrip.databinding.FragmentChatDetailBinding
import org.sfy.ttrip.domain.entity.chat.ChatDetail
import org.sfy.ttrip.presentation.base.BaseFragment

@AndroidEntryPoint
class ChatDetailFragment : BaseFragment<FragmentChatDetailBinding>(R.layout.fragment_chat_detail) {

    private lateinit var chatDetailAdapter: ChatDetailAdapter
    private val args by navArgs<ChatDetailFragmentArgs>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private var chatList: MutableList<ChatDetail> = mutableListOf()

    private val client = OkHttpClient()
    lateinit var webSocket: WebSocket
    val listener = WebSocketListener()

    override fun initView() {
        (activity as MainActivity).hideBottomNavigation(true)
        initListener()
        initRecyclerView()
        setChatInfo()
        setChatRoom(args.chatId)
        sendMessage()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomNavigation(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
    }

    override fun onStop() {
        super.onStop()
        disconnectSocket()
    }

    @SuppressLint("ResourceAsColor")
    private fun initListener() {
        binding.apply {
            binding.etChat.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    binding.rvChat.scrollToPosition(chatList.size - 1)
                }
            })
            clMatching.setOnClickListener {
                chatViewModel.chatMatch(args.chatId, args.memberId)
                it.setBackgroundResource(R.drawable.bg_rect_honey_suckle_radius40)
                it.isEnabled = false
                tvMatching.setTextColor(R.color.black)
                tvMatching.text = "매칭완료"
                ivMatching.visibility = View.GONE
                showToast("매칭 요청되었습니다!")
            }
            ivBack.setOnClickListener {
                popBackStack()
            }
        }
    }

    private fun initRecyclerView() {
        chatDetailAdapter = ChatDetailAdapter(chatList)
        binding.rvChat.apply {
            adapter = chatDetailAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun setChatInfo() {
        chatViewModel.chatId = args.chatId
        binding.apply {
            ivOtherProfile.setProfileImg(args.imagePath)
            tvOtherNickname.text = args.nickname
            tvPostContent.text = args.articleTitle
            if (args.articleTitle == "") {
                clPostContent.visibility = View.GONE
            }
        }
        if (args.isMatch) {
            binding.apply {
                clMatching.setBackgroundResource(R.drawable.bg_rect_honey_suckle_radius40)
                clMatching.isEnabled = false
                tvMatching.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                tvMatching.text = "매칭완료"
                ivMatching.visibility = View.GONE
            }
        } else {
            binding.apply {
                clMatching.setBackgroundResource(R.drawable.bg_rect_honey_suckle_transparent_radius40_stroke1)
                clMatching.isEnabled = true
                tvMatching.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.honey_suckle
                    )
                )
                tvMatching.text = "매칭"
            }
        }
        if (args.status != "D") {
            binding.tvPostContent.setOnClickListener {
                navigate(
                    ChatDetailFragmentDirections.actionChatDetailFragmentToBoardDetailFragment(
                        args.articleId,
                        -1
                    )
                )
            }
        }
        if (args.nickname == "알수없음") {
            binding.apply {
                clMatching.visibility = View.GONE
                ivSendMessage.visibility = View.GONE
                etChat.isEnabled = false
                etChat.hint = "대화 상대가 퇴장했습니다."
            }
        }
    }

    private fun sendMessage() {
        binding.ivSendMessage.setOnClickListener {
            lifecycleScope.launch {
                sendMessage(binding.etChat.text.toString())
                binding.etChat.setText("")
            }
        }
    }

    private fun setChatRoom(chatId: Int) {
        chatViewModel.chatDetail.observe(viewLifecycleOwner) { response ->
            response?.let {
                chatDetailAdapter.setChat(it)
                chatList = it.toMutableList()
                binding.rvChat.scrollToPosition(chatList.size - 1)
            }
        }
        chatViewModel.getChatDetail(chatId)
        connectSocket(chatId, ApplicationClass.preferences.userId!!, args.memberId)
    }


    private fun connectSocket(chatroomId: Int, memberUuid: String, targetUuid: String) {
        val request = Request.Builder()
            .url("ws://k8d104.p.ssafy.io:8081/ws/chat/$chatroomId/$memberUuid/$targetUuid")
            .build()
        webSocket = client.newWebSocket(request, listener)
    }

    private fun disconnectSocket() {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
    }

    private fun sendMessage(message: String) {
        webSocket.send(message)
    }

    inner class WebSocketListener : okhttp3.WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("Socket", "onOpen")
            super.onOpen(webSocket, response)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("Socket", "Receiving : $text")
            val gson = Gson()
            val chatResponse = gson.fromJson(text, ChatDetail::class.java)
            chatList.add(chatResponse)
            requireActivity().runOnUiThread {
                binding.rvChat.adapter = ChatDetailAdapter(chatList.toList())
                binding.rvChat.scrollToPosition(chatList.size - 1)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("Socket", "Receiving bytes : $bytes")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("Socket", "Closing : $code / $reason")
            webSocket.close(1000, null)
            webSocket.cancel()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("Socket", "Error : " + t.message)
        }
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}