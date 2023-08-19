package com.unovil.tardyscanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.unovil.tardyscanner.databinding.ActivityMainBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.gotrue
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

/**
 * @author Juan Miguel L. Villegas
 */
class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val supabaseUrl: String by inject(named("supabaseUrl"))
    private val supabaseKey: String by inject(named("supabaseKey"))
    private val client: SupabaseClient by inject(named("supabaseClient")) { parametersOf(supabaseUrl, supabaseKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (client.gotrue.sessionStatus.value) {
            is SessionStatus.Authenticated -> {
                proceedToActivity(ScannerActivity::class.java)
            }
            else -> {
                Log.i("MainActivity", "Value of sessionStatus is ${client.gotrue.sessionStatus.value}")
                proceedToActivity(SignInActivity::class.java)
            }
        }
    }

    private fun <T: ComponentActivity> proceedToActivity(activity: Class<T>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }
}