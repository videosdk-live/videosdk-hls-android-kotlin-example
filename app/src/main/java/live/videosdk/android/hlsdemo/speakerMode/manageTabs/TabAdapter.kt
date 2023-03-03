package live.videosdk.android.hlsdemo.speakerMode.manageTabs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import live.videosdk.android.hlsdemo.speakerMode.participantList.ParticipantListFragment
import live.videosdk.android.hlsdemo.speakerMode.stage.StageFragment

class TabAdapter(fm: FragmentManager?, lifecycle: Lifecycle?,var totalTabs:Int) :
    FragmentStateAdapter(fm!!, lifecycle!!) {

    override fun getItemCount(): Int {
        return this.totalTabs
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StageFragment()
            1 -> ParticipantListFragment()
            else -> {
                StageFragment()
            }
        }
    }
}