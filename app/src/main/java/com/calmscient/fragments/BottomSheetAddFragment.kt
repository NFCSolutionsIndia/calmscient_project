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

package com.calmscient.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import com.calmscient.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetAddFragment:BottomSheetDialogFragment() {
    lateinit var etAdd: EditText
    lateinit var addButton: AppCompatButton
    lateinit var closeButton: ImageView
    var bottomSheetListener: BottomSheetListener? = null

    interface BottomSheetListener {
        fun onTextEntered(text: String)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_journal_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etAdd = view.findViewById(R.id.et_daily_journel)
        addButton = view.findViewById(R.id.addButton)
        closeButton = view.findViewById(R.id.closeButton)
        etAdd.imeOptions = EditorInfo.IME_ACTION_DONE
        etAdd.setRawInputType(InputType.TYPE_CLASS_TEXT)
        closeButton.setOnClickListener {
            dismiss()
        }
        // Add button to send the text back to the fragment
        addButton.setOnClickListener {
            val enteredText = etAdd.text.toString()
            bottomSheetListener?.onTextEntered(enteredText)
            dismiss()
        }
    }
    override fun onResume() {
        super.onResume()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
    private fun expandBottomSheet() {
        val behavior = (dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout)
            .let { BottomSheetBehavior.from(it) }
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}