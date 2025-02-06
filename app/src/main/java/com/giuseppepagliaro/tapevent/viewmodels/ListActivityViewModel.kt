package com.giuseppepagliaro.tapevent.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giuseppepagliaro.tapevent.models.Displayable

class ListActivityViewModel(
    val itemsName: String,
    val items: LiveData<List<Displayable>>
) : ViewModel() {
    class Factory(
        private val itemsName: String,
        private val items: LiveData<List<Displayable>>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ListActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ListActivityViewModel(itemsName, items) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}