package org.sfy.ttrip.data.remote.datasorce.user

import okhttp3.MultipartBody

interface UserRemoteDataSource {

    suspend fun postUserInfo(
        nickName: String,
        intro: String,
        gender: String,
        profileFile: MultipartBody.Part?,
        markerFile: MultipartBody.Part?,
        age: String,
        fcmToken: String
    ): UserProfileDialogResponse

    suspend fun checkDuplication(nickName: String): CheckDuplicationResponse

    suspend fun postUserInfoTest(body: UserInfoTestRequest)

    suspend fun getUserProfile(nickName: String): UserProfileDialogResponse

    suspend fun postUserFcm(fcmToken: String)

    suspend fun postEvaluateUser(matchHistoryId: String, rate: Int)

    suspend fun postReportUser(
        reportContext: String,
        reportedNickname: String,
        matchHistoryId: String
    )
}