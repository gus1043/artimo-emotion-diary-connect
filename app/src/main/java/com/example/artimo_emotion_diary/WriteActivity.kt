package com.example.artimo_emotion_diary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.artimo_emotion_diary.roomdb.DiaryDaoDatabase
import com.example.artimo_emotion_diary.roomdb.DiaryTable
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

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

                // 이미지 파일을 비트맵으로 열기
                openImage(imageUri!!)
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
        val titleText = intent.getStringExtra("title") ?: ""

        // 날짜, 이모지, 타이틀 설정
        val date: TextView = findViewById(R.id.date)
        date.text = getString(R.string.date_format_emoji, year, month, day)

        loadEmoji(emoji)

        val title: TextView = findViewById(R.id.title)
        title.text = titleText

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

            // 서버에 다이어리 전송
            sendFormDataToServer(diaryText, emoji, imageUri, titleText, captionText)

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
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }

    // 서버 전송
    fun sendFormDataToServer(diaryText: String, emoji: String, imageUri: Uri?, title: String, captionText: String) {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)

        // _ 뒤의 글자만 추출
        val emotionType = emoji.substringAfter("_").substringBefore(".png")

        val diaryObject = CreateDiary(
            title = title,
            caption = captionText,
            contents = diaryText,
            emotionType = emotionType
        )

        // diary object를 json으로 만듦
        val gson = Gson()
        val diaryJson = gson.toJson(diaryObject)

        // RequestBody로 만듦
        val diaryRequestBody = RequestBody.create("application/json".toMediaTypeOrNull(), diaryJson)

        // 이미지 경로 변환
        val filePath = imageUri?.let { getFilePathFromUri(this, it) }
        val file = filePath?.let { File(it) }

        if (file != null && file.exists()) {
            val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), file)
            val imagePart = MultipartBody.Part.createFormData("imageFile", file.name, requestFile)

            // 로그로 요청 본문 출력
            Log.d("WriteActivity", "Diary JSON: $diaryJson")
            Log.d("WriteActivity", "Image Path: ${file.absolutePath}")

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            // API service
            val apiService = retrofit.create(DiaryApiService::class.java)

            // 데이터 보내기
            val call = apiService.submitDiary(
                RequestBody.create("application/json".toMediaTypeOrNull(), diaryJson),
                imagePart
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseString = response.body()?.string()
                        Log.d("WriteActivity", "성공: $responseString")

                        // JSON에서 diaryId 추출
                        val responseData = Gson().fromJson(responseString, DiaryID::class.java)
                        val diaryId = responseData.diaryId
                        val contents = responseData.contents
                        Log.d("WriteActivity", "diaryId: $diaryId, contents: $contents")

                        // Activity의 컨텍스트를 사용하여 Intent 생성, Service로 백그라운드에서 실행되게 할 것임.
                        val intent = Intent(this@WriteActivity, VideoProcessingService::class.java).apply {
                            putExtra("DIARY_ID", diaryId.toString())
                            putExtra("CONTENTS", contents)
                        }
                        startService(intent)

                    } else {
                        Log.d("WriteActivity", "실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("WriteActivity", "오류: ${t.message}")
                }
            })
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

    private fun openImage(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            todayimage.setImageBitmap(bitmap) // 이미지뷰에 설정
            inputStream?.close()
        } catch (e: Exception) {
            Log.e("WriteActivity", "Error opening image: ${e.message}")
        }
    }

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.getString(columnIndex)
        } else {
            null
        }.also {
            cursor?.close()
        }
    }
}
