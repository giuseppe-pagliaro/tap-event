package com.giuseppepagliaro.tapevent.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Animatable
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.giuseppepagliaro.tapevent.EventActivity
import com.giuseppepagliaro.tapevent.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NfcFragment : BottomSheetDialogFragment(R.layout.fragment_nfc) {
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>
    private lateinit var techLists: Array<Array<String>>

    private lateinit var tvPrompt: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ivAnimation: ImageView

    var action: NfcAction = NfcAction.READ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = requireContext()

        // Inizializzo NfcAdapter.
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (nfcAdapter == null) {
            Toast.makeText(
                context,
                context.getString(R.string.nfc_adapter_unavailable),
                Toast.LENGTH_LONG
            ).show()

            dismiss()
        } else {
            this.nfcAdapter = nfcAdapter

            // Configuro il Pending Intent Per EventActivity
            val intent = Intent(context, EventActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putIntoIntent(intent, action)

            pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            intentFilters = arrayOf()
            techLists = arrayOf(
                arrayOf(Ndef::class.java.name),
                arrayOf(NdefFormatable::class::java.name)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inizializzo le View.
        tvPrompt = view.findViewById(R.id.tv_nfc_prompt)
        tvDescription = view.findViewById(R.id.tv_nfc_description)
        ivAnimation = view.findViewById(R.id.iv_nfc_animated_image)

        onOperationRequested()
    }

    /* START funzioni lifecycle di resume/pause */
    // Stoppo o ricomincio NFC Adapter e NFC Animation.

    override fun onResume() {
        super.onResume()

        nfcAdapter.enableForegroundDispatch(
            activity,
            pendingIntent,
            intentFilters,
            techLists
        )

        val animationDrawable = ivAnimation.drawable
        if (animationDrawable is Animatable) {
            (animationDrawable as Animatable).start()
        }
    }

    override fun onPause() {
        super.onPause()

        nfcAdapter.disableForegroundDispatch(activity)

        val animationDrawable = ivAnimation.drawable
        if (animationDrawable is Animatable) {
            (animationDrawable as Animatable).stop()
        }
    }

    /* END funzioni lifecycle di resume/pause */

    /* START funzioni di setup */
    // Funzioni chiamate in corrispondenza di un evento NFC per configurare il fragment.

    private fun onOperationRequested() {
        val context = requireContext()

        tvPrompt.text =
            when (action) {
                NfcAction.READ -> context.getString(R.string.nfc_read_title)
                NfcAction.WRITE -> context.getString(R.string.nfc_write_title)
            }

        tvDescription.text = context.getString(R.string.nfc_subtitle)

        ivAnimation.setImageResource(R.drawable.avd_splash_anim)
    }

    // Chiamata durante le operazioni di gestione lettura/scrittura tag, che
    // avvengono in un altro thread.
    suspend fun onTagTapped() {
        val context = requireContext()
        requireView()

        // Passo al MainThread per manipolare la UI, perché in Android è illegale
        // modificare la UI in un altro thread.
        withContext(Dispatchers.Main) {
            tvPrompt.text =
                when (action) {
                    NfcAction.READ -> context.getString(R.string.nfc_read_in_process_title)
                    NfcAction.WRITE -> context.getString(R.string.nfc_write_in_process_title)
                }

            tvDescription.text = context.getString(R.string.nfc_in_process_subtitle)
        }
    }

    fun onErrorOccurred(error: String) {
        val context = requireContext()
        requireView()

        tvPrompt.text = context.getString(R.string.nfc_error_title)

        tvDescription.text = error

        ivAnimation.setImageResource(R.drawable.logo_error)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            delay(1500)
            dismiss()
        }
    }

    fun onOperationCompleted() {
        val context = requireContext()
        requireView()

        tvPrompt.text = context.getString(R.string.nfc_success_title)

        tvDescription.text =
            when (action) {
                NfcAction.READ -> context.getString(R.string.nfc_subtitle_read_successful)
                NfcAction.WRITE -> context.getString(R.string.nfc_subtitle_write_successful)
            }

        ivAnimation.setImageResource(R.drawable.logo_success)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            delay(1500)
            dismiss()
        }
    }

    /* END funzioni di setup */
}