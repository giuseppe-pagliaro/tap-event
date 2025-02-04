package com.giuseppepagliaro.tapevent.nfc

import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import android.widget.Toast
import com.giuseppepagliaro.tapevent.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class TapEventNfcHandler(
    private val context: Context,
    private val onTagTapped: suspend () -> Unit,
    private val onNfcReadResult: (String) -> Unit,
    private val onNfcWriteResult: (Boolean, String) -> Unit,
    private val onError: (String) -> Unit,
    private val getPassphrase: () -> String,
    private val requestNewCustomerId: () -> String?
) {
    companion object {
        const val MIME_TYPE = "application/com.giuseppepagliaro.tapevent.customerid"
        private const val LOG_TAG = "TapEventNfcHandler"
    }

    suspend fun handle(intent: Intent) {
        onTagTapped()

        @Suppress("DEPRECATION")
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (tag == null) {
            withContext(Dispatchers.Main) {
                onError(context.getString(R.string.nfc_subtitle_no_tag_found_error))
            }
            return
        }

        val actionType = getFromIntent(intent)
        when (actionType) {
            NfcAction.READ -> read(tag)
            NfcAction.WRITE -> write(tag)
            else -> {
                Log.e(LOG_TAG, "Unknown action type")

                Toast.makeText(
                    context,
                    context.getString(R.string.critical_error),
                    Toast.LENGTH_LONG
                ).show()

                return
            }
        }
    }

    private suspend fun read(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef == null) {
            withContext(Dispatchers.Main) {
                onError(context.getString(R.string.nfc_subtitle_empty_tag_error))
            }
            return
        }

        try {
            ndef.connect()

            val ndefMessage = ndef.ndefMessage
            if (ndefMessage == null) {
                withContext(Dispatchers.Main) {
                    onError(context.getString(R.string.nfc_subtitle_empty_tag_error))
                }
                ndef.close()
                return
            }

            var customerId: String? = null
            for (record in ndefMessage.records) {
                if (record.tnf != NdefRecord.TNF_MIME_MEDIA)
                    continue

                val mimeType = String(record.type)
                if (mimeType != MIME_TYPE)
                    continue

                customerId = decryptCustomerId(record.payload, tag.id)
            }

            ndef.close()

            if (customerId == null) {
                withContext(Dispatchers.Main) {
                    onError(context.getString(R.string.nfc_subtitle_id_not_found))
                }
            } else {
                withContext(Dispatchers.Main) {
                    onNfcReadResult(customerId)
                }
            }
        } catch (_: IOException) {
            withContext(Dispatchers.Main) {
                onError(context.getString(R.string.nfc_subtitle_connection_error))
            }
        }
    }

    private suspend fun write(tag: Tag) {
        val customerId = requestNewCustomerId()
        if (customerId == null) {
            withContext(Dispatchers.Main) {
                onError(context.getString(R.string.nfc_subtitle_id_not_found))
            }
            return
        }

        val encrypted = encryptCustomerId(customerId, tag.id)
        val message = NdefMessage(arrayOf(
            NdefRecord.createMime(MIME_TYPE, encrypted)
        ))

        val ndef = Ndef.get(tag)

        if (ndef == null) {
            val ndefFormattable = NdefFormatable.get(tag)

            if (ndefFormattable == null) {
                withContext(Dispatchers.Main) {
                    onNfcWriteResult(false, customerId)
                    onError(context.getString(R.string.nfc_subtitle_tag_not_formattable))
                }
                return
            }

            try {
                ndefFormattable.connect()
                ndefFormattable.format(message)
                ndefFormattable.close()
            } catch (_: IOException) {
                withContext(Dispatchers.Main) {
                    onError(context.getString(R.string.nfc_subtitle_connection_error))
                }
            }

        } else  {
            try {
                ndef.connect()
                ndef.writeNdefMessage(message)
                ndef.close()
            } catch (_: IOException) {
                withContext(Dispatchers.Main) {
                    onError(context.getString(R.string.nfc_subtitle_connection_error))
                }
            }
        }

        withContext(Dispatchers.Main) {
            onNfcWriteResult(true, customerId)
        }
    }

    private val keySize = 256 // 256-bit AES key
    private val ivSize = 12
    private val tagSize = 128
    private val iterations = 10000
    private val keyGenAlgorithm = "PBKDF2WithHmacSHA1"
    private val cipherAlgorithm = "AES"
    private val cipherTransformation = "AES/GCM/NoPadding"

    private fun encryptCustomerId(customerId: String, tagUid: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(cipherTransformation)

        // Genera un IV casuale.
        val iv = ByteArray(ivSize)
        val random = SecureRandom()
        random.nextBytes(iv)

        // Limito la dimensione dei dati alla dimensione della tag.
        val spec = GCMParameterSpec(tagSize, iv)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(tagUid), spec)

        val encryptedData = cipher.doFinal(customerId.toByteArray(StandardCharsets.UTF_8))

        // Combina IV e customer id.
        val combined = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)

        return combined
    }

    private fun decryptCustomerId(encryptedCustomerId: ByteArray, tagUid: ByteArray): String {
        // Estrai IV e dati criptati.
        val iv = Arrays.copyOfRange(encryptedCustomerId, 0, ivSize)
        val encryptedBytes = Arrays.copyOfRange(encryptedCustomerId, ivSize, encryptedCustomerId.size)

        val cipher = Cipher.getInstance(cipherTransformation)
        val spec = GCMParameterSpec(tagSize, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(tagUid), spec)

        val decryptedData = cipher.doFinal(encryptedBytes)
        return String(decryptedData, StandardCharsets.UTF_8)
    }

    private fun getKey(
        tagUid: ByteArray // L'Uid della tag è usato come salt.
    ): SecretKey {
        val factory = SecretKeyFactory.getInstance(keyGenAlgorithm)
        val spec: KeySpec = PBEKeySpec(
            getPassphrase().toCharArray(),
            tagUid,
            iterations,
            keySize
        )
        return SecretKeySpec(factory.generateSecret(spec).encoded, cipherAlgorithm)
    }
}