package com.example.ui.viewmodel

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartitionItem(
    val name: String,
    val sizeBytes: Long,
    val description: String,
    val isRequired: Boolean = false,
    var isSelected: Boolean = true,
    var isExtracted: Boolean = false,
    val blockCount: Int = kotlin.random.Random.nextInt(100, 5001)
) {
    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024
            if (kb < 1024) return "$kb KB"
            val mb = kb / 1024
            if (mb < 1024) return "$mb MB"
            val gb = mb.toDouble() / 1024
            return String.format("%.2f GB", gb)
        }
}
