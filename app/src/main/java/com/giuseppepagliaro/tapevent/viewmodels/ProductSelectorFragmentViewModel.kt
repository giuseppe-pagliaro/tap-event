package com.giuseppepagliaro.tapevent.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Transaction
import com.giuseppepagliaro.tapevent.models.TransactionResult

class ProductSelectorFragmentViewModel(
    productsTitle: String,

    // La passphrase usata per le operazioni di crypt/decrypt.
    getCustomerIdCipherPassphrase: suspend () -> String,

    getAvailableStands: suspend () -> LiveData<List<String>>,
    getProducts: suspend (location: String) -> List<Selectable>,
    executeProductTransactions: suspend (clientCode: String, items: List<Transaction>) -> TransactionResult
) : ItemSelectorFragmentViewModel(
    productsTitle,
    getCustomerIdCipherPassphrase,
    { throw IllegalStateException("This view does not add customers") },
    { throw IllegalStateException("This view does not add customers") },
    { throw IllegalStateException("This view does not add customers") },
    getAvailableStands,
    getProducts,
    executeProductTransactions
) {
    class Factory(
        private val productsTitle: String,
        private val getCustomerIdCipherPassphrase: suspend () -> String,
        private val getAvailableStands: suspend () -> LiveData<List<String>>,
        private val getProducts: suspend (location: String) -> List<Selectable>,
        private val executeProductTransactions: suspend (clientCode: String, items: List<Transaction>) -> TransactionResult
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductSelectorFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductSelectorFragmentViewModel(
                    productsTitle,
                    getCustomerIdCipherPassphrase,
                    getAvailableStands,
                    getProducts,
                    executeProductTransactions
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}