package com.example.artimo_emotion_diary

import com.example.artimo_emotion_diary.Diary
import com.example.artimo_emotion_diary.DiaryID
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DiaryApiService {
    @Multipart
    @POST("diary/create")
    fun submitDiary(
        @Part("diary") diary: RequestBody,
        @Part imageFile: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("diary/list")
    suspend fun getDiaryList(): Response<List<Diary>>

    @POST("diary/text-to-video/{id}")
    suspend fun createTextToVideo(@Path("id") id: String, @Body requestBody: VideoRequest): Response<VideoResponse>
}

