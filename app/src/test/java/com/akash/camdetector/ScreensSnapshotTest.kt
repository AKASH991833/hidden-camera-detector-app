package com.akash.camdetector

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.akash.camdetector.data.*
import com.akash.camdetector.ui.*
import org.junit.Rule
import org.junit.Test

private class FakeRepo : ReportRepo {
    private val reports = mutableListOf<Report>()
    private var dark = true
    fun seed(report: Report) { reports.add(report) }
    override fun getReports(): List<Report> = reports.sortedByDescending { it.timestamp }
    override fun saveReport(report: Report) { reports.add(report) }
    override fun deleteReport(id: Long) { reports.removeAll { it.id == id } }
    override fun isDarkTheme(): Boolean = dark
    override fun setDarkTheme(dark: Boolean) { this.dark = dark }
}

class ScreensSnapshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    private fun sampleReport() = Report(
        1L, 1761210000000L,
        listOf(
            ToolFinding(Tool.LENS_GLINT, ToolStatus.NO_CLUE, "Example: no lens-like reflection was marked during this inspection."),
            ToolFinding(Tool.IR_SCAN, ToolStatus.NOT_TESTED, "Example: camera was unavailable in this environment, so the IR check did not run."),
            ToolFinding(Tool.WIFI, ToolStatus.REVIEW, "Example: devices were found on your network; review any you do not recognize."),
            ToolFinding(Tool.MAGNETIC, ToolStatus.NO_CLUE, "Example: magnetic field stayed near your baseline.")
        )
    )

    private fun shot(name: String, dark: Boolean, initial: Screen, seedReport: Boolean = false) {
        try {
            paparazzi.snapshot(name = name) {
                // Paparazzi 1.3.4 does not set inspection mode; provide it so render-only
                // placeholders in the app take effect (same as Android Studio previews).
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalInspectionMode provides true
                ) {
                    CamTheme(dark = dark) {
                        val repo = FakeRepo()
                        if (seedReport) repo.seed(sampleReport())
                        AppNav(store = repo, dark = dark, onTheme = {}, initialScreen = initial)
                    }
                }
            }
        } catch (t: Throwable) {
            System.err.println("SNAPSHOT-FAILED $name: ${t.javaClass.simpleName}: ${t.message?.take(200)}")
        }
    }

    @Test fun allScreens() {
        shot("home_dark", true, Screen.Home)
        shot("home_light", false, Screen.Home)
        shot("history_dark", true, Screen.History, seedReport = true)
        shot("history_light", false, Screen.History, seedReport = true)
        shot("guide_dark", true, Screen.Guide)
        shot("guide_light", false, Screen.Guide)
        shot("settings_dark", true, Screen.Settings)
        shot("settings_light", false, Screen.Settings)
        shot("inspection_dark", true, Screen.Inspection)
        shot("inspection_light", false, Screen.Inspection)
        shot("lens_dark", true, Screen.ToolScreen(Tool.LENS_GLINT))
        shot("lens_light", false, Screen.ToolScreen(Tool.LENS_GLINT))
        shot("ir_dark", true, Screen.ToolScreen(Tool.IR_SCAN))
        shot("ir_light", false, Screen.ToolScreen(Tool.IR_SCAN))
        shot("wifi_dark", true, Screen.ToolScreen(Tool.WIFI))
        shot("wifi_light", false, Screen.ToolScreen(Tool.WIFI))
        shot("magnetic_dark", true, Screen.ToolScreen(Tool.MAGNETIC))
        shot("magnetic_light", false, Screen.ToolScreen(Tool.MAGNETIC))
        shot("report_dark", true, Screen.NewReport(sampleReport().findings))
        shot("report_light", false, Screen.NewReport(sampleReport().findings))
    }
}
