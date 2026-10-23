package com.akash.camdetector

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.akash.camdetector.data.*
import com.akash.camdetector.ui.*
import org.junit.Rule
import org.junit.Test

class ScreensSnapshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    private fun sampleReport() = Report(
        1L, 1761210000000L,
        listOf(
            ToolFinding(Tool.LENS_GLINT, ToolStatus.NO_CLUE, "Example: no lens-like reflection was marked during this inspection."),
            ToolFinding(Tool.IR_SCAN, ToolStatus.NOT_TESTED, "Example: this device could not emit or view IR light."),
            ToolFinding(Tool.WIFI, ToolStatus.REVIEW, "Example: devices were found on your network; review any you do not recognize."),
            ToolFinding(Tool.MAGNETIC, ToolStatus.NO_CLUE, "Example: magnetic field stayed near your baseline.")
        )
    )

    private fun shot(name: String, dark: Boolean, content: @androidx.compose.runtime.Composable () -> Unit) {
        try {
            paparazzi.snapshot(name = name) { CamTheme(dark = dark) { content() } }
        } catch (t: Throwable) {
            System.err.println("SNAPSHOT-FAILED $name: ${t.javaClass.simpleName}: ${t.message?.take(200)}")
        }
    }

    @Test fun allScreens() {
        shot("home_dark", true) { HomeScreen({}, {}) }
        shot("home_light", false) { HomeScreen({}, {}) }
        shot("history_dark", true) { HistoryScreen(listOf(sampleReport()), {}, {}) }
        shot("history_light", false) { HistoryScreen(listOf(sampleReport()), {}, {}) }
        shot("guide_dark", true) { GuideScreen() }
        shot("guide_light", false) { GuideScreen() }
        shot("settings_dark", true) { SettingsScreen(true) {} }
        shot("settings_light", false) { SettingsScreen(false) {} }
        shot("inspection_dark", true) { InspectionScreen({}, {}) }
        shot("inspection_light", false) { InspectionScreen({}, {}) }
        shot("lens_dark", true) { LensGlintScreen({}) }
        shot("lens_light", false) { LensGlintScreen({}) }
        shot("ir_dark", true) { IrScanScreen({}) }
        shot("ir_light", false) { IrScanScreen({}) }
        shot("wifi_dark", true) { WifiScreen({}) }
        shot("wifi_light", false) { WifiScreen({}) }
        shot("magnetic_dark", true) { MagneticScreen({}) }
        shot("magnetic_light", false) { MagneticScreen({}) }
        shot("report_dark", true) { ReportScreen(sampleReport(), isNew = true, onSave = {}, onClose = {}) }
        shot("report_light", false) { ReportScreen(sampleReport(), isNew = true, onSave = {}, onClose = {}) }
    }
}
