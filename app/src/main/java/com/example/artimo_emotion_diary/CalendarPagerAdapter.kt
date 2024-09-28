package com.example.artimo_emotion_diary

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.time.LocalDate

class CalendarPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        // swipe 한도 설정
        return 1000
    }

    override fun createFragment(position: Int): Fragment {
        // 현재 날짜를 기준으로 baseDate 설정
        val baseDate = LocalDate.now()
        // 현재 위치에서 가운데 위치를 기준으로 몇 개월 차이인지 계산
        val monthsOffset = position - (1000 / 2)
        // baseDate에서 monthsOffset 만큼의 월을 더한 날짜를 monthDate로 설정
        val monthDate = baseDate.plusMonths(monthsOffset.toLong())

        return CalendarFragment.newInstance(monthDate.year, monthDate.monthValue)
    }

    fun getPageIndex(year: Int, month: Int): Int {
        val baseDate = LocalDate.now()
        val targetDate = LocalDate.of(year, month, 1)
        return (targetDate.year - baseDate.year) * 12 + (targetDate.monthValue - baseDate.monthValue) + (1000 / 2)
    }
}
