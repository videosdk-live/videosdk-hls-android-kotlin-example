package live.videosdk.android.hlsdemo.common.meeting.fragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import live.videosdk.android.hlsdemo.R
import live.videosdk.android.hlsdemo.common.meeting.activity.CreateOrJoinActivity
import live.videosdk.android.hlsdemo.common.meeting.activity.MainActivity

class CreateMeetingFragment : Fragment() {

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_meeting, container, false)
        val etName = view.findViewById<EditText>(R.id.etName)
        val txtMeetingId = view.findViewById<TextView>(R.id.txtMeetingId)
        var meetingId: String? = null
        var token: String? = null
        val btnCreate = view.findViewById<Button>(R.id.btnCreate)
        (activity as CreateOrJoinActivity).setVisibilityOfPreview(View.VISIBLE)
        val bundle = arguments
        if (bundle != null) {
            meetingId = bundle.getString("meetingId")
            token = bundle.getString("token")
            txtMeetingId.text = "Meeting code: $meetingId"
        }
        val finalMeetingId = meetingId
        txtMeetingId.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (event.rawX >= txtMeetingId.right - txtMeetingId.compoundDrawables[2].bounds.width()
                ) {
                    copyTextToClipboard(finalMeetingId)
                    return@setOnTouchListener true
                }
            }
            false
        }
        val finalToken = token
        btnCreate.setOnClickListener { v: View? ->
            if ("" == etName.text.toString()) {
                Toast.makeText(context, "Please Enter Name", Toast.LENGTH_SHORT).show()
            } else {
                val intent =
                    Intent(activity as CreateOrJoinActivity, MainActivity::class.java)
                intent.putExtra("token", finalToken)
                intent.putExtra("meetingId", finalMeetingId)
                intent.putExtra(
                    "webcamEnabled",
                    (activity as CreateOrJoinActivity).isWebcamEnabled()
                )
                intent.putExtra(
                    "micEnabled",
                    (activity as CreateOrJoinActivity).isMicEnabled()
                )
                intent.putExtra("participantName", etName.text.toString().trim { it <= ' ' })
                intent.putExtra("mode", "CONFERENCE")
                startActivity(intent)
                (activity as CreateOrJoinActivity).finish()
            }
        }
        return view
    }

    private fun copyTextToClipboard(text: String?) {
        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(activity, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}