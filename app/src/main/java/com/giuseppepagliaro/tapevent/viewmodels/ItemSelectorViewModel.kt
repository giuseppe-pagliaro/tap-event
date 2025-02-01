package com.giuseppepagliaro.tapevent.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Selected

abstract class ItemSelectorViewModel : ViewModel() {
    private val logTag = "ItemSelectorVM"

    private lateinit var _selectable: MediatorLiveData<MutableList<Selectable>>
    private lateinit var _selected: MutableLiveData<MutableList<Selected>>
    private lateinit var _availableLocations: LiveData<List<String>>
    private lateinit var _selectedLocation: MediatorLiveData<String>
    private lateinit var _selectedLocationInd: MutableLiveData<Int>

    private var lastSelectedLocation: String? = null

    abstract val selectableName: String

    val availableLocations: LiveData<List<String>>
    val selectedLocation: LiveData<String>
    val selectedLocationInd: LiveData<Int>
    val selectable: LiveData<List<Selectable>>
    val selected: LiveData<List<Selected>>

    init {
        initAvailableLocations()
        initSelectedLocation()
        initializeSelectable()
        initializeSelected()

        availableLocations = _availableLocations
        selectedLocation = _selectedLocation
        selectedLocationInd = _selectedLocationInd
        selectable = _selectable.map { it.toList() }
        selected = _selected.map { it.toList() }
    }

    protected abstract fun getAvailableLocationsSource(): LiveData<List<String>>
    protected abstract fun getSelectable(location: String): List<Selectable>
    protected abstract fun executeTransaction(clientCode: String, items: List<Selected>): Boolean

    fun setSelectedLocation(position: Int) {
        if (position == selectedLocationInd.value) return

        val availableLocations = availableLocations.value
        if (availableLocations.isNullOrEmpty()) return

        val selectedLocation = availableLocations[position]

        _selectedLocationInd.value = position
        _selectedLocation.value = selectedLocation
    }

    fun selectSelectable(position: Int) {
        val selectableList = _selectable.value ?: run {
            Log.w(logTag, "Tried to modify selectable items, but it wasn't available.")
            return
        }
        val selectedList = _selected.value ?: run {
            Log.w(logTag, "Tried to modify selected items, but it wasn't available.")
            return
        }

        // It is assumed that the method is called from an adapter, where one would
        // always have access to a valid id for selectable.
        // Using an invalid index is unexpected behaviour and should be met with an exception.
        val selectable = selectableList[position]

        selectableList.removeAt(position)
        selectedList.add(Selected(selectable, 1))

        _selectable.value = selectableList
        _selected.value = selectedList
    }

    fun incrementSelected(position: Int) {
        val selectedList = _selected.value ?: run {
            Log.w(logTag, "Tried to modify selected items, but it wasn't available.")
            return
        }

        // It is assumed that the method is called from an adapter.
        val selectable = selectedList[position].item
        val count = selectedList[position].count

        selectedList[position] = Selected(
            selectable,
            count + 1
        )

        _selected.value = selectedList
    }

    fun decrementOrRemoveSelected(position: Int) {
        val selectableList = _selectable.value ?: run {
            Log.w(logTag, "Tried to modify selectable items, but it wasn't available.")
            return
        }
        val selectedList = _selected.value ?: run {
            Log.w(logTag, "Tried to modify selected items, but it wasn't available.")
            return
        }

        // It is assumed that the method is called from an adapter.
        val selected = selectedList[position]

        if (selected.count > 1) {
            selectedList[position] = Selected(
                selected.item,
                selected.count - 1
            )
        } else {
            selectedList.removeAt(position)
            selectableList.add(selected.item)
        }

        _selectable.value = selectableList
        _selected.value = selectedList
    }

    fun clearSelected() {
        val selectedLocation = selectedLocation.value ?: return

        _selectable.value = getSelectable(selectedLocation).toMutableList()
        _selected.value = mutableListOf()
    }

    fun finalizeTransaction(clientCode: String): Boolean {
        val selectedList = selected.value ?: run {
            // The UI interactions that call finalizeTransaction should be disabled if
            // selected list is not populated.
            Log.w(logTag, "Tried execute a transaction, but there were no items available.")

            return false
        }

        if (executeTransaction(clientCode, selectedList)) {
            clearSelected()
            return true
        }
        return false
    }

    private fun initializeSelectable() {
        _selectable = MediatorLiveData()

        _selectable.addSource(_selectedLocation) { location ->
            if (location == null) return@addSource

            if (location != lastSelectedLocation) {
                lastSelectedLocation = location
                _selectable.value = getSelectable(location).toMutableList()
                _selected.value = mutableListOf()
            }
        }
    }

    private fun initializeSelected() {
        _selected = MutableLiveData()
    }

    private fun initSelectedLocation() {
        _selectedLocation = MediatorLiveData()
        _selectedLocationInd = MutableLiveData()

        _selectedLocation.addSource(_availableLocations) { locations ->
            if (locations.isNullOrEmpty()) return@addSource

            var selLocation = _selectedLocation.value

            // If the location is no longer in the available locations,
            // and it was selected, it should be deselected.
            // But if the location is still available it shouldn't be changed,
            // because it would clear the selector for no reason.
            if (selLocation == null || ! locations.contains(selLocation)) {
                _selectedLocation.value = locations[0]
            }

            // Updating the index.
            selLocation = _selectedLocation.value
            // Could happen if the data was cleared in another thread.
            if (selLocation == null) return@addSource

            val selLocationInd = locations.indexOf(selLocation)
            // Could happen if the locations were changed in another thread.
            if (selLocationInd == -1) return@addSource

            if (_selectedLocationInd.value != selLocationInd) {
                _selectedLocationInd.value = selLocationInd
            }
        }
    }

    private fun initAvailableLocations() {
        _availableLocations = getAvailableLocationsSource()
    }
}