package live.videosdk.android.hlsdemo.viewerMode

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView.ControllerVisibilityListener
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import live.videosdk.android.hlsdemo.R
import live.videosdk.android.hlsdemo.common.reactions.DirectionGenerator
import live.videosdk.android.hlsdemo.common.utils.NetworkUtils
import live.videosdk.android.hlsdemo.common.reactions.ZeroGravityAnimation
import live.videosdk.android.hlsdemo.common.meeting.activity.CreateOrJoinActivity
import live.videosdk.android.hlsdemo.common.meeting.activity.MainActivity
import live.videosdk.android.hlsdemo.common.utils.ResponseListener
import live.videosdk.android.hlsdemo.speakerMode.manageTabs.SpeakerFragment
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.lib.JsonUtils
import live.videosdk.rtc.android.lib.PubSubMessage
import live.videosdk.rtc.android.listeners.MeetingEventListener
import live.videosdk.rtc.android.listeners.PubSubMessageListener
import live.videosdk.rtc.android.model.PubSubPublishOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ViewerFragment() : Fragment(), View.OnClickListener,
    ControllerVisibilityListener {
    private var meeting: Meeting? = null
    private var playerView: StyledPlayerView? = null
    private var controllerLayout: LinearLayout? = null
    private var player: ExoPlayer? = null
    private var isShowingTrackSelectionDialog = false
    private var btnQuality: Button? = null
    private var dataSourceFactory: DefaultHttpDataSource.Factory? = null
    private var trackSelectionParameters: TrackSelectionParameters? = null
    private var lastSeenTracks: Tracks? = null
    private var startAutoPlay = false
    private var startItemIndex = 0
    private var startPosition: Long = 0
    private var btnLeave: ImageView? = null
    private var exoPlay: ImageButton? = null
    private var exoPause: ImageButton? = null
    private var ended = false
    private var hlsStarted = false
    private var playbackHlsUrl: String? = null
    private var playerEventListener: PlayerEventListener? = null
    private var btnReactions: MaterialButton? = null
    private var btnAddToCart: MaterialButton? = null
    private var reactionsLayout: LinearLayout? = null
    private var emojiListener: PubSubMessageListener? = null
    private var coHostListener: PubSubMessageListener? = null
    private var viewerCount: MaterialButton? = null
    private var material_toolbar: MaterialToolbar? = null
    private var viewerEmojiHolder: FrameLayout? = null
    private var waitingLayout: LinearLayout? = null
    private var stopLiveStreamLayout: LinearLayout? = null
    private var liveActionsLayout: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_viewer, container, false)
        controllerLayout = view.findViewById(R.id.viewerControllers)
        exoPlay = view.findViewById(R.id.exoPlay)
        exoPause = view.findViewById(R.id.exoPause)
        btnQuality = view.findViewById(R.id.btnQuality)
        btnQuality!!.setOnClickListener(this)
        btnLeave = view.findViewById(R.id.btnViewerLeave)
        reactionsLayout = view.findViewById(R.id.reactionsLayout)
        btnReactions = view.findViewById(R.id.btnReactions)
        btnAddToCart = view.findViewById(R.id.btnAddToCart)
        material_toolbar = view.findViewById(R.id.material_toolbar)
        material_toolbar!!.bringToFront()
        viewerCount = view.findViewById(R.id.viewerCount)
        viewerEmojiHolder = view.findViewById(R.id.viewer_emoji_holder)
        waitingLayout = view.findViewById(R.id.waiting_layout)
        stopLiveStreamLayout = view.findViewById(R.id.stop_liveStream_layout)
        liveActionsLayout = view.findViewById(R.id.live_actions_layout)
        playerView = view.findViewById(R.id.player_view)
        playerView!!.setControllerVisibilityListener(this)
        playerView!!.requestFocus()
        if (savedInstanceState != null) {
            trackSelectionParameters = TrackSelectionParameters.fromBundle(
                savedInstanceState.getBundle(KEY_TRACK_SELECTION_PARAMETERS)!!
            )
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY)
            startItemIndex = savedInstanceState.getInt(KEY_ITEM_INDEX)
            startPosition = savedInstanceState.getLong(KEY_POSITION)
        } else {
            trackSelectionParameters = TrackSelectionParameters.Builder( /* context= */
                mContext!!
            ).build()
            clearStartPosition()
        }
        exoPause!!.setOnClickListener {
            if (player != null) {
                player!!.playWhenReady = false
            }
        }
        exoPlay!!.setOnClickListener(View.OnClickListener {
            if (player != null) {
                if (ended) {
                    player!!.seekTo(0)
                }
                player!!.playWhenReady = true
            }
        })
        if (meeting != null) {
            emojiListener =
                PubSubMessageListener { pubSubMessage ->
                    when (pubSubMessage.message) {
                        "loveEyes" -> showEmoji(resources.getDrawable(R.drawable.love_eyes_emoji))
                        "laughing" -> showEmoji(resources.getDrawable(R.drawable.laughing))
                        "thumbs_up" -> showEmoji(resources.getDrawable(R.drawable.thumbs_up))
                        "celebration" -> showEmoji(resources.getDrawable(R.drawable.celebration))
                        "clap" -> showEmoji(resources.getDrawable(R.drawable.clap))
                        "heart" -> showEmoji(resources.getDrawable(R.drawable.heart))
                    }
                }
            // notify user of any new emoji
            meeting!!.pubSub.subscribe("emoji", emojiListener)
            coHostListener = PubSubMessageListener { pubSubMessage: PubSubMessage ->
                if ((pubSubMessage.message == meeting!!.localParticipant.id)) {
                    showCoHostRequestDialog(pubSubMessage.senderName)
                }
            }
            // notify user of any new emoji
            meeting!!.pubSub.subscribe("coHost", coHostListener)
        }
        ActionListener(view)
        val networkUtils = NetworkUtils(requireContext())
        if (networkUtils.isNetworkAvailable) {
            networkUtils.getToken(object : ResponseListener<String?> {
                override fun onResponse(token: String?) {
                    networkUtils.checkActiveHls(
                        token,
                        meeting!!.meetingId,
                        object : ResponseListener<String?> {
                            override fun onResponse(url: String?) {
                                playbackHlsUrl = url
                                initializePlayer()
                                showViewerCount()
                                hlsStarted = true
                                waitingLayout!!.visibility = View.GONE
                                liveActionsLayout!!.visibility = View.VISIBLE
                                playerView!!.visibility = View.VISIBLE
                                viewerEmojiHolder!!.visibility = View.VISIBLE
                                controllerLayout!!.visibility = View.VISIBLE
                                btnReactions!!.isEnabled = true
                                btnAddToCart!!.isEnabled = true
                            }
                        })
                }
            })
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is Activity) {
            mActivity = context
            meeting = (mActivity as MainActivity?)!!.getMeeting()
        }
    }

    private fun ActionListener(view: View) {
        val pubSubPublishOptions = PubSubPublishOptions()
        pubSubPublishOptions.isPersist = false
        btnReactions!!.setOnClickListener { v: View? ->
            reactionsLayout!!.visibility = View.VISIBLE
        }
        view.findViewById<View>(R.id.loveEyes).setOnClickListener { v: View? ->
            showEmoji(resources.getDrawable(R.drawable.love_eyes_emoji))
            meeting!!.pubSub.publish("emoji", "love_eyes", pubSubPublishOptions)
            reactionsLayout!!.visibility = View.GONE
        }
        view.findViewById<View>(R.id.laughing).setOnClickListener { v: View? ->
            meeting!!.pubSub.publish("emoji", "laughing", pubSubPublishOptions)
            reactionsLayout!!.visibility = View.GONE
        }
        view.findViewById<View>(R.id.thumbs_up).setOnClickListener { v: View? ->
            meeting!!.pubSub.publish("emoji", "thumbs_up", pubSubPublishOptions)
            reactionsLayout!!.visibility = View.GONE
        }
        view.findViewById<View>(R.id.celebration).setOnClickListener { v: View? ->
            meeting!!.pubSub.publish("emoji", "celebration", pubSubPublishOptions)
            reactionsLayout!!.visibility = View.GONE
        }
        view.findViewById<View>(R.id.clap).setOnClickListener { v: View? ->
            meeting!!.pubSub.publish("emoji", "clap", pubSubPublishOptions)
            reactionsLayout!!.visibility = View.GONE
        }
        view.findViewById<View>(R.id.heart).setOnClickListener { v: View? ->
            meeting!!.pubSub.publish("emoji", "heart", pubSubPublishOptions)
            reactionsLayout!!.visibility = View.GONE
        }
        btnLeave!!.setOnClickListener { (mActivity as MainActivity?)!!.showLeaveDialog() }
        btnAddToCart!!.setOnClickListener { showProducts() }
    }

    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingLeft() {
            if (isAdded) {
                val intents = Intent(
                    mContext,
                    CreateOrJoinActivity::class.java
                )
                intents.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
                startActivity(intents)
                mActivity!!.finish()
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        override fun onHlsStateChanged(HlsState: JSONObject) {
            if (HlsState.has("status")) {
                try {
                    if ((HlsState.getString("status") == "HLS_PLAYABLE") && HlsState.has("playbackHlsUrl")) {
                        playbackHlsUrl = HlsState.getString("playbackHlsUrl")
                        initializePlayer()
                        showViewerCount()
                        hlsStarted = true
                        waitingLayout!!.visibility = View.GONE
                        liveActionsLayout!!.visibility = View.VISIBLE
                        playerView!!.visibility = View.VISIBLE
                        viewerEmojiHolder!!.visibility = View.VISIBLE
                        controllerLayout!!.visibility = View.VISIBLE
                        btnReactions!!.isEnabled = true
                        btnAddToCart!!.isEnabled = true
                    }
                    if ((HlsState.getString("status") == "HLS_STOPPED")) {
                        if (hlsStarted) {
                            releasePlayer()
                            clearStartPosition()
                            hlsStarted = false
                            stopLiveStreamLayout!!.visibility = View.VISIBLE
                            playerView!!.visibility = View.GONE
                            viewerEmojiHolder!!.visibility = View.GONE
                            controllerLayout!!.visibility = View.GONE
                            material_toolbar!!.visibility = View.VISIBLE
                            liveActionsLayout!!.visibility = View.GONE
                            btnReactions!!.isEnabled = false
                            btnAddToCart!!.isEnabled = false
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onParticipantJoined(participant: Participant) {
            showViewerCount()
        }

        override fun onParticipantLeft(participant: Participant) {
            showViewerCount()
        }
    }

    override fun onResume() {
        super.onResume()
        meeting!!.addEventListener(meetingEventListener)
        if (hlsStarted && (Build.VERSION.SDK_INT <= 23 || player == null)) {
            initializePlayer()
            if (playerView != null) {
                playerView!!.onResume()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            if (playerView != null) {
                playerView!!.onPause()
            }
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) {
            if (playerView != null) {
                playerView!!.onPause()
            }
            releasePlayer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateTrackSelectorParameters()
        updateStartPosition()
        outState.putBundle(KEY_TRACK_SELECTION_PARAMETERS, trackSelectionParameters!!.toBundle())
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay)
        outState.putInt(KEY_ITEM_INDEX, startItemIndex)
        outState.putLong(KEY_POSITION, startPosition)
    }

    override fun onClick(view: View) {
        if ((view === btnQuality
                    && !isShowingTrackSelectionDialog)
        ) {
            isShowingTrackSelectionDialog = true
            val trackSelectionDialog = TrackSelectionDialog.createForPlayer(
                (player)!!
            )  /* onDismissListener= */
            { dismissedDialog -> isShowingTrackSelectionDialog = false }
            trackSelectionDialog.show((fragmentManager)!!,  /* tag= */null)
        }
    }

    override fun onVisibilityChanged(visibility: Int) {
        controllerLayout!!.visibility = visibility
        material_toolbar!!.visibility = visibility
        if (reactionsLayout!!.visibility == View.VISIBLE) reactionsLayout!!.visibility =
            visibility
    }

    private fun initializePlayer(): Boolean {
        if (player == null) {
            lastSeenTracks = Tracks.EMPTY
            dataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory!!).createMediaSource(
                MediaItem.fromUri(Uri.parse(playbackHlsUrl))
            )
            val playerBuilder = ExoPlayer.Builder( /* context= */(mContext)!!)
            player = playerBuilder.build()
            player!!.trackSelectionParameters = (trackSelectionParameters)!!
            playerEventListener = PlayerEventListener()
            player!!.addListener((playerEventListener)!!)
            player!!.addAnalyticsListener(EventLogger())
            player!!.setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
            player!!.playWhenReady = startAutoPlay
            player!!.setMediaSource(mediaSource)
            playerView!!.player = player
        }
        val haveStartPosition = startItemIndex != C.INDEX_UNSET
        if (haveStartPosition) {
            player!!.seekTo(startItemIndex, startPosition)
        }
        player!!.prepare()
        updateButtonVisibility()
        return true
    }

    private fun releasePlayer() {
        if (player != null) {
            updateTrackSelectorParameters()
            updateStartPosition()
            player!!.removeListener((playerEventListener)!!)
            player!!.release()
            player = null
            dataSourceFactory = null
            playerView!!.player = null
        }
    }

    private fun updateTrackSelectorParameters() {
        if (player != null) {
            trackSelectionParameters = player!!.trackSelectionParameters
        }
    }

    private fun updateStartPosition() {
        if (player != null) {
            startAutoPlay = player!!.playWhenReady
            startItemIndex = player!!.currentMediaItemIndex
            startPosition = Math.max(0, player!!.contentPosition)
        }
    }

    private fun clearStartPosition() {
        startAutoPlay = true
        startItemIndex = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }

    private fun updateButtonVisibility() {
        btnQuality!!.isEnabled = player != null
    }

    private fun showControls() {
        controllerLayout!!.visibility = View.VISIBLE
        material_toolbar!!.visibility = View.VISIBLE
    }

    private inner class PlayerEventListener() : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
            if (playbackState == Player.STATE_ENDED) {
                exoPause!!.visibility = View.GONE
                exoPlay!!.visibility = View.VISIBLE
                ended = true
                showControls()
            } else {
                ended = false
            }
            updateButtonVisibility()
        }

        override fun onPlayerError(error: PlaybackException) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                player!!.seekToDefaultPosition()
                player!!.prepare()
            } else {
                updateButtonVisibility()
                showControls()
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            updateButtonVisibility()
            if (tracks === lastSeenTracks) {
                return
            }
            if (tracks.isTypeSelected(C.TRACK_TYPE_VIDEO)) {
                for (trackGroup: Tracks.Group in tracks.groups) {
                    // Group level information.
                    if (trackGroup.mediaTrackGroup.type == 2) {
                        for (i in 0 until trackGroup.length) {
                            // Individual track information.
                            val isSelected = trackGroup.isTrackSelected(i)
                            val trackFormat = trackGroup.getTrackFormat(i)
                            if (isSelected) btnQuality!!.text = trackFormat.width.toString().plus("p")
                        }
                    }
                }
            }
            lastSeenTracks = tracks
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                exoPause!!.visibility = View.VISIBLE
                exoPlay!!.visibility = View.GONE
            } else {
                exoPause!!.visibility = View.GONE
                exoPlay!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        mContext = null
        mActivity = null
        playbackHlsUrl = null
        releasePlayer()
        clearStartPosition()
        if (meeting != null) {
            meeting!!.pubSub.unsubscribe("emoji", emojiListener)
            meeting!!.pubSub.unsubscribe("coHost", coHostListener)
            meeting!!.removeAllListeners()
            meeting = null
        }
        super.onDestroy()
    }

    private fun showProducts() {
        val bottomSheetDialog = BottomSheetDialog((mActivity)!!)
        val v3 = LayoutInflater.from(mActivity).inflate(R.layout.products_layout, null)
        bottomSheetDialog.setContentView(v3)
        val recyclerView = v3.findViewById<RecyclerView>(R.id.productRcv)
        val close = v3.findViewById<ImageView>(R.id.ic_product_close)
        bottomSheetDialog.show()
        close.setOnClickListener { bottomSheetDialog.dismiss() }
        val jsonArray = JSONArray()
        val topDetails = JSONObject()
        JsonUtils.jsonPut(topDetails, "productImage", R.drawable.blackcroptop)
        JsonUtils.jsonPut(topDetails, "productName", "Black Top")
        JsonUtils.jsonPut(topDetails, "productPrice", "23")
        val jeansDetails = JSONObject()
        JsonUtils.jsonPut(jeansDetails, "productImage", R.drawable.jeans)
        JsonUtils.jsonPut(jeansDetails, "productName", "Blue Jeans")
        JsonUtils.jsonPut(jeansDetails, "productPrice", "11")
        jsonArray.put(topDetails)
        jsonArray.put(jeansDetails)
        recyclerView.adapter = ProductsAdapter(jsonArray)
    }

    private fun showEmoji(drawable: Drawable?) {
        // You can change the number of emojis that will be flying on screen
        for (i in 0..4) {
            flyObject(
                drawable,
                3000,
                DirectionGenerator.Direction.BOTTOM,
                DirectionGenerator.Direction.TOP,
                1f
            )
        }
    }

    private fun flyObject(
        drawable: Drawable?,
        duration: Int,
        from: DirectionGenerator.Direction?,
        to: DirectionGenerator.Direction?,
        scale: Float
    ) {
        val animation = ZeroGravityAnimation()
        animation.setCount(1)
        animation.setScalingFactor(scale)
        animation.setOriginationDirection((from)!!)
        animation.setDestinationDirection((to)!!)
        animation.setImage(drawable)
        animation.setDuration(duration)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })
        viewerEmojiHolder!!.bringToFront()
        animation.play(mActivity, viewerEmojiHolder)
    }

    private fun showCoHostRequestDialog(name: String) {
        val alertDialog =
            MaterialAlertDialogBuilder((mContext)!!, R.style.AlertDialogCustom).create()
        alertDialog.setCancelable(false)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.cohost_request_dialog, null)
        alertDialog.setView(dialogView)
        val message = dialogView.findViewById<TextView>(R.id.txtMessage1)
        val message2 = dialogView.findViewById<TextView>(R.id.txtMessage2)
        message.text = "$name has requested you to"
        message2.text = "join as speaker"
        val acceptBtn = dialogView.findViewById<Button>(R.id.acceptBtn)
        acceptBtn.setOnClickListener {
            meeting!!.changeMode("CONFERENCE")
            alertDialog.dismiss()
            val pubSubPublishOptions: PubSubPublishOptions = PubSubPublishOptions()
            pubSubPublishOptions.isPersist = false
            meeting!!.pubSub.publish("coHostRequestAnswer", "accept", pubSubPublishOptions)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainLayout, SpeakerFragment(), "MainFragment")
                .commit()
        }
        val declineBtn = dialogView.findViewById<Button>(R.id.declineBtn)
        declineBtn.setOnClickListener {
            alertDialog.dismiss()
            val pubSubPublishOptions: PubSubPublishOptions = PubSubPublishOptions()
            pubSubPublishOptions.isPersist = false
            meeting!!.pubSub.publish("coHostRequestAnswer", "decline", pubSubPublishOptions)
        }
        alertDialog.show()
    }

    private fun showViewerCount() {
        var viewerCount = 1
        val participants = meeting!!.participants
        for (entry: Map.Entry<String, Participant> in participants.entries) {
            val participant = entry.value
            if ((participant.mode == "VIEWER")) {
                viewerCount++
            }
        }
        this.viewerCount!!.text = viewerCount.toString()
    }

    companion object {
        private const val KEY_TRACK_SELECTION_PARAMETERS = "track_selection_parameters"
        private const val KEY_ITEM_INDEX = "item_index"
        private const val KEY_POSITION = "position"
        private const val KEY_AUTO_PLAY = "auto_play"
        private var mActivity: Activity? = null
        private var mContext: Context? = null
    }
}