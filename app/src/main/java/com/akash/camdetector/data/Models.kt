package com.akash.camdetector.data

enum class ToolStatus(val badge: String) {
    CLUE_FOUND("CHECK"),
    NO_CLUE("NO CLUE"),
    REVIEW("REVIEW"),
    NOT_TESTED("NOT TESTED")
}

enum class Tool(val title: String) {
    LENS_GLINT("Lens Glint"),
    IR_SCAN("IR Light Scan"),
    WIFI("Wi-Fi Devices"),
    MAGNETIC("Magnetic Sensor")
}

data class ToolFinding(val tool: Tool, val status: ToolStatus, val detail: String)

data class Report(val id: Long, val timestamp: Long, val findings: List<ToolFinding>)
