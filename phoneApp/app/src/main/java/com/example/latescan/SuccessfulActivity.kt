package com.example.latescan

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.plugins.*
import kotlinx.android.synthetic.main.activity_successful.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*

const val tardyListID = "tardy_datetimes"

class SuccessfulActivity : ComponentActivity(), View.OnClickListener {

    // initializes supabase connection
    private val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }

    // format of Students table
    @Serializable
    data class Tardy(
        @SerialName("lrn_id") val lrnId: String,
        val name: String,
        val section: String,
        @SerialName(tardyListID) @Contextual val tardyDateTimes: List<Instant>
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_successful)

        val timeInstantKotlin = Clock.System.now()
        val timeDate = Calendar.getInstance().apply { timeInMillis = timeInstantKotlin.toEpochMilliseconds() }.time

        // sets text
        nameTextField.text = intent.getStringExtra("name")
        sectionTextField.text = intent.getStringExtra("section")
        lrnTextField.text = intent.getStringExtra("lrn")
        successTextView.text = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(timeDate)
        timeTextView.text = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(timeDate)

        // continually tries to update or insert to supabase
        // if job is completed, enable button behavior
        // if caught in a request timeout, repeat
        var isSuccessful: Boolean
        while (true) {
            try {
                val jobGet = CoroutineScope(Dispatchers.IO).launch { updateDatabase(timeInstantKotlin) }
                runBlocking {
                    jobGet.join()
                    if (jobGet.isCompleted) {
                        confirmButton.isVisible = true
                        confirmButton.isEnabled = true
                        isSuccessful = true
                    } else isSuccessful = false
                }
                if (isSuccessful) { break }
                else {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Something went wrong! Trying again...",
                            Toast.LENGTH_SHORT).show()
                    }
                    continue
                }

            } catch (hrtex: HttpRequestTimeoutException) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Request timeout! Retrying...",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (hrex: HttpRequestException) {
                val builder = AlertDialog.Builder(this)
                builder
                    .setTitle("Internet problem")
                    .setMessage("You are not connected to the Internet. Relaunch the app with Internet.")
                    .setPositiveButton("OK") { _, _ -> }
                break
            }
        }

    }

    suspend fun updateDatabase(timeInstant: Instant) {

        return withContext(Dispatchers.IO) {
            // fetches data
            val selectResult = client.postgrest[TABLE_NAME]
                .select {
                    Tardy::lrnId eq intent.getStringExtra("lrn")
                }

            // checks if record number is not 1.
            // if true (it's 2), insert new record.
            // if false (it's not 2), update current record.
            println(selectResult)
            if (selectResult.body.toString().length == 2) { // no record yet
                val insertResult = client.postgrest[TABLE_NAME]
                    .insert(
                        Tardy(
                            intent.getStringExtra("lrn") ?: "default",
                            intent.getStringExtra("name") ?: "default",
                            intent.getStringExtra("section") ?: "default",
                            listOf(timeInstant)
                        )
                    )
                println("Insertheaders:\n${insertResult.headers}")
                println("Insertbody:\n${insertResult.body}")
            } else {
                val tardyList = selectResult.body.jsonArray[0].jsonObject[tardyListID]?.let {
                    Json.decodeFromJsonElement<List<Instant>>(it)
                }
                println(tardyList)
                val updateResult = client.postgrest[TABLE_NAME]
                    .update(
                        {
                            Tardy::tardyDateTimes setTo tardyList?.plusElement(timeInstant)
                        }
                    ) {
                        Tardy::lrnId eq intent.getStringExtra("lrn")
                    }
                println("Updateheaders:\n${updateResult.headers}")
                println("Updatebody:\n${updateResult.body}")
            }
        }
    }

    // if clicked, exit activity
    override fun onClick(view: View?) {
        if (view?.id == confirmButton.id) {
            finish()
        }
    }
}
