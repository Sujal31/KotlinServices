package anroid.threadhandler.com.myapplication

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.webkit.URLUtil
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class LocalService : Service() {

    private val binder = LocalBinder()
    lateinit var resultReceiver: ResultReceiver
    lateinit var notificationBuilder:NotificationCompat.Builder


    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this,
            NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            getString(R.string.app_name), "App notification channel.")


        val channelId = "${applicationContext.packageName}-${applicationContext.getString(R.string.app_name)}"

         notificationBuilder = NotificationCompat.Builder(applicationContext, channelId).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle("Notify")
            setContentText("It works like charm.!")
            setStyle(NotificationCompat.BigTextStyle().bigText("Your Image has been downloaded successfully..!"))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        resultReceiver = intent!!.getParcelableExtra("receiver")
        return binder
    }

    inner class LocalBinder : Binder(){
        fun getService():LocalService = this@LocalService
    }

    fun downLoadImage(url:String){
        DownLoadThread(resultReceiver,url,applicationContext).start()
    }

    fun sendNotification(){
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1001, notificationBuilder.build())
    }

    class DownLoadThread(private val resultReceiver: ResultReceiver, private val url: String,
                         private val context: Context) :Thread(){

        override fun run() {
            var input: InputStream ?= null
            var output: OutputStream ?= null
            var connection : HttpURLConnection?= null
            val file = File(context.filesDir,"downloadedImage")
            val bundle = Bundle()
            var currentSize: Long = 0
            val imageSize: Long


            try {
                val urlConnection = URL(url)
                connection = urlConnection.openConnection() as HttpURLConnection
                connection.connect()
                imageSize = connection.contentLength.toLong()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return
                }

                input = connection.inputStream

                input.let {

                    output = FileOutputStream(file, false) as OutputStream?
                    val data = ByteArray(4096)
                    var count: Int
                    do {
                        count = input.read(data)
                        if (count > 1) {
                            output!!.write(data, 0, count)
                            currentSize += count.toLong()
                            bundle.putInt("progress", (100 * currentSize / imageSize).toInt())
                            resultReceiver.send(111, bundle)
                        } else {
                            break
                        }

                    } while (count != -1)

                    val newBundle:Bundle = Bundle()
                    if(file.exists()){
                        newBundle.putString("path",file.path)
                        newBundle.putInt("progress",100);
                        resultReceiver.send(112,newBundle)
                    }
                }
            }catch (e:Exception){
                Log.e("Exception",e.message)
            }finally {
                try {
                    output?.close()
                    input?.close()
                    connection?.disconnect()
                }catch (e:IOException){
                    Log.e("IO-exception",e.message)
                }
            }
        }
    }
}