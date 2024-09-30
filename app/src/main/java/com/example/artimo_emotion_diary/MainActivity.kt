package com.example.artimo_emotion_diary

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.artimo_emotion_diary.roomdb.DiaryDaoDatabase
import com.example.artimo_emotion_diary.roomdb.DiaryTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var calendarPagerAdapter: CalendarPagerAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var diaryApiService: DiaryApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 기본값 설정 (0)
        if (!sharedPreferences.contains("default")) {
            editor.putInt("default", 0) // 기본값 설정
            editor.apply()
        }

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // 기본 URL 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        diaryApiService = retrofit.create(DiaryApiService::class.java)

        // SharedPreferences에서 값 읽기
        val defaultValue = sharedPreferences.getInt("default", 0)

        // 기본값이 0일 경우 데이터 가져오기, 아니면 캘린더 설정
        if (defaultValue == 0) {
            setupCalendar()
            fetchDiaryList()
        } else {
            setupCalendar()
        }

    }

    private fun fetchDiaryList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = diaryApiService.getDiaryList()
                if (response.isSuccessful) {
                    val diaries = response.body() ?: emptyList()
                    diaries.forEach { diary ->
                        Log.d("MainActivity", "Diary: $diary")

                        val createdAt = diary.createdAt

                        // 먼저 날짜 부분만 추출
                        val datePart = createdAt.substringBefore("T") // "2024-09-28"

                        // "-"를 기준으로 분리하여 year, month, date 추출
                        val (year, month, date) = datePart.split("-").map { it.toInt() }

                        //emotion type 연결
                        val emotiontype=getEmojiImageFile(diary.emotionType.lowercase())

                        val db = DiaryDaoDatabase.getDatabase(applicationContext)
                        val diaryDAO = db?.diaryDAO()

                        val diaryEntity = DiaryTable(
                            year = year,
                            month = month,
                            date = date,
                            emoji = emotiontype,
                            diary = diary.contents,
                            imageUri = diary.dimgUrl,
                            caption = diary.caption
                        )
                        diaryDAO?.insert(diaryEntity)

                        // 다이어리 항목이 성공적으로 저장된 후 SharedPreferences 값을 1로 변경
                        with(sharedPreferences.edit()) {
                            putInt("default", 1) // 값 변경
                            apply() // 비동기적으로 저장
                        }
                        // 모든 다이어리가 저장된 후 캘린더 업데이트
                        runOnUiThread {
                            setupCalendar()
                        }
                        val allDiaries = diaryDAO?.selectALL()
                        allDiaries?.forEach {
                            Log.d("DiaryEntry", "Year: ${it.year}, Month: ${it.month}, Day: ${it.date}, Emoji: ${it.emoji}, Diary: ${it.diary}, ImageUri: ${it.imageUri}, Caption: ${it.caption}")
                        }

                    }
                } else {
                    Log.d("MainActivity", "Failed to fetch diaries, Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching diary list: ${e.message}")
            }
        }
    }

    //캘린더 세팅
    private fun setupCalendar() {
        viewPager = findViewById(R.id.view_pager)
        calendarPagerAdapter = CalendarPagerAdapter(this)
        viewPager.adapter = calendarPagerAdapter

        val currentDate = LocalDate.now()
        viewPager.setCurrentItem(calendarPagerAdapter.getPageIndex(currentDate.year, currentDate.monthValue), false)

        // 필요한 경우 추가적인 초기화 작업 수행
    }

    fun getEmojiImageFile(emotionType: String): String {
        val emotionTypeToImageMap = mapOf(
            "happy" to "a_Happy.png",
            "love" to "c_Love.png",
            "sad" to "e_Sad.png",
            "sick" to "f_Sick.png",
            "sorrow" to "g_Sorrow.png",
            "depressed" to "h_Depressed.png",
            "upset" to "i_Upset.png",
            "tears" to "j_Tears.png",
            "laugh" to "k_Laugh.png",
            "surprise" to "l_Surprise.png",
            "sadlaugh" to "m_SadLaugh.png",
            "wonderful" to "n_Wonderful.png",
            "joyful" to "o_Joyful.png",
            "calm" to "p_Calm.png",
            "fun" to "q_Fun.png",
            "unexpected" to "r_Unexpected.png",
            "unhappy" to "t_Unhappy.png",
            "embarrassed" to "u_Embarrassed.png",
            "angry" to "v_Angry.png",
            "hard" to "w_Hard.png",
            "shocking" to "x_Shocking.png",
            "hardday" to "y_HardDay.png",
            "angel" to "z_Angel.png",
            "demon" to "z2_Demon.png"
        )

        return emotionTypeToImageMap[emotionType] ?: "default.png" // 기본값 설정
    }
}
