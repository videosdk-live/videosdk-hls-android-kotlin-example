package live.videosdk.android.hlsdemo.speakerMode.manageTabs

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import live.videosdk.android.hlsdemo.R
import live.videosdk.android.hlsdemo.common.meeting.activity.MainActivity
import kotlin.math.roundToInt

class SpeakerFragment : Fragment() {
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null
    private var adapter: TabAdapter? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_speaker, container, false)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Stage"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Participants"))
        val tabOne =
            LayoutInflater.from(context).inflate(R.layout.custom_tab_layout, null) as TextView
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
        tabOne.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (event.rawX >= tabOne.right - tabOne.compoundDrawables[2]
                        .bounds.width()
                ) {
                    showMeetingInfo()
                    return@setOnTouchListener true
                }
            }
            false
        }
        adapter = TabAdapter(childFragmentManager, lifecycle, tabLayout!!.tabCount)
        viewPager!!.adapter = adapter!!
        val mediator = TabLayoutMediator(tabLayout!!, viewPager!!
        ) { tab, position ->
            tabLayout!!.selectTab(tab)
            if (position == 0) {
                val drawable = resources.getDrawable(R.drawable.ic_info)
                val size = dpToPx(23, context)
                drawable?.setBounds(0, 0, size, size)
                tabOne.setCompoundDrawables(null, null, drawable, null)
                tab.customView = tabOne
                tabOne.text = "Stage"
            } else tab.text = "Participants"
        }
        mediator.attach()
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showMeetingInfo() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogCustom).create()
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_style, null)
        alertDialog.setView(dialogView)
        val message = dialogView.findViewById<TextView>(R.id.message)
        val message1 = dialogView.findViewById<TextView>(R.id.message1)
        message1.visibility = View.VISIBLE
        message.text = "Meeting Code"
        message.textSize = 15f
        message.setTextColor(resources.getColor(R.color.md_grey_10A))
        message1.text = "               " + (activity as MainActivity?)!!.getMeeting()!!.meetingId
        message1.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (event.rawX >= message1.right - message1.compoundDrawables[2]
                        .bounds.width()
                ) {
                    copyTextToClipboard(
                        (activity as MainActivity?)!!.getMeeting()!!.meetingId
                    )
                    return@setOnTouchListener true
                }
            }
            false
        }
        val btnLayout = dialogView.findViewById<LinearLayout>(R.id.btnLayout)
        btnLayout.visibility = View.GONE
        alertDialog.show()
    }

    private fun copyTextToClipboard(text: String) {
        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(activity, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private fun dpToPx(dp: Int, context: Context?): Int {
            val density = context!!.resources.displayMetrics.density
            return (dp.toFloat() * density).roundToInt()
        }
    }
}