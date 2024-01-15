package zs.xmx.nrfbledemo.scanner

import zs.xmx.nrfbledemo.scanner.model.BleScanResult

sealed class ScanningState {
    data object Loading : ScanningState()

    data class Error(val errorCode: Int) : ScanningState()

    data class DevicesDiscovered(val devices: List<BleScanResult>) : ScanningState() {
        val bonded: List<BleScanResult> = devices.filter { it.isBonded }

        val notBonded: List<BleScanResult> = devices.filter { !it.isBonded }

        fun size(): Int = bonded.size + notBonded.size

        fun isEmpty(): Boolean = devices.isEmpty()
    }

    fun isRunning(): Boolean {
        return this is Loading || this is DevicesDiscovered
    }
}