package zs.xmx.nrfbledemo.roller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ValueChangedCallback
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.getCharacteristic
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import zs.xmx.nrfbledemo.util.HexUtil
import zs.xmx.nrfbledemo.roller.callback.RollerBatteryNotifyCallback
import zs.xmx.nrfbledemo.roller.callback.RollerResultNotifyCallback
import zs.xmx.nrfbledemo.roller.callback.RollerSoftWareVersionCallback
import java.util.UUID

/**
 * NRF 的 Android-Ble-library 库,连接设备,订阅通知写法
 */
class RollerBleManager(
    context: Context
) : Roller by RollerManageImpl(context)

private class RollerManageImpl(
    context: Context,
) : BleManager(context), Roller {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var rollerBatteryCharacteristic: BluetoothGattCharacteristic? = null
    private var rollerSoftWareVersionCharacteristic: BluetoothGattCharacteristic? = null
    private var rollerWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var rollerResultNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private var isServiceSupported = false

    private val _rollerSoftWareVersionState = MutableStateFlow("")
    override val mRollerSoftWareVersionSate = _rollerSoftWareVersionState.asStateFlow()

    private val _batteryValue = MutableStateFlow(0)
    override val mBatteryValue = _batteryValue.asStateFlow()

    /**
     * 读取设备版本号
     */
    override suspend fun readSoftWareVersion() {
        readCharacteristic(rollerSoftWareVersionCharacteristic)
            .with(softWareVersionCallback)
            .enqueue()
    }

    private val softWareVersionCallback by lazy {
        object : RollerSoftWareVersionCallback() {
            override fun onSoftWareVersionResult(version: String) {
                _rollerSoftWareVersionState.tryEmit(version)
            }

        }
    }

    /**
     * 查询设备参数
     */
    override suspend fun queryDeviceParams() {
        Log.d(
            "TTTT",
            "查询设备参数   ${Data(HexUtil.hexStringToBytes(BleDevicesCommand.QUERY_DEVICE_PARAMS))}"
        )

        writeCharacteristic(
            rollerWriteCharacteristic,
            Data(HexUtil.hexStringToBytes(BleDevicesCommand.QUERY_DEVICE_PARAMS)),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()
    }

    /**
     * 连接设备
     */
    override suspend fun connect(device: BluetoothDevice?) {
        device?.let {
            Log.d("TTTT", "当前要连接的设备(${device.address})连接状态: $isConnected")
            if (isConnected) return
            connect(device).retry(3, 300)//连接不成功时(重连次数,重连的延迟时间)
                .useAutoConnect(false)//是否添加设备到自动重连
                .timeout(3000).suspend()
        }
    }


    /**
     * 连开设备
     */
    override fun disConnect() {
        Log.e("TTTT", "断开连接  $isConnected")

        val wasConnected = isReady
        // If the device was connected, we have to disconnect manually.
        if (wasConnected) {
            disconnect().enqueue()
        }
    }

    override fun release() {
        // Cancel all coroutines.
        scope.cancel()

        val wasConnected = isReady
        // If the device wasn't connected, it means that ConnectRequest was still pending.
        // Cancelling queue will initiate disconnecting automatically.
        cancelQueue()

        // If the device was connected, we have to disconnect manually.
        if (wasConnected) {
            disconnect().enqueue()
        }
    }

    /**
     * 设备的连接状态
     */
    override val mConnectState = stateAsFlow().map {
        when (it) {
            is ConnectionState.Connecting, is ConnectionState.Initializing -> Roller.ConnectState.LOADING

            is ConnectionState.Ready -> Roller.ConnectState.READY
            is ConnectionState.Disconnecting, is ConnectionState.Disconnected -> Roller.ConnectState.NOT_AVAILABLE
        }
    }.stateIn(scope, SharingStarted.Lazily, Roller.ConnectState.NOT_AVAILABLE)//冷流变热流

    //检验设备是否拥有我们所需的服务与特征
    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        gatt.services.forEach { services ->
            when (services.uuid) {
                UUID.fromString(BleDevicesCommand.BATTERY_SERVICE_UUID) -> {
                    rollerBatteryCharacteristic = services.getCharacteristic(
                        UUID.fromString(BleDevicesCommand.BATTERY_NOTIFY_UUID),
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY
                    )
                    Log.e("TTTT", "电量特征值: $rollerBatteryCharacteristic")
                }

                UUID.fromString(BleDevicesCommand.SOFTWARE_SERVICE_UUID) -> {
                    rollerSoftWareVersionCharacteristic = services.getCharacteristic(
                        UUID.fromString(BleDevicesCommand.SOFTWARE_READ_UUID),
                        BluetoothGattCharacteristic.PROPERTY_READ
                    )
                    Log.e("TTTT", "固件版本特征值: $rollerSoftWareVersionCharacteristic")
                }

                UUID.fromString(BleDevicesCommand.DEVICE_SERVICE_UUID) -> {
                    rollerWriteCharacteristic = services.getCharacteristic(
                        UUID.fromString(BleDevicesCommand.WRITE_UUID),
                        BluetoothGattCharacteristic.PROPERTY_WRITE
                    )
                    Log.e("TTTT", "给渔轮写入指令特征值: $rollerWriteCharacteristic")
                    rollerResultNotifyCharacteristic = services.getCharacteristic(
                        UUID.fromString(BleDevicesCommand.WRITE_NOTIFY_UUID),
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY
                    )
                    Log.e(
                        "TTTT", "给渔轮写入指令(获取结果)特征值: $rollerResultNotifyCharacteristic"
                    )
                }
            }
        }
        isServiceSupported =
            rollerBatteryCharacteristic != null && rollerSoftWareVersionCharacteristic != null && rollerWriteCharacteristic != null && rollerResultNotifyCharacteristic != null
        return isServiceSupported
    }

    /**
     * 设备连接成功后的初始化处理
     * 一般是订阅 notify/indicate 处理
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initialize() {
        //订阅渔轮电量通知
        val flow: Flow<RollerBatteryNotifyCallback> =
            setNotificationCallback(rollerBatteryCharacteristic).asValidResponseFlowExt()
        scope.launch {
            flow.map { it.batteryValue }.collect { _batteryValue.tryEmit(it) }
        }

        enableNotifications(rollerBatteryCharacteristic).enqueue()

        //订阅渔轮写入结果通知
        val resultNotifyFlow: Flow<RollerResultNotifyCallback> =
            setNotificationCallback(rollerResultNotifyCharacteristic).asValidResponseFlowExt()
        scope.launch {
            resultNotifyFlow.map { it.rawData }.collect {
                //todo callback
                // _data.value
            }
        }

        enableNotifications(rollerResultNotifyCharacteristic).enqueue()

        //读取固件版本号
        readCharacteristic(rollerSoftWareVersionCharacteristic).with(softWareVersionCallback)
            .enqueue()
        //

    }

    /**
     * 该方法下,设置所有服务和特征码无效
     */
    override fun onServicesInvalidated() {
        rollerBatteryCharacteristic = null
        rollerSoftWareVersionCharacteristic = null
        rollerWriteCharacteristic = null
        rollerResultNotifyCharacteristic = null
    }

}

@ExperimentalCoroutinesApi
inline fun <reified T : ProfileReadResponse> ValueChangedCallback.asValidResponseFlowExt(): Flow<T> =
    callbackFlow {
        // Make sure the callbacks are called without unnecessary delay.
        setHandler(null)
        with { device, data ->
            T::class.java.getDeclaredConstructor().newInstance()
                .apply { onDataReceived(device, data) }.takeIf { it.isValid }?.let { trySend(it) }
        }
        awaitClose {
            // There's no way to unregister the callback from here.
            with { _, _ -> }
        }
    }