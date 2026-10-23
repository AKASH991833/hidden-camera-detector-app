package com.akash.camdetector.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.akash.camdetector.data.ToolStatus

@Composable
fun RequireCameraPermission(content: @Composable () -> Unit) {
    if (LocalInspectionMode.current) {
        // Preview/render environments have no activity result registry; show content as if granted.
        content()
        return
    }
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }
    if (granted) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Camera access needed",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "This tool works through the phone camera. Nothing is recorded or uploaded - the preview stays on your phone.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(vertical = 14.dp)
            )
            PrimaryButton("Allow camera", { launcher.launch(Manifest.permission.CAMERA) })
        }
    }
}

@Composable
fun CameraPreviewWithTorch(torchOn: Boolean, modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        // Preview/render environments have no camera; draw the preview area as an honest placeholder.
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera preview area (live on device)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        return
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val provider = future.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            } catch (e: Exception) {
                error = "Camera could not be started on this device."
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(torchOn, camera) {
        try {
            camera?.cameraControl?.enableTorch(torchOn)
        } catch (e: Exception) {
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(error ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun LensGlintScreen(onBack: () -> Unit, flow: FlowCallbacks? = null) {
    RequireCameraPermission {
        var torchOn by remember { mutableStateOf(true) }
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader("Lens Glint", "Helps you inspect lens-like reflections", onBack)
            if (flow != null) FlowProgress(flow.step, flow.total)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            ) {
                CameraPreviewWithTorch(torchOn = torchOn, modifier = Modifier.fillMaxSize())
            }
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Sweep slowly across smoke detectors, clocks, chargers and mirrors. A small, steady bright dot can be a lens reflection - always confirm by hand.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    GhostButton(if (torchOn) "Torch: ON" else "Torch: OFF", { torchOn = !torchOn }, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (flow != null) {
                    PrimaryButton("I found a steady reflection", {
                        flow.onResult(ToolStatus.CLUE_FOUND, "A steady reflection was marked during the lens glint check - inspect the spot by hand.")
                    }, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton("Looked carefully - nothing seen", {
                        flow.onResult(ToolStatus.NO_CLUE, "No steady reflection observed in this sweep.")
                    }, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton("Skip this check", { flow.onSkip() }, Modifier.fillMaxWidth())
                } else {
                    NoteCard("Reflections are clues only. Confirm anything suspicious by physically checking the object.")
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun IrScanScreen(onBack: () -> Unit, flow: FlowCallbacks? = null) {
    RequireCameraPermission {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader("IR Light Scan", "Night-vision glow check (camera-dependent)", onBack)
            if (flow != null) FlowProgress(flow.step, flow.total)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            ) {
                CameraPreviewWithTorch(torchOn = false, modifier = Modifier.fillMaxSize())
            }
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Make the room dark, then look through the preview for small purple-white glowing dots. Many phone cameras block IR, so seeing nothing does not rule a camera out.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (flow != null) {
                    PrimaryButton("I see a glowing dot", {
                        flow.onResult(ToolStatus.CLUE_FOUND, "A glowing dot was reported during the IR check - verify what it is by hand.")
                    }, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton("No glow seen", {
                        flow.onResult(ToolStatus.NO_CLUE, "No IR glow observed. This phone's camera may filter IR, so this is not conclusive.")
                    }, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton("Skip this check", { flow.onSkip() }, Modifier.fillMaxWidth())
                } else {
                    NoteCard("This check depends on your camera's IR filter. No glow seen never means no night-vision camera is present.")
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
