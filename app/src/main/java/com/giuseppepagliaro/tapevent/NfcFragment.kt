package com.giuseppepagliaro.tapevent

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Animatable
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.giuseppepagliaro.tapevent.nfc.NfcAction
import com.giuseppepagliaro.tapevent.nfc.getFromBundle
import com.giuseppepagliaro.tapevent.nfc.putIntoIntent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NfcFragment : BottomSheetDialogFragment(R.layout.fragment_nfc) {
    private lateinit var context: Context
    private lateinit var activity: Activity

    private var nfcAdapter: NfcAdapter? = null

    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>

    private var actionType: NfcAction? = null

    private var logTag = "nfc_activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = requireContext()
        activity = requireActivity()

        initActionType()
        initNfcAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvPrompt: TextView = view.findViewById(R.id.tv_nfc_prompt)
        val ivAnimation: ImageView = view.findViewById(R.id.iv_nfc_animated_image)
        val animationDrawable = ivAnimation.drawable

        when (actionType) {
            NfcAction.READ -> tvPrompt.text = context.getString(R.string.nfc_read_title)
            NfcAction.WRITE -> tvPrompt.text = context.getString(R.string.nfc_write_title)
            else -> throw IllegalStateException("Unknown action type")
        }

        if (animationDrawable is Animatable) {
            (animationDrawable as Animatable).start()
        }
    }

    override fun onResume() {
        super.onResume()

        nfcAdapter?.enableForegroundDispatch(
            activity,
            pendingIntent,
            intentFilters,
            null
        )
    }

    override fun onPause() {
        super.onPause()

        nfcAdapter?.disableForegroundDispatch(activity)
    }

    private fun initActionType() {
        actionType = getFromBundle(arguments ?: Bundle())

        if (actionType == null) {
            Log.e(logTag, "Nfc fragment arguments missing")

            Toast.makeText(
                context,
                context.getString(R.string.critical_error),
                Toast.LENGTH_LONG
            ).show()

            dismissNow()
        }
    }

    private fun initNfcAdapter() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (nfcAdapter == null) {
            Toast.makeText(
                context,
                context.getString(R.string.nfc_adapter_unavailable),
                Toast.LENGTH_LONG
            ).show()
            dismissNow()
        } else {
            val intent = Intent(context, javaClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            putIntoIntent(
                intent,
                actionType ?: throw IllegalStateException("Unknown action type")
            )

            pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            intentFilters = arrayOf()
        }
    }
}