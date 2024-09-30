package com.example.artimo_emotion_diary

data class CreateDiary(
    val title: String,
    val caption: String,
    val contents: String,
    val emotionType: String,
    val createdAt : String
)
