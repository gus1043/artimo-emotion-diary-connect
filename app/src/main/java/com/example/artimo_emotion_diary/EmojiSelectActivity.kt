package com.example.artimo_emotion_diary

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.artimo_emotion_diary.EmojiPagerAdapter
import com.example.artimo_emotion_diary.R
import java.io.IOException

class EmojiSelectActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var emojiPagerAdapter: EmojiPagerAdapter
    private var emojiList = mutableListOf<String>()

    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emojiselect)

        year = intent.getIntExtra("YEAR", 0)
        month = intent.getIntExtra("MONTH", 0)
        day = intent.getIntExtra("DAY", 0)

        // 날짜 설정
        val date: TextView = findViewById(R.id.date)
        date.text = getString(R.string.date_format_emoji, year, month, day)

        // ViewPager2 초기화
        viewPager = findViewById(R.id.viewPager)

        // 이미지 파일 리스트 가져오기 (assets 폴더 내의 이미지 파일명들)
        try {
            val assetManager: AssetManager = assets
            val images = assetManager.list("") ?: emptyArray()

            Log.d("EmojiList", images.joinToString(", "))

            // 필터링하여 이미지 파일만 추가
            emojiList.addAll(images.filter { it.lowercase().endsWith(".jpg") || it.lowercase().endsWith(".png") })

        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 어댑터 설정
        emojiPagerAdapter = EmojiPagerAdapter(this, emojiList, year, month, day)
        Log.d("EmojiSelectActivity", "$emojiList")
        viewPager.adapter = emojiPagerAdapter
    }
}
