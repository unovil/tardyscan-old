package com.example.latescan

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.latescan.databinding.ActivityMainBinding

/**
 * @author JUAN MIGUEL L. VILLEGAS
 *
 * DO NOT REPRODUCE THE PROGRAM IN ANY FORM.
 * all rights reserved, 2023.
 */
class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

}