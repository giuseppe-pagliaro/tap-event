package com.giuseppepagliaro.tapevent.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Transaction
import com.giuseppepagliaro.tapevent.models.TransactionResult

class TicketSelectorFragmentViewModel(
    ticketsTitle: String,

    // La passphrase usata per le operazioni di crypt/decrypt.
    getCustomerIdCipherPassphrase: suspend () -> String,
    // Richiede un nuovo customer ID, che non sarà valido finché non verrà
    // confermato dal richiedente.
    requestNewCustomerId: suspend () -> String?,
    // Informa la fonte dei customer ID che la scrittura della tag è andata
    // a buon fine e che l'ID può essere confermato.
    confirmCustomerId: suspend (id: String) -> Unit,
    // Informa la fonte dei customer ID che si è verificato un errore durante
    // la scrittura della tag e che l'ID va eliminato.
    cancelCustomerId: suspend (id: String) -> Unit,

    getAvailableCashPoints: suspend () -> LiveData<List<String>>,
    getTickets: suspend (location: String) -> List<Selectable>,
    executeTicketTransactions: suspend (clientCode: String, items: List<Transaction>) -> TransactionResult
) : ItemSelectorFragmentViewModel(
    ticketsTitle,
    getCustomerIdCipherPassphrase,
    requestNewCustomerId,
    confirmCustomerId,
    cancelCustomerId,
    getAvailableCashPoints,
    getTickets,
    executeTicketTransactions
) {
    class Factory(
        private val ticketsTitle: String,
        private val getCustomerIdCipherPassphrase: suspend () -> String,
        private val requestNewCustomerId: suspend () -> String?,
        private val confirmCustomerId: suspend (id: String) -> Unit,
        private val cancelCustomerId: suspend (id: String) -> Unit,
        private val getAvailableCashPoints: suspend () -> LiveData<List<String>>,
        private val getTickets: suspend (location: String) -> List<Selectable>,
        private val executeTicketTransactions: suspend (clientCode: String, items: List<Transaction>) -> TransactionResult
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TicketSelectorFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TicketSelectorFragmentViewModel(
                    ticketsTitle,
                    getCustomerIdCipherPassphrase,
                    requestNewCustomerId,
                    confirmCustomerId,
                    cancelCustomerId,
                    getAvailableCashPoints,
                    getTickets,
                    executeTicketTransactions
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}