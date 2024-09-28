package com.example.artimo_emotion_diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artimo_emotion_diary.roomdb.DiaryDAO
import com.example.artimo_emotion_diary.roomdb.DiaryDaoDatabase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class CalendarFragment : Fragment() {

    // 요일 이름 리스트
    private val dayNames = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )

    private val itemList = arrayListOf<Date>()
    private lateinit var headerRow: RecyclerView
    private lateinit var calendarList: RecyclerView
    private lateinit var listAdapter: CalendarAdapter
    private lateinit var diaryDAO: DiaryDAO

    companion object {
        private const val ARG_YEAR = "year"
        private const val ARG_MONTH = "month"

        fun newInstance(year: Int, month: Int): CalendarFragment {
            return CalendarFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_YEAR, year)
                    putInt(ARG_MONTH, month)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DAO 초기화
        diaryDAO = DiaryDaoDatabase.getDatabase(requireContext()).diaryDAO()

        // 연도와 월을 설정하고 텍스트 뷰에 표시
        val dateTextView: TextView = view.findViewById(R.id.month_year_tv)
        val year = arguments?.getInt(ARG_YEAR) ?: LocalDate.now().year
        val month = arguments?.getInt(ARG_MONTH) ?: LocalDate.now().monthValue
        dateTextView.text = getString(R.string.date_format_main, year, month)

        // 요일 헤더 설정
        headerRow = view.findViewById(R.id.header_row)
        headerRow.layoutManager = GridLayoutManager(context, 7)
        headerRow.adapter = WeekAdapter(dayNames)

        // 캘린더 리스트 설정
        calendarList = view.findViewById(R.id.calendar_list)
        calendarList.layoutManager = GridLayoutManager(context, 7)

        // 어댑터 초기화
        listAdapter = CalendarAdapter(
            requireContext(),
            itemList,
            object : CalendarAdapter.OnItemClickListener {
                override fun onItemClick(date: Date) {
                    Log.d("CalendarFragment", "Item clicked: $date")

                    //  DAO에서 LiveData를 관찰하여 다이어리 데이터를 가져옴
                    diaryDAO.getDiaryEntry(year, month, date.date).observe(viewLifecycleOwner, { diaryData ->
                        if (diaryData != null && diaryData.emoji != null) {
                            val intent = Intent(context, DiaryActivity::class.java).apply {
                                putExtra("YEAR", year)
                                putExtra("MONTH", month)
                                putExtra("DAY", date.date)
                                putExtra("emoji", diaryData.emoji)
                                putExtra("imageUri", diaryData.imageUri.toString())
                                putExtra("diary", diaryData.diary)
                                putExtra("caption", diaryData.caption)

                                Log.d("CalendarFragment", "Caption: ${diaryData.caption}")
                                Log.d("CalendarFragment", "Image URI: ${diaryData.imageUri}")
                                Log.d("CalendarFragment", "Diary: ${diaryData.diary}")
                            }
                            startActivity(intent)
                        } else {
                            // 다이어리 데이터가 없으면 EmojiSelectActivity로 이동
                            val intent = Intent(context, EmojiSelectActivity::class.java).apply {
                                putExtra("YEAR", year)
                                putExtra("MONTH", month)
                                putExtra("DAY", date.date)
                            }
                            startActivity(intent)
                        }
                    })
                }
            }
        )
        calendarList.adapter = listAdapter

        setListView(year, month)
    }

    // 캘린더 리스트를 설정
    private fun setListView(year: Int, month: Int) {

        val date = LocalDate.of(year, month, 1)
        val lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth())
        val dayOfMonthCount = lastDayOfMonth.dayOfMonth
        val firstDayOfWeek = date.dayOfWeek.value % 7 // 요일을 인덱스로(0월요일, 6일요일)

        // 기존의 itemList를 초기화
        itemList.clear()

        // 매달 1일 시작 전에 빈칸 설정
        for (i in 0 until firstDayOfWeek) {
            itemList.add(Date(0, 0, 0, null))
        }

        // 실제 달의 날짜를 추가
        for (i in 1..dayOfMonthCount) {
            itemList.add(Date(year, month, i, null))
        }

        // 마지막 주를 완성하기 위한 마지막 날 이후 필요한 빈 칸을 추가
        val totalCells = itemList.size
        val totalWeeks = (totalCells + 6) / 7 * 7 // Calculate total cells needed to fill the calendar grid
        for (i in totalCells until totalWeeks) {
            itemList.add(Date(0, 0, 0, null)) // Empty placeholder for alignment
        }

        // itemList 이모지 데이터
        for (i in 1..dayOfMonthCount) {
            diaryDAO.getDiaryEntry(year, month, i).observe(viewLifecycleOwner, { diaryEntry ->
                Log.d("CalendarFragment", "Diary entry for $year-$month-$i: $diaryEntry")

                val emoji = diaryEntry?.emoji
                Log.d("CalendarFragment", "Processing date: $date, Emoji: $emoji")

                // itemList의 해당 날짜 객체를 업데이트
                val index = firstDayOfWeek + i - 1
                if (index < itemList.size) {
                    val currentDate = itemList[index]
                    itemList[index] = Date(currentDate.year, currentDate.month, currentDate.date, emoji)
                    listAdapter.notifyItemChanged(index)
                }
            })
        }

        Log.d("CalendarFragment", "itemList: $itemList")

        listAdapter.notifyDataSetChanged()
    }


}
