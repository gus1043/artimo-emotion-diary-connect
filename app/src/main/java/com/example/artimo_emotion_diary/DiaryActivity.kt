package com.example.artimo_emotion_diary

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class DiaryActivity : AppCompatActivity() {

    private lateinit var todayimage: ImageView
    private lateinit var todayemoji: ImageView
    private lateinit var todaydiary: TextView
    private lateinit var todaycaption: TextView
    private lateinit var tomainbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        todayimage = findViewById(R.id.todayimage)
        todayemoji = findViewById(R.id.todayemoji)
        todaydiary = findViewById(R.id.todaydiary)
        todaycaption = findViewById(R.id.todaycaption)
        tomainbtn = findViewById(R.id.tomainbtn)

        val year = intent.getIntExtra("YEAR", 0)
        val month = intent.getIntExtra("MONTH", 0)
        val day = intent.getIntExtra("DAY", 0)
        val emoji = intent.getStringExtra("emoji") ?: ""
        val caption = intent.getStringExtra("caption") ?: ""
        val imageUriString = intent.getStringExtra("imageUri") ?: ""
        val diary = intent.getStringExtra("diary") ?: ""

        Log.d("DiaryActivity", "Received - YEAR: $year, MONTH: $month, DAY: $day, emoji: $emoji, caption: $caption, imageUri: $imageUriString, diary: $diary")

        // 안드로이드 버전 로그 추가
        val androidVersion = Build.VERSION.RELEASE
        Log.d("DiaryActivity", "Android Version: $androidVersion")
        Log.d("DiaryActivity", "Received - YEAR: $year, MONTH: $month, DAY: $day, emoji: $emoji, caption: $caption, imageUri: $imageUriString, diary: $diary")

        // 날짜 텍스트뷰에 날짜 정보를 설정
        val date: TextView = findViewById(R.id.date)
        date.text = getString(R.string.date_format_emoji, year, month, day)

        // 이모지 로드
        try {
            val inputStream = assets.open(emoji)
            val drawable = Drawable.createFromStream(inputStream, null)
            todayemoji.setImageDrawable(drawable)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            todayemoji.setImageResource(R.drawable.logo)
        }

        // 일기 내용, 캡션
        todaydiary.text = diary
        todaycaption.text = caption

        // 이미지 URI 처리
        if (imageUriString.isNotEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            try {
                // URI 권한을 퍼미션으로 설정
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(imageUri, takeFlags)

                // URI를 통해 이미지 입력 스트림 열기
                contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    todayimage.setImageBitmap(bitmap)
                } ?: run {
                    Log.e("DiaryActivity", "Input stream for image URI is null")
                    todayimage.setImageResource(R.drawable.logo) // 입력 스트림이 null인 경우 기본 이미지 설정
                }
            } catch (e: SecurityException) {
                Log.e("DiaryActivity", "SecurityException: ${e.message}")
                todayimage.setImageResource(R.drawable.logo) // 권한 문제 발생 시 기본 이미지 설정
            } catch (e: Exception) {
                Log.e("DiaryActivity", "Error setting image URI: ${e.message}")
                todayimage.setImageResource(R.drawable.logo) // 기타 예외 발생 시 기본 이미지 설정
            }

        } else {
            todayimage.setImageResource(R.drawable.logo) // 이미지가 없을 경우 기본 이미지 설정
        }

        tomainbtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }
}
