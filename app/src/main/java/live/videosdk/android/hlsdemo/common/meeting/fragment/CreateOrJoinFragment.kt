package live.videosdk.android.hlsdemo.common.meeting.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import live.videosdk.android.hlsdemo.R
import live.videosdk.android.hlsdemo.common.utils.NetworkUtils
import live.videosdk.android.hlsdemo.common.meeting.activity.CreateOrJoinActivity
import live.videosdk.android.hlsdemo.common.utils.ResponseListener

class CreateOrJoinFragment : Fragment() {

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_or_join, container, false)
        view.findViewById<View>(R.id.btnCreateMeeting).setOnClickListener { v: View? ->
            val networkUtils = NetworkUtils(requireContext())
            if (networkUtils.isNetworkAvailable) {
                networkUtils.getToken(object : ResponseListener<String?> {
                    override fun onResponse(token: String?) {
                        networkUtils.createMeeting(token, object : ResponseListener<String?> {
                            override fun onResponse(meetingId: String?) {
                                (this@CreateOrJoinFragment.activity as CreateOrJoinActivity).setActionBar()
                                (this@CreateOrJoinFragment.activity as CreateOrJoinActivity).title =
                                    "Create a meeting"
                                val bundle = Bundle()
                                bundle.putString("meetingId", meetingId)
                                bundle.putString("token", token)
                                val createMeetingFragment = CreateMeetingFragment()
                                createMeetingFragment.arguments = bundle
                                val ft =
                                    this@CreateOrJoinFragment.requireFragmentManager().beginTransaction()
                                ft.replace(
                                    R.id.fragContainer,
                                    createMeetingFragment,
                                    "CreateMeeting"
                                )
                                ft.addToBackStack("CreateOrJoinFragment")
                                ft.commit()
                            }
                        })
                    }
                })
            }else{
                Toast.makeText(requireContext(),"No Internet Connection",Toast.LENGTH_LONG).show()
            }
        }
            view.findViewById<View>(R.id.btnJoinSpeakerMeeting).setOnClickListener { v: View? ->
                (activity as CreateOrJoinActivity).setActionBar()
                (activity as CreateOrJoinActivity).title = "Join as a speaker"
                joinMeetingFragment("CONFERENCE")
            }
            view.findViewById<View>(R.id.btnJoinViewerMeeting).setOnClickListener { v: View? ->
                (activity as CreateOrJoinActivity).setActionBar()
                (activity as CreateOrJoinActivity).title = "Join as a viewer"
                joinMeetingFragment("VIEWER")
            }


            // Inflate the layout for this fragment
            return view
        }

        private fun joinMeetingFragment(mode: String) {
            val bundle = Bundle()
            bundle.putString("mode", mode)
            val joinMeetingFragment = JoinMeetingFragment()
            joinMeetingFragment.arguments = bundle
            val ft = requireFragmentManager().beginTransaction()
            ft.replace(R.id.fragContainer, joinMeetingFragment, "joinMeetingFragment")
            ft.addToBackStack("CreateOrJoinFragment")
            ft.commit()
        }
    }