package com.maksimowiczm.foodyou.scale.infrastructure

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.maksimowiczm.foodyou.scale.domain.ScaleConnectionState
import com.maksimowiczm.foodyou.scale.domain.ScaleProtocol
import com.maksimowiczm.foodyou.scale.domain.ScaleRepository
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

@SuppressLint("MissingPermission")
class AndroidBleScaleManager(
    private val context: Context,
    private val protocol: ScaleProtocol,
) : ScaleRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val bluetoothManager: BluetoothManager?
        get() = context.getSystemService(BluetoothManager::class.java)

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private val scanner
        get() = bluetoothAdapter?.bluetoothLeScanner

    private var gatt: BluetoothGatt? = null
    private var scanCallback: ScanCallback? = null
    private var scanTimeoutRunnable: Runnable? = null
    private var stateEmitter: ((ScaleConnectionState) -> Unit)? = null

    override val connectionState: StateFlow<ScaleConnectionState> =
        callbackFlow {
            stateEmitter = { state -> trySend(state) }

            awaitClose {
                stopScanning()
                gatt?.disconnect()
                // close() is called in onConnectionStateChange when STATE_DISCONNECTED
                gatt = null
                stateEmitter = null
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = ScaleConnectionState.Idle,
        )

    private val gattCallback =
        object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        stateEmitter?.invoke(ScaleConnectionState.Connected(null))
                        mainHandler.post { gatt.discoverServices() }
                    }

                    BluetoothGatt.STATE_DISCONNECTED -> {
                        stateEmitter?.invoke(ScaleConnectionState.Disconnected)
                        gatt.close()
                        if (this@AndroidBleScaleManager.gatt == gatt) {
                            this@AndroidBleScaleManager.gatt = null
                        }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    stateEmitter?.invoke(ScaleConnectionState.Error("Failed to discover services: $status"))
                    return
                }

                val service = gatt.getService(protocol.serviceUuid) ?: findServiceByUuid(gatt.services)
                val characteristic = service?.getCharacteristic(protocol.notifyCharacteristicUuid)
                if (characteristic == null) {
                    stateEmitter?.invoke(ScaleConnectionState.Error("Notify characteristic not found"))
                    return
                }

                val notificationsEnabled = gatt.setCharacteristicNotification(characteristic, true)
                if (!notificationsEnabled) {
                    stateEmitter?.invoke(ScaleConnectionState.Error("Failed to enable notifications"))
                    return
                }

                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                if (descriptor == null) {
                    stateEmitter?.invoke(ScaleConnectionState.Error("CCCD descriptor not found"))
                    return
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    run {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    }
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
            ) {
                handleCharacteristicChange(characteristic.uuid, value)
            }

            @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
            ) {
                val value = characteristic.value ?: return
                handleCharacteristicChange(characteristic.uuid, value)
            }
        }

    override fun startScanning() {
        if (scanCallback != null) {
            return
        }

        val bluetoothScanner = scanner
        if (bluetoothScanner == null) {
            stateEmitter?.invoke(ScaleConnectionState.Error("Bluetooth LE scanner unavailable"))
            return
        }

        stateEmitter?.invoke(ScaleConnectionState.Scanning)

        val callback =
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    val device = result?.device ?: return
                    if (!protocol.matchesDevice(device.name, emptyList())) {
                        return
                    }

                    stopScanning()
                    connect(device)
                }

                override fun onScanFailed(errorCode: Int) {
                    stateEmitter?.invoke(ScaleConnectionState.Error("BLE scan failed: $errorCode"))
                }
            }

        scanCallback = callback

        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        bluetoothScanner.startScan(null, settings, callback)

        val timeoutRunnable = Runnable { stopScanning() }
        scanTimeoutRunnable = timeoutRunnable
        mainHandler.postDelayed(timeoutRunnable, SCAN_TIMEOUT_MS)
    }

    override fun stopScanning() {
        val callback = scanCallback ?: return
        scanner?.stopScan(callback)
        scanCallback = null

        scanTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        scanTimeoutRunnable = null
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    private fun connect(device: BluetoothDevice) {
        gatt?.disconnect()
        stateEmitter?.invoke(ScaleConnectionState.Connecting)
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    private fun handleCharacteristicChange(characteristicUuid: UUID, value: ByteArray) {
        val reading = protocol.parseNotification(characteristicUuid, value) ?: return
        stateEmitter?.invoke(ScaleConnectionState.Connected(reading))
    }

    private fun findServiceByUuid(services: List<BluetoothGattService>): BluetoothGattService? {
        return services.firstOrNull { it.uuid == protocol.serviceUuid }
    }

    private companion object {
        private const val SCAN_TIMEOUT_MS = 15_000L
        private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
