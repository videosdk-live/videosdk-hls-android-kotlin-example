package live.videosdk.android.hlsdemo.viewerMode

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.google.android.exoplayer2.ui.TrackSelectionView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.collect.ImmutableList
import live.videosdk.android.hlsdemo.R

/**
 * Dialog to select tracks.
 */
class TrackSelectionDialog : BottomSheetDialogFragment(),
    TrackSelectionView.TrackSelectionListener {
    override fun onTrackSelectionChanged(
        isDisabled: Boolean,
        overrides: Map<TrackGroup, TrackSelectionOverride>
    ) {
        this.isDisabled = isDisabled
        this.overrides = overrides
    }

    /**
     * Called when tracks are selected.
     */
    interface TrackSelectionListener {
        /**
         * Called when tracks are selected.
         *
         * @param trackSelectionParameters A [TrackSelectionParameters] representing the selected
         * tracks. Any manual selections are defined by [ ][TrackSelectionParameters.disabledTrackTypes] and [                                 ][TrackSelectionParameters.overrides].
         */
        fun onTracksSelected(trackSelectionParameters: TrackSelectionParameters?)
    }

    private var onClickListener: DialogInterface.OnClickListener? = null
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private var trackSelectionView: TrackSelectionView? = null
    private var trackGroups: List<Tracks.Group>? = null
    private var allowAdaptiveSelections = false
    private var allowMultipleOverrides = false

    /* package */
    var isDisabled = false

    /* package */
    var overrides: Map<TrackGroup, TrackSelectionOverride>? = null

    init {
        // Retain instance across activity re-creation to prevent losing access to init data.
        retainInstance = true
    }

    private fun init(
        tracks: Tracks,
        trackSelectionParameters: TrackSelectionParameters,
        allowAdaptiveSelections: Boolean,
        allowMultipleOverrides: Boolean,
        onClickListener: DialogInterface.OnClickListener,
        onDismissListener: DialogInterface.OnDismissListener?
    ) {
        this.onClickListener = onClickListener
        this.onDismissListener = onDismissListener
        for (i in SUPPORTED_TRACK_TYPES.indices) {
            val trackType = SUPPORTED_TRACK_TYPES[i]
            val trackGroups = ArrayList<Tracks.Group>()
            for (trackGroup in tracks.groups) {
                if (trackGroup.type == trackType) {
                    trackGroups.add(trackGroup)
                }
            }
            if (trackGroups.isNotEmpty()) {
                this.allowAdaptiveSelections = allowAdaptiveSelections
                this.allowMultipleOverrides = allowMultipleOverrides
                this.trackGroups = trackGroups
                isDisabled = trackSelectionParameters.disabledTrackTypes.contains(trackType)
                overrides = trackSelectionParameters.overrides
            }
        }
    }

    /**
     * if you want use dialog instead of BottomSheetDialog then change TrackSelectionDialog extends DialogFragment.
     * And use this method.
     */
    /*  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // We need to own the view to let tab layout work correctly on all API levels. We can't use
    // AlertDialog because it owns the view itself, so we use AppCompatDialog instead, themed using
    // the AlertDialog theme overlay with force-enabled title.

    AppCompatDialog dialog =
        new AppCompatDialog(getActivity(), R.style.TrackSelectionDialog);
    dialog.setTitle("title");
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }*/

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener!!.onDismiss(dialog)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val dialogView = inflater.inflate(R.layout.track_selection_dialog, container, false)
        val cancelButton = dialogView.findViewById<ImageView>(R.id.cancelBtn)
        val applyBtn = dialogView.findViewById<Button>(R.id.applyBtn)
        trackSelectionView = dialogView.findViewById(R.id.exo_track_selection_init_view)
        trackSelectionView!!.setShowDisableOption(true)
        trackSelectionView!!.setAllowMultipleOverrides(allowMultipleOverrides)
        trackSelectionView!!.setAllowAdaptiveSelections(allowAdaptiveSelections)
        trackSelectionView!!.init(
            trackGroups!!,
            isDisabled,
            overrides!!,  /* trackFormatComparator= */
            null,  /* listener= */
            this
        )
        cancelButton.setOnClickListener { dismiss() }
        applyBtn.setOnClickListener {
            onClickListener!!.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }
        return dialogView
    }

    companion object {
        val SUPPORTED_TRACK_TYPES = ImmutableList.of(C.TRACK_TYPE_VIDEO)

        /**
         * Returns whether a track selection dialog will have content to display if initialized with the
         * specified [Player].
         */
        fun willHaveContent(player: Player): Boolean {
            return willHaveContent(player.currentTracks)
        }

        /**
         * Returns whether a track selection dialog will have content to display if initialized with the
         * specified [Tracks].
         */
        private fun willHaveContent(tracks: Tracks): Boolean {
            for (trackGroup in tracks.groups) {
                if (SUPPORTED_TRACK_TYPES.contains(trackGroup.type)) {
                    return true
                }
            }
            return false
        }

        /**
         * Creates a dialog for a given [Player], whose parameters will be automatically updated
         * when tracks are selected.
         *
         * @param player            The [Player].
         * @param onDismissListener A [DialogInterface.OnDismissListener] to call when the dialog is
         * dismissed.
         */
        fun createForPlayer(
            player: Player, onDismissListener: DialogInterface.OnDismissListener?
        ): TrackSelectionDialog {
            return createForTracksAndParameters(
                player.currentTracks,
                player.trackSelectionParameters,  /* allowAdaptiveSelections= */
                false,  /* allowMultipleOverrides= */
                false,
                object : TrackSelectionListener {
                    override fun onTracksSelected(trackSelectionParameters: TrackSelectionParameters?) {
                        player.trackSelectionParameters = trackSelectionParameters!!
                    }
                },
                onDismissListener
            )
        }

        /**
         * Creates a dialog for given [Tracks] and [TrackSelectionParameters].
         *
         * @param tracks                   The [Tracks] describing the tracks to display.
         * @param trackSelectionParameters The initial [TrackSelectionParameters].
         * @param allowAdaptiveSelections  Whether adaptive selections (consisting of more than one track)
         * can be made.
         * @param allowMultipleOverrides   Whether tracks from multiple track groups can be selected.
         * @param trackSelectionListener   Called when tracks are selected.
         * @param onDismissListener        [DialogInterface.OnDismissListener] called when the dialog is
         */
        private fun createForTracksAndParameters(
            tracks: Tracks,
            trackSelectionParameters: TrackSelectionParameters,
            allowAdaptiveSelections: Boolean,
            allowMultipleOverrides: Boolean,
            trackSelectionListener: TrackSelectionListener,
            onDismissListener: DialogInterface.OnDismissListener?
        ): TrackSelectionDialog {
            val trackSelectionDialog = TrackSelectionDialog()
            trackSelectionDialog.init(
                tracks,
                trackSelectionParameters,
                allowAdaptiveSelections,
                allowMultipleOverrides,  /* onClickListener= */
                { dialog: DialogInterface?, which: Int ->
                    val builder = trackSelectionParameters.buildUpon()
                    for (i in SUPPORTED_TRACK_TYPES.indices) {
                        val trackType = SUPPORTED_TRACK_TYPES[i]
                        builder.setTrackTypeDisabled(trackType, trackSelectionDialog.isDisabled)
                        builder.clearOverridesOfType(trackType)
                        val overrides =
                            if (trackSelectionDialog.overrides == null) emptyMap() else trackSelectionDialog.overrides!!
                        for (override in overrides.values) {
                            builder.addOverride(override)
                        }
                    }
                    trackSelectionListener.onTracksSelected(builder.build())
                },
                onDismissListener
            )
            return trackSelectionDialog
        }
    }
}