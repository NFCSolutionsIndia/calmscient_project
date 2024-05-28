import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.CardItemDataClass
import com.calmscient.di.remote.ItemType

class GridViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.gridview_fragment, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewVideos)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        val adapter = GridItemAdapter(cardItemsFavorites())
        recyclerView.adapter = adapter
        return view
    }

    private fun cardItemsFavorites(): List<CardItemDataClass> {
        val card1 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/L1-1-Neuropsychology+of+Anxiety.mp4",
            contentIcons = listOf(R.drawable.lesson_1_1),
            description = getString(R.string.neuropsychology),
            isCompleted = false,
            heading = getString(R.string.the_neuropsychology),
            summary = getString(R.string.lesson1_video_summary),
            dialogText = getString(R.string.lesson1_video1_description)
        )

        val card2 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+4-1+Implementing+body+calming+skills.mp4",
            contentIcons = listOf(R.drawable.lesson_4_1),
            description = getString(R.string.make_plan_card6_text2),
            isCompleted = false,
            heading = getString(R.string.make_plan_card6_text2),
            summary =  getString(R.string.lesson4_video_summary),
            dialogText = getString(R.string.lesson4_video1_description)
        )

        val card3 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.AUDIO),
            audioResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+1-2+Meet+Nora%2C+Austin+and+Melanie.wav",
            videoResourceId = null,
            contentIcons = listOf(R.drawable.audio_lesson_1_2),
            description = getString(R.string.meet_nora_austin),
            isCompleted = false,
            heading = null,
            summary = null,
            dialogText = null
        )

        val card4 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.AUDIO),
            audioResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/Lesson+3+Moral+deficiency+or+anxiety+with+music+English.wav",
            videoResourceId = null,
            contentIcons = listOf(R.drawable.audio_lesson_1_2),
            description = getString(R.string.moral_deficiency),
            isCompleted = false,
            heading = null,
            summary = null,
            dialogText = null
        )

        val card5 = CardItemDataClass(
            availableContentTypes = listOf(ItemType.VIDEO),
            audioResourceId = null,
            videoResourceId = "https://calmscient-videos.s3.ap-south-1.amazonaws.com/1+Anxiety+and+exercise.mp4",
            contentIcons = listOf(R.drawable.additional_1),
            description = getString(R.string.anxiety_exercise),
            isCompleted = false,
            heading = getString(R.string.fitness_tips),
            summary = getString(R.string.additional_anxiety_exercise_summary),
            dialogText = getString(R.string.additional_anxiety_exercise_dialogText)
        )

        // Add more CardItemDataClass instances as needed for lesson3
        return listOf(card1, card2, card3, card4, card5)
    }
}
