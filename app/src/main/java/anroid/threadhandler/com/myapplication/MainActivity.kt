package anroid.threadhandler.com.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),CustomResultReceiver.AppReceive {

    private lateinit var mService: LocalService
    private var mBound : Boolean =  false

    private val connection = object : ServiceConnection{

        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as LocalService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener(View.OnClickListener { if(mBound){ mService.downLoadImage("https://drive.google.com/uc?export=download&id=0Bz2rNlhjsD37M2hpbEJRMlZLSGotUFZRRF9NbHhVcHFqeWpZ")} })

    }


    override fun onStart() {
        super.onStart()
        // Bind to Local service
        val intent = Intent(this,LocalService::class.java)
        val mResultReceiver =  CustomResultReceiver(this)
        intent.putExtra("receiver", mResultReceiver);
        bindService(intent,connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound =  false
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        progressBar.visibility = View.VISIBLE
        progressBar.setProgress(resultData.get("progress") as Int)

        if (resultCode == 111 && resultData.get("progress") == 100){
            progressBar.visibility = View.GONE
        }

        if(resultCode == 112){
            val bitmap:Bitmap = BitmapFactory.decodeFile(resultData.getString("path"))
            imageView.setImageBitmap(bitmap)

            if(resultData.get("progress") == 100){
                progressBar.visibility = View.GONE
                mService.sendNotification()
            }

        }

    }

}
