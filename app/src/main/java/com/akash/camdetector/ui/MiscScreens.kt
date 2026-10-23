package com.akash.camdetector.ui

import com.akash.camdetector.BuildConfig

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akash.camdetector.data.Report
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    reports: List<Report>,
    onOpen: (Report) -> Unit,
    onDelete: (Report) -> Unit
) {
    val fmt = remember { SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("History", "Your saved inspection reports")
        if (reports.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No reports yet", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Run a Guided Room Inspection and save the report to see it here. Reports stay on this phone only.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                for (r in reports) {
                    val clues = r.findings.count { it.status == com.akash.camdetector.data.ToolStatus.CLUE_FOUND }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .clickable { onOpen(r) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Room check", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                fmt.format(Date(r.timestamp)) + "  -  " + if (clues > 0) "$clues item(s) to check" else "no items flagged",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        GhostButton("Delete", { onDelete(r) })
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun GuideScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("Guide", "What each tool can and cannot do")
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            GuideItem(
                "Lens Glint",
                "Camera lenses reflect a bright, steady dot when a light shines at them. This tool turns the torch on so you can sweep the room through the preview and inspect suspicious reflections.",
                "Helps you look; it does not detect automatically. Many shiny surfaces reflect. Always confirm by hand."
            )
            GuideItem(
                "IR Light Scan",
                "Night-vision cameras emit infrared light that is invisible to the eye but can appear as a glow on some phone cameras in a dark room.",
                "Many phones block IR with a filter. Seeing no glow does not rule out a night-vision camera."
            )
            GuideItem(
                "Wi-Fi Devices",
                "Wireless cameras usually join the same Wi-Fi network. This tool lists devices on your own network so you can spot anything you do not recognise.",
                "Only use on networks you own or may inspect. Wired cameras, cameras on another network, and offline recording cameras will not appear."
            )
            GuideItem(
                "Magnetic Sensor",
                "Electronics with speakers or motors disturb the magnetic field. The magnetometer shows the field strength so you can compare objects against a baseline.",
                "Speakers, motors and chargers cause the same spikes. A spike is a clue, never proof."
            )
            Spacer(modifier = Modifier.height(6.dp))
            NoteCard("No app can guarantee a room is camera-free. If you have real safety concerns, check the place physically and consider professional help.")
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun GuideItem(title: String, body: String, limit: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(body, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 5.dp))
        Text("Limit: $limit", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
fun SettingsScreen(dark: Boolean, onTheme: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("Settings", "Appearance and about")
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark theme", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Light theme is also available", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                Switch(
                    checked = dark,
                    onCheckedChange = onTheme,
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text("About", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Hidden Camera Detector v${BuildConfig.VERSION_NAME}\n\nA guided room-inspection toolkit. It helps you check a room using your phone's camera, magnetometer and your own Wi-Fi network. It does not automatically detect cameras, and it never declares a room safe.\n\nEverything runs on this phone. No account, no internet upload, no ads, no tracking.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
