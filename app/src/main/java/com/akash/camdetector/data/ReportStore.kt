package com.akash.camdetector.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ReportStore(context: Context) : ReportRepo {
    private val prefs = context.getSharedPreferences("camdetector", Context.MODE_PRIVATE)

    override fun isDarkTheme(): Boolean = prefs.getBoolean("dark", true)
    override fun setDarkTheme(dark: Boolean) = prefs.edit().putBoolean("dark", dark).apply()

    override fun saveReport(report: Report) {
        val arr = JSONArray(prefs.getString("reports", "[]"))
        val obj = JSONObject()
        obj.put("id", report.id)
        obj.put("ts", report.timestamp)
        val fa = JSONArray()
        for (f in report.findings) {
            val fo = JSONObject()
            fo.put("tool", f.tool.name)
            fo.put("status", f.status.name)
            fo.put("detail", f.detail)
            fa.put(fo)
        }
        obj.put("findings", fa)
        arr.put(obj)
        prefs.edit().putString("reports", arr.toString()).apply()
    }

    override fun getReports(): List<Report> {
        val out = mutableListOf<Report>()
        val arr = JSONArray(prefs.getString("reports", "[]"))
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val fa = obj.getJSONArray("findings")
            val findings = mutableListOf<ToolFinding>()
            for (j in 0 until fa.length()) {
                val fo = fa.getJSONObject(j)
                findings.add(
                    ToolFinding(
                        Tool.valueOf(fo.getString("tool")),
                        ToolStatus.valueOf(fo.getString("status")),
                        fo.getString("detail")
                    )
                )
            }
            out.add(Report(obj.getLong("id"), obj.getLong("ts"), findings))
        }
        return out.sortedByDescending { it.timestamp }
    }

    override fun deleteReport(id: Long) {
        val kept = getReports().filter { it.id != id }
        prefs.edit().remove("reports").apply()
        for (r in kept.sortedBy { it.timestamp }) saveReport(r)
    }
}
