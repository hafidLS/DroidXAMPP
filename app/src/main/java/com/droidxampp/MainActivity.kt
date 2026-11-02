package com.droidxampp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droidxampp.termux.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DroidXamppApp() }
    }
}

private enum class Page(val label: String) {
    DASHBOARD("Dashboard"),
    DATABASE("Database"),
    FILES("Files"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DroidXamppApp() {
    val context = LocalContext.current

    var page by remember { mutableStateOf(Page.DASHBOARD) }
    var message by remember {
        mutableStateOf("Siap. Hubungkan aplikasi dengan Termux.")
    }

    var monitorText by remember {
        mutableStateOf(TermuxResultStore.monitor.value)
    }

    LaunchedEffect(Unit) {
        TermuxResultStore.monitor.collectLatest {
            monitorText = it
        }
    }

    val apacheStatus = monitorText
        .lineSequence()
        .firstOrNull { it.startsWith("APACHE_STATUS=") }
        ?.substringAfter("=")

    val mariaStatus = monitorText
        .lineSequence()
        .firstOrNull { it.startsWith("MARIADB_STATUS=") }
        ?.substringAfter("=")

    val apacheRunning = apacheStatus == "RUNNING"
    val mariaRunning = mariaStatus == "RUNNING"

    // Pemantauan otomatis saat Dashboard dibuka.
    LaunchedEffect(page) {
        if (page == Page.DASHBOARD) {
            while (isActive) {
                requestMonitor(
                    context,
                    onError = { message = it }
                )
                delay(5000)
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF42D392),
            secondary = androidx.compose.ui.graphics.Color(0xFF80CBC4)
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "DroidXAMPP",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Termux control panel",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    Page.values().forEach { item ->
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item },
                            icon = {
                                Icon(
                                    imageVector = when (item) {
                                        Page.DASHBOARD -> Icons.Default.Dashboard
                                        Page.DATABASE -> Icons.Default.Storage
                                        Page.FILES -> Icons.Default.Folder
                                        Page.SETTINGS -> Icons.Default.Tune
                                    },
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (page) {
                    Page.DASHBOARD -> {
                        Text(
                            "Server Dashboard",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text("Kelola layanan lokal yang berjalan melalui Termux.")

                        ServiceCard(
                            title = "Apache",
                            subtitle = "HTTP Server • Port 8080",
                            statusText = apacheStatus ?: "Belum diperiksa",
                            isRunning = apacheRunning,
                            start = {
                                runService(
                                    context,
                                    Service.APACHE,
                                    Action.START,
                                    { message = it },
                                    { message = it }
                                )
                            },
                            stop = {
                                runService(
                                    context,
                                    Service.APACHE,
                                    Action.STOP,
                                    { message = it },
                                    { message = it }
                                )
                            },
                            status = {
                                requestMonitor(context) {
                                    message = it
                                }
                            }
                        )

                        ServiceCard(
                            title = "MariaDB",
                            subtitle = "Database Server • Port 3306",
                            statusText = mariaStatus ?: "Belum diperiksa",
                            isRunning = mariaRunning,
                            start = {
                                runService(
                                    context,
                                    Service.MARIADB,
                                    Action.START,
                                    { message = it },
                                    { message = it }
                                )
                            },
                            stop = {
                                runService(
                                    context,
                                    Service.MARIADB,
                                    Action.STOP,
                                    { message = it },
                                    { message = it }
                                )
                            },
                            status = {
                                requestMonitor(context) {
                                    message = it
                                }
                            }
                        )

                        Text(
                            "Akses Layanan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                openLocalPage(
                                    context,
                                    "http://127.0.0.1:8080",
                                    { message = it }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Language, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Buka Localhost Apache")
                        }

                        OutlinedButton(
                            onClick = {
                                openLocalPage(
                                    context,
                                    "http://127.0.0.1:8080/phpmyadmin/",
                                    { message = it }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Storage, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Buka Database (phpMyAdmin)")
                        }

                        InfoCard(
                            "PHP",
                            "PHP berjalan melalui integrasi Apache di Termux."
                        )

                        InfoCard("Aktivitas terakhir", message)

                        LogPanel(
                            extractLogs(monitorText)
                        )

                    }

                    Page.DATABASE -> {
                        Text(
                            "Database Manager",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        InfoCard(
                            "phpMyAdmin",
                            "Panel database akan menggunakan phpMyAdmin melalui WebView lokal."
                        )

                        InfoCard(
                            "MariaDB",
                            "Host: 127.0.0.1\nPort: 3306\n\nMariaDB adalah layanan database, bukan halaman web yang bisa dibuka langsung melalui browser."
                        )

                        InfoCard(
                            "Status pengembangan",
                            "Integrasi Database Manager belum dibuat."
                        )
                    }

                    Page.FILES -> {
                        Text(
                            "File Manager",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        InfoCard(
                            "Root penyimpanan",
                            "/storage/emulated/0/"
                        )

                        InfoCard(
                            "Folder website",
                            "/storage/emulated/0/htdocs/"
                        )

                        InfoCard(
                            "Status pengembangan",
                            "File Manager belum terhubung. Fitur daftar, buat, edit, hapus, dan unggah file akan dibuat pada tahap berikutnya."
                        )
                    }

                    Page.SETTINGS -> {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        InfoCard("Termux Home", TermuxCommand.HOME)

                        InfoCard(
                            "Folder skrip",
                            TermuxCommand.SCRIPT_DIR
                        )

                        InfoCard(
                            "Persiapan Termux",
                            "1. Aktifkan allow-external-apps=true.\n2. Berikan izin RUN_COMMAND kepada DroidXAMPP.\n3. Letakkan skrip di ~/droidxampp/scripts/."
                        )

                        Button(
                            onClick = {
                                try {
                                    val launch = context.packageManager
                                        .getLaunchIntentForPackage("com.termux")

                                    if (launch != null) {
                                        context.startActivity(launch)
                                    } else {
                                        message = "Termux tidak ditemukan."
                                    }
                                } catch (e: Exception) {
                                    message = "Termux tidak dapat dibuka."
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Buka Termux")
                        }
                    }
                }
            }
        }
    }
}

private fun requestMonitor(
    context: android.content.Context,
    onError: (String) -> Unit
) {
    TermuxCommandRunner.run(
        context,
        Service.MONITOR,
        Action.STATUS,
        {},
        { onError("Monitor gagal: $it") }
    )
}

private fun runService(
    context: android.content.Context,
    service: Service,
    action: Action,
    success: (String) -> Unit,
    failure: (String) -> Unit
) {
    TermuxCommandRunner.run(
        context,
        service,
        action,
        {
            success("${service.title}: perintah ${action.arg} dikirim ke Termux.")
        },
        {
            failure("Gagal: $it")
        }
    )
}

private fun openLocalPage(
    context: android.content.Context,
    url: String,
    onError: (String) -> Unit
) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        )
    } catch (e: Exception) {
        onError("Halaman tidak dapat dibuka: ${e.message}")
    }
}

private fun extractLogs(raw: String): String {
    val start = raw.indexOf("LOG_BEGIN")
    val end = raw.indexOf("LOG_END")

    if (start == -1 || end == -1 || end <= start) {
        return "Menunggu data log dari Termux..."
    }

    return raw.substring(
        start + "LOG_BEGIN".length,
        end
    ).trim().ifBlank {
        "Belum ada aktivitas."
    }
}

@Composable
private fun ServiceCard(
    title: String,
    subtitle: String,
    statusText: String,
    isRunning: Boolean,
    start: () -> Unit,
    stop: () -> Unit,
    status: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Dns,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    statusText,
                    color = if (isRunning) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = if (isRunning) 1f else 0f,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start")
                }

                OutlinedButton(
                    onClick = stop,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }

                OutlinedButton(
                    onClick = status,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Refresh")
                }
            }
        }
    }
}

\n@Composable
private fun LogPanel(logs: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Log Aktivitas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "Riwayat aktivitas Apache dan MariaDB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    logs,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
