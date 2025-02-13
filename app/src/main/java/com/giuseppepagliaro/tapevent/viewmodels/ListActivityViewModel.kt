package com.giuseppepagliaro.tapevent.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giuseppepagliaro.tapevent.models.Displayable
import kotlinx.coroutines.runBlocking

class ListActivityViewModel(
    val itemsName: String,
    private val getItems: suspend () -> LiveData<List<Displayable>>
) : ViewModel() {
    val items: LiveData<List<Displayable>>

    init {
        val items: LiveData<List<Displayable>>
        runBlocking {
            items = getItems()
        }
        this.items = items
    }

    class Factory(
        private val itemsName: String,
        private val getItems: suspend () -> LiveData<List<Displayable>>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ListActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ListActivityViewModel(itemsName, getItems) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}