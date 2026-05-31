package com.example.data.repository

import com.example.data.local.FirmwareDao
import com.example.data.local.FlashLogDao
import com.example.data.model.FirmwarePackage
import com.example.data.model.FlashLog
import kotlinx.coroutines.flow.Flow

class FirmwareRepository(
    private val firmwareDao: FirmwareDao,
    private val flashLogDao: FlashLogDao
) {
    val allPackages: Flow<List<FirmwarePackage>> = firmwareDao.getAllPackages()
    val allLogs: Flow<List<FlashLog>> = flashLogDao.getAllLogs()

    suspend fun getPackageById(id: Long): FirmwarePackage? = firmwareDao.getPackageById(id)
    suspend fun insertPackage(pkg: FirmwarePackage): Long = firmwareDao.insertPackage(pkg)
    suspend fun updatePackage(pkg: FirmwarePackage) = firmwareDao.updatePackage(pkg)
    suspend fun deletePackage(pkg: FirmwarePackage) = firmwareDao.deletePackage(pkg)
    suspend fun deleteAllPackages() = firmwareDao.deleteAllPackages()

    suspend fun getLogById(id: Long): FlashLog? = flashLogDao.getLogById(id)
    suspend fun insertLog(log: FlashLog): Long = flashLogDao.insertLog(log)
    suspend fun updateLog(log: FlashLog) = flashLogDao.updateLog(log)
    suspend fun deleteLogById(id: Long) = flashLogDao.deleteLogById(id)
    suspend fun deleteAllLogs() = flashLogDao.deleteAllLogs()
}
