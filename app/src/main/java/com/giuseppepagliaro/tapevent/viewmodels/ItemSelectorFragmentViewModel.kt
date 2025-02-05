package com.giuseppepagliaro.tapevent.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Selected

abstract class ItemSelectorFragmentViewModel : ViewModel() {
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

    // La passphrase usata per le operazioni di crypt/decrypt.
    abstract fun getCustomerIdCipherPassphrase(): String
    // Richiede un nuovo customer ID, che non sarà valido finché non verrà
    // confermato dal richiedente.
    abstract fun requestNewCustomerId(): String?
    // Informa la fonte dei customer ID che la scrittura della tag è andata
    // a buon fine e che l'ID può essere confermato.
    abstract fun confirmCustomerId(id: String)
    // Informa la fonte dei customer ID che si è verificato un errore durante
    // la scrittura della tag e che l'ID va eliminato.
    abstract fun cancelCustomerId(id: String)

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

        // Questo metodo deve essere chiamato esclusivamente all'interno di un Adapter,
        // dove si dispone di un indice valido per accedere alla lista di Selectable.
        // Usare un indice invalido è un comportamento inaspettato e deve provocare un'eccezione.
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

        // Questo metodo deve essere chiamato esclusivamente all'interno di un Adapter.
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

        // Questo metodo deve essere chiamato esclusivamente all'interno di un Adapter.
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
            // Le azioni della UI che chiamano finalizeTransaction dovrebbero essere disabilitate
            // se selectedList è vuota, quindi questo non dovrebbe mai succedere.
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

            // Se la Location non è più disponibile ed era selezionata, deselezionala.
            // Ma, se la Location esiste ancora, lanciare un update della UI sarebbe inutile.
            if (selLocation == null || ! locations.contains(selLocation)) {
                _selectedLocation.value = locations[0]
            }

            // Aggiorno l'indice.
            selLocation = _selectedLocation.value

            // Potrebbe succedere se i dati sono stati eliminati da un altro thread.
            if (selLocation == null) return@addSource

            val selLocationInd = locations.indexOf(selLocation)

            // Potrebbe succedere se la Location è stata cambiata in un altro thread.
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