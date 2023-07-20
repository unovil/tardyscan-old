package com.unovil.tardyscanner

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import com.unovil.tardyscanner.addrecord.TABLE_NAME
import com.unovil.tardyscanner.databinding.ActivitySuccessfulBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.PostgrestResult
import io.github.jan.supabase.postgrest.query.Returning
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.Headers
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*


class SuccessfulActivity : ComponentActivity(), View.OnClickListener {

    companion object {
        const val LrnID = "lrn_id"
        const val tardyListID = "tardy_datetimes"
    }

    // initializes supabase connection
    private lateinit var client: SupabaseClient

    /**
     * This is a serializable data class that holds the data to be inserted into the database, as
     * required by the `supabase-kt` library.
     * @param lrnId LRN ID
     * @param name student name
     * @param section student section
     * @param tardyDateTimes list of [Instant] objects containing the date and time of tardiness
     */
    @Serializable
    data class Tardy(
        @SerialName(LrnID) val lrnId: String,
        val name: String,
        val section: String,
        @SerialName(tardyListID) @Contextual val tardyDateTimes: List<Instant>
    )

    /**
     * This is a simple data class that holds the response headers and body. This is used by
     * the [insertData] and [updateData] functions.
     * @param headers response headers
     * @param body response body
     */
    data class Response(val headers: Headers, val body: JsonElement?)

    private lateinit var binding: ActivitySuccessfulBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessfulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = createSupabaseClient(
            supabaseUrl = intent.getStringExtra("SUPABASE_URL") ?: "",
            supabaseKey = intent.getStringExtra("SUPABASE_KEY") ?: ""
        ) {
            install(Postgrest) {
                propertyConversionMethod = PropertyConversionMethod.SERIAL_NAME
            }
        }

        val timeInstantKotlin = Clock.System.now()
        val timeDate = Calendar.getInstance()
            .apply { timeInMillis = timeInstantKotlin.toEpochMilliseconds() }.time

        // sets school logo
        binding.logoImageView.contentDescription = "Pasig City Science High School" + " logo"
        binding.logoImageView.setImageResource(R.drawable.school_image)

        // sets text
        binding.nameTextField.text = intent.getStringExtra("name")
        binding.sectionTextField.text = intent.getStringExtra("section")
        binding.lrnTextField.text = intent.getStringExtra("lrn")
        binding.successTextView.text =
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(timeDate)
        binding.timeTextView.text =
            SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(timeDate)

        // continually tries to update or insert to supabase
        // if job is completed, enable button behavior
        // if caught in a request timeout, repeat
        var isSuccessful: Boolean
        while (true) {
            val jobGet = CoroutineScope(Dispatchers.IO).launch { updateDatabase(timeInstantKotlin) }
            runBlocking {
                jobGet.join()
                if (jobGet.isCompleted) {
                    binding.confirmButton.isVisible = true
                    binding.confirmButton.isEnabled = true
                    isSuccessful = true
                } else isSuccessful = false
            }
            if (isSuccessful) { break }
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Something went wrong! Trying again...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Updates the database. This function uses [withContext] to create a context-filled
     * [CoroutineDispatcher] object. This fetches data from the database, and checks if the
     * the response body is empty. If it is, it inserts a new record with [insertData]. If it is
     * not, it updates the current record with [updateData]. This function also handles
     * exceptions thrown by [insertData] and [updateData].
     * @param timeInstant current time
     * @return a context-filled CoroutineDispatcher object
     */
    private suspend fun updateDatabase(timeInstant: Instant) {
        return withContext(Dispatchers.IO) {
            while (true) {
                try {
                    // fetches data
                    val selectResult = client.postgrest[TABLE_NAME]
                        .select {
                            Tardy::lrnId eq intent.getStringExtra("lrn")
                        }

                    // checks if record number is not 1.
                    // if true (it's 2), insert new record.
                    // if false (it's not 2), update current record.
                    if (selectResult.body!!.toString().length == 2) { // no record yet
                        val (headers, body) = insertData(timeInstant)
                        Log.i("SuccessfulActivity", "Insert headers: $headers")
                        Log.i("SuccessfulActivity", "Insert body: $body")
                    } else {
                        val (headers, body) = updateData(timeInstant, selectResult)
                        Log.i("SuccessfulActivity", "Update headers: $headers")
                        Log.i("SuccessfulActivity", "Update body: $body")
                    }
                    break
                } catch (hrtex: HttpRequestTimeoutException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@SuccessfulActivity,
                            "Request timeout! Retrying...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (hrex: HttpRequestException) {
                    val builder = AlertDialog.Builder(this@SuccessfulActivity)
                    runOnUiThread {
                        builder
                            .setTitle("Internet problem")
                            .setMessage("You are not connected to the Internet. Relaunch the app with Internet.")
                            .setPositiveButton("OK") { _, _ ->
                                val intent =
                                    Intent(this@SuccessfulActivity, MainActivity::class.java)
                                this@SuccessfulActivity.startActivity(intent)
                                finishAffinity()
                            }
                            .show()
                    }
                    break
                }
            }
        }
    }

    /**
     * Inserts data into the database if the record doesn't exist yet.
     * Note that this function is used inside the [updateDatabase] function, which handles the
     * exceptions thrown by this function.
     * @see updateDatabase
     * @param timeInstant current time
     * @return [Response] object containing the headers and body of the request
     * @exception HttpRequestTimeoutException thrown when the request times out
     * @exception HttpRequestException thrown when the request fails
     */
    private suspend fun insertData(timeInstant: Instant) : Response {
        /* TODO: Ask Miss if this is okay, or if I should just alert the user that the person doesn't exist yet */
        val insertResult = client.postgrest[TABLE_NAME]
            .insert(
                Tardy(
                    intent.getStringExtra("lrn") ?: "default",
                    intent.getStringExtra("name") ?: "default",
                    intent.getStringExtra("section") ?: "default",
                    listOf(timeInstant)
                )
            )

        return Response(insertResult.headers, insertResult.body)
    }

    /**
     * Updates data of an existing record in the database.
     * Note that this function is used inside the [updateDatabase] function, which handles the
     * exceptions thrown by this function.
     * @see updateDatabase
     * @param timeInstant current time
     * @return [Response] object containing the headers and body of the request
     * @exception HttpRequestTimeoutException thrown when the request times out
     * @exception HttpRequestException thrown when the request fails
     */
    private suspend fun updateData(timeInstant: Instant, selectResult: PostgrestResult) : Response {
        val tardyList =
            selectResult.body!!.jsonArray[0].jsonObject[tardyListID]?.let {
                Json.decodeFromJsonElement<List<Instant>>(it)
            }
        val updateResult = client.postgrest[TABLE_NAME]
            .update(
                returning = Returning.REPRESENTATION,
                update = {
                    Tardy::tardyDateTimes setTo tardyList?.plusElement(timeInstant)
                }
            ) {
                Tardy::lrnId eq intent.getStringExtra("lrn")
            }

        return Response(updateResult.headers, updateResult.body)
    }

    // if clicked, exit activity
    override fun onClick(view: View?) {
        if (view?.id == R.id.confirmButton) {
            finish()
        }
    }
}
