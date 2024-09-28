package com.example.artimo_emotion_diary.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
@Dao
interface DiaryDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(diarytable: DiaryTable)

    @Update
    fun update(diarytable: DiaryTable)

    @Delete
    fun delete(diarytable: DiaryTable)

    @Query("SELECT * FROM DiaryTable")
    fun selectALL(): List<DiaryTable>

    @Query("SELECT * FROM DiaryTable WHERE year = :year AND month = :month AND date = :date")
    fun getDiaryEntry(year: Int, month: Int, date: Int): LiveData<DiaryTable?> //LiveData는 데이터의 변경 사항 관찰 가능

}
