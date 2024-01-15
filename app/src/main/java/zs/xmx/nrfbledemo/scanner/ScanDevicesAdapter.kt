package zs.xmx.nrfbledemo.scanner

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import zs.xmx.nrfbledemo.R
import zs.xmx.nrfbledemo.scanner.model.BleScanResult

class ScanDevicesAdapter :
    BaseQuickAdapter<BleScanResult, BaseViewHolder>(R.layout.item_scan_devices) {

    override fun convert(holder: BaseViewHolder, item: BleScanResult) {
        with(holder) {
            setText(R.id.tv_deviceName, item.name)
            setText(R.id.tv_deviceMac, item.address)
        }

    }

}