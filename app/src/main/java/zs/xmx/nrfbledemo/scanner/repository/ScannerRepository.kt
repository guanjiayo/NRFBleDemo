package zs.xmx.nrfbledemo.scanner.repository

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import zs.xmx.nrfbledemo.scanner.ScanningState
import zs.xmx.nrfbledemo.scanner.aggregator.BleScanResultAggregator
import javax.inject.Inject

@ViewModelScoped
class ScannerRepository @Inject constructor(
    private val aggregator: BleScanResultAggregator
) {

    private var scanCallback: ScanCallback? = null

    fun getScannerState2(): Flow<ScanningState> = callbackFlow {
        scanCallback = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.isConnectable) {
                    aggregator.addNewDevice(result)

                    trySend(ScanningState.DevicesDiscovered(aggregator.devices))
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                val newResults = results.filter { it.isConnectable }
                newResults.forEach {
                    aggregator.addNewDevice(it)
                }
                if (newResults.isNotEmpty()) {
                    trySend(ScanningState.DevicesDiscovered(aggregator.devices))
                }
            }

            override fun onScanFailed(errorCode: Int) {
                trySend(ScanningState.Error(errorCode))
            }
        }

        //todo 改成开始扫描的状态
        trySend(ScanningState.Loading)

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setLegacy(false)
            .setReportDelay(500)
            .setUseHardwareBatchingIfSupported(false)
            .build()
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.startScan(null, settings, scanCallback!!)

        awaitClose {
            scanner.stopScan(scanCallback!!)
        }
    }

    fun clear() {
        aggregator.clear()
    }

    fun stopScanDevice() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanCallback?.let { scanner.stopScan(it) }
    }


}