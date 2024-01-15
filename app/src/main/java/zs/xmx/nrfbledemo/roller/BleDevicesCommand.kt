package zs.xmx.nrfbledemo.roller

object BleDevicesCommand {
    //渔轮Service_UUID
    const val DEVICE_SERVICE_UUID = "0000ff00-0000-1000-8000-00805f9b34fb"

    //渔轮 Battery_UUID (电量UUID)
    const val BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb"

    //notify/read 同一个
    const val BATTERY_NOTIFY_UUID = "00002a19-0000-1000-8000-00805f9b34fb"

    //渔轮固件版本 Software_UUID
    const val SOFTWARE_SERVICE_UUID = "0000180a-0000-1000-8000-00805f9b34fb"

    const val SOFTWARE_READ_UUID = "00002a28-0000-1000-8000-00805f9b34fb"

    //渔轮可写的write,write_no_response  UUID(指令发送通道)
    const val WRITE_UUID = "0000ff01-0000-1000-8000-00805f9b34fb"

    //渔轮 notify_UUID (写入的指令,结果返回通道)
    const val WRITE_NOTIFY_UUID = "0000ff02-0000-1000-8000-00805f9b34fb"

    //查询当前线径、杆长、线容量、开机次数、断线次数、断线总长度
    //FF02通道返回 BE-FE-OF-06-1byte(线径)-2byte(竿长)-2byte(当前绕线长度cm)-2byte(开机次数)-1byte(断线次数)-2byte(断线总长度)-1byte(呼吸灯状态)-1byte(抛投状态)-1byte(上线状态)-ED
    const val QUERY_DEVICE_PARAMS = "BEFE0206ED"
    const val QUERY_DEVICE_PARAMS_SUCCESS = "BEFE0F06"

}