package com.unovil.tardyscanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.unovil.tardyscanner.databinding.ActivityMainBinding
import java.util.Properties

lateinit var properties: Properties

/**
 * @author Juan Miguel L. Villegas
 */
class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        properties = Properties().apply {
            resources.openRawResource(R.raw.server_link).use { inputStream ->
                load(inputStream)
            }
        }

        val intent = Intent(this, ScannerActivity::class.java)
        intent.putExtra("SECRET_KEY", properties.getProperty("SECRET_KEY"))
        intent.putExtra("SUPABASE_URL", properties.getProperty("SUPABASE_URL"))
        intent.putExtra("SUPABASE_KEY", properties.getProperty("SUPABASE_KEY"))
        startActivity(intent)
    }
}