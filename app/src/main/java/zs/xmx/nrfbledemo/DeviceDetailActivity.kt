package zs.xmx.nrfbledemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dylanc.viewbinding.binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zs.xmx.nrfbledemo.databinding.ActivityDeviceDetailBinding
import zs.xmx.nrfbledemo.scanner.model.BleScanResult

@AndroidEntryPoint
class DeviceDetailActivity : AppCompatActivity() {
    private val TAG = DeviceDetailActivity::class.java.simpleName

    private val mBinding by binding<ActivityDeviceDetailBinding>()

    private val mRollerViewModel by viewModels<RollerViewModel>()

    private lateinit var mBleDevice: BleScanResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initObserve()
        initEvent()
    }

    private fun initObserve() {
        lifecycleScope.launch {
            mRollerViewModel.mConnectState.collect {
                Log.e("TTTT", "连接状态 -----  $it")
            }
        }

        lifecycleScope.launch {
            mRollerViewModel.mBatteryValue.collect {
                Log.e("TTTT", "电量 -----  $it")
            }
        }

        lifecycleScope.launch {
            mRollerViewModel.mSoftWareVersion.collect {
                Log.e("TTTT", "固件版本 -----  $it")
            }
        }
    }

    private fun initEvent() {
        //Android-Ble-Library
        mBinding.actionConnect1.setOnClickListener {
            mRollerViewModel.connect(mBleDevice.device)
        }
        mBinding.actionDisconnect1.setOnClickListener {
            mRollerViewModel.disConnect()
        }
        mBinding.actionRead1.setOnClickListener {
            mRollerViewModel.readRollerSoftWareVersion()
        }
        mBinding.actionWrite1.setOnClickListener {
            mRollerViewModel.queryDeviceParams()
        }
        //Kotlin-Ble-Library
        mBinding.actionConnect2.setOnClickListener {
            mRollerViewModel.connect(mBleDevice.device.address)
        }
        mBinding.actionDisconnect2.setOnClickListener {
            mRollerViewModel.testDisConnect()
        }
        mBinding.actionRead2.setOnClickListener {
            mRollerViewModel.testRead()
        }
        mBinding.actionWrite2.setOnClickListener {
            mRollerViewModel.testWrite()
        }
    }

    private fun initData() {
        mBleDevice = intent.getParcelableExtra(KEY_BLE_DEVICE)!!
    }

    companion object {
        private const val KEY_BLE_DEVICE = "key_ble_device"

        fun start(context: Context, bleScanResult: BleScanResult) {
            val intent = Intent(context, DeviceDetailActivity::class.java)
            intent.putExtra(KEY_BLE_DEVICE, bleScanResult)
            context.startActivity(intent)
        }
    }
}