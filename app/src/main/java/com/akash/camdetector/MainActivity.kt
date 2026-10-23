package com.akash.camdetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.akash.camdetector.data.Report
import com.akash.camdetector.data.ReportStore
import com.akash.camdetector.data.Tool
import com.akash.camdetector.data.ToolFinding
import com.akash.camdetector.ui.*

sealed class Screen {
    object Home : Screen()
    object History : Screen()
    object Guide : Screen()
    object Settings : Screen()
    data class ToolScreen(val tool: Tool) : Screen()
    object Inspection : Screen()
    data class NewReport(val findings: List<ToolFinding>) : Screen()
    data class ViewReport(val report: Report) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ReportStore(this)
        setContent {
            var dark by remember { mutableStateOf(store.isDarkTheme()) }
            CamTheme(dark = dark) {
                AppNav(
                    store = store,
                    dark = dark,
                    onTheme = {
                        dark = it
                        store.setDarkTheme(it)
                    }
                )
            }
        }
    }
}

@Composable
fun AppNav(store: ReportStore, dark: Boolean, onTheme: (Boolean) -> Unit) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    val backStack = remember { mutableStateListOf<Screen>() }
    var reportsVersion by remember { mutableStateOf(0) }

    fun nav(s: Screen) {
        backStack.add(screen)
        screen = s
    }

    fun navTop(s: Screen) {
        backStack.clear()
        screen = s
    }

    fun goBack(): Boolean {
        return if (backStack.isNotEmpty()) {
            screen = backStack.removeAt(backStack.size - 1)
            true
        } else {
            false
        }
    }

    BackHandler(enabled = backStack.isNotEmpty()) { goBack() }

    val isTopLevel = screen is Screen.Home || screen is Screen.History || screen is Screen.Guide || screen is Screen.Settings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Home,
                        onClick = { navTop(Screen.Home) },
                        icon = { Text("H", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        label = { Text("Home", fontSize = 10.sp) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = screen is Screen.History,
                        onClick = { navTop(Screen.History) },
                        icon = { Text("R", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        label = { Text("History", fontSize = 10.sp) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Guide,
                        onClick = { navTop(Screen.Guide) },
                        icon = { Text("G", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        label = { Text("Guide", fontSize = 10.sp) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Settings,
                        onClick = { navTop(Screen.Settings) },
                        icon = { Text("S", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        label = { Text("Settings", fontSize = 10.sp) },
                        colors = itemColors
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when (val s = screen) {
                is Screen.Home -> HomeScreen(
                    onStartInspection = { nav(Screen.Inspection) },
                    onOpenTool = { nav(Screen.ToolScreen(it)) }
                )
                is Screen.History -> {
                    reportsVersion
                    HistoryScreen(
                        reports = store.getReports(),
                        onOpen = { nav(Screen.ViewReport(it)) },
                        onDelete = {
                            store.deleteReport(it.id)
                            reportsVersion += 1
                        }
                    )
                }
                is Screen.Guide -> GuideScreen()
                is Screen.Settings -> SettingsScreen(dark = dark, onTheme = onTheme)
                is Screen.ToolScreen -> when (s.tool) {
                    Tool.LENS_GLINT -> LensGlintScreen({ goBack() })
                    Tool.IR_SCAN -> IrScanScreen({ goBack() })
                    Tool.WIFI -> WifiScreen({ goBack() })
                    Tool.MAGNETIC -> MagneticScreen({ goBack() })
                }
                is Screen.Inspection -> InspectionScreen(
                    onDone = { findings ->
                        backStack.clear()
                        screen = Screen.NewReport(findings)
                    },
                    onCancel = { goBack() }
                )
                is Screen.NewReport -> ReportScreen(
                    report = Report(System.currentTimeMillis(), System.currentTimeMillis(), s.findings),
                    isNew = true,
                    onSave = {
                        store.saveReport(Report(System.currentTimeMillis(), System.currentTimeMillis(), s.findings))
                        reportsVersion += 1
                        navTop(Screen.Home)
                    },
                    onClose = { navTop(Screen.Home) }
                )
                is Screen.ViewReport -> ReportScreen(
                    report = s.report,
                    isNew = false,
                    onClose = { goBack() }
                )
            }
        }
    }
}
