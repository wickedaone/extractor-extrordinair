package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.FirmwarePackage
import com.example.data.model.FlashLog
import com.example.data.repository.FirmwareRepository
import com.example.data.repository.GeminiRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirmwareViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "FirmwareViewModel"

    private val db = AppDatabase.getDatabase(application)
    private val repo = FirmwareRepository(db.firmwareDao(), db.flashLogDao())
    private val geminiRepo = GeminiRepository()

    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val partitionListType = Types.newParameterizedType(List::class.java, PartitionItem::class.java)
    private val partitionAdapter = moshi.adapter<List<PartitionItem>>(partitionListType)

    private val p2pListType = Types.newParameterizedType(List::class.java, P2PSharedFile::class.java)
    private val p2pAdapter = moshi.adapter<List<P2PSharedFile>>(p2pListType)

    private val threadListType = Types.newParameterizedType(List::class.java, ForumThread::class.java)
    private val threadAdapter = moshi.adapter<List<ForumThread>>(threadListType)

    // --- UI Navigation / UI States ---
    private val _activeTab = MutableStateFlow("unpack") // "unpack", "flash", "copilot", "logs"
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    // --- Database Flows ---
    private val _firmwarePackages = MutableStateFlow<List<FirmwarePackage>>(emptyList())
    val firmwarePackages: StateFlow<List<FirmwarePackage>> = _firmwarePackages.asStateFlow()

    private val _flashLogs = MutableStateFlow<List<FlashLog>>(emptyList())
    val flashLogs: StateFlow<List<FlashLog>> = _flashLogs.asStateFlow()

    init {
        viewModelScope.launch {
            repo.allPackages.collectLatest { pkgs ->
                if (pkgs.isEmpty()) {
                    // Seed initial demo firmware packages for quick friction-free UI testing
                    seedDemoPackages()
                } else {
                    _firmwarePackages.value = pkgs
                }
            }
        }
        viewModelScope.launch {
            repo.allLogs.collectLatest { logs ->
                _flashLogs.value = logs
            }
        }
        // Load persistency data for Community swarm and Help board
        loadP2PAndForumData()
    }

    // --- Active states inside Firmware Unpacker (Extract Tab) ---
    private val _selectedPackage = MutableStateFlow<FirmwarePackage?>(null)
    val selectedPackage: StateFlow<FirmwarePackage?> = _selectedPackage.asStateFlow()

    private val _activePartitions = MutableStateFlow<List<PartitionItem>>(emptyList())
    val activePartitions: StateFlow<List<PartitionItem>> = _activePartitions.asStateFlow()

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _currentExtractingPartition = MutableStateFlow<String?>(null)
    val currentExtractingPartition: StateFlow<String?> = _currentExtractingPartition.asStateFlow()

    private val _extractionProgress = MutableStateFlow(0f)
    val extractionProgress: StateFlow<Float> = _extractionProgress.asStateFlow()

    private val _unpackerConsoleLogs = MutableStateFlow<List<String>>(emptyList())
    val unpackerConsoleLogs: StateFlow<List<String>> = _unpackerConsoleLogs.asStateFlow()

    // --- Active states inside Flasher Component (Flash Tab) ---
    private val _usbDeviceState = MutableStateFlow<DeviceState>(DeviceState.Disconnected)
    val usbDeviceState: StateFlow<DeviceState> = _usbDeviceState.asStateFlow()

    private val _isFlashing = MutableStateFlow(false)
    val isFlashing: StateFlow<Boolean> = _isFlashing.asStateFlow()

    private val _flashingProgress = MutableStateFlow(0f)
    val flashingProgress: StateFlow<Float> = _flashingProgress.asStateFlow()

    private val _flashingConsoleLogs = MutableStateFlow<List<String>>(emptyList())
    val flashingConsoleLogs: StateFlow<List<String>> = _flashingConsoleLogs.asStateFlow()

    private val _showFlashResultDialog = MutableStateFlow(false)
    val showFlashResultDialog: StateFlow<Boolean> = _showFlashResultDialog.asStateFlow()

    private val _lastFlashingStatus = MutableStateFlow<FlashStatus?>(null)
    val lastFlashingStatus: StateFlow<FlashStatus?> = _lastFlashingStatus.asStateFlow()

    // --- Gemini Support Panel (AI Co-Pilot Tab) ---
    private val _copilotMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            text = "Welcome to DroidFlash AI Co-Pilot! 👋\n\nI can assist you in verifying partition layouts, writing fastboot scripts, or diagnosing flashing failures. Ask me a question or paste your terminal error logs below!",
            isUser = false
        )
    ))
    val copilotMessages: StateFlow<List<ChatMessage>> = _copilotMessages.asStateFlow()

    private val _isCopilotLoading = MutableStateFlow(false)
    val isCopilotLoading: StateFlow<Boolean> = _isCopilotLoading.asStateFlow()

    // Error Diagnosis fields
    private val _diagnoseLogsInput = MutableStateFlow("")
    val diagnoseLogsInput: StateFlow<String> = _diagnoseLogsInput.asStateFlow()

    private val _diagnoseSelectedDevice = MutableStateFlow("Google Pixel 8 (shiba)")
    val diagnoseSelectedDevice: StateFlow<String> = _diagnoseSelectedDevice.asStateFlow()

    private val _diagnoseSelectedFormat = MutableStateFlow("FASTBOOT_ZIP")
    val diagnoseSelectedFormat: StateFlow<String> = _diagnoseSelectedFormat.asStateFlow()

    // --- Frija Stock OTA Downloader Tool State ---
    private val _frijaModel = MutableStateFlow("SM-S918B")
    val frijaModel: StateFlow<String> = _frijaModel.asStateFlow()

    private val _frijaCsc = MutableStateFlow("EUX")
    val frijaCsc: StateFlow<String> = _frijaCsc.asStateFlow()

    private val _frijaAutoMode = MutableStateFlow(true)
    val frijaAutoMode: StateFlow<Boolean> = _frijaAutoMode.asStateFlow()

    private val _frijaManualPda = MutableStateFlow("S918BXXU3BWK2")
    val frijaManualPda: StateFlow<String> = _frijaManualPda.asStateFlow()

    private val _frijaManualCsc = MutableStateFlow("S918BOXM3BWK2")
    val frijaManualCsc: StateFlow<String> = _frijaManualCsc.asStateFlow()

    private val _frijaManualPhone = MutableStateFlow("S918BXXU3BWK2")
    val frijaManualPhone: StateFlow<String> = _frijaManualPhone.asStateFlow()

    private val _isCheckingFrijaUpdate = MutableStateFlow(false)
    val isCheckingFrijaUpdate: StateFlow<Boolean> = _isCheckingFrijaUpdate.asStateFlow()

    private val _frijaUpdateResult = MutableStateFlow<FrijaFirmwareInfo?>(null)
    val frijaUpdateResult: StateFlow<FrijaFirmwareInfo?> = _frijaUpdateResult.asStateFlow()

    private val _frijaConsoleLogs = MutableStateFlow<List<String>>(listOf(
        "⚙️ Frija OEM Downloading Portal Online: Awaiting input...",
        "📟 Ready. Select 'Check Update' to query development & factory servers."
    ))
    val frijaConsoleLogs: StateFlow<List<String>> = _frijaConsoleLogs.asStateFlow()

    private val _isDownloadingFrija = MutableStateFlow(false)
    val isDownloadingFrija: StateFlow<Boolean> = _isDownloadingFrija.asStateFlow()

    private val _frijaDownloadProgress = MutableStateFlow(0f)
    val frijaDownloadProgress: StateFlow<Float> = _frijaDownloadProgress.asStateFlow()

    private val _frijaDownloadSpeed = MutableStateFlow("")
    val frijaDownloadSpeed: StateFlow<String> = _frijaDownloadSpeed.asStateFlow()

    // --- P2P Mesh & Forum Board State ---
    private val _p2pFiles = MutableStateFlow<List<P2PSharedFile>>(emptyList())
    val p2pFiles: StateFlow<List<P2PSharedFile>> = _p2pFiles.asStateFlow()

    private val _forumThreads = MutableStateFlow<List<ForumThread>>(emptyList())
    val forumThreads: StateFlow<List<ForumThread>> = _forumThreads.asStateFlow()

    private val _activeThreadDetail = MutableStateFlow<ForumThread?>(null)
    val activeThreadDetail: StateFlow<ForumThread?> = _activeThreadDetail.asStateFlow()

    private val _isP2PDownloading = MutableStateFlow(false)
    val isP2PDownloading: StateFlow<Boolean> = _isP2PDownloading.asStateFlow()

    private val _p2pProgress = MutableStateFlow(0f)
    val p2pProgress: StateFlow<Float> = _p2pProgress.asStateFlow()

    private val _p2pDownloadingFileName = MutableStateFlow("")
    val p2pDownloadingFileName: StateFlow<String> = _p2pDownloadingFileName.asStateFlow()

    fun selectThread(thread: ForumThread?) {
        _activeThreadDetail.value = thread
    }

    // --- ADB and Fastboot Commands Toolkit State ---
    private val _toolkitConsoleLogs = MutableStateFlow<List<String>>(listOf(
        "🔧 Android SDK ADB & Fastboot Platform Utility Console online.",
        "🖥️ Ready. Select an interactive command button to begin host emulation."
    ))
    val toolkitConsoleLogs: StateFlow<List<String>> = _toolkitConsoleLogs.asStateFlow()

    private val _isExecutingToolkitCommand = MutableStateFlow(false)
    val isExecutingToolkitCommand: StateFlow<Boolean> = _isExecutingToolkitCommand.asStateFlow()

    fun clearToolkitTerminal() {
        _toolkitConsoleLogs.value = listOf("📟 Ready. Awaiting ADB/Fastboot toolkit commands...")
    }

    fun addToolkitLog(log: String) {
        val current = _toolkitConsoleLogs.value.toMutableList()
        current.add(log)
        _toolkitConsoleLogs.value = current
    }

    fun executeToolkitCommand(cmd: String) {
        if (_isExecutingToolkitCommand.value) return
        _isExecutingToolkitCommand.value = true
        addToolkitLog("──────────────────────────────────────")
        addToolkitLog("$ $cmd")

        viewModelScope.launch {
            delay(400)
            when (cmd) {
                "adb devices" -> {
                    val status = if (usbDeviceState.value is DeviceState.Disconnected) {
                        ""
                    } else if (usbDeviceState.value is DeviceState.AdbSideload) {
                        "8A9X19280\tsideload\n"
                    } else {
                        "8A9X19280\tdevice\n"
                    }
                    if (status.isEmpty()) {
                        addToolkitLog("List of devices attached\n(No device found. Connect USB debug port or install Android OEM driver.)")
                    } else {
                        addToolkitLog("List of devices attached\n$status")
                    }
                }
                "adb reboot bootloader" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected) {
                        addToolkitLog("error: no devices/emulators found")
                    } else {
                        addToolkitLog("Rebooting device into bootloader mode...")
                        delay(1200)
                        addToolkitLog("Device disconnected on adb interface.")
                        delay(600)
                        changeUsbDeviceState(DeviceState.FastbootUnlocked)
                        addToolkitLog("🔌 Device detected in Fastboot Mode!")
                    }
                }
                "fastboot devices" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected || usbDeviceState.value is DeviceState.AdbSideload) {
                        addToolkitLog("")
                    } else {
                        addToolkitLog("8A9X19280\tfastboot")
                    }
                }
                "fastboot getvar all" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected || usbDeviceState.value is DeviceState.AdbSideload) {
                        addToolkitLog("FAILED (remote: 'command only works in Fastboot mode')\nfinished. total time: 0.002s")
                    } else {
                        addToolkitLog("(bootloader) version-bootloader: 1.0_prod_c1")
                        delay(200)
                        addToolkitLog("(bootloader) version-baseband: g5300-00129")
                        addToolkitLog("(bootloader) product: shiba")
                        addToolkitLog("(bootloader) secure: yes")
                        delay(200)
                        addToolkitLog("(bootloader) slot-count: 2")
                        val currentSlot = if (kotlin.random.Random.nextBoolean()) "a" else "b"
                        addToolkitLog("(bootloader) current-slot: $currentSlot")
                        val isUnlocked = usbDeviceState.value is DeviceState.FastbootUnlocked
                        addToolkitLog("(bootloader) unlocked: ${if (isUnlocked) "yes" else "no"}")
                        addToolkitLog("finished. total time: 0.145s")
                    }
                }
                "fastboot oem device-info" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected || usbDeviceState.value is DeviceState.AdbSideload) {
                        addToolkitLog("FAILED (remote: 'command only works in Fastboot mode')\nfinished. total time: 0.001s")
                    } else {
                        val isUnlocked = usbDeviceState.value is DeviceState.FastbootUnlocked
                        addToolkitLog("(bootloader) Device tampered: false")
                        delay(300)
                        addToolkitLog("(bootloader) Device unlocked: $isUnlocked")
                        addToolkitLog("(bootloader) Device critical unlocked: $isUnlocked")
                        addToolkitLog("(bootloader) Charger screen enabled: true")
                        addToolkitLog("OKAY [  0.112s]\nFinished. Total time: 0.115s")
                    }
                }
                "fastboot flashing unlock" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected || usbDeviceState.value is DeviceState.AdbSideload) {
                        addToolkitLog("FAILED (remote: 'command only works in Fastboot mode')\nfinished. total time: 0.001s")
                    } else if (usbDeviceState.value is DeviceState.FastbootUnlocked) {
                        addToolkitLog("(bootloader) Device is already unlocked!")
                        addToolkitLog("OKAY [  0.010s]\nfinished. total time: 0.012s")
                    } else {
                        addToolkitLog("Initiating bootloader unlock signature sequence...")
                        delay(1200)
                        addToolkitLog("(bootloader) Erasing Userdata ... OKAY [ 2.450s]")
                        addToolkitLog("(bootloader) Writing brand new partition tables ... OKAY")
                        delay(600)
                        changeUsbDeviceState(DeviceState.FastbootUnlocked)
                        addToolkitLog("Device unlocked successfully! State updated.")
                        addToolkitLog("OKAY [  3.120s]\nfinished. total time: 3.125s")
                    }
                }
                "fastboot erase cache" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected || usbDeviceState.value is DeviceState.AdbSideload) {
                        addToolkitLog("FAILED (remote: 'command only works in Fastboot mode')")
                    } else {
                        addToolkitLog("Erasing 'cache' ...")
                        delay(600)
                        addToolkitLog("OKAY [  0.045s]\nfinished. total time: 0.048s")
                    }
                }
                "fastboot format userdata" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected || usbDeviceState.value is DeviceState.AdbSideload) {
                        addToolkitLog("FAILED (remote: 'command only works in Fastboot mode')")
                    } else {
                        addToolkitLog("Creating filesystem with parameters:")
                        addToolkitLog("  Size: 2457600000 bytes")
                        addToolkitLog("  Format: ext4")
                        delay(400)
                        addToolkitLog("Sending 'userdata' (124 KB) ...")
                        delay(1000)
                        addToolkitLog("Writing 'userdata' ... OKAY [ 1.120s]")
                        addToolkitLog("OKAY [  2.240s]\nfinished. total time: 2.245s")
                    }
                }
                "fastboot reboot-bootloader" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected || usbDeviceState.value is DeviceState.AdbSideload) {
                        addToolkitLog("FAILED (remote: 'command only works in Fastboot mode')")
                    } else {
                        addToolkitLog("Rebooting into bootloader ...")
                        delay(1000)
                        changeUsbDeviceState(usbDeviceState.value) // re-trigger connection refresh
                        addToolkitLog("OKAY [  0.920s]\nfinished. total time: 0.925s")
                    }
                }
                "fastboot reboot" -> {
                    if (usbDeviceState.value is DeviceState.Disconnected || usbDeviceState.value is DeviceState.AdbSideload) {
                        addToolkitLog("FAILED (remote: 'command only works in Fastboot mode')")
                    } else {
                        addToolkitLog("Rebooting device ...")
                        delay(850)
                        changeUsbDeviceState(DeviceState.Disconnected)
                        addToolkitLog("Device disconnected on fastboot socket.")
                        addToolkitLog("OKAY [  0.810s]\nfinished. total time: 0.815s")
                    }
                }
                else -> {
                    addToolkitLog("command unknown or platform SDK syntax error.")
                }
            }
            _isExecutingToolkitCommand.value = false
        }
    }

    fun updateDiagnoseLogsInput(logs: String) {
        _diagnoseLogsInput.value = logs
    }

    fun setDiagnoseDevice(device: String) {
        _diagnoseSelectedDevice.value = device
    }

    fun setDiagnoseFormat(format: String) {
        _diagnoseSelectedFormat.value = format
    }

    // --- Action Methods ---

    fun selectPackage(pkg: FirmwarePackage) {
        _selectedPackage.value = pkg
        _activePartitions.value = deserializePartitions(pkg.partitionsJson)
        _unpackerConsoleLogs.value = listOf(
            "Loaded firmware payload package: ${pkg.name}",
            "Device Model: ${pkg.deviceModel}",
            "Build ID: ${pkg.buildId}",
            "Size: ${formatSizeBytes(pkg.sizeBytes)}",
            "Waiting for partition extraction instructions..."
        )
        _extractionProgress.value = 0f
        _currentExtractingPartition.value = null
    }

    fun togglePartitionSelection(partitionName: String) {
        val updated = _activePartitions.value.map {
            if (it.name == partitionName) {
                it.copy(isSelected = !it.isSelected)
            } else {
                it
            }
        }
        _activePartitions.value = updated
    }

    /**
     * Executes simulated low-level unpacking of the actual payload chunks.
     * We populate highly realistic console offsets, sector hashes, and verify signatures.
     */
    fun startExtraction() {
        val pkg = _selectedPackage.value ?: return
        val partitionsToExtract = _activePartitions.value.filter { it.isSelected && !it.isExtracted }
        if (partitionsToExtract.isEmpty()) {
            addUnpackerLog("⚠️ No pending or selected partitions to extract.")
            return
        }

        _isExtracting.value = true
        _extractionProgress.value = 0f

        viewModelScope.launch {
            addUnpackerLog("⚡ Commencing block payload extraction protocol...")
            addUnpackerLog("📂 Source: ${pkg.filePath}")
            addUnpackerLog("🔑 Verifying android boot signature certificates... SUCCESS")
            
            var totalExtractedBytes = 0L
            val totalBytesSelection = partitionsToExtract.sumOf { it.sizeBytes }

            for ((index, item) in partitionsToExtract.withIndex()) {
                _currentExtractingPartition.value = item.name
                addUnpackerLog("──────────────────────────────────────")
                addUnpackerLog("📦 Extracting block: `${item.name}` (${item.formattedSize})")
                
                val partitionSize = item.sizeBytes
                var chunkBytes = 0L
                val speedMb = kotlin.random.Random.nextInt(35, 66) // Simulation of disk write speeds

                // Simulate block reading packets
                val stepCount = 5
                val stepSize = partitionSize / stepCount
                
                for (step in 1..stepCount) {
                    delay(350)
                    chunkBytes += stepSize
                    totalExtractedBytes += stepSize
                    val localProgress = chunkBytes.toFloat() / partitionSize
                    _extractionProgress.value = totalExtractedBytes.toFloat() / totalBytesSelection

                    val hexOffset = String.format("0x%08X", (stepCount * index + step) * 0x100000)
                    addUnpackerLog("  ├─ Sector $hexOffset: Read block dynamic chunk [${(localProgress * 100).toInt()}%] at $speedMb MB/s")
                }

                addUnpackerLog("  └─ Generation SHA256 secure hash verify ... PASSED")
                
                // Mark this extracted in partitions list
                _activePartitions.value = _activePartitions.value.map {
                    if (it.name == item.name) it.copy(isExtracted = true) else it
                }
            }

            // Unpacking success routines
            _currentExtractingPartition.value = null
            _isExtracting.value = false
            _extractionProgress.value = 1f
            addUnpackerLog("──────────────────────────────────────")
            addUnpackerLog("🎉 COMPLETED: Selected partition images successfully extracted to root output directory.")
            
            // Save state back to DB
            val updatedJson = serializePartitions(_activePartitions.value)
            val extractedCount = _activePartitions.value.count { it.isExtracted }
            val isPkgFullyExtracted = extractedCount == _activePartitions.value.size

            val updatedPkg = pkg.copy(
                partitionsJson = updatedJson,
                extractedCount = extractedCount,
                isExtracted = isPkgFullyExtracted
            )
            repo.updatePackage(updatedPkg)
            _selectedPackage.value = updatedPkg
            
            addUnpackerLog("🏁 Unpacker thread asleep. Package updated in system register.")
        }
    }

    private fun addUnpackerLog(message: String) {
        val current = _unpackerConsoleLogs.value.toMutableList()
        current.add(message)
        _unpackerConsoleLogs.value = current
    }

    // --- Flashing Simulator Panel API ---

    fun changeUsbDeviceState(state: DeviceState) {
        _usbDeviceState.value = state
        addFlashLogString("🔌 Device USB Host Status changed: $state")
    }

    fun clearFlashTerminal() {
        _flashingConsoleLogs.value = listOf("📟 Ready. Awaiting USB connection sequence...")
    }

    fun startAutomatedFlashing(pkg: FirmwarePackage) {
        val devState = _usbDeviceState.value
        if (devState == DeviceState.Disconnected) {
            addFlashLogString("❌ ERROR: No target Android device detected on USB OTG Host controller.")
            return
        }

        // Verify if we have extracted partitions before flashing
        val partitions = deserializePartitions(pkg.partitionsJson)
        val extractedImages = partitions.filter { it.isExtracted }
        if (extractedImages.isEmpty()) {
            addFlashLogString("❌ ERROR: No unpacked image binaries (.img) found for ${pkg.name}. Enter \"Extract Manager\" tab to dump partition blocks first.")
            return
        }

        _isFlashing.value = true
        _flashingProgress.value = 0f
        addFlashLogString("⚡ Triggering Automated OS Flash Routine...")
        addFlashLogString("📲 Target Device: ${devState.name} [Status: Serial verified]")
        addFlashLogString("💿 Firmware: ${pkg.name} (${pkg.deviceModel})")

        viewModelScope.launch {
            val serial = "8A9X19280"
            addFlashLogString("$ fastboot devices")
            delay(400)
            addFlashLogString("$serial    fastboot")

            addFlashLogString("$ fastboot getvar product")
            delay(300)
            val codeModel = if (pkg.deviceModel.contains("Pixel")) "shiba" else "oneplus12"
            addFlashLogString("product: $codeModel\nFinished. Total time: 0.120s")

            // Simulate partition blocks flashing sequence
            var flashErrorsOccurred = false
            var completedCount = 0
            val totalToFlash = extractedImages.size

            for (part in extractedImages) {
                addFlashLogString("──────────────────────────────────────")
                addFlashLogString("$ fastboot flash ${part.name} ${part.name}.img")
                
                // Fastboot logs simulation
                delay(700)
                addFlashLogString("Sending '${part.name}' (${part.sizeBytes / 1024} KB) ...")
                delay(1000)
                val sendTime = 0.5 + kotlin.random.Random.nextDouble() * 1.3
                addFlashLogString("Sending '${part.name}' ... OKAY [${String.format("%.2f", sendTime)}s]")
                addFlashLogString("Writing '${part.name}' ...")
                delay(800)
                
                // Introduce an occasional realistic bootloader error scenario if fastboot is locked or slot error (only if a specific random condition is met, otherwise succeed)
                // Let's create an elegant way where users can test correct vs incorrect locks.
                if (devState == DeviceState.FastbootLocked && part.name == "boot") {
                    addFlashLogString("FAILED (remote: 'Flashing is not allowed in Locked State')")
                    addFlashLogString("❌ CRITICAL FLASHER FAULT: Device bootloader locked. Command rejected.")
                    flashErrorsOccurred = true
                    break
                } else {
                    val writeTime = 0.1 + kotlin.random.Random.nextDouble() * 0.5
                    addFlashLogString("Writing '${part.name}' ... OKAY [${String.format("%.2f", writeTime)}s]")
                }

                completedCount++
                _flashingProgress.value = completedCount.toFloat() / totalToFlash
            }

            if (flashErrorsOccurred) {
                addFlashLogString("──────────────────────────────────────")
                addFlashLogString("❌ Flashing routine aborted prematurely. Result: FAIL STATE.")
                _isFlashing.value = false
                _lastFlashingStatus.value = FlashStatus.Failed("FAILED (remote: 'Flashing is not allowed in Locked State')")
                _showFlashResultDialog.value = true
                
                saveTransactionToHistory(
                    pkg = pkg,
                    status = "FAILED",
                    logsCombined = _flashingConsoleLogs.value.joinToString("\n"),
                    err = "FAILED (remote: 'Flashing is not allowed in Locked State')"
                )
            } else {
                addFlashLogString("──────────────────────────────────────")
                addFlashLogString("$ fastboot reboot")
                delay(500)
                addFlashLogString("Rebooting device ... OKAY")
                addFlashLogString("\n🌟 Automated Flashing process completed successfully!")
                addFlashLogString("📱 Your target device is rebooting to stock Android.")
                _isFlashing.value = false
                _flashingProgress.value = 1.0f
                _lastFlashingStatus.value = FlashStatus.Success
                _showFlashResultDialog.value = true

                saveTransactionToHistory(
                    pkg = pkg,
                    status = "SUCCESS",
                    logsCombined = _flashingConsoleLogs.value.joinToString("\n")
                )
            }
        }
    }

    private fun addFlashLogString(message: String) {
        val current = _flashingConsoleLogs.value.toMutableList()
        current.add(message)
        _flashingConsoleLogs.value = current
    }

    fun dismissFlashDialog() {
        _showFlashResultDialog.value = false
    }

    private suspend fun saveTransactionToHistory(
        pkg: FirmwarePackage,
        status: String,
        logsCombined: String,
        err: String? = null
    ) {
        val statusLabel = if (status == "SUCCESS") "SUCCESS" else "FAILED"
        val log = FlashLog(
            deviceName = pkg.deviceModel,
            deviceSerial = "USB_OTG_8A9X19",
            firmwareName = pkg.name,
            flashType = pkg.fileFormat,
            status = statusLabel,
            logs = logsCombined,
            errorMessage = err
        )
        repo.insertLog(log)
    }

    fun deleteHistoricalLogs() {
        viewModelScope.launch {
            repo.deleteAllLogs()
        }
    }

    // --- Gemini Support API Actions (AI Co-Pilot) ---

    fun sendCopilotMessage(text: String) {
        if (text.isBlank()) return
        val currentM = _copilotMessages.value.toMutableList()
        currentM.add(ChatMessage(text = text, isUser = true))
        _copilotMessages.value = currentM

        _isCopilotLoading.value = true

        viewModelScope.launch {
            val response = geminiRepo.askFlashingAssistant(text)
            val updatedM = _copilotMessages.value.toMutableList()
            updatedM.add(ChatMessage(text = response, isUser = false))
            _copilotMessages.value = updatedM
            _isCopilotLoading.value = false
        }
    }

    fun runLiveDiagnostics() {
        val logs = _diagnoseLogsInput.value
        if (logs.isBlank()) return
        
        _isCopilotLoading.value = true
        val currentM = _copilotMessages.value.toMutableList()
        currentM.add(ChatMessage(text = "Please analyze this flashing logs failure for ${_diagnoseSelectedDevice.value} (${_diagnoseSelectedFormat.value}):\n\n$logs", isUser = true))
        _copilotMessages.value = currentM

        viewModelScope.launch {
            val analysis = geminiRepo.analyzeFlashingIssue(
                deviceModel = _diagnoseSelectedDevice.value,
                romType = _diagnoseSelectedFormat.value,
                errorLogs = logs
            )
            val updatedM = _copilotMessages.value.toMutableList()
            updatedM.add(ChatMessage(text = "🕵️ **AI Flashing Diagnostics Report:**\n\n$analysis", isUser = false))
            _copilotMessages.value = updatedM
            _isCopilotLoading.value = false
            // Clear input
            _diagnoseLogsInput.value = ""
        }
    }

    // --- Private Helper Utilities ---

    private fun serializePartitions(partitions: List<PartitionItem>): String {
        return try {
            partitionAdapter.toJson(partitions) ?: "[]"
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializePartitions(json: String): List<PartitionItem> {
        return try {
            partitionAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getExtractedPartitionsList(): List<Pair<PartitionItem, FirmwarePackage>> {
        return _firmwarePackages.value.flatMap { pkg ->
            deserializePartitions(pkg.partitionsJson).filter { it.isExtracted }.map { it to pkg }
        }
    }

    fun deleteExtractedPartition(pkgId: Long, partitionName: String) {
        val pkg = _firmwarePackages.value.find { it.id == pkgId } ?: return
        val currentParts = deserializePartitions(pkg.partitionsJson).map {
            if (it.name == partitionName) {
                it.copy(isExtracted = false)
            } else {
                it
            }
        }
        val updatedJson = serializePartitions(currentParts)
        val extractedCount = currentParts.count { it.isExtracted }
        val isPkgFullyExtracted = extractedCount == currentParts.size

        val updatedPkg = pkg.copy(
            partitionsJson = updatedJson,
            extractedCount = extractedCount,
            isExtracted = isPkgFullyExtracted
        )
        viewModelScope.launch {
            repo.updatePackage(updatedPkg)
            if (_selectedPackage.value?.id == pkgId) {
                _selectedPackage.value = updatedPkg
                _activePartitions.value = currentParts
            }
            addUnpackerLog("🗑️ Extracted Partition image '$partitionName' deleted from local database.")
        }
    }

    private fun formatSizeBytes(bytes: Long): String {
        val kb = bytes / 1024
        val mb = kb / 1024
        if (mb < 1024) return "$mb MB"
        val gb = mb.toDouble() / 1024
        return String.format("%.2f GB", gb)
    }

    private suspend fun seedDemoPackages() = withContext(Dispatchers.IO) {
        val googlePartitions = listOf(
            PartitionItem("boot", 67108864, "Core Linux Kernel, modules & boot structures. Essential for rooting (Magisk/KernelSU).", isRequired = true),
            PartitionItem("init_boot", 8388608, "Secondary initialization ramdisk for newer Android 13+ devices.", isRequired = true),
            PartitionItem("vendor_boot", 67108864, "Vendor specific kernel ramdisk drivers and security policies.", isRequired = true),
            PartitionItem("vbmeta", 8192, "Verified Boot master metadata. Mandatory for Android Signature checks (AVB 2.0).", isRequired = true),
            PartitionItem("vbmeta_system", 8192, "Verified Boot system partition signature overlays."),
            PartitionItem("dtbo", 16777216, "Device Tree Blob Overlays matching hardware architectures.", isRequired = true),
            PartitionItem("system", 2576980378, "Main System OS image holding framework, preloaded apps & libs."),
            PartitionItem("vendor", 536870912, "Vendor proprietary libraries, hardware abstraction layers (HALs)."),
            PartitionItem("product", 1073741824, "Product partition holding OEM customization profiles and overlays.")
        )

        val oneplusPartitions = listOf(
            PartitionItem("boot", 100663296, "OxygenOS custom compiled Linux Kernel & boot ramdisk.", isRequired = true),
            PartitionItem("init_boot", 16777216, "OxygenOS startup kernel ramdisk.", isRequired = true),
            PartitionItem("vbmeta", 8192, "AVB signature security metadata block.", isRequired = true),
            PartitionItem("recovery", 106954752, "Stock recovery partition holding updater configs."),
            PartitionItem("my_manifest", 12582912, "OnePlus device layout update configurations."),
            PartitionItem("super", 3758096384, "Primary dynamic super block containing system, vendor, product partitions.")
        )

        val samsungPartitions = listOf(
            PartitionItem("AP_SM-S928B_SYSTEM", 4194304000, "Core firmware AP Tarball. Contains system, vendor, product lz4 structures.", isRequired = true),
            PartitionItem("BL_SM-S928B_BOOTLOADER", 157286400, "Secondary Bootloader loader. Core security partition. Includes sboot.bin.", isRequired = true),
            PartitionItem("CP_SM-S928B_MODEM", 94371840, "Modem radio firmware. Controls baseband network layers."),
            PartitionItem("CSC_OXM_S928B_REGIONAL", 209715200, "Regional and carrier customizations. Controls partition resizing schemas.", isRequired = true)
        )

        val pkg1 = FirmwarePackage(
            name = "Pixel 8 \"shiba\" Fastboot Flash ROM",
            deviceModel = "Google Pixel 8 (shiba)",
            androidVersion = "14.0 (UPS1.240305.019)",
            buildId = "shiba-ap1a.240305.019",
            fileFormat = "FASTBOOT_ZIP",
            sizeBytes = 2576980378 + 536870912 + 150000000,
            filePath = "/storage/emulated/0/Download/shiba-factory-ap1a.zip",
            partitionCount = googlePartitions.size,
            partitionsJson = serializePartitions(googlePartitions)
        )

        val pkg2 = FirmwarePackage(
            name = "OnePlus 12 OOS Payload Archive",
            deviceModel = "OnePlus 12 (CPH2583)",
            androidVersion = "14.0 (EX01_14.0.0.604)",
            buildId = "CPH2583_14.0.0.604",
            fileFormat = "PAYLOAD_BIN",
            sizeBytes = 3758096384 + 100000000,
            filePath = "/storage/emulated/0/Download/OnePlus12_OxygenOS_Payload.zip",
            partitionCount = oneplusPartitions.size,
            partitionsJson = serializePartitions(oneplusPartitions)
        )

        val pkg3 = FirmwarePackage(
            name = "Galaxy S24 Ultra Odin Firmware",
            deviceModel = "Samsung Galaxy S24 Ultra (SM-S928B)",
            androidVersion = "14.0 (OneUI 6.1)",
            buildId = "S928BXXU1AXB5",
            fileFormat = "ODIN_TAR",
            sizeBytes = 4194304000 + 400000000,
            filePath = "/storage/emulated/0/Download/SM-S928B_OXM_S928BXXU1AXB5.zip",
            partitionCount = samsungPartitions.size,
            partitionsJson = serializePartitions(samsungPartitions)
        )

        repo.insertPackage(pkg1)
        repo.insertPackage(pkg2)
        repo.insertPackage(pkg3)
    }

    /**
     * User imports their own custom file. We construct a realistic custom firmware profile matching it.
     */
    fun importCustomFirmwareFile(fileName: String, fileUriPath: String, size: Long) {
        val format = when {
            fileName.endsWith(".bin", true) || fileName.contains("payload", true) -> "PAYLOAD_BIN"
            fileName.contains("odin", true) || fileName.contains("tar", true) -> "ODIN_TAR"
            else -> "FASTBOOT_ZIP"
        }

        val customPartitions = listOf(
            PartitionItem("boot", 67108864, "Core bootloader kernel partition.", isRequired = true),
            PartitionItem("recovery", 67108864, "Custom ROM diagnostic Recovery terminal partition."),
            PartitionItem("vbmeta", 8192, "AVB verified security signature module.", isRequired = true),
            PartitionItem("system", size / 2, "Main user OS partition block.")
        )

        viewModelScope.launch {
            val customPkg = FirmwarePackage(
                name = fileName.substringBeforeLast("."),
                deviceModel = "Generic Android Target Device",
                androidVersion = "14.0 (Custom Build)",
                buildId = "CUSTOM-BUILD-${(10000..99999).random()}",
                fileFormat = format,
                sizeBytes = size,
                filePath = fileUriPath,
                partitionCount = customPartitions.size,
                partitionsJson = serializePartitions(customPartitions)
            )
            val newId = repo.insertPackage(customPkg)
            val savedPkg = customPkg.copy(id = newId)
            selectPackage(savedPkg)
        }
    }

    // --- Frija Stock Downloader and P2P Forum Internal Methods ---

    private fun getPrefs() = getApplication<Application>().getSharedPreferences("droidflash_p2p_forum_v2", android.content.Context.MODE_PRIVATE)

    private fun loadP2PAndForumData() {
        val prefs = getPrefs()
        val p2pJson = prefs.getString("p2p_files", null)
        val forumJson = prefs.getString("forum_threads", null)

        if (p2pJson != null) {
            try {
                _p2pFiles.value = p2pAdapter.fromJson(p2pJson) ?: emptyList()
            } catch (e: Exception) {
                seedInitialP2PData()
            }
        } else {
            seedInitialP2PData()
        }

        if (forumJson != null) {
            try {
                _forumThreads.value = threadAdapter.fromJson(forumJson) ?: emptyList()
            } catch (e: Exception) {
                seedInitialForumData()
            }
        } else {
            seedInitialForumData()
        }
    }

    private fun saveP2PAndForumData() {
        val prefs = getPrefs()
        prefs.edit().apply {
            putString("p2p_files", p2pAdapter.toJson(_p2pFiles.value))
            putString("forum_threads", threadAdapter.toJson(_forumThreads.value))
            apply()
        }
    }

    private fun seedInitialP2PData() {
        val p2pCommentAdapter = moshi.adapter<List<P2PComment>>(Types.newParameterizedType(List::class.java, P2PComment::class.java))
        val comments1 = p2pCommentAdapter.toJson(listOf(
            P2PComment("RootSlayer88", "This works perfectly! Patched on Samsung S23 series.", "2026-05-28 14:22"),
            P2PComment("OdinKing", "Confirmed this is clean of boot verification issues.", "2026-05-29 09:10")
        ))
        val comments2 = p2pCommentAdapter.toJson(listOf(
            P2PComment("HexDumper", "Use standard TWRP fastboot boot instructions to load.", "2026-05-25 18:05"),
            P2PComment("KernelDev_Z", "For snapdragon variants ONLY! Exynos will bootloop.", "2026-05-27 11:45")
        ))
        val comments3 = p2pCommentAdapter.toJson(listOf(
            P2PComment("DroidHacker_01", "Solves red-state boot verification loop instantly.", "2026-05-30 08:31")
        ))

        _p2pFiles.value = listOf(
            P2PSharedFile(1L, "boot.img", "Galaxy S23 Ultra (SM-S918B)", "96.0 MB", "8F1A2B3C...4D5E", "RootSlayer88", 24, comments1, false),
            P2PSharedFile(2L, "recovery.img", "OnePlus 11 (CPH2447)", "64.0 MB", "CF12B45A...86C3", "HexDumper", 15, comments2, false),
            P2PSharedFile(3L, "vbmeta.img", "Google Pixel 8 (shiba)", "8.0 KB", "DA39A3EE...0000", "DroidHacker_01", 42, comments3, false)
        )
        saveP2PAndForumData()
    }

    private fun seedInitialForumData() {
        _forumThreads.value = listOf(
            ForumThread(
                id = 1L,
                title = "🔥 [BOOTLOOP] Locked bootloader with patched vbmeta.img? Urgent Help!",
                author = "NoobFlasher_99",
                category = "BOOTLOOP",
                content = "I accidentally enabled OEM Locking again with a custom boot partition running on slot_b! Now the phone displays a dark 'Red State: Device signature failed' screen on bootup and powers down immediately. Is this a hard brick or can I recover it?",
                dateFormatted = "2026-05-30 22:12",
                repliesJson = moshi.adapter<List<ForumReply>>(Types.newParameterizedType(List::class.java, ForumReply::class.java)).toJson(listOf(
                    ForumReply("RootSlayer88", "Oh no! Locking the bootloader with custom binaries is the classic way to soft-brick. Connect in Fastboot mode and run 'fastboot flashing unlock' if you can. If fastboot is denied, you'll need the emergency download tool (EDL) for Snapdragon, or flash full official Odin packages for Samsung.", "2026-05-30 22:45"),
                    ForumReply("AndroidGuru", "Check if you can get into raw bootloader mode by holding Vol Down + Power while plugging in USB. Once in Fastboot, flash the official OEM vbmeta and complete full stock firmware erase.", "2026-05-31 01:10")
                ))
            ),
            ForumThread(
                id = 2L,
                title = "🔧 [GUIDE] Custom Recovery installation without bricking A/B slot system",
                author = "KernelDev_Z",
                category = "GUIDE",
                content = "With newer Virtual A/B storage architectures, standard fastboot bootloader partition rules have changed. Do not write TWRP directly into boot.img! Always load TWRP temporarily via 'fastboot boot recovery-twrp.img', or flash it directly to vendor_boot or init_boot if supported by the kernel.",
                dateFormatted = "2026-05-28 11:05",
                repliesJson = moshi.adapter<List<ForumReply>>(Types.newParameterizedType(List::class.java, ForumReply::class.java)).toJson(listOf(
                    ForumReply("FlastExpert", "Incredible guide. The init_boot distinction is extremely important for Android 13/14 devices.", "2026-05-28 12:30"),
                    ForumReply("SaberTooth_0", "Tried this on my OnePlus 11 and it booted TWRP successfully without corrupting slot_a or boot sectors. Excellent work!", "2026-05-29 18:15")
                ))
            ),
            ForumThread(
                id = 3L,
                title = "⚡ [FASTBOOT] Command 'fastboot devices' doesn't return anything on AMD board",
                author = "RyzenRider",
                category = "FASTBOOT",
                content = "Hey guys, just upgraded my PC to an AMD Ryzen 7 CPU. When I connect any Android phone in fastboot mode, 'fastboot devices' is completely empty, even though ADB works 100% when booted. Device Manager shows Android yellow code 10 error. Any fixes?",
                dateFormatted = "2026-05-29 14:02",
                repliesJson = moshi.adapter<List<ForumReply>>(Types.newParameterizedType(List::class.java, ForumReply::class.java)).toJson(listOf(
                    ForumReply("HexDumper", "AMD USB 3.0 controllers have a known chipset timing issue with fastboot bootloader protocols. Try connecting to a USB 2.0 port on the back of the motherboard or use an USB hub.", "2026-05-29 14:30"),
                    ForumReply("RyzenRider", "Holy cow, plugging it into a USB 2.0 port at the backend of the board solved it instantly! Thanks a million!", "2026-05-29 15:10")
                ))
            )
        )
        saveP2PAndForumData()
    }

    fun publishExtractedPartition(partitionName: String, pkgName: String, sizeBytes: Long, formattedSize: String) {
        val currentP2p = _p2pFiles.value.toMutableList()
        val newId = (currentP2p.maxOfOrNull { it.id } ?: 0L) + 1L

        val p2pCommentAdapter = moshi.adapter<List<P2PComment>>(Types.newParameterizedType(List::class.java, P2PComment::class.java))
        val newShared = P2PSharedFile(
            id = newId,
            fileName = "$partitionName.img",
            packageName = pkgName,
            fileSize = formattedSize,
            sha256 = String.format("%016X", kotlin.random.Random.nextLong()) + "...SHA",
            uploadedBy = "You (Local Seed)",
            commentsJson = p2pCommentAdapter.toJson(emptyList()),
            isUserShared = true
        )
        currentP2p.add(0, newShared)
        _p2pFiles.value = currentP2p
        saveP2PAndForumData()
        addUnpackerLog("📡 [P2P SWARM] Extracted block file '$partitionName.img' published to decentralized network hive.")
    }

    fun downloadP2PImage(sharedFile: P2PSharedFile) {
        if (_isP2PDownloading.value) return
        _isP2PDownloading.value = true
        _p2pDownloadingFileName.value = sharedFile.fileName
        _p2pProgress.value = 0f

        viewModelScope.launch {
            // Simulate BitTorrent block packets gathering coroutine loop
            val steps = 10
            for (i in 1..steps) {
                delay(200)
                _p2pProgress.value = i.toFloat() / steps
            }

            // Register it as a custom package in our database
            val partName = sharedFile.fileName.substringBeforeLast(".")
            val bytes = when {
                sharedFile.fileSize.contains("MB") -> (sharedFile.fileSize.substringBefore(" ").toDouble() * 1024 * 1024).toLong()
                sharedFile.fileSize.contains("KB") -> (sharedFile.fileSize.substringBefore(" ").toDouble() * 1024).toLong()
                else -> 1024L * 1024L
            }

            val customPartitions = listOf(
                PartitionItem(partName, bytes, "Decentralized P2P Mesh swarm downloaded block image.", isRequired = true, isSelected = true, isExtracted = true)
            )

            val p2pLoadedPkg = FirmwarePackage(
                name = "[P2P] ${sharedFile.packageName} $partName",
                deviceModel = sharedFile.packageName,
                androidVersion = "14.0 (P2P Mesh)",
                buildId = "P2P-${(1000..9999).random()}",
                fileFormat = "DOWNLOADED_IMG",
                sizeBytes = bytes,
                filePath = "p2p_swarm/cache/${sharedFile.fileName}",
                partitionCount = 1,
                extractedCount = 1,
                isExtracted = true,
                partitionsJson = serializePartitions(customPartitions)
            )

            val newId = repo.insertPackage(p2pLoadedPkg)
            _isP2PDownloading.value = false
            _p2pDownloadingFileName.value = ""
            addUnpackerLog("📡 [P2P SWARM] Downloaded block image '${sharedFile.fileName}' registered into local Saved Image Library.")
        }
    }

    fun addP2PComment(fileId: Long, author: String, text: String) {
        val currentP2p = _p2pFiles.value.map { file ->
            if (file.id == fileId) {
                val adapter = moshi.adapter<List<P2PComment>>(Types.newParameterizedType(List::class.java, P2PComment::class.java))
                val commentsList = try {
                    adapter.fromJson(file.commentsJson)?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) {
                    mutableListOf()
                }
                commentsList.add(P2PComment(author.ifBlank { "User_${(100..999).random()}" }, text, "Just now"))
                file.copy(
                    commentsJson = adapter.toJson(commentsList),
                    downloadCount = file.downloadCount + 1
                )
            } else {
                file
            }
        }
        _p2pFiles.value = currentP2p
        saveP2PAndForumData()
    }

    fun createForumThread(title: String, author: String, category: String, content: String) {
        val currentThreads = _forumThreads.value.toMutableList()
        val newId = (currentThreads.maxOfOrNull { it.id } ?: 0L) + 1L
        val formatTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        val newThread = ForumThread(
            id = newId,
            title = title,
            author = author.ifBlank { "Coder_${(100..999).random()}" },
            category = category,
            content = content,
            dateFormatted = formatTime,
            repliesJson = moshi.adapter<List<ForumReply>>(Types.newParameterizedType(List::class.java, ForumReply::class.java)).toJson(emptyList())
        )
        currentThreads.add(0, newThread)
        _forumThreads.value = currentThreads
        saveP2PAndForumData()
    }

    fun addForumReply(threadId: Long, author: String, text: String) {
        val currentThreads = _forumThreads.value.map { thread ->
            if (thread.id == threadId) {
                val adapter = moshi.adapter<List<ForumReply>>(Types.newParameterizedType(List::class.java, ForumReply::class.java))
                val repliesList = try {
                    adapter.fromJson(thread.repliesJson)?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) {
                    mutableListOf()
                }
                repliesList.add(ForumReply(author.ifBlank { "User_${(100..999).random()}" }, text, "Just now"))
                thread.copy(repliesJson = adapter.toJson(repliesList))
            } else {
                thread
            }
        }
        _forumThreads.value = currentThreads
        _activeThreadDetail.value = currentThreads.find { it.id == threadId }
        saveP2PAndForumData()
    }

    fun askAiCopilotOnThread(threadId: Long) {
        val thread = _forumThreads.value.find { it.id == threadId } ?: return
        _isCopilotLoading.value = true

        viewModelScope.launch {
            delay(1200) // Analysis delay

            val aiResponse = when (thread.category) {
                "BOOTLOOP" -> "🤖 [DFlash AI Support] Hello! This signature error occurs because when the bootloader is locked, Android Verified Boot (AVB) checks the SHA-256 signatures of boot-critical partitions. If any partition is patched, verification fails. Connect in Fastboot mode and run 'fastboot flashing unlock' if OEM settings allow, or flash raw clean factory system files to repair lock signature blocks."
                "GUIDE" -> "🤖 [DFlash AI Support] Great reference! To expand, 'init_boot' holds kernel configuration scripts and primary ramdisk structures for modern devices. TWRP can be booted directly using 'fastboot boot recovery.img' from computer without overwriting underlying boot sectors permanently."
                "FASTBOOT" -> "🤖 [DFlash AI Support] Hello! AMD Ryzen USB driver conflicts are usually caused by USB 3.0 XHCI controller latency settings in BIOS bootloader contexts. Try plugging into a legacy USB 2.0 (often black) motherboard connector or use an unpowered intermediary hub to fix driver identification instantly."
                else -> "🤖 [DFlash AI Support] Hello! Make sure you are using high-quality C-to-C or A-to-C cables connected directly to motherboard USB lanes rather than front ports to ensure solid host handshake logs."
            }

            addForumReply(threadId, "AI Experts Copilot", aiResponse)
            _isCopilotLoading.value = false
        }
    }

    fun updateFrijaParams(model: String, csc: String, isAuto: Boolean, pda: String, cscPhone: String, phone: String) {
        _frijaModel.value = model
        _frijaCsc.value = csc
        _frijaAutoMode.value = isAuto
        _frijaManualPda.value = pda
        _frijaManualCsc.value = cscPhone
        _frijaManualPhone.value = phone
    }

    fun clearFrijaLogs() {
        _frijaConsoleLogs.value = listOf("📟 Ready. Awaiting Frija updates query...")
    }

    fun addFrijaLog(log: String) {
        val current = _frijaConsoleLogs.value.toMutableList()
        current.add(log)
        _frijaConsoleLogs.value = current
    }

    fun checkFrijaUpdate() {
        if (_isCheckingFrijaUpdate.value) return
        _isCheckingFrijaUpdate.value = true
        addFrijaLog("──────────────────────────────────────")
        addFrijaLog("🔍 Querying central OTA factory ROM servers for model: ${_frijaModel.value} [CSC: ${_frijaCsc.value}]")

        viewModelScope.launch {
            delay(1200)

            val model = _frijaModel.value.trim().uppercase()
            val csc = _frijaCsc.value.trim().uppercase()

            if (model.isEmpty() || csc.isEmpty()) {
                addFrijaLog("❌ Query Error: Model and CSC parameters cannot be empty.")
                _frijaUpdateResult.value = null
                _isCheckingFrijaUpdate.value = false
                return@launch
            }

            val pdaCode = if (_frijaAutoMode.value) "S918BXXU3BWK2" else _frijaManualPda.value
            val cscCode = if (_frijaAutoMode.value) "S918BOXM3BWK2" else _frijaManualCsc.value
            val phoneCode = if (_frijaAutoMode.value) "S918BXXU3BWK2" else _frijaManualPhone.value

            addFrijaLog("📡 Server connection established. Accessing download servers.")
            delay(400)

            addFrijaLog("✔ SUCCESS: Found Stock Firmware matching parameters!")
            addFrijaLog("  ├─ PDA: $pdaCode")
            addFrijaLog("  ├─ CSC: $cscCode")
            addFrijaLog("  ├─ PHONE: $phoneCode")

            val sizeBytes = 6854124992L // ~6.38 GB

            val info = FrijaFirmwareInfo(
                version = pdaCode,
                sizeBytes = sizeBytes,
                osVersion = "Android 14 (Upside Down Cake)",
                changelogUrl = "https://doc.samsungmobile.com/$model/$csc/doc.html",
                buildDate = "2026-05-30",
                fileStatus = "Available on official Samsung fast-CDN servers."
            )

            _frijaUpdateResult.value = info
            addFrijaLog("🖥️ Firmware database entry successfully parsed. Package ready to start download.")
            _isCheckingFrijaUpdate.value = false
        }
    }

    fun downloadFrijaFirmware() {
        val update = _frijaUpdateResult.value ?: return
        if (_isDownloadingFrija.value) return

        _isDownloadingFrija.value = true
        _frijaDownloadProgress.value = 0f
        addFrijaLog("──────────────────────────────────────")
        addFrijaLog("📥 Starting download sequence for: ${_frijaModel.value}_${_frijaCsc.value}_${update.version}.zip")

        viewModelScope.launch {
            val steps = 10
            for (step in 1..steps) {
                delay(300)
                _frijaDownloadProgress.value = step.toFloat() / steps
                _frijaDownloadSpeed.value = "${kotlin.random.Random.nextInt(45, 82)} MB/s"
                addFrijaLog("  ├─ Received blocks [${(step.toFloat()/steps * 100).toInt()}%] at ${_frijaDownloadSpeed.value}...")
            }

            addFrijaLog("✔ Download complete. Verifying secure checksum hashes...")
            delay(400)
            addFrijaLog("  ├─ Calculated CRC32: 0x8BE39FA2. Target CRC32: 0x8BE39FA2. OKAY")

            addFrijaLog("⚙ Decrypting package payloads using proprietary Samsung ENC4 crypto cipher...")
            delay(820)
            addFrijaLog("✔ Decrypted stock binary successfully saved to application directory.")

            val frijaPartitions = listOf(
                PartitionItem("boot", 67108864, "Raw Linux standard kernel bootloader layer. Patch this via Magisk for Root.", isRequired = true),
                PartitionItem("init_boot", 16777216, "Initialization RAM disk configuration for newer security frameworks.", isRequired = true),
                PartitionItem("recovery", 100663296, "Standard OEM factory emergency diagnostic recovery image."),
                PartitionItem("vbmeta", 8192, "AVB verified signature structure metadata.", isRequired = true),
                PartitionItem("system", 2147483648L, "Main stock operating system system files."),
                PartitionItem("vendor", 536870912L, "OEM specific device driver blocks and dynamic binary resources.")
            )

            val newPkg = FirmwarePackage(
                name = "${_frijaModel.value}_${_frijaCsc.value}_OfficialStock.zip",
                deviceModel = _frijaModel.value,
                androidVersion = update.osVersion,
                buildId = update.version,
                fileFormat = "ODIN_TAR",
                sizeBytes = update.sizeBytes,
                filePath = "downloads/frija/${_frijaModel.value}_${_frijaCsc.value}_${update.version}.zip",
                partitionCount = frijaPartitions.size,
                partitionsJson = serializePartitions(frijaPartitions)
            )

            val newId = repo.insertPackage(newPkg)
            val savedPkg = newPkg.copy(id = newId)

            _isDownloadingFrija.value = false
            _frijaUpdateResult.value = null
            addFrijaLog("🎉 REGISTERED: Downloaded stock package successfully parsed! Registered into factory ROM archives.")

            selectPackage(savedPkg)
        }
    }
}

// --- Supporting States ---

sealed interface DeviceState {
    val name: String
    
    object Disconnected : DeviceState {
        override val name = "Disconnected"
    }
    
    object FastbootUnlocked : DeviceState {
        override val name = "Fastboot Mode (Serial: 8A9X19 - UNLOCKED)"
    }

    object FastbootLocked : DeviceState {
        override val name = "Fastboot Mode (Serial: 8A9X19 - LOCKED)"
    }

    object AdbSideload : DeviceState {
        override val name = "ADB Sideload Status (Serial: 8A9X19)"
    }
}

sealed interface FlashStatus {
    object Success : FlashStatus
    data class Failed(val error: String) : FlashStatus
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// --- Frija and Community Support Models ---

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class FrijaFirmwareInfo(
    val version: String,
    val sizeBytes: Long,
    val osVersion: String,
    val changelogUrl: String,
    val buildDate: String,
    val fileStatus: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class P2PSharedFile(
    val id: Long,
    val fileName: String,
    val packageName: String,
    val fileSize: String,
    val sha256: String,
    val uploadedBy: String,
    val downloadCount: Int = 12,
    val commentsJson: String,
    val isUserShared: Boolean = false
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class P2PComment(
    val author: String,
    val commentText: String,
    val dateFormatted: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ForumThread(
    val id: Long,
    val title: String,
    val author: String,
    val category: String, // e.g. "BOOTLOOP", "GUIDE", "ROOT/MAGISK", "FASTBOOT"
    val content: String,
    val dateFormatted: String,
    val repliesJson: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ForumReply(
    val author: String,
    val content: String,
    val dateFormatted: String
)

