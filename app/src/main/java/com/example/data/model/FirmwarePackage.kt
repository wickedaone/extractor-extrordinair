package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "firmware_packages")
data class FirmwarePackage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val deviceModel: String,
    val androidVersion: String,
    val buildId: String,
    val fileFormat: String, // "PAYLOAD_BIN", "FASTBOOT_ZIP", "ODIN_TAR"
    val sizeBytes: Long,
    val importDate: Long = System.currentTimeMillis(),
    val partitionCount: Int,
    val extractedCount: Int = 0,
    val filePath: String,
    val isExtracted: Boolean = false,
    val partitionsJson: String // Serialized partition list: name, size, type, isExtracted
) : Serializable
