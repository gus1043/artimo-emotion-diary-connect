package com.example.artimo_emotion_diary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.artimo_emotion_diary.roomdb.DiaryDaoDatabase
import com.example.artimo_emotion_diary.roomdb.DiaryTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WriteActivity : AppCompatActivity() {

    private lateinit var todayimage: ImageView
    private lateinit var todayemoji: ImageView
    private lateinit var diary: EditText
    private lateinit var caption: EditText
    private lateinit var writebtn: Button

    private var imageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 권한이 허용된 경우 이미지 선택 허용
                selectImage()
            } else {
                Log.e("WriteActivity", "Storage permission denied")
                // 권한이 거부된 경우
            }
        }
    // 이미지 선택 결과를 처리 ActivityResultLauncher
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                todayimage.setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        todayimage = findViewById(R.id.todayimage)
        todayemoji = findViewById(R.id.todayemoji)
        diary = findViewById(R.id.diary)
        caption = findViewById(R.id.caption)
        writebtn = findViewById(R.id.writebtn)

        val year = intent.getIntExtra("YEAR", 0)
        val month = intent.getIntExtra("MONTH", 0)
        val day = intent.getIntExtra("DAY", 0)
        val emoji = intent.getStringExtra("emoji") ?: ""

        // 날짜, 이모지 설정
        val date: TextView = findViewById(R.id.date)
        date.text = getString(R.string.date_format_emoji, year, month, day)

        loadEmoji(emoji)

        // 이미지 선택 버튼
        todayimage.setOnClickListener {
            // 권한을 요청, 이미지를 선택
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 안드로이드 13 이상
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                // 안드로이드 12 이하
                selectImage()
            }
        }

        // 작성 완료 버튼
        writebtn.setOnClickListener {
            val diaryText = diary.text.toString()
            val captionText = caption.text.toString()

            // db 저장
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = DiaryDaoDatabase.getDatabase(applicationContext)
                    val diaryDAO = db?.diaryDAO()

                    val diaryEntity = DiaryTable(
                        year = year,
                        month = month,
                        date = day,
                        emoji = emoji,
                        diary = diaryText,
                        imageUri = imageUri?.toString(),
                        caption = captionText
                    )
                    diaryDAO?.insert(diaryEntity)

                    val allDiaries = diaryDAO?.selectALL()
                    allDiaries?.forEach {
                        Log.d("DiaryEntry", "Year: ${it.year}, Month: ${it.month}, Day: ${it.date}, Emoji: ${it.emoji}, Diary: ${it.diary}, ImageUri: ${it.imageUri}, Caption: ${it.caption}")
                    }
                }
            }

            val intent = Intent(this, DiaryActivity::class.java).apply {
                putExtra("YEAR", year)
                putExtra("MONTH", month)
                putExtra("DAY", day)
                putExtra("emoji", emoji)
                putExtra("imageUri", imageUri?.toString())
                putExtra("diary", diaryText)
                putExtra("caption", captionText)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish()
        }
    }

    private fun loadEmoji(emoji: String) {
        try {
            val inputStream = assets.open(emoji)
            val drawable = Drawable.createFromStream(inputStream, null)
            todayemoji.setImageDrawable(drawable)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            todayemoji.setImageResource(R.drawable.logo)
        }
    }

    private fun selectImage() {
        selectImageLauncher.launch("image/*")
    }
}
