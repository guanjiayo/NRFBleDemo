### kotlin-Ble-Library

https://github.com/NordicSemiconductor/Kotlin-BLE-Library

> Demo:
https://github.com/NordicSemiconductor/Android-nRF-Toolbox

> 基本实现流程

1. 自定义类实现BleManager
2. 重写isRequiredServiceSupported() , 定义好所有所需的蓝牙Services和Characteristic
3. 重写initialize()方法,将连接设备成功需要订阅的通知订阅上
4. 按需实现连接,断开连接,释放资源,读写操作等方法

### Android-BLE-Library

https://github.com/NordicSemiconductor/Android-BLE-Library

> Demo
https://github.com/NordicSemiconductor/Android-nRF-Blinky

> 基本实现流程

1. ClientBleGatt.connect(xxx) 获取ClientBleGatt对象
2. ClientBleGatt.discoverServices() 拿到蓝牙设备所有Services和Characteristic
3. 拿到ClientBleGattCharacteristic,按需订阅通知,读写操作即可