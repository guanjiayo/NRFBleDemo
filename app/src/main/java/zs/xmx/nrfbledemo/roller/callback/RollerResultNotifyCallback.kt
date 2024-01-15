package zs.xmx.nrfbledemo.roller.callback

import android.bluetooth.BluetoothDevice
import android.util.Log
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

class RollerResultNotifyCallback : ProfileReadResponse() {
    //通知返回的数据
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        Log.i("TTTT", "Result: DATA: $data ")
    }

    //可如下扩展方法,直接将解析好的结果返回
    /*
          override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() == 1) {
            val buttonState = data.getIntValue(Data.FORMAT_UINT8, 0) == 0x01
            onButtonStateChanged(device, buttonState)
        } else {
            onInvalidDataReceived(device, data)
        }
    }

    abstract fun onButtonStateChanged(device: BluetoothDevice, state: Boolean)
     */
}