package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val description: String = "",
    val colorHex: String = "#0284C7",
    val createdAt: Long = System.currentTimeMillis()
)
