package com.example.hudrelay

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

object BleSender {
    private val SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")
    private val CHARACTERISTIC_UUID = UUID.fromString("abcd")

    private var bluetoothGatt: BluetoothGatt? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private val sendQueue = LinkedBlockingQueue<String>()

    private lateinit var context: Context
    private var device: BluetoothDevice? = null
    private var isConnected = false

    fun init(ctx: Context, deviceAddress: String) {
        context = ctx
        val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        device?.connectGatt(context, false, gattCallback)
    }

    fun enqueue(message: String) {
        if (isConnected && characteristic != null) {
            sendQueue.offer(message)
            sendNext()
        } else {
            Log.w("BleSender", "Not connected, cannot send: $message")
        }
    }

    private fun sendNext() {
        val msg = sendQueue.poll() ?: return
        characteristic?.value = msg.toByteArray(Charsets.UTF_8)
        bluetoothGatt?.writeCharacteristic(characteristic)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                Log.i("BleSender", "Connected to GATT server")
                bluetoothGatt = gatt
                gatt.discoverServices()
            } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("BleSender", "Disconnected from GATT server")
                isConnected = false
                bluetoothGatt = null
                characteristic = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                    isConnected = true
                    sendNext()
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendNext()
            } else {
                Log.w("BleSender", "Characteristic write failed")
            }
        }
    }
}
