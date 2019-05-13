package com.example.carcontroller

import android.app.PendingIntent.getActivity
import android.os.Message
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.*
import android.R
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.support.annotation.MainThread
import android.view.LayoutInflater
import java.security.AccessController.getContext
import android.os.Looper
import kotlin.coroutines.coroutineContext


// Osztály az Udp szál kezeléséhez, (külön szálon kell futtatni mert, lefagyasztja a fő szálat
class UdpThread(internal var dstAddress: String, internal var dstPort: Int, internal var handler: MainActivity.UdpClientHandler?, var accData: AccDataContainer): Thread()
{
    private var running: Boolean = false

    internal var socket: DatagramSocket? = null // socket létrehozása
    internal var address: InetAddress? = null // saját Ip cím

    private var connected: Boolean = false


    var lastStr: String = "alap" // az utoljára elküldött String

    fun setRunning()
    {
        this.running = running
    }

    private fun sendState(state: String) // A szál állapotának elküldése a fő szál felé
    {
        handler!!.sendMessage(Message.obtain(handler, MainActivity.UdpClientHandler!!.UPDATE_CONNECTED, state))
    }

    override fun run() // A szál
    {
        handler!!.sendEmptyMessage(MainActivity.UdpClientHandler.UPDATE_CONNECTING)
        running = true

        while(running)
        {
            try {
                socket = DatagramSocket() // új DatagramSocket létrehozása
                address = InetAddress.getByName(dstAddress) // saját Ip cím lekérése

                //var str = accData.getLastS()
                var str = accData.getStr() // szenzoradat lekérése a tárolóból
                if(str != lastStr) // ha az utoljára küldött adat nem egyezik meg a jelenlegivel
                {
                    var buf = str.toByteArray() // A küldendő adat átalakítása ByteArray()-re a küldéshez
                    var packet = DatagramPacket(buf, buf.size, address, dstPort) // küldendő csomag összeállítása
                    socket!!.send(packet) // csomag küldése
                }
                lastStr = str // az utoljára küldött String felülírása
            }
            // Kivételek kezelése
            catch (e: SocketException) {
                e.printStackTrace()
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                handler!!.sendEmptyMessage(MainActivity.UdpClientHandler.UPDATE_NOTCONNECTED)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (socket != null) {
                    socket!!.close()
                    handler?.sendEmptyMessage(MainActivity.UdpClientHandler.UPDATE_END)
                }
            }
        }
    }
}