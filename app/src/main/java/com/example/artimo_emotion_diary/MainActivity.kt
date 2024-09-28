package com.example.artimo_emotion_diary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var calendarPagerAdapter: CalendarPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        calendarPagerAdapter = CalendarPagerAdapter(this)
        viewPager.adapter = calendarPagerAdapter

        val currentDate = LocalDate.now()
        viewPager.setCurrentItem(calendarPagerAdapter.getPageIndex(currentDate.year, currentDate.monthValue), false)
    }

}
