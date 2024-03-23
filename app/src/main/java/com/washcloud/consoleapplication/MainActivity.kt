package com.washcloud.consoleapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.screenBackground
import com.washcloud.consoleapplication.utils.secondaryColor
import com.washcloud.consoleapplication.utils.unSelectedTextColor

class MainActivity : ComponentActivity() {
    private var screenHeight = 0.0.dp
    private var screenWidth = 0.0.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsoleApplicationTheme {
                screenHeight = LocalConfiguration.current.screenHeightDp.dp
                screenWidth = LocalConfiguration.current.screenWidthDp.dp
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Box {
                        Image(
                            painterResource(R.drawable.background),
                            "background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillWidth
                        )
                        MainScreen()

                    }
                }
            }
        }
    }


    @Composable
    fun MainScreen() {

        Column(
            modifier = Modifier
                .width(screenWidth)
        ) {
            Box(
                modifier = Modifier
                    .height(0.08 * screenHeight)
                    .background(color = Color.White)
            )

            Column(
                modifier = Modifier
                    .height(0.78 * screenHeight)
                    .width(screenWidth)
                    .background(color = screenBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painterResource(R.drawable.empty_image),
                    "ad_2",
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.ad2),
                    style = TextStyle(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = lightGrey
                    ),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(0.04 * screenHeight))

            BottomNavigation()

        }
    }

    @Composable
    fun BottomNavigation() {
        Box(
            modifier = Modifier
                .width(screenWidth)
                .height(0.1 * screenHeight)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.06 * screenWidth, end = 0.06 * screenWidth),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(R.drawable.help),
                        "help",
                        modifier = Modifier
                            .width(0.05 * screenWidth)
                            .height(0.05 * screenWidth)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.help),
                        style = TextStyle(
                            color = unSelectedTextColor,
                            fontSize = 15.sp
                        )
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(2f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(24.dp)
                            )
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        blueGradient,
                                        secondaryColor,
                                    ),
                                )
                            )
                            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                    ) {
                        Row {
                            Image(
                                painterResource(R.drawable.phone),
                                "phone",
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "920031915",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(R.drawable.language),
                        "language",
                        modifier = Modifier
                            .width(0.05 * screenWidth)
                            .height(0.05 * screenWidth)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.language),
                        style = TextStyle(
                            color = unSelectedTextColor,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }

}


