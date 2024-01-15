package zs.xmx.nrfbledemo

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.binding
import dagger.hilt.android.AndroidEntryPoint
import zs.xmx.nrfbledemo.databinding.ActivityScanBinding
import zs.xmx.nrfbledemo.scanner.ScanDevicesAdapter
import zs.xmx.nrfbledemo.scanner.ScannerViewModel
import zs.xmx.nrfbledemo.scanner.ScanningState
import zs.xmx.nrfbledemo.scanner.model.BleScanResult

/**
 * 补充权限判断:
 * 1. 先判断蓝牙开关
 * 2. 再判断定位开关是否开启
 */
@AndroidEntryPoint
class ScanActivity : AppCompatActivity() {

    private val TAG = ScanActivity::class.java.simpleName

    private val mBinding by binding<ActivityScanBinding>()
    private val mScannerViewModel by viewModels<ScannerViewModel>()


    private lateinit var mAdapter: ScanDevicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAdapter()
        initObserve()
        initEvent()
    }

    private fun initAdapter() {
        mAdapter = ScanDevicesAdapter()
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        mBinding.recyclerView.adapter = mAdapter
    }

    private fun initObserve() {
        mScannerViewModel.getBleScanResult.observe(this) { state ->
            Log.i(TAG, "$state")
            when (state) {
                is ScanningState.Loading -> {

                }

                is ScanningState.DevicesDiscovered -> {
                    mAdapter.setList(state.devices)
                }

                is ScanningState.Error -> {}
            }

        }
    }

    private fun initEvent() {
        mBinding.actionScan.setOnClickListener {
            mAdapter.setList(null)
            mScannerViewModel.scan()
        }
        mBinding.actionStopScan.setOnClickListener {
            mScannerViewModel.stopScan()
        }
        mAdapter.setOnItemClickListener { adapter, _, position ->
            mScannerViewModel.stopScan()
            val device = (adapter.getItem(position) as BleScanResult)
            DeviceDetailActivity.start(this@ScanActivity, device)
        }

    }


}