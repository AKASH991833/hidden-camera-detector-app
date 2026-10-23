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
import com.akash.camdetector.data.ToolStatus
import com.akash.camdetector.util.NetDevice
import com.akash.camdetector.util.NetworkScan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WifiScreen(onBack: () -> Unit, flow: FlowCallbacks? = null) {
    var scanning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    var devices by remember { mutableStateOf<List<NetDevice>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var asserted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("Wi-Fi Devices", "Devices on your own network only", onBack)
        if (flow != null) FlowProgress(flow.step, flow.total)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Lists devices connected to the Wi-Fi network this phone is on. Only scan networks you own or are allowed to inspect. A wired or offline camera will not appear here. On newer Android versions the system may hide device MAC addresses - then brand info is unavailable, which is normal.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (!asserted) {
                PrimaryButton("This is my own network, or I am allowed to inspect it", { asserted = true }, Modifier.fillMaxWidth())
            } else if (!scanning && devices == null) {
                PrimaryButton("Scan this network", {
                    scanning = true
                    progress = 0
                    error = null
                    scope.launch {
                        val found = withContext(Dispatchers.IO) {
                            val prefix = NetworkScan.localPrefix()
                            if (prefix == null) {
                                null
                            } else {
                                val ips = NetworkScan.sweep(prefix) { progress = it }
                                val arp = NetworkScan.arpTable()
                                ips.map { ip ->
                                    val mac = arp[ip]
                                    NetDevice(ip, mac, NetworkScan.vendorFor(mac))
                                }.sortedBy { it.ip }
                            }
                        }
                        scanning = false
                        if (found == null) {
                            error = "Could not read this network. Connect to Wi-Fi and try again."
                        } else {
                            devices = found
                        }
                    }
                }, Modifier.fillMaxWidth())
            }

            if (scanning) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Scanning... $progress / 254 addresses",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.toFloat() / 254f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(14.dp))
                NoteCard(error ?: "")
            }

            val list = devices
            if (list != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "${list.size} device(s) found",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                for (d in list) {
                    DeviceRow(d)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (flow != null) {
                    val unknown = list.count { it.vendor == "Unknown brand" }
                    PrimaryButton("I recognise every device", {
                        flow.onResult(ToolStatus.NO_CLUE, "${list.size} device(s) on the network; user recognised all of them.")
                    }, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton("There is a device I do not recognise", {
                        flow.onResult(ToolStatus.REVIEW, "$unknown of ${list.size} device(s) have an unknown brand. Identify each unknown device physically.")
                    }, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton("Skip this check", { flow.onSkip() }, Modifier.fillMaxWidth())
                } else {
                    NoteCard("Unknown brand only means the device was not recognised - it is not proof of a camera. Identify unknown devices by checking your router and your own gadgets.")
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun DeviceRow(d: NetDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(d.ip, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                d.mac ?: "MAC not visible",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        Text(
            d.vendor,
            color = if (d.vendor == "Unknown brand") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
