package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FirmwarePackage
import com.example.data.model.FlashLog
import com.example.ui.viewmodel.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom high contrast neon-cyberpunk/material-tech design colors
val TechDarkBg = Color(0xFF090D14)
val TechCardBg = Color(0xFF101622)
val TechCyan = Color(0xFF00ADB5)
val TechBlue = Color(0xFF3F72AF)
val TechGreen = Color(0xFF28C76F)
val TechAmber = Color(0xFFFF9F43)
val TechRed = Color(0xFFEA5455)
val TechTextPrimary = Color(0xFFEEEEEE)
val TechTextSecondary = Color(0xFF8B949E)
val TechBorder = Color(0xFF1E293B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: FirmwareViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val packages by viewModel.firmwarePackages.collectAsStateWithLifecycle()
    val selectedPkg by viewModel.selectedPackage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(TechCyan, TechBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsInputHdmi,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DroidFlash Utility",
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimary,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Platform OS Extraction & Automation Suite",
                                style = MaterialTheme.typography.bodySmall,
                                color = TechTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechDarkBg,
                    titleContentColor = TechTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TechCardBg,
                tonalElevation = 8.dp,
                modifier = Modifier.border(width = 1.dp, color = TechBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = activeTab == "unpack",
                    onClick = { viewModel.setActiveTab("unpack") },
                    icon = { Icon(Icons.Default.FolderZip, contentDescription = "Extract") },
                    label = { Text("Extract Manager", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TechCyan,
                        selectedTextColor = TechCyan,
                        indicatorColor = TechBorder,
                        unselectedIconColor = TechTextSecondary,
                        unselectedTextColor = TechTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_extract")
                )
                NavigationBarItem(
                    selected = activeTab == "flash",
                    onClick = { viewModel.setActiveTab("flash") },
                    icon = { Icon(Icons.Default.OfflineBolt, contentDescription = "Flash") },
                    label = { Text("Flasher", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TechCyan,
                        selectedTextColor = TechCyan,
                        indicatorColor = TechBorder,
                        unselectedIconColor = TechTextSecondary,
                        unselectedTextColor = TechTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_flash")
                )
                NavigationBarItem(
                    selected = activeTab == "commands",
                    onClick = { viewModel.setActiveTab("commands") },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Commands") },
                    label = { Text("Command Tools", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TechCyan,
                        selectedTextColor = TechCyan,
                        indicatorColor = TechBorder,
                        unselectedIconColor = TechTextSecondary,
                        unselectedTextColor = TechTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_commands")
                )
                NavigationBarItem(
                    selected = activeTab == "copilot",
                    onClick = { viewModel.setActiveTab("copilot") },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Community Hub") },
                    label = { Text("Community Hub", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TechCyan,
                        selectedTextColor = TechCyan,
                        indicatorColor = TechBorder,
                        unselectedIconColor = TechTextSecondary,
                        unselectedTextColor = TechTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_copilot")
                )
                NavigationBarItem(
                    selected = activeTab == "logs",
                    onClick = { viewModel.setActiveTab("logs") },
                    icon = { Icon(Icons.Default.History, contentDescription = "History Logs") },
                    label = { Text("History logs", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TechCyan,
                        selectedTextColor = TechCyan,
                        indicatorColor = TechBorder,
                        unselectedIconColor = TechTextSecondary,
                        unselectedTextColor = TechTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_logs")
                )
            }
        },
        containerColor = TechDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TechDarkBg)
        ) {
            // Live Status Panel: Shows connected device & selected package universally
            DeviceStatusTickerLayer(viewModel)

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = TechDarkBg
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        "unpack" -> ExtractManagerTab(viewModel, packages, selectedPkg)
                        "flash" -> AutoFlasherTab(viewModel, selectedPkg)
                        "commands" -> AdbFastbootToolkitTab(viewModel)
                        "copilot" -> CommunityAiHubTab(viewModel)
                        "logs" -> HistoricalLogsTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceStatusTickerLayer(viewModel: FirmwareViewModel) {
    val usbState by viewModel.usbDeviceState.collectAsStateWithLifecycle()
    val selectedPkg by viewModel.selectedPackage.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(width = 1.dp, color = TechBorder, shape = RoundedCornerShape(12.dp))
            .background(TechCardBg)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (usbState is DeviceState.Disconnected) TechRed else TechGreen
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "USB Connection Status",
                    style = MaterialTheme.typography.bodySmall,
                    color = TechTextSecondary,
                    fontSize = 11.sp
                )
            }
            Text(
                text = when (usbState) {
                    is DeviceState.Disconnected -> "NO DEVICE DETECTED"
                    is DeviceState.FastbootUnlocked -> "FASTBOOT (UNLOCKED)"
                    is DeviceState.FastbootLocked -> "FASTBOOT (LOCKED ⚠️)"
                    is DeviceState.AdbSideload -> "ADB RECOVERY SIDELOAD"
                },
                color = if (usbState is DeviceState.Disconnected) TechRed else TechCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(28.dp)
                .background(TechBorder)
                .padding(horizontal = 8.dp)
        )

        Column(
            modifier = Modifier
                .weight(1.2f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = "Extraction Package Profile",
                style = MaterialTheme.typography.bodySmall,
                color = TechTextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = selectedPkg?.name ?: "No Package Loaded",
                color = if (selectedPkg != null) TechTextPrimary else TechTextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ExtractManagerTab(
    viewModel: FirmwareViewModel,
    packages: List<FirmwarePackage>,
    selectedPkg: FirmwarePackage?
) {
    var showImportDialog by remember { mutableStateOf(false) }

    if (showImportDialog) {
        CustomImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { name: String, path: String, size: Long ->
                viewModel.importCustomFirmwareFile(name, path, size)
                showImportDialog = false
            }
        )
    }

    if (selectedPkg == null) {
        var subTab by remember { mutableStateOf("vault") } 
        val extractedList = viewModel.getExtractedPartitionsList()

        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = if (subTab == "vault") 0 else 1,
                containerColor = TechDarkBg,
                contentColor = TechCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (subTab == "vault") 0 else 1]),
                        color = TechCyan
                    )
                }
            ) {
                Tab(
                    selected = subTab == "vault",
                    onClick = { subTab = "vault" },
                    text = { Text("🗳️ ROM Vault", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = subTab == "frija",
                    onClick = { subTab = "frija" },
                    text = { Text("⚡ Frija Downloader", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            if (subTab == "vault") {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Unloaded",
                        tint = TechCyan,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Load Stock Firmware Package",
                        color = TechTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select a preloaded demo package below to inspect secure Android partition frames, or register custom files instantly.",
                        color = TechTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Factory ROM archives card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Available Factory ROM Packages",
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        packages.forEach { pkg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectPackage(pkg) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(TechBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (pkg.fileFormat) {
                                                "PAYLOAD_BIN" -> Icons.Default.Code
                                                "ODIN_TAR" -> Icons.Default.DeveloperBoard
                                                else -> Icons.Default.Layers
                                            },
                                            contentDescription = "Format",
                                            tint = TechCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = pkg.name,
                                            color = TechTextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${pkg.deviceModel} • Partitions: ${pkg.partitionCount}",
                                            color = TechTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "load",
                                    tint = TechTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            HorizontalDivider(color = TechBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // Custom ROM registration button
            item {
                Button(
                    onClick = { showImportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("import_custom_btn")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Upload", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register Custom Firmware File", fontSize = 13.sp)
                }
            }

            // Dedicated local extracted images library card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechCardBg.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📂 Saved Extracted Image Library",
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TechCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${extractedList.size} IMAGES TOTAL",
                                    color = TechCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (extractedList.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Empty",
                                    tint = TechTextSecondary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No extracted system images stored yet.",
                                    color = TechTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Select a firmware archive and click 'Unpack' to populate.",
                                    color = TechTextSecondary.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            extractedList.forEach { (part, pkg) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${part.name}.img",
                                                color = TechGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Verified image",
                                                tint = TechGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Text(
                                            text = "Source: ${pkg.name}",
                                            color = TechTextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Size: ${part.formattedSize} • block count: ${part.blockCount}",
                                            color = TechTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { 
                                                viewModel.publishExtractedPartition(
                                                    partitionName = part.name,
                                                    pkgName = pkg.name,
                                                    sizeBytes = part.sizeBytes,
                                                    formattedSize = part.formattedSize
                                                )
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share partition in P2P mesh",
                                                tint = TechCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteExtractedPartition(pkg.id ?: 0L, part.name) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteForever,
                                                contentDescription = "Delete partition image",
                                                tint = TechRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = TechBorder, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    } else {
        FrijaDownloaderView(viewModel)
    }
}
} else {
        // Active Sub-package extraction layout: List partitions + Extraction logs console
        val partitions by viewModel.activePartitions.collectAsStateWithLifecycle()
        val isExtracting by viewModel.isExtracting.collectAsStateWithLifecycle()
        val currentPart by viewModel.currentExtractingPartition.collectAsStateWithLifecycle()
        val progress by viewModel.extractionProgress.collectAsStateWithLifecycle()
        val unpackLogs by viewModel.unpackerConsoleLogs.collectAsStateWithLifecycle()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.deleteHistoricalLogs(); viewModel.setActiveTab("unpack"); viewModel.selectPackage(selectedPkg!!) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Package", color = TechCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Text(
                        text = "Format: ${selectedPkg!!.fileFormat}",
                        color = TechAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(TechBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            item {
                Text(
                    text = "Android Block Unpacker",
                    color = TechTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Select individual blocks from the factory storage tree. Click \"Unpack Payload\" to extract target binaries synchronously with integrity confirmation hashes.",
                    color = TechTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Firmware Partition Tree",
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            val totalImg = partitions.size
                            val extractedM = partitions.count { it.isExtracted }
                            Text(
                                text = "Extracted Image Files: $extractedM / $totalImg",
                                color = TechTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        partitions.forEach { part ->
                            PartitionRowItem(
                                partition = part,
                                isExtracting = isExtracting,
                                onToggle = { viewModel.togglePartitionSelection(part.name) }
                            )
                        }
                    }
                }
            }

            item {
                if (isExtracting) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                            .background(TechCardBg)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Decompressing Block: ${currentPart ?: "Reading catalog..."}",
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = TechCyan,
                            trackColor = TechBorder,
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.startExtraction() },
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("extract_payload_btn")
                    ) {
                        Icon(Icons.Default.FlipToBack, contentDescription = "extract")
                        Spacer(modifier = Modifier.width(8.dp))
                        val countExt = partitions.count { it.isSelected && !it.isExtracted }
                        Text("Unpack $countExt Partition Image Blocks")
                    }
                }
            }

            // Realtime extraction terminal log layer
            item {
                TerminalLogConsole(
                    title = "Block Extraction Console Logs",
                    logsList = unpackLogs
                )
            }
        }
    }
}

@Composable
fun PartitionRowItem(
    partition: PartitionItem,
    isExtracting: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (partition.isExtracted) TechBorder.copy(alpha = 0.3f) else Color.Transparent)
            .clickable(enabled = !isExtracting && !partition.isExtracted) { onToggle() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = partition.isSelected || partition.isExtracted,
                onCheckedChange = { onToggle() },
                enabled = !isExtracting && !partition.isExtracted,
                colors = CheckboxDefaults.colors(
                    checkedColor = TechCyan,
                    checkmarkColor = Color.Black,
                    uncheckedColor = TechTextSecondary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = partition.name,
                        color = if (partition.isExtracted) TechGreen else TechTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    if (partition.isRequired) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CRITICAL",
                            color = TechAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .border(0.5.dp, TechAmber, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = partition.description,
                    color = TechTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = partition.formattedSize,
                color = TechTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (partition.isExtracted) "EXTRACTED ✓" else "PENDING",
                color = if (partition.isExtracted) TechGreen else TechTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    HorizontalDivider(color = TechBorder, thickness = 0.5.dp)
}

@Composable
fun TerminalLogConsole(
    title: String,
    logsList: List<String>
) {
    val shellScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Autoscroll logic inside logs console
    LaunchedEffect(logsList.size) {
        if (logsList.isNotEmpty()) {
            scope.launch {
                shellScrollState.animateScrollToItem(logsList.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            .background(Color(0xFF03070E))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TechTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Console",
                tint = TechTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            LazyColumn(
                state = shellScrollState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logsList) { log ->
                    Text(
                        text = log,
                        color = when {
                            log.contains("❌") || log.contains("FAILED") -> TechRed
                            log.contains("⚠️") -> TechAmber
                            log.contains("✓") || log.contains("COMPLETED") || log.contains("SUCCESS") -> TechGreen
                            log.startsWith("$") -> TechCyan
                            else -> TechTextPrimary
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AutoFlasherTab(
    viewModel: FirmwareViewModel,
    selectedPkg: FirmwarePackage?
) {
    val usbState by viewModel.usbDeviceState.collectAsStateWithLifecycle()
    val isFlashing by viewModel.isFlashing.collectAsStateWithLifecycle()
    val flashProgress by viewModel.flashingProgress.collectAsStateWithLifecycle()
    val flashLogs by viewModel.flashingConsoleLogs.collectAsStateWithLifecycle()
    val showResultDialog by viewModel.showFlashResultDialog.collectAsStateWithLifecycle()
    val lastResultStatus by viewModel.lastFlashingStatus.collectAsStateWithLifecycle()

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFlashDialog() },
            title = {
                Text(
                    text = if (lastResultStatus is FlashStatus.Success) "Flashing Succeeded! 🎉" else "Flashing Failed ⚠️",
                    color = TechTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (lastResultStatus is FlashStatus.Success) {
                        "All system images flashed successfully over Fastboot. Your Android device is ready and rebooting automatically."
                    } else {
                        "Automated flashing process aborted due to locked bootloader. Unlock bootloader or consult the Gemini Co-Pilot."
                    },
                    color = TechTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissFlashDialog() }) {
                    Text("OK", color = TechCyan)
                }
            },
            containerColor = TechCardBg
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Automated ROM Flasher",
                color = TechTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Connect your target device via USB OTG. The DroidFlash engine will scan for Fastboot protocols, flash extracted image binaries, and execute secure reboot schemas.",
                color = TechTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Host USB controller settings: Select mock USB cable input
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "OTG USB Port Emulator Controller",
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Select a simulated Android device hardware status to simulate custom low-level secure flashing:",
                        color = TechTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.changeUsbDeviceState(DeviceState.Disconnected) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = usbState is DeviceState.Disconnected,
                            onClick = { viewModel.changeUsbDeviceState(DeviceState.Disconnected) },
                            colors = RadioButtonDefaults.colors(selectedColor = TechCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("No Target Connected (Disconnected)", color = TechTextPrimary, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.changeUsbDeviceState(DeviceState.FastbootUnlocked) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = usbState is DeviceState.FastbootUnlocked,
                            onClick = { viewModel.changeUsbDeviceState(DeviceState.FastbootUnlocked) },
                            colors = RadioButtonDefaults.colors(selectedColor = TechCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fastboot Mode (Bootloader UNLOCKED ✅)", color = TechTextPrimary, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.changeUsbDeviceState(DeviceState.FastbootLocked) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = usbState is DeviceState.FastbootLocked,
                            onClick = { viewModel.changeUsbDeviceState(DeviceState.FastbootLocked) },
                            colors = RadioButtonDefaults.colors(selectedColor = TechCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fastboot Mode (Bootloader LOCKED ⚠️)", color = TechTextPrimary, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.changeUsbDeviceState(DeviceState.AdbSideload) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = usbState is DeviceState.AdbSideload,
                            onClick = { viewModel.changeUsbDeviceState(DeviceState.AdbSideload) },
                            colors = RadioButtonDefaults.colors(selectedColor = TechCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADB Recovery Sideload Protocol Mode", color = TechTextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            if (isFlashing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                        .background(TechCardBg)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Flashing System Components...",
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${(flashProgress * 100).toInt()}%",
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { flashProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TechCyan,
                        trackColor = TechBorder,
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (selectedPkg != null) {
                            viewModel.startAutomatedFlashing(selectedPkg)
                        }
                    },
                    enabled = selectedPkg != null && usbState !is DeviceState.Disconnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechCyan,
                        disabledContainerColor = TechBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flash_firmware_btn")
                ) {
                    Icon(Icons.Default.OfflineBolt, contentDescription = "Flash Bolt")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            selectedPkg == null -> "Select ROM Package First"
                            usbState is DeviceState.Disconnected -> "Connect USB Cable First"
                            else -> "Start Flashing Sequence"
                        }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Flashing Command Output Output", color = TechTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(
                    text = "Clear Terminal",
                    color = TechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { viewModel.clearFlashTerminal() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }

        item {
            TerminalLogConsole(
                title = "Live Fastboot/ADB Console Executions",
                logsList = flashLogs
            )
        }
    }
}

@Composable
fun CoPilotDiagnosticsTab(viewModel: FirmwareViewModel) {
    val messages by viewModel.copilotMessages.collectAsStateWithLifecycle()
    val isCopilotLoading by viewModel.isCopilotLoading.collectAsStateWithLifecycle()
    val logsInput by viewModel.diagnoseLogsInput.collectAsStateWithLifecycle()
    val diagnoseDevice by viewModel.diagnoseSelectedDevice.collectAsStateWithLifecycle()
    val diagnoseFormat by viewModel.diagnoseSelectedFormat.collectAsStateWithLifecycle()

    val chatScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var userMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // AutoScroll the chat when message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                chatScrollState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Gemini AI Diagnostics Co-Pilot",
            color = TechTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "DroidFlash Co-Pilot is connected to Gemini, ready to analyze failed flashing outputs, explain complex brick loops, or generate flash commands instantly.",
            color = TechTextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Split view style: Upper side holds interactive Chat, bottom side allows pasting logs block for quick diagnosis
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
                .background(TechCardBg)
                .padding(12.dp)
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Awaiting input prompts...", color = TechTextSecondary, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    state = chatScrollState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubbleItem(msg)
                    }
                    if (isCopilotLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = TechCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini analyzing payload data...", color = TechCyan, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Direct Logs diagnosis container
        Card(
            colors = CardDefaults.cardColors(containerColor = TechCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Instant Failed Flashing Diagnostic Portal",
                    color = TechCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
                Text(
                    text = "Force feed raw fastboot/script terminal logs below for AI review:",
                    color = TechTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = diagnoseDevice,
                        onValueChange = { viewModel.setDiagnoseDevice(it) },
                        label = { Text("TARGET DEVICE", fontSize = 9.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechBlue,
                            unfocusedBorderColor = TechBorder,
                            focusedTextColor = TechTextPrimary,
                            unfocusedTextColor = TechTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = diagnoseFormat,
                        onValueChange = { viewModel.setDiagnoseFormat(it) },
                        label = { Text("ROM FORMAT", fontSize = 9.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechBlue,
                            unfocusedBorderColor = TechBorder,
                            focusedTextColor = TechTextPrimary,
                            unfocusedTextColor = TechTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = logsInput,
                    onValueChange = { viewModel.updateDiagnoseLogsInput(it) },
                    placeholder = {
                        Text(
                            text = "Paste your terminal error logs here (e.g. FAILED (remote: partition doesn't exist))...",
                            fontSize = 11.sp,
                            color = TechTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .testTag("ai_logs_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = TechBorder,
                        focusedTextColor = TechTextPrimary,
                        unfocusedTextColor = TechTextPrimary
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.runLiveDiagnostics()
                        focusManager.clearFocus()
                    },
                    enabled = logsInput.isNotBlank() && !isCopilotLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Healing, contentDescription = "Heal", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Live AI Troubleshooting Engine", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                placeholder = { Text("Ask Co-Pilot. e.g. \"how to unlock pixel bootloader?\"", fontSize = 12.sp, color = TechTextSecondary) },
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechCyan,
                    unfocusedBorderColor = TechBorder,
                    focusedTextColor = TechTextPrimary,
                    unfocusedTextColor = TechTextPrimary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (userMessage.isNotBlank()) {
                        viewModel.sendCopilotMessage(userMessage)
                        userMessage = ""
                        focusManager.clearFocus()
                    }
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        viewModel.sendCopilotMessage(userMessage)
                        userMessage = ""
                        focusManager.clearFocus()
                    }
                },
                enabled = userMessage.isNotBlank() && !isCopilotLoading,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (userMessage.isNotBlank()) TechCyan else TechBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (userMessage.isNotBlank()) Color.Black else TechTextSecondary
                )
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val alignStyle = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) TechBlue else TechBorder.copy(alpha = 0.6f)
    val textStyleColor = TechTextPrimary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignStyle
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!message.isUser) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI",
                    tint = TechCyan,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 6.dp)
                )
                Text("DroidFlash AI", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            } else {
                Text("You", color = TechBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(end = 4.dp))
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (message.isUser) 12.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 12.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = textStyleColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun HistoricalLogsTab(viewModel: FirmwareViewModel) {
    val logs by viewModel.flashLogs.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Flashing History Logs Database",
                        color = TechTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "History of local platform installation logs recorded persistently:",
                        color = TechTextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.deleteHistoricalLogs() },
                        modifier = Modifier
                            .background(TechBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Clear database", tint = TechRed)
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = "Empty Data",
                        tint = TechTextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No recorded flashing history logs found in SQLite.",
                        color = TechTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(logs) { log ->
                HistoryLogCard(log)
            }
        }
    }
}

@Composable
fun HistoryLogCard(log: FlashLog) {
    var isExpanded by remember { mutableStateOf(false) }
    val formattedDate = remember(log.timestamp) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = TechCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = log.deviceName,
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Package: ${log.firmwareName}",
                        color = TechTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (log.status == "SUCCESS") TechGreen.copy(alpha = 0.2f) else TechRed.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = log.status,
                        color = if (log.status == "SUCCESS") TechGreen else TechRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Serial: ${log.deviceSerial} • Format: ${log.flashType}",
                    color = TechTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = formattedDate,
                    color = TechTextSecondary,
                    fontSize = 11.sp
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = TechBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Historical Terminal Logs Output:",
                    color = TechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black)
                        .padding(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = log.logs,
                            color = TechTextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomImportDialog(
    onDismiss: () -> Unit,
    onImport: (name: String, path: String, sizeBytes: Long) -> Unit
) {
    var rawName by remember { mutableStateOf("OxygenOS_CustomUnionMod_Payload.bin") }
    var rawPath by remember { mutableStateOf("/storage/emulated/0/Download/custom_mod.bin") }
    var sizeLabelInMb by remember { mutableStateOf("3200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Custom Android Firmware Archive", color = TechTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Simulate loading modular OTA files, fastboot packages, or factory zip blocks securely into local DroidFlash registers.",
                    color = TechTextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = rawName,
                    onValueChange = { rawName = it },
                    label = { Text("File Name with extension") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TechTextPrimary,
                        unfocusedTextColor = TechTextPrimary,
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = TechBorder
                    )
                )
                OutlinedTextField(
                    value = rawPath,
                    onValueChange = { rawPath = it },
                    label = { Text("Simulated Local Storage Path") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TechTextPrimary,
                        unfocusedTextColor = TechTextPrimary,
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = TechBorder
                    )
                )
                OutlinedTextField(
                    value = sizeLabelInMb,
                    onValueChange = { sizeLabelInMb = it },
                    label = { Text("Package size in Megabytes (MB)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TechTextPrimary,
                        unfocusedTextColor = TechTextPrimary,
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = TechBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val actualMb = sizeLabelInMb.toLongOrNull() ?: 2048L
                    onImport(rawName, rawPath, actualMb * 1024L * 1024L)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TechCyan)
            ) {
                Text("Register Package")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TechTextSecondary)
            }
        },
        containerColor = TechCardBg
    )
}

@Composable
fun AdbFastbootToolkitTab(viewModel: FirmwareViewModel) {
    val logs by viewModel.toolkitConsoleLogs.collectAsStateWithLifecycle()
    val isExecuting by viewModel.isExecutingToolkitCommand.collectAsStateWithLifecycle()
    val usbState by viewModel.usbDeviceState.collectAsStateWithLifecycle()

    var showUnlockWarning by remember { mutableStateOf(false) }
    var showDriversTroubleshoot by remember { mutableStateOf(false) }

    // State for interactive checkboxes
    var stepDevOptions by remember { mutableStateOf(true) }
    var stepUsbDebug by remember { mutableStateOf(true) }
    var stepOemUnlock by remember { mutableStateOf(false) }
    var stepUsbCable by remember { mutableStateOf(true) }

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll terminal when logs change
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            scope.launch {
                lazyListState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    if (showUnlockWarning) {
        AlertDialog(
            onDismissRequest = { showUnlockWarning = false },
            title = { Text("⚠️ CRITICAL SECURITY WARNING", color = TechRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Executing 'fastboot flashing unlock' will instantly wipe your entire device (factory reset), deleting all photos, files, and account keys. It also reduces device secure signature checks.\n\nDo you want to proceed with the unlocking simulation?",
                    color = TechTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlockWarning = false
                        viewModel.executeToolkitCommand("fastboot flashing unlock")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechRed)
                ) {
                    Text("Unlock Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockWarning = false }) {
                    Text("Cancel", color = TechTextSecondary)
                }
            },
            containerColor = TechCardBg
        )
    }

    if (showDriversTroubleshoot) {
        AlertDialog(
            onDismissRequest = { showDriversTroubleshoot = false },
            title = { Text("🔌 USB Drivers Troubleshooting Hub", color = TechCyan, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text(
                            text = "If your computer says 'device not found' or fastboot commands hang indefinitely, complete the following:",
                            color = TechTextPrimary,
                            fontSize = 12.sp
                        )
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TechBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("1. Windows OEM Drivers Required", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Download Google USB Driver (or vendor specific drivers for Samsung, OnePlus, Xiaomi) from official channels and install via Device Manager.", color = TechTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TechBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("2. Use Core High Quality USB Ports", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Always connect to USB 2.0 or certified USB 3.0 ports directly on the motherboard. Avoid unpowered USB hubs or extensions.", color = TechTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TechBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("3. Cable Connection Type", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Use the original OEM C-to-C or A-to-C fast charging cable. Do not use audio-only cables.", color = TechTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDriversTroubleshoot = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan)
                ) {
                    Text("Got It")
                }
            },
            containerColor = TechCardBg
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        item {
            Column {
                Text(
                    text = "ADB & Fastboot Command Center",
                    color = TechTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Verify terminal connection nodes, toggle fastboot states, or run standard ADB instructions with custom touch commands.",
                    color = TechTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Checklist + Connection diagnostics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔌 USB Connection Diagnostics",
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (usbState) {
                                        is DeviceState.Disconnected -> TechRed.copy(alpha = 0.15f)
                                        else -> TechGreen.copy(alpha = 0.15f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (usbState) {
                                    is DeviceState.Disconnected -> "DISCONNECTED"
                                    is DeviceState.FastbootUnlocked -> "FASTBOOT (UNLOCKED)"
                                    is DeviceState.FastbootLocked -> "FASTBOOT (LOCKED)"
                                    is DeviceState.AdbSideload -> "ADB SIDELOAD"
                                },
                                color = when (usbState) {
                                    is DeviceState.Disconnected -> TechRed
                                    else -> TechGreen
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Initialize your phone for ADB commands by checking off the connection prerequisites below:",
                        color = TechTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Interactive rows
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { stepDevOptions = !stepDevOptions }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = stepDevOptions,
                                onCheckedChange = { stepDevOptions = it },
                                colors = CheckboxDefaults.colors(checkedColor = TechCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "1. Enable Developer Options",
                                    color = if (stepDevOptions) TechTextPrimary else TechTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    textDecoration = if (stepDevOptions) androidx.compose.ui.text.style.TextDecoration.None else androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                                Text(
                                    text = "Tap Build Number 7 times inside Settings -> About Phone.",
                                    color = TechTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { stepUsbDebug = !stepUsbDebug }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = stepUsbDebug,
                                onCheckedChange = { stepUsbDebug = it },
                                colors = CheckboxDefaults.colors(checkedColor = TechCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "2. Turn on USB Debugging",
                                    color = if (stepUsbDebug) TechTextPrimary else TechTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    textDecoration = if (stepUsbDebug) androidx.compose.ui.text.style.TextDecoration.None else androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                                Text(
                                    text = "Enable inside Settings -> System -> Developer Options.",
                                    color = TechTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { stepOemUnlock = !stepOemUnlock }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = stepOemUnlock,
                                onCheckedChange = { stepOemUnlock = it },
                                colors = CheckboxDefaults.colors(checkedColor = TechCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "3. Approve OEM Bootloader Unlocking",
                                    color = if (stepOemUnlock) TechTextPrimary else TechTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Toggle the 'OEM Unlocking' switch in developer settings to ON.",
                                    color = TechTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { stepUsbCable = !stepUsbCable }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = stepUsbCable,
                                onCheckedChange = { stepUsbCable = it },
                                colors = CheckboxDefaults.colors(checkedColor = TechCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "4. Establish Stable USB connection link",
                                    color = if (stepUsbCable) TechTextPrimary else TechTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Use dynamic USB 3.0 Cables connected straight to primary motherboard ports.",
                                    color = TechTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDriversTroubleshoot = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TechBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Healing, contentDescription = "Trouble", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fix Driver Errors", fontSize = 11.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                if (usbState is DeviceState.Disconnected) {
                                    viewModel.changeUsbDeviceState(DeviceState.FastbootUnlocked)
                                    viewModel.addToolkitLog("🔌 Emulated plug-in: Google Pixel 8 connected in Fastboot unlocked mode!")
                                } else {
                                    viewModel.changeUsbDeviceState(DeviceState.Disconnected)
                                    viewModel.addToolkitLog("🔌 Emulated cable disconnected by user.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (usbState is DeviceState.Disconnected) TechCyan else TechRed
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OfflineBolt, contentDescription = "Connect", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (usbState is DeviceState.Disconnected) "Emulate Plug-In" else "Unplug Cable", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Command tools suite
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🛠️ ADB Instruction Simulator",
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Common utility shell triggers executed over standard USB bridge protocol:",
                        color = TechTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.executeToolkitCommand("adb devices") },
                            enabled = !isExecuting,
                            colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("adb devices", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }

                        Button(
                            onClick = { viewModel.executeToolkitCommand("adb reboot bootloader") },
                            enabled = !isExecuting,
                            colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("reboot bootloader", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚡ Android Fastboot Command Suite",
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Tap any button below to trigger secure fastboot operations on the target partition table:",
                        color = TechTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeToolkitCommand("fastboot devices") },
                                enabled = !isExecuting,
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("fastboot devices", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { viewModel.executeToolkitCommand("fastboot getvar all") },
                                enabled = !isExecuting,
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("getvar all", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeToolkitCommand("fastboot oem device-info") },
                                enabled = !isExecuting,
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("oem device-info", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { showUnlockWarning = true },
                                enabled = !isExecuting,
                                colors = ButtonDefaults.buttonColors(containerColor = TechRed),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("flashing unlock ⚠️", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeToolkitCommand("fastboot erase cache") },
                                enabled = !isExecuting,
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("erase cache", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { viewModel.executeToolkitCommand("fastboot format userdata") },
                                enabled = !isExecuting,
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("format userdata", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeToolkitCommand("fastboot reboot-bootloader") },
                                enabled = !isExecuting,
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("reboot bootloader", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { viewModel.executeToolkitCommand("fastboot reboot") },
                                enabled = !isExecuting,
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("reboot target OS", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live actions console log output
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📟 Platform Tools Terminal Console Output",
                    color = TechTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Clear Terminal",
                    color = TechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { viewModel.clearToolkitTerminal() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(1.dp, TechBorder, RoundedCornerShape(8.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { logLine ->
                            val textColor = when {
                                logLine.startsWith("$ ") -> TechCyan
                                logLine.contains("FAILED") || logLine.contains("error:") || logLine.contains("FAILED (remote:") -> TechRed
                                logLine.contains("OKAY") || logLine.contains("successfully!") || logLine.contains("🔌 Device detected") -> TechGreen
                                logLine.startsWith("(") -> TechTextSecondary
                                else -> TechTextPrimary
                            }
                            Text(
                                text = logLine,
                                color = textColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Frija and Community Board Support Views ---

@Composable
fun FrijaDownloaderView(viewModel: FirmwareViewModel) {
    val model by viewModel.frijaModel.collectAsStateWithLifecycle()
    val csc by viewModel.frijaCsc.collectAsStateWithLifecycle()
    val autoMode by viewModel.frijaAutoMode.collectAsStateWithLifecycle()
    val manualPda by viewModel.frijaManualPda.collectAsStateWithLifecycle()
    val manualCsc by viewModel.frijaManualCsc.collectAsStateWithLifecycle()
    val manualPhone by viewModel.frijaManualPhone.collectAsStateWithLifecycle()
    
    val checking by viewModel.isCheckingFrijaUpdate.collectAsStateWithLifecycle()
    val result by viewModel.frijaUpdateResult.collectAsStateWithLifecycle()
    val logs by viewModel.frijaConsoleLogs.collectAsStateWithLifecycle()
    
    val downloading by viewModel.isDownloadingFrija.collectAsStateWithLifecycle()
    val progress by viewModel.frijaDownloadProgress.collectAsStateWithLifecycle()
    val speed by viewModel.frijaDownloadSpeed.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                border = BorderStroke(1.dp, TechBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚡ Frija Stock OEM ROM Download Engine",
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connects directly to OEM secure OTA provisioning gateways to request, download, and decrypt factory updates directly to your local workstation library.",
                        color = TechTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                border = BorderStroke(1.dp, TechBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = model,
                            onValueChange = { viewModel.updateFrijaParams(it, csc, autoMode, manualPda, manualCsc, manualPhone) },
                            label = { Text("MODEL (e.g. SM-S918B)", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechCyan,
                                unfocusedBorderColor = TechBorder,
                                focusedTextColor = TechTextPrimary,
                                unfocusedTextColor = TechTextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = csc,
                            onValueChange = { viewModel.updateFrijaParams(model, it, autoMode, manualPda, manualCsc, manualPhone) },
                            label = { Text("CSC/REGION (e.g. EUX)", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechCyan,
                                unfocusedBorderColor = TechBorder,
                                focusedTextColor = TechTextPrimary,
                                unfocusedTextColor = TechTextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Automatic Firmware Detection", color = TechTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Queries the latest official public release OTA version.", color = TechTextSecondary, fontSize = 10.sp)
                        }
                        Switch(
                            checked = autoMode,
                            onCheckedChange = { viewModel.updateFrijaParams(model, csc, it, manualPda, manualCsc, manualPhone) },
                            colors = SwitchDefaults.colors(checkedThumbColor = TechCyan, checkedTrackColor = TechCyan.copy(alpha = 0.4f))
                        )
                    }

                    if (!autoMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TechDarkBg),
                            border = BorderStroke(0.5.dp, TechBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Manual ROM Cipher Codes", color = TechAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                
                                OutlinedTextField(
                                    value = manualPda,
                                    onValueChange = { viewModel.updateFrijaParams(model, csc, autoMode, it, manualCsc, manualPhone) },
                                    label = { Text("PDA VERSION", fontSize = 9.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TechCyan, unfocusedBorderColor = TechBorder),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = manualCsc,
                                    onValueChange = { viewModel.updateFrijaParams(model, csc, autoMode, manualPda, it, manualPhone) },
                                    label = { Text("CSC VERSION", fontSize = 9.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TechCyan, unfocusedBorderColor = TechBorder),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = manualPhone,
                                    onValueChange = { viewModel.updateFrijaParams(model, csc, autoMode, manualPda, manualCsc, it) },
                                    label = { Text("PHONE VERSION", fontSize = 9.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TechCyan, unfocusedBorderColor = TechBorder),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.checkFrijaUpdate()
                            focusManager.clearFocus()
                        },
                        enabled = !checking && !downloading,
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (checking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Querying OEM Servers...", color = Color.Black, fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Wifi, contentDescription = "wifi", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check for Stock OTA update", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (result != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechCardBg),
                    border = BorderStroke(1.dp, TechGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Cloud", tint = TechGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("STOCK DISCOVERED: ${model.uppercase()}", color = TechGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text("• Version (PDA): ${result!!.version}", color = TechTextPrimary, fontSize = 11.sp)
                        Text("• Android Target: ${result!!.osVersion}", color = TechTextPrimary, fontSize = 11.sp)
                        Text("• Build date: ${result!!.buildDate}", color = TechTextPrimary, fontSize = 11.sp)
                        val formattedSize = remember(result!!.sizeBytes) {
                            val gb = result!!.sizeBytes.toDouble() / 1024 / 1024 / 1024
                            String.format("%.2f GB", gb)
                        }
                        Text("• Archive Size: $formattedSize", color = TechTextPrimary, fontSize = 11.sp)
                        Text("• OTA Download Key: Samsung ENC4 decrypted server path.", color = TechTextSecondary, fontSize = 10.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        if (downloading) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Transferring bytes: ${(progress * 100).toInt()}%", color = TechCyan, fontSize = 11.sp)
                                    Text(speed, color = TechCyan, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = TechCyan,
                                    trackColor = TechBorder
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.downloadFrijaFirmware() },
                                colors = ButtonDefaults.buttonColors(containerColor = TechGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download Stock Package Zip", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Frija Server Telemetry Logging", color = TechTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.clearFrijaLogs() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "clear logs", tint = TechTextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                        .background(TechDarkBg)
                        .padding(8.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = true) {
                        items(logs.asReversed()) { log ->
                            Text(
                                text = log,
                                color = if (log.contains("✔") || log.contains("SUCCESS") || log.contains("REGISTERED")) TechGreen else if (log.contains("❌") || log.contains("Error")) TechRed else TechTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityAiHubTab(viewModel: FirmwareViewModel) {
    var selectedTab by remember { mutableStateOf("diagnostics") } // "diagnostics", "swarm", "forum"

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = when (selectedTab) {
                "diagnostics" -> 0
                "swarm" -> 1
                else -> 2
            },
            containerColor = TechDarkBg,
            contentColor = TechCyan,
            indicator = { tabPositions ->
                val idx = when (selectedTab) {
                    "diagnostics" -> 0
                    "swarm" -> 1
                    else -> 2
                }
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[idx]),
                    color = TechCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == "diagnostics",
                onClick = { selectedTab = "diagnostics" },
                text = { Text("💬 AI Assist", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == "swarm",
                onClick = { selectedTab = "swarm" },
                text = { Text("🌀 P2P Swarm", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == "forum",
                onClick = { selectedTab = "forum" },
                text = { Text("📣 Help Board", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        when (selectedTab) {
            "diagnostics" -> CoPilotDiagnosticsTab(viewModel)
            "swarm" -> P2PSwarmView(viewModel)
            "forum" -> ForumBoardView(viewModel)
        }
    }
}

@Composable
fun P2PSwarmView(viewModel: FirmwareViewModel) {
    val p2pFiles by viewModel.p2pFiles.collectAsStateWithLifecycle()
    val downloading by viewModel.isP2PDownloading.collectAsStateWithLifecycle()
    val progress by viewModel.p2pProgress.collectAsStateWithLifecycle()
    val downloadingFileName by viewModel.p2pDownloadingFileName.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var expandedFileId by remember { mutableStateOf<Long?>(null) }
    var commentText by remember { mutableStateOf("") }

    val filteredFiles = remember(p2pFiles, searchQuery) {
        if (searchQuery.isBlank()) p2pFiles
        else p2pFiles.filter { it.fileName.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TechCardBg),
                border = BorderStroke(1.dp, TechBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🌀 Decentralized P2P ROM Mesh Swarm", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Powered by BitTorrent-style distributed DHT indexes. Download block-level partition shares (boot, recovery, vbmeta) or seed your own extracted files with other users.", color = TechTextSecondary, fontSize = 11.sp)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TechDarkBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Peer Nodes: 45", color = TechGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Swarm Type: Kademlia DHT", color = TechCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Bit-rate: 2.1 GB/s", color = TechAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search swarm shares (e.g. boot.img, OnePlus)...", fontSize = 11.sp, color = TechTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TechTextSecondary, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TechCyan, unfocusedBorderColor = TechBorder),
                singleLine = true
            )
        }

        if (downloading) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechDarkBg),
                    border = BorderStroke(1.dp, TechCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pulling P2P block: $downloadingFileName", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${(progress * 100).toInt()}%", color = TechCyan, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = TechCyan,
                            trackColor = TechBorder,
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Verifying swarm bit distribution integrity, streaming chunk bytes...", color = TechCyan, fontSize = 10.sp)
                    }
                }
            }
        }

        if (filteredFiles.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No shared files found in mesh index.", color = TechTextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            items(filteredFiles) { file ->
                val isExpanded = expandedFileId == file.id
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechCardBg),
                    border = BorderStroke(1.dp, if (file.isUserShared) TechCyan else TechBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedFileId = if (isExpanded) null else file.id }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(file.fileName, color = TechTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (file.isUserShared) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Seeding", fontSize = 8.sp, color = TechCyan) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = TechBorder)
                                        )
                                    }
                                }
                                Text("Device: ${file.packageName}", color = TechTextSecondary, fontSize = 11.sp)
                                Text("Uploader: ${file.uploadedBy} • SHA-256 Verified", color = TechTextSecondary, fontSize = 10.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(file.fileSize, color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (!file.isUserShared) {
                                    Button(
                                        onClick = { viewModel.downloadP2PImage(file) },
                                        enabled = !downloading,
                                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Pull", tint = Color.Black, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("P2P Pull", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = TechBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Technical Comments from Peers", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            
                            val adapter = remember { viewModel.moshi.adapter<List<P2PComment>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, P2PComment::class.java)) }
                            val comments = remember(file.commentsJson) {
                                try { adapter.fromJson(file.commentsJson) ?: emptyList() } catch(e: Exception) { emptyList() }
                            }

                            if (comments.isEmpty()) {
                                Text("No verification notes posted yet. Post a signature review note below!", color = TechTextSecondary, fontSize = 10.sp, modifier = Modifier.padding(vertical = 4.dp))
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                    comments.forEach { comment ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(TechDarkBg, RoundedCornerShape(4.dp))
                                                .padding(6.dp)
                                        ) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(comment.author, color = TechGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                Text(comment.dateFormatted, color = TechTextSecondary, fontSize = 8.sp)
                                            }
                                            Text(comment.commentText, color = TechTextPrimary, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    placeholder = { Text("Type technical review note...", fontSize = 10.sp, color = TechTextSecondary) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TechCyan, unfocusedBorderColor = TechBorder),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (commentText.isNotBlank()) {
                                            viewModel.addP2PComment(file.id, "LocalUser", commentText)
                                            commentText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Post", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForumBoardView(viewModel: FirmwareViewModel) {
    val threads by viewModel.forumThreads.collectAsStateWithLifecycle()
    val activeThread by viewModel.activeThreadDetail.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isCopilotLoading.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newBody by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("BOOTLOOP") }
    var replyText by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Launch New Discussion Thread", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("THREAD TITLE", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TechCyan, unfocusedBorderColor = TechBorder),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Category:", color = TechTextSecondary, fontSize = 12.sp)
                        listOf("BOOTLOOP", "GUIDE", "FASTBOOT").forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .clickable { newCategory = cat }
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = newCategory == cat,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = TechCyan)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(cat, color = TechTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newBody,
                        onValueChange = { newBody = it },
                        label = { Text("DETAILED QUESTION OR LOGS DATA", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TechCyan, unfocusedBorderColor = TechBorder),
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newBody.isNotBlank()) {
                            viewModel.createForumThread(newTitle, "FlasherPro", newCategory, newBody)
                            newTitle = ""
                            newBody = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan)
                ) {
                    Text("Publish Thread", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TechTextSecondary)
                }
            },
            containerColor = TechCardBg
        )
    }

    if (activeThread != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectThread(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TechCyan)
                }
                Text("Back to Help Threads", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val tagColor = when (activeThread!!.category) {
                        "BOOTLOOP" -> TechRed
                        "GUIDE" -> TechGreen
                        else -> TechAmber
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TechCardBg),
                        border = BorderStroke(1.dp, TechBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(activeThread!!.category, color = tagColor, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = TechBorder)
                                )
                                Text(activeThread!!.dateFormatted, color = TechTextSecondary, fontSize = 9.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(activeThread!!.title, color = TechTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Author: ${activeThread!!.author}", color = TechCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = TechBorder)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(activeThread!!.content, color = TechTextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = { viewModel.askAiCopilotOnThread(activeThread!!.id) },
                                enabled = !isAiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isAiLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mobilizing Gemini Experts...", color = Color.White, fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.Psychology, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ask AI Expert Copilot to Answer", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Discussion Replies Stream", color = TechTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                val adapter = viewModel.moshi.adapter<List<ForumReply>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, ForumReply::class.java))
                val replies = try { adapter.fromJson(activeThread!!.repliesJson) ?: emptyList() } catch(e: Exception) { emptyList() }

                if (replies.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("No technical replies posted yet. Share your experience!", color = TechTextSecondary, fontSize = 11.sp)
                        }
                    }
                } else {
                    items(replies) { reply ->
                        val isAiResponse = reply.author.contains("AI", ignoreCase = true)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isAiResponse) TechDarkBg else TechCardBg),
                            border = BorderStroke(1.dp, if (isAiResponse) TechCyan else TechBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = reply.author,
                                        color = if (isAiResponse) TechCyan else TechGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(reply.dateFormatted, color = TechTextSecondary, fontSize = 9.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(reply.content, color = TechTextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Write technical guidance response...", fontSize = 11.sp, color = TechTextSecondary) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TechCyan, unfocusedBorderColor = TechBorder),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            viewModel.addForumReply(activeThread!!.id, "CoderYou", replyText)
                            replyText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Reply", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechCardBg),
                    border = BorderStroke(1.dp, TechBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text("📣 Interactive Technical Support Forum", color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Post questions, seek logs validation help from expert flashers, or mobilize AI expert guidance on active threads.", color = TechTextSecondary, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Thread", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(threads) { thread ->
                val adapter = viewModel.moshi.adapter<List<ForumReply>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, ForumReply::class.java))
                val replyCount = try { adapter.fromJson(thread.repliesJson)?.size ?: 0 } catch(e: Exception) { 0 }
                val tagColor = when (thread.category) {
                    "BOOTLOOP" -> TechRed
                    "GUIDE" -> TechGreen
                    else -> TechAmber
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = TechCardBg),
                    border = BorderStroke(1.dp, TechBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectThread(thread) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(thread.category, color = tagColor, fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = TechBorder)
                            )
                            Text(thread.dateFormatted, color = TechTextSecondary, fontSize = 9.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(thread.title, color = TechTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(thread.content, color = TechTextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = TechBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Author: ${thread.author}", color = TechCyan.copy(alpha = 0.8f), fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Comment, contentDescription = "replies", tint = TechTextSecondary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$replyCount replies", color = TechTextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
