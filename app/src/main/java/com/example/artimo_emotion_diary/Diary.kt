package com.example.artimo_emotion_diary

data class Diary(
    val title: String,
    val caption: String,
    val contents: String,
    val dimgUrl: String?,
    val emotionType: String,
    val createdAt: String,
    val updatedAt: String
)

