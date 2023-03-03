package live.videosdk.android.hlsdemo.common.utils

import android.app.Application
import com.androidnetworking.AndroidNetworking
import live.videosdk.rtc.android.VideoSDK

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        VideoSDK.initialize(applicationContext)
        AndroidNetworking.initialize(applicationContext)
    }
}