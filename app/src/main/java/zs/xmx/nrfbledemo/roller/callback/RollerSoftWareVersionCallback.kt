package zs.xmx.nrfbledemo.roller.callback

import android.bluetooth.BluetoothDevice
import android.text.TextUtils
import android.util.Log
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

abstract class RollerSoftWareVersionCallback : ProfileReadResponse() {
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        Log.i("TTTT", "固件版本 DATA: ${data.getStringValue(0)} ")
        if (!TextUtils.isEmpty(data.getStringValue(0))) {
            onSoftWareVersionResult(data.getStringValue(0)!!)
        }
    }

    abstract  fun onSoftWareVersionResult(version: String)
}