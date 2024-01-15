package zs.xmx.nrfbledemo

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.common.logger.DefaultBleLogger
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BondState
import zs.xmx.nrfbledemo.repository.RollerRepository
import zs.xmx.nrfbledemo.roller.BleDevicesCommand
import zs.xmx.nrfbledemo.util.ByteUtils
import zs.xmx.nrfbledemo.util.HexUtil
import java.util.UUID
import javax.inject.Inject


@SuppressLint("MissingPermission")
@HiltViewModel
class RollerViewModel @Inject constructor(
    @ApplicationContext context: Context, private val rollerRepository: RollerRepository
) : AndroidViewModel(context as Application) {

    private val tag = "BleViewModel"


    //-------------------Android-BLE-Library---------------------------

    //设备连接状态
    val mConnectState = rollerRepository.mConnectState

    //电量
    val mBatteryValue = rollerRepository.mBatteryValue

    //固件版本
    val mSoftWareVersion = rollerRepository.mRollerSoftWareVersionSate.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ""
    )

    fun connect(device: BluetoothDevice) {
        val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            // This method may throw an exception if the connection fails,
            // Bluetooth is disabled, etc.
            // The exception will be caught by the exception handler and will be ignored.
            rollerRepository.connect(device)
        }
    }

    fun disConnect() {
        rollerRepository.disConnect()
    }

    fun readRollerSoftWareVersion() {
        viewModelScope.launch {
            rollerRepository.readSoftWareVersion()
        }
    }

    fun queryDeviceParams() {
        viewModelScope.launch {
            rollerRepository.queryDeviceParams()
        }
    }

    override fun onCleared() {
        super.onCleared()
        rollerRepository.release()
    }

    //-------------------Kotlin-BLE-Library---------------------------

    private var client: ClientBleGatt? = null
    private lateinit var rollerWriteCharacteristic: ClientBleGattCharacteristic
    private lateinit var rollerSoftWareVersionCharacteristic: ClientBleGattCharacteristic

    private lateinit var logger: BleLoggerAndLauncher

    fun connect(macAddress: String) = viewModelScope.launch {

        //profile 可以直接用app_name
        logger = DefaultBleLogger.create(getApplication(), null, key = tag, name = macAddress)

        val client =
            ClientBleGatt.connect(getApplication(), macAddress, viewModelScope, logger = logger)
        this@RollerViewModel.client = client

        //连接状态回调
        client.connectionState.filterNotNull().onEach {
            Log.d(tag, "connect state: $it")
        }.launchIn(viewModelScope)

        if (!client.isConnected) {
            return@launch
        }

        //订阅通知
        try {
            val services = client.discoverServices()
            configureGatt(services)
        } catch (e: Exception) {
            onMissingServices()
        }
    }

    fun testDisConnect() {
        client?.disconnect()
    }

    private suspend fun configureGatt(services: ClientBleGattServices) {
        //渔轮Service
        val rollerServices =
            services.findService(UUID.fromString(BleDevicesCommand.DEVICE_SERVICE_UUID))!!

        //给渔轮写指令的特征值
        rollerWriteCharacteristic =
            rollerServices.findCharacteristic(UUID.fromString(BleDevicesCommand.WRITE_UUID))!!

        //读取固件版本号
        rollerSoftWareVersionCharacteristic =
            services.findService(UUID.fromString(BleDevicesCommand.SOFTWARE_SERVICE_UUID))
                ?.findCharacteristic(UUID.fromString(BleDevicesCommand.SOFTWARE_READ_UUID))!!

        //渔轮写入指令(获取结果)特征值
        rollerServices.findCharacteristic(UUID.fromString(BleDevicesCommand.WRITE_NOTIFY_UUID))
            ?.getNotifications()
            ?.onEach { byteArray ->
                Log.i(tag, "result: $byteArray")
            }
            ?.catch { it.printStackTrace() }
            ?.launchIn(viewModelScope)

        //电量notify
        services.findService(UUID.fromString(BleDevicesCommand.BATTERY_SERVICE_UUID))
            ?.findCharacteristic(UUID.fromString(BleDevicesCommand.BATTERY_NOTIFY_UUID))
            ?.getNotifications()?.mapNotNull {
                //解析数据
                ByteUtils.getResultByLen(byteArrayOf(it.value[0]))
            }?.onEach { battery ->
                Log.i(tag, "电量值: $battery")
            }//解析后的数据结果
            ?.catch { it.printStackTrace() }
            ?.launchIn(viewModelScope)

        //设备固件版本
        rollerSoftWareVersionCharacteristic.getNotifications()
            .onEach { byteArray ->
                Log.i(tag, "设备版本号: $byteArray")
            }//解析后的数据结果
            .catch { it.printStackTrace() }
            .launchIn(viewModelScope)

    }

    private fun onMissingServices() {
        //todo _state.value = _state.value.copy(missingServices = true)
        client?.disconnect()
    }

    fun testRead() {
        viewModelScope.launch {
            try {
                rollerSoftWareVersionCharacteristic.read()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun testWrite() {
        viewModelScope.launch {
            try {
                rollerWriteCharacteristic.write(
                    DataByteArray(
                        HexUtil.hexStringToBytes(
                            BleDevicesCommand.QUERY_DEVICE_PARAMS
                        )
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


}


