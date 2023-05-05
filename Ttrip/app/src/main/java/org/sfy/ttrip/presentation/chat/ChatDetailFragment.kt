package org.sfy.ttrip.presentation.chat

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.sfy.ttrip.MainActivity
import org.sfy.ttrip.R
import org.sfy.ttrip.common.util.BindingAdapters.setProfileImg
import org.sfy.ttrip.databinding.FragmentChatDetailBinding
import org.sfy.ttrip.presentation.base.BaseFragment

@AndroidEntryPoint
class ChatDetailFragment : BaseFragment<FragmentChatDetailBinding>(R.layout.fragment_chat_detail) {

    private val args by navArgs<ChatDetailFragmentArgs>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private val chatDetailAdapter by lazy { ChatDetailAdapter() }

    override fun initView() {
        (activity as MainActivity).hideBottomNavigation(true)
        initRecyclerView()
        setChatInfo()
        setChatRoom(args.chatId)
    }

    private fun initRecyclerView() {
        binding.rvChat.apply {
            adapter = chatDetailAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun setChatInfo() {
        binding.apply {
            ivOtherProfile.setProfileImg(args.imagePath)
            tvOtherNickname.text = args.nickname
            tvPostContent.text = args.articleTitle
        }
    }

    private fun setChatRoom(chatId: Int) {
        chatViewModel.chatDetail.observe(viewLifecycleOwner) { response ->
            response?.let { chatDetailAdapter.setChat(it) }
        }
        chatViewModel.getChatDetail(chatId)
    }
}