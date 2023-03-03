package live.videosdk.android.hlsdemo.common.meeting.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import live.videosdk.android.hlsdemo.R
import live.videosdk.android.hlsdemo.common.utils.NetworkUtils
import live.videosdk.android.hlsdemo.common.meeting.activity.CreateOrJoinActivity
import live.videosdk.android.hlsdemo.common.meeting.activity.MainActivity
import live.videosdk.android.hlsdemo.common.utils.ResponseListener

class JoinMeetingFragment : Fragment() {

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_join_meeting, container, false)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etMeetingId = view.findViewById<EditText>(R.id.etMeetingId)
        val btnJoin = view.findViewById<Button>(R.id.btnJoin)
        val webcamEnabled = booleanArrayOf(false)
        val micEnabled = booleanArrayOf(false)
        val bundle = arguments
        if (bundle != null && bundle.getString("mode") == "VIEWER") {
            btnJoin.text = "Join as a viewer"
            (activity as CreateOrJoinActivity).setVisibilityOfPreview(View.GONE)
        } else {
            btnJoin.text = "Join as a speaker"
            (activity as CreateOrJoinActivity).setVisibilityOfPreview(View.VISIBLE)
        }

        btnJoin.setOnClickListener { v: View? ->
            val meetingId = etMeetingId!!.text.toString().trim { it <= ' ' }
            val pattern = Regex("\\w{4}-\\w{4}-\\w{4}")
            if ("" == etMeetingId.text.toString().trim { it <= ' ' }) {
                Toast.makeText(
                    context, "Please enter meeting ID",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!pattern.matches(meetingId)) {
                Toast.makeText(
                    context, "Please enter valid meeting ID",
                    Toast.LENGTH_SHORT
                ).show()
            } else if ("" == etName.text.toString()) {
                Toast.makeText(context, "Please Enter Name", Toast.LENGTH_SHORT).show()
            } else {
                val networkUtils = NetworkUtils(requireContext())
                if (networkUtils.isNetworkAvailable) {
                    networkUtils.getToken(object : ResponseListener<String?> {
                        override fun onResponse(token: String?) {
                            networkUtils.joinMeeting(
                                token,
                                etMeetingId.text.toString().trim { it <= ' ' },
                                object : ResponseListener<String?> {
                                    override fun onResponse(meetingId: String?) {
                                        val intent = Intent(
                                            activity as CreateOrJoinActivity,
                                            MainActivity::class.java
                                        )
                                        val mode =
                                            if (bundle != null) bundle.getString("mode") else "CONFERENCE"
                                        if (mode == "CONFERENCE") {
                                            webcamEnabled[0] =
                                                (activity as CreateOrJoinActivity).isWebcamEnabled()
                                            micEnabled[0] =
                                                (activity as CreateOrJoinActivity).isMicEnabled()
                                        }
                                        intent.putExtra("token", token)
                                        intent.putExtra("meetingId", meetingId)
                                        intent.putExtra("webcamEnabled", webcamEnabled[0])
                                        intent.putExtra("micEnabled", micEnabled[0])
                                        intent.putExtra(
                                            "participantName",
                                            etName.text.toString().trim { it <= ' ' })
                                        intent.putExtra("mode", mode)
                                        startActivity(intent)
                                        (activity as CreateOrJoinActivity).finish()
                                    }
                                })
                        }
                    })
                } else {
                    Toast.makeText(requireContext(),"No Internet Connection",Toast.LENGTH_LONG).show()
                }
            }
        }
        return view
    }
}