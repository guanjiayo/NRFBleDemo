package zs.xmx.nrfbledemo.scanner.aggregator

import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.support.v18.scanner.ScanResult
import zs.xmx.nrfbledemo.scanner.model.BleScanResult
import zs.xmx.nrfbledemo.scanner.model.toBleScanResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 将扫描到蓝牙设备重新聚合
 */
@Singleton
class BleScanResultAggregator @Inject constructor() {
    val devices = mutableListOf<BleScanResult>()
    val data = MutableStateFlow(devices.toList())

    fun addNewDevice(scanResult: ScanResult) {
        devices.firstOrNull { it.device == scanResult.device }?.let { device ->
            val i = devices.indexOf(device)
            devices.set(i, device.update(scanResult))
        } ?: scanResult.toBleScanResult().also { devices.add(it) }

        data.value = devices.toList()
    }

    fun clear() {
        devices.clear()
        data.value = devices
    }
}

data class DevicesScanFilter(
    val filterUuidRequired: Boolean?,
    val filterNearbyOnly: Boolean,
    val filterWithNames: Boolean
)