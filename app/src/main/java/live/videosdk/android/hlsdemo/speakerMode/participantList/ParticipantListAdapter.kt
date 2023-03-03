package live.videosdk.android.hlsdemo.speakerMode.participantList

import android.content.Context
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.listeners.PubSubMessageListener
import live.videosdk.rtc.android.listeners.MeetingEventListener
import org.json.JSONObject
import live.videosdk.android.hlsdemo.R
import live.videosdk.rtc.android.listeners.ParticipantEventListener
import live.videosdk.rtc.android.model.PubSubPublishOptions
import android.os.Build
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Stream
import java.util.ArrayList

class ParticipantListAdapter(
    items: ArrayList<Participant?>?,
    private val meeting: Meeting,
    private val context: Context
) : RecyclerView.Adapter<ParticipantListAdapter.ViewHolder>() {
    private var participants = ArrayList<Participant?>()
    private var coHostAnswerListener: PubSubMessageListener? = null

    init {
        participants.add(meeting.localParticipant)
        participants.addAll(items!!)
        meeting.addEventListener(object : MeetingEventListener() {
            override fun onParticipantJoined(participant: Participant) {
                super.onParticipantJoined(participant)
                participants = ArrayList()
                participants.add(meeting.localParticipant)
                participants.addAll(allParticipants)
                notifyDataSetChanged()
            }

            override fun onParticipantLeft(participant: Participant) {
                super.onParticipantLeft(participant)
                participants = ArrayList()
                participants.add(meeting.localParticipant)
                participants.addAll(allParticipants)
                notifyDataSetChanged()
            }

            override fun onParticipantModeChanged(data: JSONObject) {
                participants = ArrayList()
                participants.add(meeting.localParticipant)
                participants.addAll(allParticipants)
                notifyDataSetChanged()
            }
        })
    }

    private val allParticipants: ArrayList<Participant?>
        get() {
            val participantList: ArrayList<Participant?> = ArrayList<Participant?>()
            var participants: Iterator<Participant> = meeting.participants.values.iterator()
            for (i in 0 until meeting.participants.size) {
                val participant = participants.next()
                if (participant.mode == "CONFERENCE") {
                    participantList.add(participant)
                }
            }
            participants = meeting.participants.values.iterator()
            for (i in 0 until meeting.participants.size) {
                val participant = participants.next()
                if (participant.mode == "VIEWER") {
                    participantList.add(participant)
                }
            }
            return participantList
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_participant_list_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = participants[position]
        if (participants[position]!!.isLocal) {
            holder.participantName.text = "You"
        } else {
            var participantName = participants[position]!!.displayName
            if (participantName.length > 10) {
                participantName = participantName.substring(0, 10) + ".."
            }
            holder.participantName.text = participantName
        }
        holder.participantNameFirstLetter.text =
            participants[position]!!.displayName.subSequence(0, 1)
        if (participant!!.mode == "VIEWER") {
            holder.micStatus.visibility = View.GONE
            holder.camStatus.visibility = View.GONE
            holder.hostIndicator.visibility = View.GONE
        } else {
            holder.micStatus.visibility = View.VISIBLE
            holder.camStatus.visibility = View.VISIBLE
            holder.hostIndicator.visibility = View.VISIBLE
        }
        for ((_, stream) in participant.streams) {
            if (stream.kind.equals("video", ignoreCase = true)) {
                holder.camStatus.setImageResource(R.drawable.ic_video_camera)
                holder.camStatus.layoutParams.width = 60
                holder.camStatus.requestLayout()
                break
            }
            if (stream.kind.equals("audio", ignoreCase = true)) {
                holder.micStatus.setImageResource(R.drawable.ic_mic_on)
                holder.micStatus.layoutParams.width = 60
                holder.micStatus.requestLayout()
            }
        }
        participant.addEventListener(object : ParticipantEventListener() {
            override fun onStreamEnabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.camStatus.setImageResource(R.drawable.ic_video_camera)
                    holder.camStatus.layoutParams.width = 60
                    holder.camStatus.requestLayout()
                }
                if (stream.kind.equals("audio", ignoreCase = true)) {
                    holder.micStatus.setImageResource(R.drawable.ic_mic_on)
                    holder.micStatus.layoutParams.width = 60
                    holder.micStatus.requestLayout()
                }
            }

            override fun onStreamDisabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.camStatus.setImageResource(R.drawable.ic_webcam_off_style)
                    holder.camStatus.layoutParams.width = 110
                    holder.camStatus.requestLayout()
                }
                if (stream.kind.equals("audio", ignoreCase = true)) {
                    holder.micStatus.setImageResource(R.drawable.ic_mic_off_style)
                    holder.micStatus.layoutParams.width = 110
                    holder.micStatus.requestLayout()
                }
            }
        })
        if (participant.isLocal) {
            holder.btnParticipantMoreOptions.visibility = View.GONE
        }

        //
        holder.btnParticipantMoreOptions.setOnClickListener { v: View? ->
            showPopup(
                holder,
                participant
            )
        }
        coHostAnswerListener = PubSubMessageListener { pubSubMessage ->
            if (pubSubMessage.senderId == participant.id) {
                if (pubSubMessage.message == "decline") {
                    Toast.makeText(
                        context,
                        pubSubMessage.senderName + " has declined your request.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (pubSubMessage.message == "accept") {
                    Toast.makeText(
                        context,
                        pubSubMessage.senderName + " has accept your request.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                holder.requestedIndicator.visibility = View.GONE
                holder.btnParticipantMoreOptions.isEnabled = true
            }
        }

        // notify user when viewer answer the request
        meeting.pubSub.subscribe("coHostRequestAnswer", coHostAnswerListener)
    }

    private fun showPopup(holder: ViewHolder, participant: Participant?) {
        val popup = PopupMenu(context, holder.btnParticipantMoreOptions)
        if (participant!!.mode == "VIEWER") {
            popup.menu.add("Add as a co-host")
        } else {
            holder.micStatus.visibility = View.VISIBLE
            holder.camStatus.visibility = View.VISIBLE
            holder.hostIndicator.visibility = View.VISIBLE
            popup.menu.add("Remove from co-host")
        }
        popup.menu.add("Remove Participant")
        popup.setOnMenuItemClickListener { item: MenuItem ->
            if (item.toString() == "Remove Participant") {
                participant.remove()
                return@setOnMenuItemClickListener true
            } else if (item.toString() == "Add as a co-host") {
                val pubSubPublishOptions = PubSubPublishOptions()
                pubSubPublishOptions.isPersist = false
                meeting.pubSub.publish("coHost", participant.id, pubSubPublishOptions)
                holder.requestedIndicator.visibility = View.VISIBLE
                holder.btnParticipantMoreOptions.isEnabled = false
                return@setOnMenuItemClickListener true
            } else if (item.toString() == "Remove from co-host") {
                val pubSubPublishOptions = PubSubPublishOptions()
                pubSubPublishOptions.isPersist = false
                meeting.pubSub.publish("removeCoHost", participant.id, pubSubPublishOptions)
                return@setOnMenuItemClickListener true
            }
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.gravity = Gravity.END
        }
        popup.show()
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var participantName: TextView
        var micStatus: ImageView
        var camStatus: ImageView
        var participantNameFirstLetter: TextView
        var btnParticipantMoreOptions: ImageButton
        var hostIndicator: TextView
        var requestedIndicator: TextView

        init {
            participantName = itemView.findViewById(R.id.participant_Name)
            micStatus = itemView.findViewById(R.id.mic_status)
            camStatus = itemView.findViewById(R.id.cam_status)
            btnParticipantMoreOptions = itemView.findViewById(R.id.btnParticipantMoreOptions)
            participantNameFirstLetter = itemView.findViewById(R.id.participantNameFirstLetter)
            hostIndicator = itemView.findViewById(R.id.hostIndicator)
            requestedIndicator = itemView.findViewById(R.id.requestedIndicator)
        }
    }
}