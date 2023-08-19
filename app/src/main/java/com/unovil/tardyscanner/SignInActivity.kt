package com.unovil.tardyscanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import com.stevdzasan.onetap.rememberOneTapSignInState
import com.unovil.tardyscanner.databinding.ActivitySigninBinding
import com.unovil.tardyscanner.koin.SERVER_CLIENT_ID
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.handleDeeplinks
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class SignInActivity : ComponentActivity() {
    private lateinit var binding: ActivitySigninBinding
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val supabaseUrl: String by inject(named("supabaseUrl"))
    private val supabaseKey: String by inject(named("supabaseKey"))
    private val client: SupabaseClient by inject(named("supabaseClient")) { parametersOf(supabaseUrl, supabaseKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i("SignInActivity", "Started SignInActivity")

        client.handleDeeplinks(intent)
        findViewById<ComposeView>(R.id.composeView).setContent {
            Log.i("SignInActivity", "started Compose!")
            val status = client.gotrue.sessionStatus.collectAsState()
            when (status.value) {
                is SessionStatus.Authenticated -> {
                    Log.i("SignInActivity", "value of session status is ${status.value}")
                    startActivity(Intent(this, ScannerActivity::class.java))
                }
                else -> { Log.i("SignInActivity", "value of session status is ${status.value}") }
            }

            OneTapSignIn()
        }
    }

    @Composable
    private fun OneTapSignIn() {
        Log.i("SignInActivity", "started oneTap!")
        val oneTapSignInState = rememberOneTapSignInState()
        OneTapSignInWithGoogle(
            state = oneTapSignInState,
            clientId = SERVER_CLIENT_ID,
            onTokenIdReceived = { loginWithIdToken(it) },
            onDialogDismissed = { }
        )

        Button(
            onClick = { oneTapSignInState.open() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
        ) {
            Image(painterResource(id = R.drawable.google_logo), contentDescription = "google logo")
            Text(text = "Sign In")
        }
    }

    private fun loginWithIdToken(idToken: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                client.gotrue.loginWith(IDToken)  {
                    this.idToken = idToken
                    provider = Google
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}
