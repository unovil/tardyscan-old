package com.unovil.tardyscanner

import android.os.Bundle
// import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.text.*
import com.unovil.tardyscanner.databinding.ActivityAboutBinding

class AboutActivity : ComponentActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.aboutText.text = HtmlCompat.fromHtml("""
            <h1>About</h1>
            <p>
                <strong>Tardy Scanner</strong> is a simple app that scans QR codes and saves the date and time of the scan,
                as well as the student's LRN, name, and section.
                It is intended to be used by teachers to record the tardiness of their students.
            </p>
            <p>
                This app is made by Juan Miguel L. Villegas, a student of Pasig City Science High School,
                under the guidance and supervision of Ms. Carliss Salangsang.
            </p>
        """.trimIndent(), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun onClick(view: View?) {
        if (view?.id == binding.backButton.id) {
            finish()
        }
    }
}
