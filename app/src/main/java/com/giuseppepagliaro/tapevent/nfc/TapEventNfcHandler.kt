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
    private val onNfcReadResult: (String) -> Unit,
    private val onNfcWriteResult: (Boolean) -> Unit,
    private val getPassphrase: () -> String,
    private val requestNewCustomerId: () -> String?
) {
    val mimeType = "application/com.giuseppepagliaro.tapevent.customerid"
    private val logTag = "tap_event_nfc_handler"

    fun handle(intent: Intent) {
        val actionType = getFromIntent(intent)

        @Suppress("DEPRECATION")
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (tag == null) {
            Toast.makeText(
                context,
                context.getString(R.string.nfc_no_tag_found),
                Toast.LENGTH_LONG
            ).show()

            return
        }

        when (actionType) {
            NfcAction.READ -> read(tag)
            NfcAction.WRITE -> write(tag)
            else -> {
                Log.e(logTag, "Unknown action type")

                Toast.makeText(
                    context,
                    context.getString(R.string.critical_error),
                    Toast.LENGTH_LONG
                ).show()

                return
            }
        }
    }

    private fun read(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef == null) {
            Toast.makeText(
                context,
                context.getString(R.string.nfc_tag_empty),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        ndef.connect()

        val ndefMessage = ndef.ndefMessage
        if (ndefMessage == null) {
            Toast.makeText(
                context,
                context.getString(R.string.nfc_tag_empty),
                Toast.LENGTH_SHORT
            ).show()

            ndef.close()
            return
        }

        var customerId: String? = null
        for (record in ndefMessage.records) {
            if (record.tnf != NdefRecord.TNF_MIME_MEDIA)
                continue

            val mimeType = String(record.type)
            if (mimeType != this.mimeType)
                continue

            customerId = decryptCustomerId(record.payload, tag.id)
        }

        ndef.close()

        if (customerId == null) {
            Toast.makeText(
                context,
                context.getString(R.string.nfc_tag_does_not_contain_id),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            onNfcReadResult(customerId)
        }
    }

    private fun write(tag: Tag) {
        val customerId = requestNewCustomerId()
        if (customerId == null) {
            Toast.makeText(
                context,
                context.getString(R.string.nfc_tag_does_not_contain_id),
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val encrypted = encryptCustomerId(customerId, tag.id)
        val message = NdefMessage(arrayOf(
            NdefRecord.createMime(mimeType, encrypted)
        ))

        val ndef = Ndef.get(tag)

        if (ndef == null) {
            val ndefFormattable = NdefFormatable.get(tag)

            if (ndefFormattable == null) {
                onNfcWriteResult(false)
                Toast.makeText(
                    context,
                    context.getString(R.string.nfc_tag_not_formattable),
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            ndefFormattable.connect()
            ndefFormattable.format(message)
            ndefFormattable.close()
        } else  {
            ndef.connect()
            ndef.writeNdefMessage(message)
            ndef.close()
        }

        onNfcWriteResult(true)
        Toast.makeText(
            context,
            context.getString(R.string.nfc_tag_written_successfully),
            Toast.LENGTH_SHORT
        ).show()
    }

    private val keySize = 256 // 256-bit AES key
    private val ivSize = 12
    private val tagSize = 128
    private val iterations = 10000
    private val keyGenAlgorithm = "PBKDF2WithHmacSHA256"
    private val cipherAlgorithm = "AES"
    private val cipherTransformation = "AES/GCM/NoPadding"

    private fun encryptCustomerId(customerId: String, tagUid: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(cipherTransformation)

        // Generate a random IV
        val iv = ByteArray(ivSize)
        val random = SecureRandom()
        random.nextBytes(iv)

        // Capping the message size to the size of the tag.
        val spec = GCMParameterSpec(tagSize, iv)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(tagUid), spec)

        val encryptedData = cipher.doFinal(customerId.toByteArray(StandardCharsets.UTF_8))

        // Combine IV and encrypted customer id
        val combined = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)

        return combined
    }

    private fun decryptCustomerId(encryptedCustomerId: ByteArray, tagUid: ByteArray): String {
        // Extract IV and encrypted data
        val iv = Arrays.copyOfRange(encryptedCustomerId, 0, ivSize)
        val encryptedBytes = Arrays.copyOfRange(encryptedCustomerId, ivSize, encryptedCustomerId.size)

        val cipher = Cipher.getInstance(cipherTransformation)
        val spec = GCMParameterSpec(tagSize, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(tagUid), spec)

        val decryptedData = cipher.doFinal(encryptedBytes)
        return String(decryptedData, StandardCharsets.UTF_8)
    }

    private fun getKey(
        tagUid: ByteArray // The uid of the tag is used as salt
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