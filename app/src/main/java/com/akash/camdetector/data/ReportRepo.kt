package com.akash.camdetector.data

interface ReportRepo {
    fun getReports(): List<Report>
    fun saveReport(report: Report)
    fun deleteReport(id: Long)
    fun isDarkTheme(): Boolean
    fun setDarkTheme(dark: Boolean)
}
