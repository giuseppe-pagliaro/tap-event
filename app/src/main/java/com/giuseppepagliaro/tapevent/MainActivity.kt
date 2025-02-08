package com.giuseppepagliaro.tapevent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.giuseppepagliaro.tapevent.users.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {
    companion object {
        fun onSessionIdInvalidated(activity: Activity) {
            Toast.makeText(
                activity,
                activity.getString(R.string.session_expired_msg),
                Toast.LENGTH_SHORT
            ).show()

            // Ritorna all schermata di login.
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("was_session_invalidated", true)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuro lo SplashScreen ed eseguo le operazioni di Startup.

        val areStartupOperationsRunning = AtomicBoolean(true)
        suspend fun startupOperations() {
            userRepository = UserRepository() // TODO initialization will be more complecated

            // Se era stato memorizzato un session id, bypasso la schermata di login.
            // È possibile fornire la flag was_session_invalidated per saltare il
            // recupero del session id.
            if (!intent.getBooleanExtra("was_session_invalidated", false)) {
                val savedSessionId = userRepository.getSessionId()
                if (savedSessionId != null) {
                    withContext(Dispatchers.Main) {
                        startHomeActivity(savedSessionId)
                        finish()
                    }
                }
            }
            areStartupOperationsRunning.set(false)
        }

        val splashscreen = installSplashScreen()
        splashscreen.setKeepOnScreenCondition { areStartupOperationsRunning.get() }

        CoroutineScope(Dispatchers.IO).launch {
            startupOperations()
        }

        // Configuro Activity View.

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsername: EditText = findViewById(R.id.et_username)
        val etPassword: EditText = findViewById(R.id.et_password)
        val checkRememberMe: CheckBox = findViewById(R.id.check_login_remember_me)
        val btnLogin: Button = findViewById(R.id.btn_login)

        // Configuro Login Button.
        btnLogin.setOnClickListener {
            if (etUsername.text.isNullOrEmpty() || etPassword.text.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.login_missing_fields_msg),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val sessionId = userRepository.login(
                etUsername.text.toString(),
                etPassword.text.toString(),
                checkRememberMe.isChecked
            )

            if (sessionId == null) {
                Toast.makeText(
                    this,
                    getString(R.string.login_failed_msg),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            startHomeActivity(sessionId)
        }
    }

    private fun startHomeActivity(sessionId: String) {
        val intent = Intent(this, HomeActivityImpl::class.java)
        intent.putExtra("session_id", sessionId)
        startActivity(intent)
    }
}