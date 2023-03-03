package live.videosdk.android.hlsdemo.speakerMode.participantList

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import live.videosdk.android.hlsdemo.R
import live.videosdk.android.hlsdemo.common.meeting.activity.MainActivity
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant

class ParticipantListFragment : Fragment() {
    private var mContext: Context? = null
    private var mActivity: Activity? = null
    private var meeting: Meeting? = null
    private var participantsListView: RecyclerView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is Activity) {
            mActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_participant, container, false)
        participantsListView = view.findViewById(R.id.rvParticipantsLinearView)
        meeting = (activity as MainActivity?)!!.getMeeting()
        val participants =
            allParticipants
        participantsListView!!.layoutManager = LinearLayoutManager(mContext)
        participantsListView!!.adapter = ParticipantListAdapter(participants, meeting!!, mActivity!!)
        val dividerItemDecoration =
            DividerItemDecoration(participantsListView!!.context, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.divider))
        participantsListView!!.addItemDecoration(dividerItemDecoration)
        return view
    }

    private val allParticipants: ArrayList<Participant?>
        get() {
            val participantList: ArrayList<Participant?> = ArrayList()
            if (meeting != null) {
                val participants: Iterator<Participant> = meeting!!.participants.values.iterator()
                for (i in 0 until meeting!!.participants.size) {
                    val participant = participants.next()
                    if (participant.mode == "CONFERENCE") {
                        participantList.add(participant)
                    }
                }
                val participantIterator: Iterator<Participant> =
                    meeting!!.participants.values.iterator()
                for (i in 0 until meeting!!.participants.size) {
                    val participant = participantIterator.next()
                    if (participant.mode == "VIEWER") {
                        participantList.add(participant)
                    }
                }
            }
            return participantList
        }
}