package agro.hackathon.hackathon

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import java.io.IOException
import java.util.*
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.os.Message
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception


class ConnectThread(private val mmDevice: BluetoothDevice, private val main: MainActivity) : Thread() {
    private val mmSocket: BluetoothSocket?
    val myUUID = "00001101-0000-1000-8000-00805F9B34FB"
    var output: OutputStream? = null
    var input: InputStream? = null

    init {
        // Objeto temporário atribuído a mmSocket,
        var socket: BluetoothSocket? = null

        // Conectar ao dispositivo Bluetooth
        try {

            socket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(myUUID))
            //BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        } catch (e: IOException) {
            e.printStackTrace()
        }

        mmSocket = socket
    }

    override fun run() {
        // Cancele a descoberta pq vai diminuir a velocidade
        //mBluetoothAdapter.cancelDiscovery()

        try {
            // Conecte o dispositivo através do soquete. Isso irá bloquear
            println("Conectando...")
            mmSocket!!.connect()
            output = mmSocket.outputStream;
            input = mmSocket.inputStream
            Thread() {
                run() {
                    var erro = false
                    while (mmSocket!!.isConnected) {
                        var bytes = input!!.read()
                        val readMessage = input!!.read()
                        println(readMessage)
                        if (readMessage==0 && !erro) {
                            println("ERR")
                            erro = true;
                            toMainActivity("ERRO")
                        }else if (readMessage!=0) {
                            erro = false
                        }
                    }
                }
            }.start()
            toMainActivity("CONN")
            println("FEITOOOOOOOOOOOOOOOOOOOOO")
        } catch (connectException: IOException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket!!.close()
            } catch (closeException: IOException) {
            }

            return
        }


    }

    /** Will cancel an in-progress connection, and close the socket  */
    fun cancel() {
        try {
            mmSocket!!.close()
        } catch (e: IOException) {
        }

    }

    private fun toMainActivity(data: String) {

        val message = Message()
        val bundle = Bundle()
        bundle.putByteArray("data", data.toByteArray())
        message.setData(bundle)
        main.handler.sendMessage(message)
    }

    fun senMsg(msg: String) {
        if (output != null) {
            try {
                println("->$msg")
                if (msg.contains("desligar")) {
                    output!!.write("0".toByteArray())
                    toMainActivity("DESLIGAR")
                } else if (msg.contains("ligar")) {
                    output!!.write("1".toByteArray())
                    toMainActivity("LIGAR")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}