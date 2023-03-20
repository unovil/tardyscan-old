package com.example.latescan

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.latescan.ui.theme.LateScanTheme

class SuccessfulActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LateScanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SuccessActivity(intent.getStringExtra("decryptedString") ?: "default")
                }
            }
        }
    }

    fun goBack() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun SuccessActivity(decryptedString: String = """
    Student: Juan Miguel L. Villegas
    Section: 9 - Bohr
""".trimIndent()) {
    LateScanTheme {
        Column (
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text (
                text = "Success!",
                fontSize = 50.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(50.dp))

            Text (
                text = "Student Information",
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                modifier = Modifier.padding(50.dp))
            Text (
                text = decryptedString,
                modifier = Modifier.padding(10.dp),
                fontSize = 20.sp,
                textAlign = TextAlign.Left)
            Spacer(modifier = Modifier.height(20.dp))

            Row (modifier = Modifier.padding()){
                /*Button(onClick = { }, colors = ButtonDefaults.buttonColors(Color.Gray)) {
                    Text (text = "Cancel")
                }
                Spacer(modifier = Modifier.width(10.dp))*/
                Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(Color.Green)) {
                    Text (text = "Confirm")
                }
            }
        }
        
    }
}
