package com.washcloud.consoleapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.utils.lightGreen
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.primaryDark
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

                var showLoginForm by remember { mutableStateOf(false) }
                var showAd2Form by remember { mutableStateOf(true) }
                var showHelpForm by remember { mutableStateOf(false) }

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
                        if (showAd2Form)
                            MainScreen(
                                {
                                    showAd2Form = true
                                    showHelpForm = false
                                    showLoginForm = false
                                },
                                {//show login form
                                    showLoginForm = true
                                    showAd2Form = false
                                },
                                {//show help form
                                    showAd2Form = false
                                    showHelpForm = true
                                },
                            )
                        if (showLoginForm)
                            LoginForm {
                                showLoginForm = false
                                showAd2Form = true
                            }
                        if (showHelpForm)
                            HelpForm {
                                showAd2Form = true
                                showHelpForm = false
                            }

                    }
                }
            }
        }
    }


    @Composable
    fun MainScreen(
        showAd2: () -> Unit,
        showStaffLogin: () -> Unit,
        showHelpForm: () -> Unit,
    ) {

        Column(
            modifier = Modifier
                .width(screenWidth)
        ) {

            Row(
                Modifier
                    .height(0.08 * screenHeight)
                    .width(screenWidth)
            ) {
                Box(
                    modifier =
                    Modifier
                        .weight(1f, true)
                        .fillMaxHeight()
                )
                Box(
                    modifier =
                    Modifier
                        .weight(1f, true)
                        .fillMaxHeight()
                        .clickable {
                            showStaffLogin()
                        }
                )
            }


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

            BottomNavigation(showAd2, showHelpForm, false)

        }
    }

    @Composable
    fun BottomNavigation(
        showAd2: () -> Unit, showHelpForm: () -> Unit,
        isHelpFormShown: Boolean
    ) {
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
                        .weight(1f)
                        .clickable {
                            if (!isHelpFormShown) {
                                showHelpForm()
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(R.drawable.help),
                        "help",
                        colorFilter = if (isHelpFormShown) ColorFilter.tint(color = secondaryColor) else null,
                        modifier = Modifier
                            .width(0.05 * screenWidth)
                            .height(0.05 * screenWidth)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.help),
                        style = TextStyle(
                            color = if (isHelpFormShown) secondaryColor else unSelectedTextColor,
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

    @Composable
    fun LoginForm(showAd2: () -> Unit) {

        Column(
            modifier = Modifier
                .width(screenWidth)
        ) {


            Column(
                modifier = Modifier
                    .height(screenHeight)
                    .width(screenWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.staff_login),
                    style = TextStyle(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryDark
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        text = "02:30",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = borderColor
                        ),
                        textAlign = TextAlign.Start,
                    )
                    Box(
                        modifier =
                        Modifier.weight(1f)
                    )
                    Text(
                        text = "02/03/2024",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = borderColor
                        ),
                        textAlign = TextAlign.Start,
                    )
                    Spacer(modifier = Modifier.width(24.dp))

                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .border(
                            color = borderColor,
                            width = 1.dp,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .fillMaxWidth()
                        .height(0.72 * screenHeight)
                ){
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .border(
                                color = borderColor,
                                width = 1.dp,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .fillMaxWidth()
                    ){
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.mobile_number),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    color = Color.Black
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .border(
                                        color = borderColor,
                                        width = 1.dp,
                                        shape = RoundedCornerShape(36.dp)
                                    )
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(36.dp)
                                    )
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
                            ){
                                Text(text = "        ",
                                    style = TextStyle(
                                        color = hints
                                    )
                                )
                            }
                            Text(
                                text = stringResource(id = R.string.password),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    color = Color.Black
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .border(
                                        color = borderColor,
                                        width = 1.dp,
                                        shape = RoundedCornerShape(36.dp)
                                    )
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(36.dp)
                                    )
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
                            ){
                                Text(text = "XXXX-XXXX-XXXX",
                                    style = TextStyle(
                                        color = hints
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(start = 24.dp, end = 24.dp)
                                    .clip(
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                blueGradient,
                                                secondaryColor,
                                            ),
                                        )
                                    )
                                    .padding(
                                        start = 16.dp,
                                        top = 12.dp,
                                        end = 16.dp,
                                        bottom = 12.dp
                                    )
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Text(text = stringResource(id = R.string.login),
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Box(
                    modifier =
                    Modifier.weight(1f)
                )
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
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(12.dp)
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
                                .clickable {
                                    showAd2()
                                }
                        ) {
                            Row {
                                Image(
                                    painterResource(R.drawable.arrow_back),
                                    "arrow_back",
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(id = R.string.back),
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }
                        Box(
                            modifier =
                            Modifier.weight(1f)
                        )
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painterResource(R.drawable.timer),
                                "timer",
                                modifier = Modifier
                                    .width(0.05 * screenWidth)
                                    .height(0.05 * screenWidth)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "03:59",
                                style = TextStyle(
                                    color = secondaryColor,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }


        }
    }

    @Composable
    fun HelpForm(showAd2: () -> Unit) {

        Column(
            modifier = Modifier
                .width(screenWidth)
        ) {


            Column(
                modifier = Modifier
                    .height(screenHeight)
                    .width(screenWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Spacer(modifier = Modifier.width(24.dp))
                    Column {
                        Spacer(modifier = Modifier.padding(top = 12.dp))
                        Image(
                            painterResource(R.drawable.arrow_back),
                            "arrow_back",
                            colorFilter = ColorFilter.tint(color = primaryDark),
                            modifier = Modifier
                                .width(24.dp)
                                .height(24.dp)
                                .clickable {
                                    showAd2()
                                }
                        )
                    }
                    Box(
                        modifier =
                        Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(id = R.string.help),
                        style = TextStyle(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryDark
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Box(
                        modifier =
                        Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(24.dp))

                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .border(
                            color = borderColor,
                            width = 1.dp,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .fillMaxWidth()
                        .height(0.72 * screenHeight)
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        Image(
                            painterResource(R.drawable.steps),
                            "steps",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .width(screenWidth - 50.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Spacer(modifier = Modifier.width(24.dp))
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .width(50.dp)
                                    .height(50.dp)
                                    .background(lightGreen, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painterResource(R.drawable.outline_phone),
                                    "phone",
                                    colorFilter = ColorFilter.tint(color = secondaryColor),
                                )
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Text(
                                text = "920031915",
                                style = TextStyle(
                                    color = Color.Black,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Spacer(modifier = Modifier.width(24.dp))
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .width(50.dp)
                                    .height(50.dp)
                                    .background(lightGreen, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painterResource(R.drawable.outline_email),
                                    "email",
                                    colorFilter = ColorFilter.tint(color = secondaryColor),
                                )

                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Text(
                                text = "info@horizonscloud.net",
                                style = TextStyle(
                                    color = Color.Black,
                                    fontSize = 18.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Spacer(modifier =
                        Modifier
                            .height(1.dp)
                            .background(color = borderColor)
                            .padding(start = 24.dp, end = 24.dp)
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.quick_open),
                            style = TextStyle(
                                fontSize = 24.sp,
                                color = Color.Black
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(screenWidth)
                        )
                        Box(
                            modifier = Modifier
                                .padding(24.dp)
                                .border(
                                    color = borderColor,
                                    width = 1.dp,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .fillMaxWidth()
                        ){
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(id = R.string.enter_security),
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                        color = Color.Black
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .border(
                                            color = borderColor,
                                            width = 1.dp,
                                            shape = RoundedCornerShape(36.dp)
                                        )
                                        .background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(36.dp)
                                        )
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
                                ){
                                    Text(text = "XXXX-XXXX-XXXX",
                                        style = TextStyle(
                                            color = hints
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(start = 24.dp, end = 24.dp)
                                        .clip(
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    blueGradient,
                                                    secondaryColor,
                                                ),
                                            )
                                        )
                                        .padding(
                                            start = 16.dp,
                                            top = 8.dp,
                                            end = 16.dp,
                                            bottom = 8.dp
                                        )
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 24.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(text = stringResource(id = R.string.open),
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = 20.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                            }
                        }
                    }
                }
                Box(
                    modifier =
                    Modifier.weight(1f)
                )
                BottomNavigation(showAd2, {}, true)
            }


        }
    }
}


