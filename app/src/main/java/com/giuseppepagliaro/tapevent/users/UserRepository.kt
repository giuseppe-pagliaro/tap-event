package com.giuseppepagliaro.tapevent.users

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.giuseppepagliaro.tapevent.TapEventDatabase
import com.giuseppepagliaro.tapevent.entities.InternalUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class UserRepository(
    private val context: Context,
    private val tapEventDB: TapEventDatabase
) {
    companion object {
        private const val SESSION_ID_KEY = "session_id"
        private const val SESSION_ID_FILE_NAME = "session_ids"
    }

    suspend fun getSessionId(): String? {
        return withContext(Dispatchers.IO) {
            getEncryptedPrefs().getString(SESSION_ID_KEY, null)
        }
    }

    suspend fun login(username: String, password: String, rememberMe: Boolean): String? {
        var sessionId: String?
        withContext(Dispatchers.IO) {
            val userCod = tapEventDB.users().getInternalCodByUserAndPwd(username, password)
            if (userCod == null) {
                sessionId = null
                return@withContext
            }

            try {
                sessionId = UUID.randomUUID().toString()
                tapEventDB.sessions().insert(Session(sessionId as String, userCod))
            } catch (_: SQLiteException) {
                sessionId = null
                return@withContext
            }

            if (rememberMe && sessionId != null) {
                saveSessionId(sessionId as String)
            }
        }

        return sessionId
    }

    suspend fun logout(sessionId: String): Boolean {
        val result: Boolean
        withContext(Dispatchers.IO) {
            val internalCod = tapEventDB.sessions().getInternalCodBySession(sessionId)
            if (internalCod == null) {
                result = false
                return@withContext
            }

            deleteSessionId()
            tapEventDB.sessions().delete(Session(sessionId, internalCod))
            result = true
        }

        return result
    }

    suspend fun getUsername(sessionId: String): LiveData<String>? {
        var username: LiveData<String>?
        withContext(Dispatchers.IO) {
            val internalCod = tapEventDB.sessions().getInternalCodBySession(sessionId) ?: run {
                username = null
                return@withContext
            }
            username = tapEventDB.users().getUsernameByCod(internalCod) ?: run {
                username = null
                return@withContext
            }
        }

        return username
    }

    suspend fun getProfilePic(sessionId: String): LiveData<Uri>? {
        var profilePic: LiveData<Uri>?
        withContext(Dispatchers.IO) {
            val internalCod = tapEventDB.sessions().getInternalCodBySession(sessionId) ?: run {
                profilePic = null
                return@withContext
            }
            val profilePicRaw = tapEventDB.internalUsers().getProfilePicByCod(internalCod) ?: run {
                profilePic = null
                return@withContext
            }

            profilePic = MediatorLiveData<Uri>().apply {
                addSource(profilePicRaw) { uriStr ->
                    if (uriStr == null) return@addSource

                    value = Uri.parse(uriStr)
                }
            }
        }

        return profilePic
    }

    suspend fun add(
        username: String,
        password: String,
        profilePicture: Uri = InternalUser.DEFAULT_PROPIC_URL
    ): Boolean {
        var result: Boolean
        withContext(Dispatchers.IO) {
            try {
                tapEventDB.runInTransaction {
                    val internalCod = tapEventDB
                        .internalUsers()
                        .insert(InternalUser(profilePic = profilePicture.toString()))

                    tapEventDB.users().insert(User(internalCod, username, password))
                }
            } catch (_: Exception) {
                // Se un'eccezione è lanciata, è avvenuto un rollback.
                result = false
                return@withContext
            }

            result = true
        }

        return result
    }

    suspend fun delete(sessionId: String): Boolean {
        var result: Boolean
        withContext(Dispatchers.IO) {
            try {
                tapEventDB.runInTransaction {
                    val internalCod = tapEventDB.sessions().getInternalCodBySession(sessionId)
                        ?: throw Exception("Rollback")
                    val internalUser = tapEventDB.internalUsers().getByCod(internalCod)
                        ?: throw Exception("Rollback")
                    val userCredentials = tapEventDB.users().getByCod(internalCod)
                        ?: throw Exception("Rollback")

                    tapEventDB.users().delete(userCredentials)
                    tapEventDB.internalUsers().delete(internalUser)
                }
            } catch (_: Exception) {
                result = false
                return@withContext
            }

            result = true
        }

        return result
    }

    private fun saveSessionId(id: String) {
        getEncryptedPrefs().edit().putString(SESSION_ID_KEY, id).apply()
    }

    private fun deleteSessionId() {
        getEncryptedPrefs().edit().remove(SESSION_ID_KEY).apply()
    }

    private fun getEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            SESSION_ID_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}