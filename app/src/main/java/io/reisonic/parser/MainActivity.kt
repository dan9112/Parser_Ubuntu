package io.reisonic.parser

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

/**
 * Класс-шаблон для прослушивания сообщений,
 * передающихся через интерфейс Bluetooth по конкретному MAC-адресу
 */
class MainActivity : AppCompatActivity() {

    /**
     * Адаптер локального устройства для взаимодействия с интерфейсом Bluetooth
     */
    private var bluetoothAdapter: BluetoothAdapter? = null

    /**
     * Разъём для подключения через интерфейс Bluetooth
     */
    private var bluetoothSocket: BluetoothSocket? = null

    /**
     * ???
     */
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * Статус соединения
     */
    private var connectionStatus: Boolean = false // Message sending status

    /**
     * ???
     */
    private var connectionSocket: Boolean = true // Message sending status

    private val macAddress = "00:01:95:4A:BB:2D" // 00:01:95:4A:BB:2D - алкотестер

    override fun onCreate(savedInstanceState: Bundle?) = runBlocking {
        super.onCreate(savedInstanceState)

        withContext(Dispatchers.IO) {
            while (!connectionStatus) {
                connectBluetoothDevice()
            }

            while (connectionSocket) {
                if (bluetoothSocket != null) {
                    try {
                        val byteArray = ByteArray(20)
                        bluetoothSocket?.inputStream?.read(byteArray)

                        var c = 0
                        val arrayList: ArrayList<String> = ArrayList()
                        while (c != byteArray.size) {
                            arrayList.add(byteArray[c].toString())
                            c++
                        }

                        var message = ""
                        for (byte in byteArray) {
                            val decodedChar = byte.toChar()
                            if (decodedChar != '\r' && decodedChar != '\u0000') message += decodedChar // Ждём символ NULL для выхода из цикла
                            else break
                        }

                        Log.i("Message", message)
                        Log.i("Log", arrayList.toString())
                        arrayList.clear()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                delay(100)
            }
        }
        // Toast.makeText(this@MainActivity, "Программа запущена!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Функция подключения устройства через интерфейс Bluetooth
     */
    private fun connectBluetoothDevice() {
        try {
            bluetoothAdapter =
                    BluetoothAdapter.getDefaultAdapter() // get the mobile bluetooth device
            val device: BluetoothDevice =
                    bluetoothAdapter?.getRemoteDevice(macAddress)!! // connects to the device's address and checks if it's available
            bluetoothSocket =
                    device.createInsecureRfcommSocketToServiceRecord(uuid) // create a RFCOMM (SPP) connection
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
            bluetoothSocket?.connect() //start connection

        } catch (e: Exception) {
            e.printStackTrace()
        }
        connectionStatus = true
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionStatus = true
        connectionSocket = false
        bluetoothSocket?.close()
    }
}
