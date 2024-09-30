/*
 *
 *      Copyright (c) 2023- NFC Solutions, - All Rights Reserved
 *      All source code contained herein remains the property of NFC Solutions Incorporated
 *      and protected by trade secret or copyright law of USA.
 *      Dissemination, De-compilation, Modification and Distribution are strictly prohibited unless
 *      there is a prior written permission or license agreement from NFC Solutions.
 *
 *      Author : @Pardha Saradhi
 */
package com.calmscient.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.calmscient.R
import com.calmscient.di.remote.TakingControlScreeningItem
import com.calmscient.di.remote.response.ScreeningItem
import com.calmscient.fragments.AUDITQuestionFragment
import com.calmscient.fragments.DASTQuestionFragment
import com.calmscient.fragments.GADQuestionFragment
import com.calmscient.fragments.QuestionFragment
import com.calmscient.utils.common.CommonClass
import com.calmscient.viewmodels.FlagsViewModel

interface PayloadCallback {
    fun onPayloadConstructed(item: TakingControlScreeningItem)
}

class TakingControlScreeningAdapter(private val fragmentManager: FragmentManager, private val items: List<TakingControlScreeningItem>, private val context: Context,private var screeningResponse: List<ScreeningItem>, private val callback: PayloadCallback, private val takingControlIndexJson:String) :
    RecyclerView.Adapter<TakingControlScreeningAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.taking_control_screening_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.screeningName.text = item.name
        holder.buttonDoesnotApply.text = item.buttonText
        updateCardAppearance(holder, item.isApplied)

        holder.buttonDoesnotApply.setOnClickListener {
            showDialog(holder, item)
        }
        holder.screeningName.setOnClickListener{
            if (CommonClass.isNetworkAvailable(holder.itemView.context)) {

                val bundle = Bundle().apply {
                    putString("source", "TakingControlIntroductionFragment")  // Pass fragment source name
                    putString("takingControlIndexResponse", takingControlIndexJson)
                }
                // Load corresponding fragment based on screeningType
                val fragment = screeningResponse.indexOfFirst { it.screeningType.trim() == item.name }.takeIf { it >= 0 }?.let { index ->
                    when {
                        item.name.equals("PHQ-9", ignoreCase = true) ->
                            if (!item.isApplied) QuestionFragment(screeningResponse[index]).apply { arguments = bundle } else null

                        item.name.equals("GAD-7", ignoreCase = true) ||
                                item.name.equals("TAG-7", ignoreCase = true) ->
                            if (!item.isApplied) GADQuestionFragment(screeningResponse[index]).apply { arguments = bundle } else null

                        item.name.equals(context.getString(R.string.audit_heading), ignoreCase = true) ||
                                item.name.equals(context.getString(R.string.audit_heading), ignoreCase = true) ->
                            if (!item.isApplied) AUDITQuestionFragment(screeningResponse[index]).apply { arguments = bundle } else null

                        item.name.equals("DAST-10", ignoreCase = true) ->
                            if (!item.isApplied) DASTQuestionFragment(screeningResponse[index]).apply { arguments = bundle } else null

                        // Add more cases for other screening types if needed
                        else -> null
                    }

                }


                fragment?.let {
                    fragmentManager.beginTransaction()
                        .replace(R.id.flFragment, it)
                        .addToBackStack("TakingControl")
                        .commit()
                }
            } else {
                // Show internet connection dialog
                CommonClass.showInternetDialogue(holder.itemView.context)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val screeningName: TextView = view.findViewById(R.id.tv_screeningName)
        val buttonDoesnotApply: AppCompatButton = view.findViewById(R.id.btn_doesnt_apply)
        val cardLinearLayout: LinearLayoutCompat = view.findViewById(R.id.cardLinearLayout)
    }

    private fun showDialog(holder: ViewHolder, item: TakingControlScreeningItem) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.taking_control_screening_dialog, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val descText = dialogView.findViewById<TextView>(R.id.tv_dialog_message)
        val btnYes: AppCompatButton = dialogView.findViewById(R.id.buttonYes)
        val btnNo: AppCompatButton = dialogView.findViewById(R.id.btn_no)

        descText.text = if (item.isApplied) "${item.name} ${context.getString(R.string.apply_to_me)}"  else  "${item.name} ${context.getString(R.string.doesn_t_apply_for_me)}"

        btnYes.setOnClickListener {
            item.isApplied = !item.isApplied // Toggle the state
            item.buttonText = if (item.isApplied) context.getString(R.string.apply_to_me) else context.getString(R.string.doesn_t_apply_for_me)
            updateCardAppearance(holder, item.isApplied)
            dialogBuilder.dismiss()

            callback.onPayloadConstructed(item)

           /* val payload = constructPayload(item)

            Log.d("payload","$payload")*/
        }

        btnNo.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }

    private fun updateCardAppearance(holder: ViewHolder, isApplied: Boolean) {
        if (isApplied) {
            holder.cardLinearLayout.setBackgroundResource(R.drawable.taking_control_selected)
        } else {
            holder.cardLinearLayout.setBackgroundResource(R.drawable.dialog_border)
        }
        holder.buttonDoesnotApply.text = if (isApplied) context.getString(R.string.apply_to_me) else context.getString(R.string.doesn_t_apply_for_me)
    }

    private fun constructPayload(item: TakingControlScreeningItem): String {
        val patientId = 4
        val clientId = 1
        val plId = 4
        val introductionFlag = 0
        val auditFlag = if (item.name == context.getString(R.string.audit_heading) && item.isApplied) 1 else null
        val dastFlag = if (item.name == context.getString(R.string.dast_heading) && item.isApplied) 1 else null
        val cageFlag = if (item.name == "CAGE" && item.isApplied) 1 else null
        val tutorialFlag = null

        return """
            {
                "patientId": $patientId,
                "clientId": $clientId,
                "plId": $plId,
                "introductionFlag": $introductionFlag,
                "auditFlag": $auditFlag,
                "dastFlag": $dastFlag,
                "cageFlag": $cageFlag,
                "tutorialFlag": $tutorialFlag
            }
        """.trimIndent()
    }
}
