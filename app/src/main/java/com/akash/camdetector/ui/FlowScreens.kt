package com.akash.camdetector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akash.camdetector.data.Report
import com.akash.camdetector.data.Tool
import com.akash.camdetector.data.ToolFinding
import com.akash.camdetector.data.ToolStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val stepOrder = listOf(Tool.LENS_GLINT, Tool.IR_SCAN, Tool.WIFI, Tool.MAGNETIC)

@Composable
fun InspectionScreen(
    onDone: (List<ToolFinding>) -> Unit,
    onCancel: () -> Unit
) {
    var stepIndex by remember { mutableStateOf(0) }
    val findings = remember { mutableStateListOf<ToolFinding>() }
    val total = stepOrder.size
    val tool = stepOrder[stepIndex]

    fun advance(status: ToolStatus, detail: String) {
        findings.add(ToolFinding(tool, status, detail))
        if (stepIndex + 1 < total) {
            stepIndex += 1
        } else {
            onDone(findings.toList())
        }
    }

    fun skip() {
        advance(ToolStatus.NOT_TESTED, "Skipped in this inspection.")
    }

    val flow = FlowCallbacks(stepIndex + 1, total, { st, d -> advance(st, d) }, { skip() })

    when (tool) {
        Tool.LENS_GLINT -> LensGlintScreen(onCancel, flow)
        Tool.IR_SCAN -> IrScanScreen(onCancel, flow)
        Tool.WIFI -> WifiScreen(onCancel, flow)
        Tool.MAGNETIC -> MagneticScreen(onCancel, flow)
    }
}

@Composable
fun ReportScreen(
    report: Report,
    isNew: Boolean,
    onSave: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    val fmt = remember { SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader("Scan Report", "Room check - " + fmt.format(Date(report.timestamp)), onClose)

        val done = report.findings.count { it.status != ToolStatus.NOT_TESTED }
        val notTested = report.findings.count { it.status == ToolStatus.NOT_TESTED }
        val flagged = report.findings.count { it.status == ToolStatus.CLUE_FOUND || it.status == ToolStatus.REVIEW }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "$done of ${report.findings.size} checks done",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (flagged > 0) "$flagged item(s) need a closer look. Check those spots by hand."
                    else if (notTested > 0) "Nothing flagged in the checks that ran. $notTested check(s) did not run, which tells nothing either way."
                    else "No item was flagged. That is not a guarantee - see the note below.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        Text(
            "FINDINGS",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 10.dp)
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            for (f in report.findings) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(f.tool.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(f.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    BadgeChip(f.status)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        NoteCard(
            "These are clues from your phone's sensors, not proof. Nothing found never means a room is guaranteed safe.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        if (isNew && onSave != null) {
            PrimaryButton("Save Report", onSave, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
