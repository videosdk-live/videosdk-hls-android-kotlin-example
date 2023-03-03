package live.videosdk.android.hlsdemo.common.utils

interface ResponseListener<T> {
    fun onResponse(response: T)
}