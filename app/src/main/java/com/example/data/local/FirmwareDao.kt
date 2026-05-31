package com.example.data.local

import androidx.room.*
import com.example.data.model.FirmwarePackage
import kotlinx.coroutines.flow.Flow

@Dao
interface FirmwareDao {
    @Query("SELECT * FROM firmware_packages ORDER BY importDate DESC")
    fun getAllPackages(): Flow<List<FirmwarePackage>>

    @Query("SELECT * FROM firmware_packages WHERE id = :id LIMIT 1")
    suspend fun getPackageById(id: Long): FirmwarePackage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: FirmwarePackage): Long

    @Update
    suspend fun updatePackage(pkg: FirmwarePackage)

    @Delete
    suspend fun deletePackage(pkg: FirmwarePackage)

    @Query("DELETE FROM firmware_packages")
    suspend fun deleteAllPackages()
}
