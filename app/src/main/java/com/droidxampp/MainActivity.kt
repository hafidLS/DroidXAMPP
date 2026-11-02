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

private enum class Page(val label: String) { HOME("Dashboard"), SERVICES("Services"), DATABASE("Database"), FILES("Files"), LOGS("Logs"), SETTINGS("Settings") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DroidXamppApp() {
    val context = LocalContext.current
    var page by remember { mutableStateOf(Page.HOME) }
    var message by remember { mutableStateOf("Siap. Hubungkan aplikasi dengan Termux.") }
    var result by remember { mutableStateOf(TermuxResultStore.latest.value) }
    LaunchedEffect(Unit) { TermuxResultStore.latest.collectLatest { result = it } }
    MaterialTheme(colorScheme = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF42D392), secondary = androidx.compose.ui.graphics.Color(0xFF80CBC4))) {
        Scaffold(
            topBar = { TopAppBar(title = { Column { Text("DroidXAMPP", fontWeight = FontWeight.Bold); Text("Termux control panel", style = MaterialTheme.typography.labelSmall) } }) },
            bottomBar = {
                NavigationBar {
                    listOf(Page.HOME, Page.SERVICES, Page.DATABASE, Page.FILES, Page.LOGS, Page.SETTINGS).forEach { item ->
                        NavigationBarItem(selected = page == item, onClick = { page = item }, icon = {
                            Icon(when(item) { Page.HOME -> Icons.Default.Dashboard; Page.SERVICES -> Icons.Default.Settings; Page.DATABASE -> Icons.Default.Storage; Page.FILES -> Icons.Default.Folder; Page.LOGS -> Icons.Default.Terminal; Page.SETTINGS -> Icons.Default.Tune }, contentDescription = item.label)
                        }, label = { Text(item.label) })
                    }
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when(page) {
                    Page.HOME -> {
                        Text("Local server control", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Kontrol layanan yang berjalan di Termux. Status ditampilkan dari hasil perintah, bukan ditebak.")
                        ServiceCard("Apache", "HTTP server • port 8080", { runService(context, Service.APACHE, Action.START, {message=it},{message=it}) }, { runService(context, Service.APACHE, Action.STOP, {message=it},{message=it}) }, { runService(context, Service.APACHE, Action.STATUS, {message=it},{message=it}) })
                        ServiceCard("MariaDB", "Database server • port 3306", { runService(context, Service.MARIADB, Action.START, {message=it},{message=it}) }, { runService(context, Service.MARIADB, Action.STOP, {message=it},{message=it}) }, { runService(context, Service.MARIADB, Action.STATUS, {message=it},{message=it}) })
                        InfoCard("PHP", "PHP berjalan melalui integrasi Apache di Termux. Pengaturan modul PHP dilakukan di konfigurasi Termux, bukan di APK.")
                        InfoCard("Status terakhir", message)
                        InfoCard("Hasil Termux", result)
                    }
                    Page.SERVICES -> {
                        Text("Services", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        ServiceCard("Apache", "Start, stop, cek status, dan versi", {runService(context,Service.APACHE,Action.START,{message=it},{message=it})},{runService(context,Service.APACHE,Action.STOP,{message=it},{message=it})},{runService(context,Service.APACHE,Action.STATUS,{message=it},{message=it})})
                        ServiceCard("MariaDB", "Start, stop, cek status, dan versi", {runService(context,Service.MARIADB,Action.START,{message=it},{message=it})},{runService(context,Service.MARIADB,Action.STOP,{message=it},{message=it})},{runService(context,Service.MARIADB,Action.STATUS,{message=it},{message=it})})
                        InfoCard("Catatan", "Perintah dikirim ke Termux melalui RUN_COMMAND. Termux harus terpasang, izin RUN_COMMAND diberikan, dan allow-external-apps=true.")
                    }
                    Page.DATABASE -> {
                        Text("Database Manager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        InfoCard("Tahap berikutnya", "Panel ini belum menjalankan query SQL. Fitur daftar database, buat database, backup, dan restore perlu dibuat dengan autentikasi yang aman. Jangan menaruh password database di source APK.")
                        Button(onClick = { page = Page.SERVICES }, modifier = Modifier.fillMaxWidth()) { Text("Buka kontrol MariaDB") }
                    }
                    Page.FILES -> {
                        Text("File Manager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        InfoCard("Document root", "/data/data/com.termux/files/home/htdocs")
                        InfoCard("Tahap berikutnya", "Manajer file akan dibatasi ke folder htdocs. Versi ini belum mengubah, menghapus, atau mengunggah file.")
                    }
                    Page.LOGS -> {
                        Text("Logs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        InfoCard("Hasil perintah terakhir", result)
                        InfoCard("Status aplikasi", message)
                    }
                    Page.SETTINGS -> {
                        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        InfoCard("Termux home", TermuxCommand.HOME)
                        InfoCard("Folder skrip", TermuxCommand.SCRIPT_DIR)
                        InfoCard("Persiapan Termux", "1. Aktifkan allow-external-apps=true. 2. Berikan izin Jalankan perintah di Termux kepada DroidXAMPP. 3. Letakkan skrip di ~/droidxampp/scripts/.")
                        Button(onClick = { try { context.startActivity(context.packageManager.getLaunchIntentForPackage("com.termux")) } catch (_: Exception) { message = "Termux tidak ditemukan." } }, modifier = Modifier.fillMaxWidth()) { Text("Buka Termux") }
                    }
                }
            }
        }
    }
}

private fun runService(context: android.content.Context, service: Service, action: Action, success: (String)->Unit, failure: (String)->Unit) {
    TermuxCommandRunner.run(context, service, action, { success("${service.title}: perintah ${action.arg} dikirim ke Termux.") }, { failure("Gagal: $it") })
}

@Composable
private fun ServiceCard(title: String, subtitle: String, start: () -> Unit, stop: () -> Unit, status: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dns, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(12.dp))
                Column { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = start, modifier = Modifier.weight(1f)) { Text("Start") }
                OutlinedButton(onClick = stop, modifier = Modifier.weight(1f)) { Text("Stop") }
                OutlinedButton(onClick = status, modifier = Modifier.weight(1f)) { Text("Status") }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
