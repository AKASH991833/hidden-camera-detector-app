package com.akash.camdetector.util

import java.io.BufferedReader
import java.io.FileReader
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class NetDevice(val ip: String, val mac: String?, val vendor: String)

object NetworkScan {

    // Chip vendors commonly seen in IoT / camera hardware. Prefix match on MAC.
    private val oui = mapOf(
        "24:0A:C4" to "Espressif (IoT chip, common in cams)",
        "30:AE:A4" to "Espressif (IoT chip, common in cams)",
        "3C:71:BF" to "Espressif (IoT chip, common in cams)",
        "A4:CF:12" to "Espressif (IoT chip, common in cams)",
        "84:CC:A8" to "Espressif (IoT chip, common in cams)",
        "C4:4F:33" to "Espressif (IoT chip, common in cams)",
        "EC:FA:BC" to "Espressif (IoT chip, common in cams)",
        "F0:08:D1" to "Espressif (IoT chip, common in cams)",
        "44:19:B6" to "Hikvision",
        "C0:56:E3" to "Hikvision"
    )

    fun localPrefix(): String? {
        val ifs = NetworkInterface.getNetworkInterfaces() ?: return null
        for (ni in ifs) {
            if (!ni.isUp || ni.isLoopback) continue
            for (addr in ni.inetAddresses) {
                if (addr is Inet4Address && !addr.isLoopbackAddress && addr.hostAddress != null) {
                    val parts = addr.hostAddress!!.split(".")
                    if (parts.size == 4) return parts[0] + "." + parts[1] + "." + parts[2] + "."
                }
            }
        }
        return null
    }

    suspend fun sweep(prefix: String, onProgress: (Int) -> Unit): List<String> = coroutineScope {
        val jobs = (1..254).map { i ->
            async(Dispatchers.IO) {
                val ip = prefix + i
                val alive = try {
                    InetAddress.getByName(ip).isReachable(200)
                } catch (e: Exception) {
                    false
                }
                onProgress(i)
                if (alive) ip else null
            }
        }
        jobs.awaitAll().filterNotNull()
    }

    fun arpTable(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val reader = BufferedReader(FileReader("/proc/net/arp"))
            reader.readLine()
            var line = reader.readLine()
            while (line != null) {
                val parts = line.split(Regex("\\s+"))
                if (parts.size >= 4 && parts[3].contains(":")) {
                    map[parts[0]] = parts[3].uppercase()
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
        }
        return map
    }

    fun vendorFor(mac: String?): String {
        if (mac == null) return "Unknown brand"
        val prefix = mac.take(8)
        return oui[prefix] ?: "Unknown brand"
    }
}
