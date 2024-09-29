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
import com.bumptech.glide.Glide
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

        //타이틀 설정
        val title: TextView = findViewById(R.id.title)
        val imageToTitleMap = mapOf(
            "a_Happy.png" to "행복 가득한 하루였어요.",
            "b_Happy.png" to "행복 가득한 하루였어요.",
            "c_Love.png" to "사랑 가득한 하루를 보냈어요.",
            "d_Love.png" to "사랑에 빠진 듯한 하루였어요.",
            "e_Sad.png" to "조금 속상한 하루였어요.",
            "f_Sick.png" to "몸이나 마음이 조금 아팠어요.",
            "g_Sorrow.png" to "슬픔이 있었던 하루였어요.",
            "h_Depressed.png" to "우울한 하루를 보냈어요.",
            "i_Upset.png" to "속상한 일이 있었어요.",
            "j_Tears.png" to "눈물이 흐르는 일이 있었어요.",
            "k_Laugh.png" to "웃음 가득한 하루를 보냈어요.",
            "l_Surprise.png" to "깜짝 놀랄 만한 일이 있었어요.",
            "m_SadLaugh.png" to "웃픈 하루를 보냈어요.",
            "n_Wonderful.png" to "멋진 하루를 보냈어요.",
            "o_Joyful.png" to "유쾌한 하루를 보냈어요.",
            "p_Calm.png" to "마음이 편안해지는 하루였어요.",
            "q_Fun.png" to "재미있는 하루를 보냈군요?",
            "r_Unexpected.png" to "예상치 못한 일이 생겼어요.",
            "s_Upset.png" to "속상한 일이 있었어요.",
            "t_Unhappy.png" to "불만이 쌓였던 하루였군요.",
            "u_Embarrassed.png" to "당황스러운 하루였어요.",
            "v_Angry.png" to "화가 났던 날이었어요.",
            "w_Hard.png" to "정말 힘든 하루를 보냈군요.",
            "x_Shocking.png" to "충격적인 일이 있었어요.",
            "y_HardDay.png" to "오늘 하루 정말 힘들었어요.",
            "z_Angel.png" to "천사같은 하루를 보냈어요.",
            "z2_Demon.png" to "악마같은 하루를 보냈어요."
        )

        // 이모지 파일명에 맞는 타이틀을 가져와서 TextView에 설정
        val diaryTitle = imageToTitleMap[emoji] ?: "기본 타이틀"
        title.text = diaryTitle

        // 이미지 URI 처리
        if (imageUriString.isNotEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            try {
                // URI가 HTTP URL인지 확인
                if (imageUri.scheme.equals("http", true) || imageUri.scheme.equals("https", true)) {
                    // S3와 같은 HTTP URL로부터 이미지 로드 (Glide 사용)
                    Glide.with(this)
                        .load(imageUriString)
                        .placeholder(R.drawable.blank)
                        .error(R.drawable.logo) // 로딩 실패 시 기본 이미지 설정
                        .into(todayimage)
                } else {
                    // 로컬 URI 처리
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
