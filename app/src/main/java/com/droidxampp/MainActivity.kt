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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droidxampp.termux.*
import kotlinx.coroutines.flow.collectLatest

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
    var result by remember {
        mutableStateOf(TermuxResultStore.latest.value)
    }

    LaunchedEffect(Unit) {
        TermuxResultStore.latest.collectLatest {
            result = it
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

                        Text(
                            "Kelola layanan lokal yang berjalan melalui Termux."
                        )

                        ServiceCard(
                            "Apache",
                            "HTTP Server • Port 8080",
                            {
                                runService(
                                    context, Service.APACHE, Action.START,
                                    { message = it }, { message = it }
                                )
                            },
                            {
                                runService(
                                    context, Service.APACHE, Action.STOP,
                                    { message = it }, { message = it }
                                )
                            },
                            {
                                runService(
                                    context, Service.APACHE, Action.STATUS,
                                    { message = it }, { message = it }
                                )
                            }
                        )

                        ServiceCard(
                            "MariaDB",
                            "Database Server • Port 3306",
                            {
                                runService(
                                    context, Service.MARIADB, Action.START,
                                    { message = it }, { message = it }
                                )
                            },
                            {
                                runService(
                                    context, Service.MARIADB, Action.STOP,
                                    { message = it }, { message = it }
                                )
                            },
                            {
                                runService(
                                    context, Service.MARIADB, Action.STATUS,
                                    { message = it }, { message = it }
                                )
                            }
                        )

                        Button(
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("http://127.0.0.1:8080")
                                        )
                                    )
                                } catch (e: Exception) {
                                    message = "Browser tidak dapat dibuka."
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Language, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Buka Localhost Apache")
                        }

                        InfoCard("PHP", "PHP berjalan melalui integrasi Apache di Termux.")

                        InfoCard("Aktivitas terakhir", message)

                        InfoCard(
                            "Log aktivitas",
                            result
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
                            "Integrasi phpMyAdmin belum dibuat."
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
                                    context.startActivity(
                                        context.packageManager
                                            .getLaunchIntentForPackage("com.termux")
                                    )
                                } catch (e: Exception) {
                                    message = "Termux tidak ditemukan."
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

@Composable
private fun ServiceCard(
    title: String,
    subtitle: String,
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

                Column {
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
            }

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
                    Text("Status")
                }
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
