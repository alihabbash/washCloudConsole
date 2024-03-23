package com.washcloud.consoleapplication.mainad

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washcloud.consoleapplication.MainActivity
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.screenBackground

class MainAdActivity  : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsoleApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = screenBackground
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clickable {
                                finish()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(
                                    intent
                                )
                            }
                    ) {
                        Image(
                            painterResource(R.drawable.empty_image),
                            "ad_1",
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.ad1),
                            style = TextStyle(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = lightGrey
                            ),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

