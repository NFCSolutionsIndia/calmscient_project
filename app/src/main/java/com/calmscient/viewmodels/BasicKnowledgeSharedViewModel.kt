package com.calmscient.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.calmscient.di.remote.BasicKnowledgeItem

class BasicKnowledgeSharedViewModel : ViewModel() {
    private val _selectedItem = MutableLiveData<BasicKnowledgeItem?>()
    val selectedItem: LiveData<BasicKnowledgeItem?> get() = _selectedItem

    fun selectItem(item: BasicKnowledgeItem) {
        _selectedItem.value = null // Clear the previous item
        _selectedItem.value = item // Set the new item
    }
}
