package com.akash.camdetector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akash.camdetector.data.Tool

@Composable
fun HomeScreen(
    onStartInspection: () -> Unit,
    onOpenTool: (Tool) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("HD", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Hidden Camera Detector", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("Room safety toolkit", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column {
                Text("Guided Room Inspection", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Walk through each check step by step, one tool at a time.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 5.dp, bottom = 12.dp)
                )
                PrimaryButton(text = "Start Inspection", onClick = onStartInspection)
            }
        }

        Text(
            "INSPECTION TOOLS",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 10.dp)
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row {
                ToolCard("Lens Glint", "Helps you inspect lens-like reflections", Modifier.weight(1f)) { onOpenTool(Tool.LENS_GLINT) }
                Spacer(modifier = Modifier.width(12.dp))
                ToolCard("IR Light Scan", "Find night-vision glow (camera-dependent)", Modifier.weight(1f)) { onOpenTool(Tool.IR_SCAN) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                ToolCard("Wi-Fi Devices", "List devices on your own network", Modifier.weight(1f)) { onOpenTool(Tool.WIFI) }
                Spacer(modifier = Modifier.width(12.dp))
                ToolCard("Magnetic Sensor", "View magnetic field changes near objects", Modifier.weight(1f)) { onOpenTool(Tool.MAGNETIC) }
            }
        }

        NoteCard(
            "Results are clues, not proof. No signal found does not mean a room is safe.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ToolCard(title: String, desc: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
