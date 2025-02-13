package com.giuseppepagliaro.tapevent.nfc

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// Mette insieme la UI e la Business Logic per l'Nfc.
class TapEventNfcProvider(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val onNfcReadResult: (String) -> Unit,
    private val onNfcWriteResult: suspend (Boolean, String) -> Unit,
    private val getPassphrase: suspend () -> String,
    private val requestNewCustomerId: suspend () -> String?
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val nfcFragment = NfcFragment()

    private lateinit var nfcHandler: TapEventNfcHandler

    fun handle(intent: Intent) {
        scope.launch {
            nfcHandler.handle(intent)
        }
    }

    fun request(action: NfcAction) {
        nfcHandler = TapEventNfcHandler(
            context,

            nfcFragment::onTagTapped,

            { cId ->
                nfcFragment.onOperationCompleted()
                onNfcReadResult(cId)
            },

            { res, cId ->
                nfcFragment.onOperationCompleted()
                onNfcWriteResult(res, cId)
            },

            nfcFragment::onErrorOccurred,

            getPassphrase,

            requestNewCustomerId
        )

        // Lancia l'Nfc Fragment.
        nfcFragment.action = action
        nfcFragment.show(fragmentManager, "nfc_fragment")
    }

    fun dispose() {
        scope.cancel()
    }
}