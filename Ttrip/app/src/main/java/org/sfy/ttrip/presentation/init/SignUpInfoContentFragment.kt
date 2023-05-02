package org.sfy.ttrip.presentation.init

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.sfy.ttrip.R
import org.sfy.ttrip.databinding.FragmentSignUpInfoContentBinding
import org.sfy.ttrip.domain.entity.user.SurveyItem
import org.sfy.ttrip.presentation.base.BaseFragment
import java.io.File

class SignUpInfoContentFragment :
    BaseFragment<FragmentSignUpInfoContentBinding>(R.layout.fragment_sign_up_info_content) {

    private var bannerPosition = -1
    private val userInfoTestListAdapter by lazy {
        UserInfoTestListAdapter(this::onUserTestClicked)
    }
    private val userInfoViewModel by activityViewModels<UserInfoViewModel>()
    private val fromAlbumActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        result.data?.let {
            if (it.data != null) {
                userInfoViewModel.setProfileImg(
                    it.data as Uri,
                    File(absolutelyPath(it.data, requireContext()))
                )
            }
        }
    }
    private var testList = listOf(
        SurveyItem(
            "번화한 도시보다 자연 풍경이 좋다", 0, 0
        ),
        SurveyItem(
            "미리 여행 계획을 세워야 마음이 편하다", 1, 0
        ),
        SurveyItem(
            "경유보다 직항을 선호한다", 2, 0
        ),
        SurveyItem(
            "숙소에는 경비를 줄이고 싶다", 3, 0
        ),
        SurveyItem(
            "맛집은 무조건 찾아가야 한다", 4, 0
        ),
        SurveyItem(
            "택시보다 대중교통을 이용한다", 5, 0
        ),
        SurveyItem(
            "개인별 경비를 선호한다", 6, 0
        ),
        SurveyItem(
            "타이트한 여행을 추구한다", 7, 0
        ),
        SurveyItem(
            "명소 관광보다 쇼핑이 좋다", 8, 0
        )
    )

    override fun initView() {
        binding.viewModel = userInfoViewModel

        initListener()
        setTextWatcher()

        bannerPosition = arguments?.getInt("banner_position")!!
        with(bannerPosition) {
            val contentData =
                listOf(
                    binding.clUserInfoNickName,
                    binding.clUserInfoAge,
                    binding.clUserInfoSex,
                    binding.clUserInfoProfile,
                    binding.clUserInfoTest,
                )

            when (bannerPosition) {
                0 -> {
                    changeVisibility(0, contentData)
                }
                1 -> {
                    binding.etUserInfoAge.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            userInfoViewModel.postAge(binding.etUserInfoAge.text.toString())
                        }

                        override fun afterTextChanged(s: Editable?) {
                            userInfoViewModel.postAge(binding.etUserInfoAge.text.toString())
                        }
                    })

                    changeVisibility(1, contentData)
                }
                2 -> {
                    if (userInfoViewModel.userSex.value == "FEMALE") {
                        binding.apply {
                            tvUserInfoSexMale.setBackgroundResource(R.drawable.bg_rect_whisper_radius20)
                            tvUserInfoSexFemale.setBackgroundResource(R.drawable.bg_rect_honey_suckle_radius20)
                        }
                    } else if (userInfoViewModel.userSex.value == "MALE") {
                        binding.apply {
                            tvUserInfoSexMale.setBackgroundResource(R.drawable.bg_rect_honey_suckle_radius20)
                            tvUserInfoSexFemale.setBackgroundResource(R.drawable.bg_rect_whisper_radius20)
                        }
                    }
                    changeVisibility(2, contentData)
                }
                3 -> {
                    binding.etUserInfoIntroduction.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            userInfoViewModel.postIntro(binding.etUserInfoIntroduction.text.toString())
                        }

                        override fun afterTextChanged(s: Editable?) {
                            userInfoViewModel.postIntro(binding.etUserInfoIntroduction.text.toString())
                        }
                    })

                    userInfoViewModel.profileImgUri.observe(this@SignUpInfoContentFragment) {
                        when (it) {
                            null -> binding.ivUserInfoProfilePhotoBlack.visibility = View.VISIBLE
                            else -> binding.ivUserInfoProfilePhotoBlack.visibility = View.GONE
                        }
                    }

                    changeVisibility(3, contentData)
                }
                4 -> {
                    changeVisibility(4, contentData)
                }
            }
        }

        binding.rvUserInfoTest.apply {
            adapter = userInfoTestListAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            userInfoTestListAdapter.setTestInfo(testList)
        }
    }

    private fun initListener() {
        binding.apply {
            tvUserInfoCheckNickName.setOnClickListener {
                if (userInfoViewModel.nickname.value == null) {
                    showToast("닉네임을 입력해주세요.")
                } else {
                    lifecycleScope.launch {
                        val async = userInfoViewModel.checkDuplication()
                        showDuplicateInfo(async)
                    }
                }
            }

            tvUserInfoSexMale.setOnClickListener {
                tvUserInfoSexMale.setBackgroundResource(R.drawable.bg_rect_honey_suckle_radius20)
                tvUserInfoSexFemale.setBackgroundResource(R.drawable.bg_rect_whisper_radius20)
                userInfoViewModel.postSex("MALE")
            }

            tvUserInfoSexFemale.setOnClickListener {
                tvUserInfoSexMale.setBackgroundResource(R.drawable.bg_rect_whisper_radius20)
                tvUserInfoSexFemale.setBackgroundResource(R.drawable.bg_rect_honey_suckle_radius20)
                userInfoViewModel.postSex("FEMALE")
            }

            clUserInfoProfilePhoto.setOnClickListener { setAlbumView() }
        }
    }

    private fun showDuplicateInfo(async: Int) {
        if (userInfoViewModel.isDuplicate.value == true) {
            showToast("중복된 닉네임입니다.")
        } else {
            showToast("사용할 수 있는 닉네임입니다.")
        }
    }

    private fun setTextWatcher() {
        binding.etUserInfoNickName.addTextChangedListener {
            userInfoViewModel.nickname.value = binding.etUserInfoNickName.text.toString()
            userInfoViewModel.returnDuplicationTrue()
        }
    }

    private fun onUserTestClicked(position: Int, record: Int) {
        testList[position].score = record
        userInfoTestListAdapter.setTestInfo(testList)
        userInfoViewModel.checkSurvey(position, record)
    }

    private fun changeVisibility(position: Int, bannerData: List<ConstraintLayout>) {
        for (i in 0..4) {
            bannerData[i].visibility = View.GONE
        }
        bannerData[position].visibility = View.VISIBLE
    }

    private fun setAlbumView() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                fromAlbumActivityLauncher.launch(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                )
            }
            else -> {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_STORAGE_PERMISSION
                )
            }
        }
    }

    private fun absolutelyPath(path: Uri?, context: Context): String {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        val index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        val result = c?.getString(index!!)
        c?.close()
        return result!!
    }

    companion object {
        const val REQUEST_READ_STORAGE_PERMISSION = 1
    }
}