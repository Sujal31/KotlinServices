package anroid.threadhandler.com.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver


class CustomResultReceiver(appReceiver: AppReceive) : ResultReceiver(Handler()) {

    var appReceive: AppReceive? = appReceiver

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        super.onReceiveResult(resultCode, resultData)
        resultData?.let { appReceive?.onReceiveResult(resultCode, it) }
    }

    interface AppReceive{
        fun onReceiveResult(resultCode:Int,resultData: Bundle)
    }
}