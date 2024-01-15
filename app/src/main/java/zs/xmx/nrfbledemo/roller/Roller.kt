package zs.xmx.nrfbledemo.roller

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface Roller {

    /**
     *  渔轮固件版本
     */
    val mRollerSoftWareVersionSate: StateFlow<String>

    /**
     * 设备电量
     */
    val mBatteryValue: StateFlow<Int>

    /**
     * 设备连接状态
     */
    enum class ConnectState {
        LOADING,
        READY,
        NOT_AVAILABLE
    }

    /**
     * 设备的连接状态
     */
    val mConnectState: StateFlow<ConnectState>


    /**
     * Connects to the device.
     */
    suspend fun connect(device: BluetoothDevice?)

    /**
     * 断开连接
     */
    fun disConnect()

    /**
     * 释放资源
     */
    fun release()

    /**
     * 读取固件版本
     */
    suspend fun readSoftWareVersion()

    /**
     * 查询设备信息
     */
    suspend fun queryDeviceParams()


}