package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flash_logs")
data class FlashLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceName: String,
    val deviceSerial: String,
    val firmwareName: String,
    val flashType: String, // "FASTBOOT", "SIDELOAD", "ODIN"
    val status: String, // "SUCCESS", "FAILED", "PENDING"
    val timestamp: Long = System.currentTimeMillis(),
    val logs: String, // Raw sequence commands + responses
    val errorMessage: String? = null,
    val aiTroubleshoot: String? = null // AI suggestions if failed
)
