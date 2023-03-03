package live.videosdk.android.hlsdemo.common.meeting.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import live.videosdk.android.hlsdemo.R
import live.videosdk.android.hlsdemo.common.meeting.fragment.CreateOrJoinFragment
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.lib.PeerConnectionUtils
import org.webrtc.*

class CreateOrJoinActivity : AppCompatActivity() {

    private var micEnabled = false
    private var webcamEnabled = false

    private var btnMic: FloatingActionButton? = null
    private  var btnWebcam:FloatingActionButton? = null
    private var joinView: VideoView? = null

    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null

    private var videoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var initializationOptions: PeerConnectionFactory.InitializationOptions? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var videoSource: VideoSource? = null

    var permissionsGranted: Boolean = false
    private val permissionHandler: PermissionHandler = object : PermissionHandler() {
        override fun onGranted() {
            permissionsGranted = true
            micEnabled = true
            btnMic!!.setImageResource(R.drawable.ic_mic_on)
            changeFloatingActionButtonLayout(btnMic, micEnabled)
            webcamEnabled = true
            btnWebcam!!.setImageResource(R.drawable.ic_video_camera)
            changeFloatingActionButtonLayout(btnWebcam, webcamEnabled)
            updateCameraView()
        }

        override fun onDenied(context: Context?, deniedPermissions: ArrayList<String?>?) {
            super.onDenied(context, deniedPermissions)
            Toast.makeText(
                this@CreateOrJoinActivity,
                "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT
            ).show()
        }

        override fun onBlocked(context: Context?, blockedList: ArrayList<String?>?): Boolean {
            Toast.makeText(
                this@CreateOrJoinActivity,
                "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT
            ).show()
            return super.onBlocked(context, blockedList)
        }
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_or_join)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.title = "HLS Demo"
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(false)
        btnMic = findViewById(R.id.btnMic)
        btnWebcam = findViewById(R.id.btnWebcam)
        joinView = findViewById(R.id.joiningView)
        checkPermissions()
        val fragContainer = findViewById<View>(R.id.fragContainer) as LinearLayout
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        supportFragmentManager.beginTransaction()
            .add(R.id.fragContainer, CreateOrJoinFragment(), "CreateOrJoinFragment").commit()
        fragContainer.addView(ll)
        btnMic!!.setOnClickListener{ toggleMic() }
        btnWebcam!!.setOnClickListener { toggleWebcam() }
    }

    fun isMicEnabled(): Boolean {
        return micEnabled
    }

    fun isWebcamEnabled(): Boolean {
        return webcamEnabled
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setBackStackChangedListener()
        }
        return super.onOptionsItemSelected(item)
    }

    fun setActionBar() {
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
        } else {
            throw NullPointerException("Something went wrong")
        }
    }

    private fun setTitle(title: String) {
        if (actionBar != null && title.isNotEmpty()) {
            actionBar!!.title = title
        }
    }

    private fun checkPermissions() {
        val permissionList: MutableList<String> = ArrayList()
        permissionList.add(Manifest.permission.INTERNET)
        permissionList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        permissionList.add(Manifest.permission.RECORD_AUDIO)
        permissionList.add(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) permissionList.add(
            Manifest.permission.BLUETOOTH_CONNECT
        )

        val rationale = "Please provide permissions"
        val options =
            Permissions.Options().setRationaleDialogTitle("Info").setSettingsDialogTitle("Warning")
        Permissions.check(
            this,
            permissionList.toTypedArray(),
            rationale,
            options,
            permissionHandler
        )
    }

    private fun changeFloatingActionButtonLayout(btn: FloatingActionButton?, enabled: Boolean) {
        if (enabled) {
            btn!!.setColorFilter(Color.BLACK)
            btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
        } else {
            btn!!.setColorFilter(Color.WHITE)
            btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
        }
    }

    private fun toggleMic() {
        if (!permissionsGranted) {
            checkPermissions()
            return
        }
        micEnabled = !micEnabled
        if (micEnabled) {
            btnMic!!.setImageResource(R.drawable.ic_mic_on)
        } else {
            btnMic!!.setImageResource(R.drawable.ic_mic_off)
        }
        changeFloatingActionButtonLayout(btnMic, micEnabled)
    }

    private fun toggleWebcam() {
        if (!permissionsGranted) {
            checkPermissions()
            return
        }
        webcamEnabled = !webcamEnabled
        if (webcamEnabled) {
            btnWebcam!!.setImageResource(R.drawable.ic_video_camera)
        } else {
            btnWebcam!!.setImageResource(R.drawable.ic_video_camera_off)
        }
        updateCameraView()
        changeFloatingActionButtonLayout(btnWebcam, webcamEnabled)
    }


    private fun updateCameraView() {
        if (webcamEnabled) {
            // create PeerConnectionFactory
            initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initializationOptions)
            peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
            joinView!!.setMirror(true)
            val surfaceTextureHelper: SurfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", PeerConnectionUtils.getEglContext())

            // create VideoCapturer
            videoCapturer = createCameraCapturer()
            videoSource = peerConnectionFactory!!.createVideoSource(videoCapturer!!.isScreencast)
            videoCapturer!!.initialize(
                surfaceTextureHelper,
                applicationContext,
                videoSource!!.capturerObserver
            )
            videoCapturer!!.startCapture(480, 640, 30)

            // create VideoTrack
            videoTrack = peerConnectionFactory!!.createVideoTrack("100", videoSource)

            // display in localView
            joinView!!.addTrack(videoTrack)
        } else {
            joinView!!.removeTrack()
            joinView!!.releaseSurfaceViewRenderer()
        }
    }


    private fun createCameraCapturer(): VideoCapturer? {
        val enumerator = Camera1Enumerator(false)
        val deviceNames: Array<String> = enumerator.deviceNames

        // First, try to find front facing camera
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Front facing camera not found, try something else
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    override fun onDestroy() {
        joinView!!.removeTrack()
        joinView!!.releaseSurfaceViewRenderer()
        closeCapturer()
        super.onDestroy()
    }

    override fun onPause() {
        joinView!!.removeTrack()
        joinView!!.releaseSurfaceViewRenderer()
        closeCapturer()
        super.onPause()
    }

    override fun onRestart() {
        updateCameraView()
        setVisibilityOfPreview(View.GONE)
        super.onRestart()
    }

    private fun closeCapturer() {
        if (videoCapturer != null) {
            try {
                videoCapturer!!.stopCapture()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            videoCapturer!!.dispose()
            videoCapturer = null
        }
        if (videoSource != null) {
            videoSource!!.dispose()
            videoSource = null
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory!!.stopAecDump()
            peerConnectionFactory!!.dispose()
            peerConnectionFactory = null
        }
        PeerConnectionFactory.stopInternalTracingCapture()
        PeerConnectionFactory.shutdownInternalTracer()
    }


    fun setVisibilityOfPreview(visibility: Int) {
        findViewById<View>(R.id.previewLayout).visibility = visibility
    }

    override fun onBackPressed() {
        setBackStackChangedListener()
    }

    private fun setBackStackChangedListener() {
        fragmentManager.addOnBackStackChangedListener {
            if (fragmentManager.backStackEntryCount > 0) {
                actionBar!!.setDisplayHomeAsUpEnabled(true)
            } else {
                actionBar!!.setDisplayHomeAsUpEnabled(false)
                setVisibilityOfPreview(View.GONE)
                setTitle("HLS Demo")
            }
            toolbar!!.invalidate()
        }
        fragmentManager.popBackStack()
    }

}