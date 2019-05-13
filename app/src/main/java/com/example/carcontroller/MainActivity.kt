package com.example.carcontroller

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.carcontroller.AccDataContainer
import com.example.carcontroller.UdpThread
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var sensorManager: SensorManager
    var accDataContainer = AccDataContainer() // Tároló osztály a szenzoradatok számára

    var x: Double = 0.0 // Szenzor adat x koordinátája
    var y: Double = 0.0 // Szenzor adat y koordinátája

    var s: String = "" // String amit az accdataContainer-nek átadunk

    var brake: Boolean = true // fék alapállapotban
    var lastImage: String = "alap" // a nyíl képek alapállapota

    internal var udpClientHandler: UdpClientHandler? = null // Udp szál kezelője
    internal var udpClientThread: UdpThread? = null // Udp szál létrehozása

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) // A SensorEventListener osztály függvénye, felül kell írni, de nincs kifejtve
    {
    }

    override fun onSensorChanged(event: SensorEvent?) // A SensorEventListener osztály függvénye, itt lehet megadni, hogy mi történjen, ha változnak a szenoradatok
    {

        y = event!!.values[0].toDouble() // a szenzor adatok x értének változóba mentése
        x = event.values[1].toDouble() // s szenor adatok y értékének változóba mentése

        //accDataContainer.addValues(s)

        if(brake) // ha a fék aktív, akkor az autó "alap" állapotban van
        {
            s = "alap"
        }
        else // különben kiszámolja a az x és y értékek alapján az irányt
        {
            when
            {
                //Irányok: alap elore hatra jobbra balra balraelore jobbraelore balrahatra jobbrahatra

                (x < 2 && x > -2) && (y < 6 && y > 2)  -> s = "alap"
                (x < 2 && x > -2) && (y > 2)           -> s = "hatra"
                (x < 2 && x > -2) && (y < 2)           -> s = "elore"
                (x > 2) && (y < 6 && y > 2)            -> s = "jobbra"
                (x < 2) && (y < 6 && y > 2)            -> s = "balra"
                x > 2 && y > 6                         -> s = "jobbrahatra"
                x < -2 && y > 6                        -> s = "balrahatra"
                x > 2 && y < 2                         -> s = "jobbraelore"
                x < -2 && y < 2                        -> s = "balraelore"
                //else -> s = "Alap"
            }
        }

        if(s != lastImage) // ha az előző kép más volt mint az új
        {
            changeArrow(s) // a nyíl kép kicserélése
        }
        lastImage = s // az előző kép lesz az új kép (a következő vizsgálathoz)
        accDataContainer.addValues(s) // hozzáadjuk az s Stringet a tárolóhoz
    }

    fun buttonConnectOnClickListener(view: View) // Mi történjen ha megnyomjuk a "Connect" gombot
    {
        udpClientThread = UdpThread(ipAddress.text.toString(), Integer.parseInt(portNumber.text.toString()), udpClientHandler, accDataContainer) // Udp szál deklarálása
        udpClientThread!!.start() // Udp szál elindítása

        Toast.makeText(applicationContext, "Connecting...", Toast.LENGTH_SHORT).show()

        //connect.text = "Connected"
        //connect.setTextColor()
        //connect.isEnabled = false
        //ipAddress.isEnabled = false
        //portNumber.isEnabled = false

    }

    private fun updateState(state: String)
    {
        showToast(state)
    }

    private fun clientEnd()
    {
        udpClientThread = null
        connect.isEnabled = true
    }

    class UdpClientHandler(private val parent: MainActivity) : Handler() // Udp szál kezelésére szolgáló osztály
    {

        override fun handleMessage(msg: Message) // Az Udp szál által a fő szál felé küldött üzenetek feldolgozása
        {
            var data = "Connected to ${parent.ipAddress.text}(${parent.portNumber.text})" // String ami tartalmazza, hogy melyik Ip címre és melyik Portra csatlakoztunk (a kijelzőn való megjelenítéshez)
            when (msg.what)
            {
                UPDATE_CONNECTED -> parent.connState.text = data // Ha csatlakozott, akkor kiírja az Ip címet és a portot
                UPDATE_NOTCONNECTED -> parent.connState.text = "Unknow Ip address" // Ha nem akkor az "Unknown Ip address" szöveget írja ki
            }
        }

        companion object {
            val UPDATE_CONNECTING = 0
            val UPDATE_CONNECTED = 1
            val UPDATE_END = 2
            val UPDATE_NOTCONNECTED = 3
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) // Az alkalmazás indításakor ez a függvény hívódik meg legelőször
    {
        super.onCreate(savedInstanceState)

        requestedOrientation =(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) // Landsacpe mode indításkor

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN) // Teljes képernyős mód

        supportActionBar?.hide()
        actionBar?.hide()

        setContentView(R.layout.activity_main)

        // Accelerometer data listener
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager // Szenzor létrehozása
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL) // A Szenzor típusának beállítása(ACCELEROMETER)

        udpClientHandler = UdpClientHandler(this)
    }

    fun changeArrow(image: String)
    {
        when(image) // A képernyőn látható nyíl változtatása az irány szerint
        {
            "elore"       -> kep.setImageResource(R.drawable.elore)
            "hatra"       -> kep.setImageResource(R.drawable.hatra)
            "jobbra"      -> kep.setImageResource(R.drawable.jobbra)
            "balra"       -> kep.setImageResource(R.drawable.balra)
            "balrahatra"  -> kep.setImageResource(R.drawable.balrahatra)
            "balraelore"  -> kep.setImageResource(R.drawable.balraelore)
            "jobbrahatra" -> kep.setImageResource(R.drawable.jobbrahatra)
            "jobbraelore" -> kep.setImageResource(R.drawable.jobraelore)
            "alap"        -> kep.setImageResource(R.drawable.brake)

            //alap elore hatra jobbra balra balraelore jobbraelore balrahatra jobbrahatra
        }
    }

    fun startStop(view: View) // A fék gomb megnyomásának kezelése
    {
        if(brake)
        {
            brake = false // Fék kiengedése
            startstop.setImageResource(R.drawable.red) // kép változtatása
        }
        else
        {
            brake = true // Fék behúzása
            startstop.setImageResource(R.drawable.green) // kép változtatása
        }
    }

    fun showToast(message: String)
    {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }
}
