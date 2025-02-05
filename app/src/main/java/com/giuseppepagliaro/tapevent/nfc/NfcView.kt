package com.giuseppepagliaro.tapevent.nfc

import android.content.Intent

// Il contratto standard per le view che utilizzano TapEventNfcProvider.
interface NfcView {
    fun handleNfcIntent(intent: Intent)
}