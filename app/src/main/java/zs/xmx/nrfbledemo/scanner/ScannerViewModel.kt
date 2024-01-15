package zs.xmx.nrfbledemo.scanner

import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import zs.xmx.nrfbledemo.scanner.repository.ScannerRepository
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scannerRepository: ScannerRepository
) : ViewModel() {

    private val tag = "BleViewModel"

    private var mScanJob: Job? = null

    //todo 改成返回状态的密封类
    private val _bleScanLiveData = MutableLiveData<ScanningState>()
    val getBleScanResult: LiveData<ScanningState>
        get() = _bleScanLiveData

    fun scan() {
        mScanJob?.cancel()
        mScanJob = viewModelScope.launch {
            scannerRepository.clear()
            scannerRepository.getScannerState2().collect { result ->
                _bleScanLiveData.value = if (result is ScanningState.DevicesDiscovered) {
                    //todo 过滤扫描得到的list数组
                    ScanningState.DevicesDiscovered(result.devices.filter { it.hadName })
                } else {
                    result
                }
            }

        }
        scannerRepository.getScannerState2()
            .onEach {

            }.launchIn(viewModelScope)
    }


    fun stopScan() {
        mScanJob?.cancel()
        scannerRepository.stopScanDevice()
    }

    override fun onCleared() {
        super.onCleared()
        scannerRepository.clear()
    }

}