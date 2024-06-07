package com.calmscient.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.response.QuestionnaireItem
import androidx.viewpager2.widget.ViewPager2
import com.calmscient.activities.CommonDialog

class QuestionAdapter(private val context: Context, private var questionnaireItems: List<QuestionnaireItem>, private var remainder : String? = null) :
    RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

        private var screeningId :Int =-1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.questions_item_card_view, parent, false)
        return QuestionViewHolder(itemView)
    }

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        val optionsRecyclerView: RecyclerView = itemView.findViewById(R.id.optionsRecyclerView)
        val infoIcon: ImageView = itemView.findViewById(R.id.informationDialogButton)

        init {
            optionsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun bindOptions(options: List<String>, selectedOptionIndex: Int?) {
            val selectedOption = questionnaireItems[adapterPosition].answerResponse.indexOfFirst { it.selected == "true" }
            val optionsAdapter = OptionsAdapter(options, selectedOption) { clickedPosition ->
                questionnaireItems[adapterPosition].answerResponse.forEachIndexed { index, answerResponse ->
                    answerResponse.selected = (index == clickedPosition).toString()
                }

//                infoIcon.setOnClickListener{
//                    val commonDialog = CommonDialog(context)
//                    if(screeningId == 0)
//                    {
//                        commonDialog.showDialog(context.getString(R.string.phq))
//                    }
//                }
                notifyDataSetChanged() // Notify the adapter of the change
            }

            optionsRecyclerView.adapter = optionsAdapter
        }
        fun getSelectedOptionId(): String? {
            val selectedOptionIndex = questionnaireItems.getOrNull(adapterPosition)?.answerResponse?.indexOfFirst { it.selected == "true" }
            return if (selectedOptionIndex != null && selectedOptionIndex != -1) {
                questionnaireItems[adapterPosition].answerResponse[selectedOptionIndex].optionLabelId
            } else {
                null
            }
        }
    }


    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val item = questionnaireItems[position]
        holder.questionTextView.text = item.questionName
        val selectedOptionIndex = item.selectedOption
        //holder.bindOptions(item.answerResponse.map { it.optionLabel }, item.selectedOption)

        // Set the options RecyclerView orientation to vertical
        //holder.optionsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

        // Bind options to the options RecyclerView
        holder.bindOptions(item.answerResponse.map { it.optionLabel }, selectedOptionIndex)

        // Set visibility of infoIcon based on position
        holder.infoIcon.visibility = if (position == 0) View.VISIBLE else View.GONE

        holder.infoIcon.setOnClickListener {
            val commonDialog = CommonDialog(context)
            // Show the dialog with the text from the screening item
            commonDialog.showDialog(remainder)
        }
    }

    override fun getItemCount(): Int {
        return questionnaireItems.size
    }
    fun updateQuestionnaireItems(newQuestionnaireItems: List<QuestionnaireItem>) {
        questionnaireItems = newQuestionnaireItems
        notifyDataSetChanged()
    }

    fun getSelectedOptionId(position: Int): String? {
        val selectedOptionIndex = questionnaireItems.getOrNull(position)?.answerResponse?.indexOfFirst { it.selected == "true" }
        return if (selectedOptionIndex != null && selectedOptionIndex != -1) {
            questionnaireItems[position].answerResponse[selectedOptionIndex].optionLabelId
        } else {
            null
        }
    }

}
