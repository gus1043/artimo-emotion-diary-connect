package com.example.artimo_emotion_diary

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class VideoProcessingService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val diaryId = intent?.getStringExtra("DIARY_ID")
        val contents = intent?.getStringExtra("CONTENTS")

        if (diaryId != null && contents != null) {
            coroutineScope.launch {
                processVideo(diaryId, contents)
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun processVideo(diaryId: String, contents: String) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(DiaryApiService::class.java)
        val requestBody = VideoRequest(contents = contents)

        try {
            val apiResponse = apiService.createTextToVideo(diaryId, requestBody)

            if (apiResponse.isSuccessful) {
                Log.d("VideoProcessingService", "이미지 생성 성공: ${apiResponse.body()}")
            } else {
                Log.d("VideoProcessingService", "이미지 생성 실패: ${apiResponse.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.d("VideoProcessingService", "오류: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
