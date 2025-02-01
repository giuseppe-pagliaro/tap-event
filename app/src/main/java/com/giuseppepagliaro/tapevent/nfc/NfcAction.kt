package com.giuseppepagliaro.tapevent.nfc

import android.content.Intent
import android.os.Bundle

enum class NfcAction {
    READ, WRITE
}

fun putIntoBundle(bundle: Bundle, action: NfcAction) {
    bundle.putString("nfc_action_type", action.name)
}

fun getFromBundle(bundle: Bundle): NfcAction? {
    val name = bundle.getString("nfc_action_type") ?: return null
    return NfcAction.valueOf(name)
}

fun putIntoIntent(intent: Intent, action: NfcAction) {
    intent.putExtra("nfc_action_type", action.name)
}

fun getFromIntent(intent: Intent): NfcAction? {
    val name = intent.getStringExtra("nfc_action_type") ?: return null
    return NfcAction.valueOf(name)
}