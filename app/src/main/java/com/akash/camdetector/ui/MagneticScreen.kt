package com.akash.camdetector.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akash.camdetector.data.ToolStatus
import kotlin.math.sqrt

@Composable
fun MagneticScreen(onBack: () -> Unit, flow: FlowCallbacks? = null) {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current
    val sensorManager = remember { if (inspectionMode) null else context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    var reading by remember { mutableStateOf<Float?>(null) }
    var baseline by remember { mutableStateOf<Float?>(null) }

    DisposableEffect(sensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                reading = sqrt(x * x + y * y + z * z)
            }
            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
        }
        if (!inspectionMode && sensor != null && sensorManager != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager?.unregisterListener(listener) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("Magnetic Sensor", "Magnetic field around objects", onBack)
        if (flow != null) FlowProgress(flow.step, flow.total)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val demoMode = inspectionMode && sensor == null
            if (sensor == null && !demoMode) {
                Spacer(modifier = Modifier.height(30.dp))
                NoteCard("This phone does not report a magnetometer, so this check cannot run here.")
            } else {
                Text(
                    "Set a baseline in open air, then slowly move the phone near clocks, chargers, speakers and detectors. A jump from the baseline is a clue with many possible causes - speakers and motors also produce magnetic fields.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                val value = if (demoMode) 48f else reading
                if (demoMode) {
                    Text(
                        "Example values - live readings appear on a phone with a magnetometer.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    if (value != null) "${value.toInt()} uT" else "-- uT",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "current magnetic field",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                val base = if (demoMode) 46f else baseline
                val spike = value != null && base != null && value > base + 25f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val frac = ((value ?: 0f) / 200f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(frac)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (spike) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    if (base != null) "Baseline: ${base.toInt()} uT" + (if (spike) "  -  spike above baseline" else "") else "No baseline set",
                    color = if (spike) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                GhostButton("Set current as baseline", {
                    val v = reading
                    if (v != null) baseline = v
                }, Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))
                if (flow != null) {
                    PrimaryButton("I noticed a spike near an object", {
                        flow.onResult(ToolStatus.CLUE_FOUND, "A magnetic spike above baseline was reported. Speakers and electronics cause similar spikes - inspect the object by hand.")
                    }, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton("No spikes noticed", {
                        flow.onResult(ToolStatus.NO_CLUE, "No magnetic spikes above baseline observed.")
                    }, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton("Skip this check", { flow.onSkip() }, Modifier.fillMaxWidth())
                } else {
                    NoteCard("Magnetic spikes have many innocent causes. This tool never proves or rules out a camera.")
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
