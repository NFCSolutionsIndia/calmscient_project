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

package com.calmscient.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.calmscient.di.remote.BasicKnowledgeItem


class BasicKnowledgeViewModel : ViewModel() {
    private val _items = MutableLiveData<List<BasicKnowledgeItem>>()
    val items: LiveData<List<BasicKnowledgeItem>> = _items

    init {
        _items.value = emptyList() // Initialize with an empty list
    }

    fun setItems(items: List<BasicKnowledgeItem>) {
        _items.value = items
    }

    fun markItemCompleted(name: String) {
        _items.value = _items.value?.map {
            if (it.name == name) {
                it.copy(isCompleted = true)
            } else {
                it
            }
        }
    }
}


