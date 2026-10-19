package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calcu_techniques",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["questionId"]),
        Index(value = ["subjectId", "hash"], unique = true)
    ]
)
data class CalcuTechniqueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val questionId: Long? = null,
    val title: String,
    val keystrokes: String,
    val calculatorModel: String = "Canon F-789SGA",
    val notes: String = "",
    val hash: String,
    val createdAt: Long = System.currentTimeMillis()
)
