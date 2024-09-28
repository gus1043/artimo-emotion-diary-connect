package com.example.artimo_emotion_diary.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "DiaryTable",
    primaryKeys = ["year", "month", "date"]
)
data class DiaryTable(
    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "month")
    val month: Int,

    @ColumnInfo(name = "date")
    val date: Int,

    @ColumnInfo(name = "emoji")
    val emoji: String,

    @ColumnInfo(name = "diary")
    val diary: String,

    @ColumnInfo(name = "imageUri")
    val imageUri: String?,

    @ColumnInfo(name = "caption")
    val caption: String
)

