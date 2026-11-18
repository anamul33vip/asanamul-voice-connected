package com.anamul.voice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anamul.voice.overlay.OverlayService
import com.anamul.voice.net.ApiClient
import com.anamul.voice.net.Voice
import kotlinx.coroutines.launch

private val bg = Color(0xFF070812)
private val panel = Color(0xFF15172A)
private val cyan = Color(0xFF00E5FF)
private val pink = Color(0xFFFF2BD6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AppUi() } }
}

@Composable private fun AppUi() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("Home") }
    var text by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Not connected") }
    var voices by remember { mutableStateOf(listOf<Voice>()) }
    var selectedVoice by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var overlayOn by remember { mutableStateOf(false) }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) { status = "Selected audio: ${uri.lastPathSegment ?: "file"}" }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary=cyan, secondary=pink, background=bg, surface=panel)) {
        Scaffold(containerColor=bg, bottomBar={ NavigationBar(containerColor=panel) {
            listOf("Home","Voices","Soundboard","Text to Voice","Settings").forEach { item ->
                NavigationBarItem(selected=page==item, onClick={page=item}, icon={ Icon(when(item){"Home"->Icons.Default.Home;"Voices"->Icons.Default.GraphicEq;"Soundboard"->Icons.Default.LibraryMusic;"Text to Voice"->Icons.Default.Mic;else->Icons.Default.Settings}, null) }, label={Text(item, fontSize=9.sp)})
            }
        }}) { pad ->
            LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
                item { Column(Modifier.fillMaxWidth().border(1.dp, Brush.horizontalGradient(listOf(cyan,pink,Color.Green)), RoundedCornerShape(18.dp)).background(panel,RoundedCornerShape(18.dp)).padding(18.dp)) {
                    Text("AS ANAMUL VOICE", color=Color.White, fontSize=24.sp, fontWeight=FontWeight.Black)
                    Text("RGB NEON EDITION  •  FREE FIRE GAMING", color=cyan, fontSize=11.sp)
                } }
                when(page) {
                    "Home" -> {
                        item { Section("Connection") { Text(status,color=Color.LightGray); OutlinedTextField(value=key,onValueChange={key=it},label={Text("Access Key")},singleLine=true,modifier=Modifier.fillMaxWidth()); Button(onClick={scope.launch { busy=true; status=runCatching { ApiClient.verify(key) }.fold({if(it) "Key verified" else "Key rejected"},{"Backend not configured / unavailable"}); busy=false }},enabled=!busy) { Text(if(busy) "Checking…" else "Connect / Verify Key") } } }
                        item { Section("Quick Actions") { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick={page="Voices"}){Text("Voice Library")}; OutlinedButton(onClick={page="Text to Voice"}){Text("Generate Voice")} } } }
                        item { Section("Floating Overlay") { Text("Overlay controls this app's playback and status. Third-party game microphone routing is device/app dependent.",color=Color.LightGray); Button(onClick={
                            if (!Settings.canDrawOverlays(context)) context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                            else { context.startService(Intent(context, OverlayService::class.java)); overlayOn=true; status="Overlay started" }
                        }) { Text(if(overlayOn) "Overlay requested" else "Enable Floating Overlay") } } }
                    }
                    "Voices" -> {
                        item { Section("Voice Library") { Button(onClick={scope.launch { busy=true; voices=runCatching { ApiClient.voices() }.getOrDefault(emptyList()); status="Loaded ${voices.size} voices"; busy=false }}) { Text("Refresh Voices") }; Text("Voice A • Voice B • Voice C • Voice D are admin-configurable examples.",color=Color.LightGray) } }
                        items(voices.size) { i -> Section(voices[i].name) { Text("${voices[i].language}  •  ${voices[i].provider}",color=Color.LightGray); Button(onClick={selectedVoice=voices[i].id}) { Text(if(selectedVoice==voices[i].id) "Selected" else "Select Voice") } } }
                    }
                    "Soundboard" -> item { Section("Soundboard & Audio") { Text("Import local audio for preview. Supported formats depend on Android media decoder.",color=Color.LightGray); Button(onClick={audioPicker.launch("audio/*")}) { Text("Choose Audio") } } }
                    "Text to Voice" -> item { Section("Text to Voice") { OutlinedTextField(value=text,onValueChange={text=it},label={Text("Enter text in Bengali or English")},modifier=Modifier.fillMaxWidth(),minLines=4); OutlinedTextField(value=selectedVoice,onValueChange={selectedVoice=it},label={Text("Voice ID")},modifier=Modifier.fillMaxWidth()); Button(onClick={scope.launch { busy=true; status=runCatching { ApiClient.generate(text,selectedVoice) }.fold({"Generated audio: $it"},{"TTS request failed: ${it.message}"}); busy=false }},enabled=text.isNotBlank()&&!busy) { Text(if(busy) "Generating…" else "Generate Audio") }; Text(status,color=Color.LightGray) } }
                    "Settings" -> item { Section("Settings") { Text("App version 1.0.0",color=Color.LightGray); TextButton(onClick={context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://t.me/crackffpanel")))}) { Text("Telegram Support: @crackffpanel") }; Text("Backend URL is configured in net/ApiClient.kt",color=Color.LightGray) } }
                }
            }
        }
    }
}

@Composable private fun Section(title:String, content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().border(1.dp,Brush.horizontalGradient(listOf(cyan.copy(alpha=.8f),pink.copy(alpha=.8f))),RoundedCornerShape(16.dp)).background(panel,RoundedCornerShape(16.dp)).padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text(title,color=Color.White,fontWeight=FontWeight.Bold,fontSize=17.sp); content()
    }
}
