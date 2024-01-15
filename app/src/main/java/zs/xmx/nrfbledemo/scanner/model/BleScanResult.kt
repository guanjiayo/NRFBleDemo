package zs.xmx.nrfbledemo.scanner.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.support.v18.scanner.ScanResult

@SuppressLint("MissingPermission")
@Parcelize
data class BleScanResult(
    val device: BluetoothDevice,
    val scanResult: ScanResult? = null,
    val name: String? = null,
    val hadName: Boolean = name != null,
    val lastScanResult: ScanResult? = null,
    val rssi: Int = 0,
    val previousRssi: Int = 0,
    val highestRssi: Int = Integer.max(rssi, previousRssi),
) : Parcelable {
    fun hasRssiLevelChanged(): Boolean {
        val newLevel =
            if (rssi <= 10) 0 else if (rssi <= 28) 1 else if (rssi <= 45) 2 else if (rssi <= 65) 3 else 4
        val oldLevel =
            if (previousRssi <= 10) 0 else if (previousRssi <= 28) 1 else if (previousRssi <= 45) 2 else if (previousRssi <= 65) 3 else 4
        return newLevel != oldLevel
    }

    fun update(scanResult: ScanResult): BleScanResult = copy(
        device = scanResult.device,
        lastScanResult = scanResult,
        name = scanResult.scanRecord?.deviceName,
        hadName = hadName || name != null,
        previousRssi = rssi,
        rssi = scanResult.rssi,
        highestRssi = if (highestRssi > rssi) highestRssi else rssi
    )

    fun matches(scanResult: ScanResult) = device.address == scanResult.device.address

    fun createBond() {
        device.createBond()
    }

    val displayName: String?
        get() = when {
            name?.isNotEmpty() == true -> name
            device.name?.isNotEmpty() == true -> device.name
            else -> null
        }

    val address: String
        get() = device.address

    val displayNameOrAddress: String
        get() = displayName ?: address

    val bondingState: Int
        get() = device.bondState

    val isBonded: Boolean
        get() = bondingState == BluetoothDevice.BOND_BONDED

    override fun hashCode() = device.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is BleScanResult) {
            return device == other.device
        }
        return super.equals(other)
    }
}

fun ScanResult.toBleScanResult() = BleScanResult(
    device = device,
    scanResult = this,
    name = scanRecord?.deviceName,
    previousRssi = rssi,
    rssi = rssi
)