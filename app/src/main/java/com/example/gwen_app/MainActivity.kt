package com.example.gwen_app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException


var wind = 0.0
var tresh: Int = 10
var notifState: Int = 0

class MainActivity : AppCompatActivity() {


    lateinit var notificationManager : NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : Notification.Builder
    private val channelId = "com.example.gwen_app"
    private val description = "Test notification"

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.fragment_first)
        val animDrawable = root_layout.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()
        setSupportActionBar(toolbar)
        val getButton= findViewById<Button>(R.id.getButton) as Button
        var c = 0
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val myWebView: WebView = findViewById(R.id.webView)
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.loadWithOverviewMode = false
        myWebView.settings.useWideViewPort = false


        //myWebView.loadUrl("file:///android_asset/index.html")
        val htmlString = "<html>\n" +
                "<head >\n" +
                " <title>Welcome</title>\n" +
                " <style type=\"text/css\">\n" +
                "#maincontainer\n" +
                "{\n" +
                "top:0;\n" +
                "left:0;\n" +
                "padding-top:0;\n" +
                "position:absolute;\n" +
                "clip: rect(130px,200px,168px,30px);\n" +
                "\n" +
                "\n" +
                "\n" +
                "}\n" +
                "\n" +
                "#big\n" +
                "{\n" +
                "top:-125;\n" +
                "left:-30;\n" +
                "padding-top:0;\n" +
                "position:absolute;\n" +
                "height: 100;\n" +
                "width: 100;\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"big\">\n" +
                "<div id=\"maincontainer\">\n" +
                " <script src=\"https://services.data.shom.fr/hdm/vignette/petite/TREBEURDEN?locale=fr\"></script>\n" +
                "</div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>"

        myWebView.loadData(htmlString, "text/html", null)


        //val myWebView: WebView = findViewById(R.id.webView)
        //myWebView.loadUrl("https://services.data.shom.fr/hdm/vignette/petite/TREBEURDEN?locale=fr")


        getButton.setOnClickListener {view ->
            // your code to perform when the user clicks on the button

            fetchJson()
            val textView = findViewById<TextView>(R.id.gustValue)
            textView.setText("$wind").toString()
            val textViewValue = textView.text

        }


        val sw1 = findViewById<Switch>(R.id.switch1)
        sw1?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {notifState = 1} else notifState=0
            println(notifState)
            if (notifState == 1 )
            {
                CoroutineScope(IO).launch {
                    getResult()

                }

            }
        }


        val seek = findViewById<SeekBar>(R.id.seekBar)
        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                println("Tresh is ${seek.progress}")
                tresh = seek.progress
                val textView = findViewById<TextView>(R.id.treshValue)
                textView.setText("$tresh").toString()
                val textViewValue = textView.text
            }
        })
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN




    }

    private suspend fun getResult()
    {
        while (notifState == 1)
        {
            fetchJson()

            setTextOnMainThread("$wind")
            if (wind > tresh){notif(wind)}

            delay(5000)
        }
    }

    private fun setNewText(input: String){

        gustValue.text = input
    }
    private suspend fun setTextOnMainThread(input: String) {
        withContext (Main) {
            setNewText(input)
        }
    }



    fun notif(wind: Double){
        val intent = Intent(this,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        val contentView = RemoteViews(packageName,R.layout.notification_layout)
        contentView.setTextViewText(R.id.tv_title,"A l'eau !")
        contentView.setTextViewText(R.id.tv_content,"Fais pas ton Brice  !! $wind noeuds")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId,description,NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this,channelId)
                .setContent(contentView)
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.ic_launcher))
                .setContentIntent(pendingIntent)
        }else{
            println("Hi !! ")
            builder = Notification.Builder(this)
                .setContent(contentView)
                .setSmallIcon(R.drawable.ic_launcher_round)
                //.setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.ic_launcher))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234,builder.build())
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }


    }
    fun fetchJson(){
        println("Attempting to fetch JSON")
        val url = "http://api.holfuy.com/live/?s=787&pw=JonKol19&m=JSON&tu=C&su=knots"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body  = response.body?.string()
                val gson = GsonBuilder().create()
                val homeFeed = gson.fromJson(body, HomeFeed::class.java)
                wind = homeFeed.wind.gust
                println(wind)


            }
            override fun onFailure(call: Call, e: IOException) {
                println("Fail to execute the request")
            }
        })
    }
}

fun minusOneSecond() {
        println("Hey")
        }

class HomeFeed(val wind:Wind)
{

}

class Wind(val speed:Double, val gust:Double)
{

}

class RSSPullService : IntentService(RSSPullService::class.simpleName) {
    override fun onHandleIntent(intent: Intent?) {
        println("Hello ! ")
    }


}

class WebAppInterface(private val mContext: Context) {

    /** Show a toast from the web page  */
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }
}




