package com.unovil.tardyscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonSyntaxException
import com.unovil.tardyscanner.databinding.ActivityScannerBinding
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named


/**
 * @author JUAN MIGUEL L. VILLEGAS
 *
 * DO NOT REPRODUCE THE PROGRAM IN ANY FORM.
 * all rights reserved, 2023.
 */
class ScannerActivity : ComponentActivity(), View.OnClickListener {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var codescanner: CodeScanner
    private val secretKey: String by inject(named("secretKey"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
            }
        else {
            startScanning()
        }
    }

    private fun startScanning() {
        val scannerView: CodeScannerView = binding.scannerView

        codescanner = CodeScanner(this, scannerView)
        codescanner.camera = CodeScanner.CAMERA_BACK
        codescanner.formats = CodeScanner.TWO_DIMENSIONAL_FORMATS

        codescanner.autoFocusMode = AutoFocusMode.SAFE
        codescanner.scanMode = ScanMode.CONTINUOUS
        codescanner.isAutoFocusEnabled = true
        codescanner.isFlashEnabled = false

        codescanner.decodeCallback = DecodeCallback {
            val decryptedString: String? = decryptWithAES(secretKey, it.text)
            if ((decryptedString != null) && !decryptedString[0].isDigit()) {
                try {
                    val deserializedMap: Map<String, String> = Gson().fromJson(
                        decryptedString,
                        object : TypeToken<Map<String, String>>() {}.type
                    )

                    val intent = Intent(this, SuccessfulActivity::class.java)
                    intent.putExtra("name", deserializedMap.getValue("Name").trim())
                    intent.putExtra("section", deserializedMap.getValue("Section").trim())
                    intent.putExtra("lrn", deserializedMap.getValue("LRN").trim())
                    startActivity(intent)
                } catch (jsex: JsonSyntaxException) {
                    runOnUiThread {
                        Toast.makeText(this, "Not valid JSON!", Toast.LENGTH_SHORT).show()
                    }
                    Log.w("ScannerActivity", "Not valid JSON: $jsex")
                } catch (nseex: NoSuchElementException) {
                    runOnUiThread {
                        Toast.makeText(this, "Not valid JSON fields!", Toast.LENGTH_SHORT).show()
                    }
                    Log.w("ScannerActivity", "Not valid fields: $nseex")
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Not valid QR! Text:\n${it.text}", Toast.LENGTH_SHORT).show()
                }
                Log.w("ScannerActivity", "Not valid QR: ${it.text}")
            }
        }

        scannerView.setOnClickListener {
            codescanner.startPreview()
        }
    }

    @Deprecated("I still use it since the new method requires API level 30.")
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                startScanning()
            }
            else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codescanner.isInitialized) {
            codescanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codescanner.isInitialized) {
            codescanner.releaseResources()
        }
        super.onPause()
    }

    override fun onClick(view: View?) {
        if (view?.id == binding.aboutButton.id) {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }
}