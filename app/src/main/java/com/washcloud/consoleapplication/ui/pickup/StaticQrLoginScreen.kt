package com.washcloud.consoleapplication.ui.pickup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.utils.*

@Composable
fun StaticQrLoginScreen(
    showAd2: () -> Unit,
    onBack: () -> Unit,
    verifyPin: (String, String, (Boolean) -> Unit) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
) {
    var phoneText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var selectedField by remember { mutableIntStateOf(0) }
    var showAlert by remember { mutableStateOf(false) }

    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(blueGradient, secondaryColor)
        )
    }

    val updateSelectedField = { value: String ->
        if (selectedField == 0) {
            if (phoneText.length < 9) phoneText += value
        } else {
            if (passwordText.length < 4) passwordText += value
        }
    }

    val clearSelectedField = {
        if (selectedField == 0) phoneText = ""
        else passwordText = ""
    }

    Box(
        modifier = Modifier
            .width(screenWidth)
            .height(screenHeight)
    ) {
        // Full-screen gradient background image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .width(screenWidth)
                .height(screenHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.customer_pickup_offline_qr),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.04f).sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryDark
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))

                dateAndTimeView(screenWidth)
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
                        .height(0.72f * screenHeight)
                ) {
                    Column {
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
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height((screenHeight.value * 0.02f).dp))

                                Text(
                                    text = stringResource(id = R.string.mobile_number),
                                    style = TextStyle(
                                        fontSize = (screenWidth.value * 0.025f).sp,
                                        color = Color.Black
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                                CustomerPhoneInputBox(
                                    phoneText = phoneText,
                                    selectedField = selectedField,
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight,
                                    selectField = { selectedField = 0 }
                                )
                                
                                Text(
                                    text = stringResource(id = R.string.enter_your_4_digit_pin),
                                    style = TextStyle(
                                        fontSize = (screenWidth.value * 0.025f).sp,
                                        color = Color.Black
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                                CustomerPasswordInputBox(
                                    passwordText = passwordText,
                                    selectedField = selectedField,
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight,
                                    selectField = { selectedField = 1 }
                                )
                                Spacer(modifier = Modifier.height((screenHeight.value * 0.01f).dp))
                            }
                        }
                        Spacer(modifier = Modifier.height((screenHeight.value * 0.03f).dp))
                        
                        NumericKeypad(
                            screenWidth = screenWidth,
                            screenHeight = screenHeight,
                            gradientBrush = gradientBrush,
                            onNumberClick = updateSelectedField,
                            onClearClick = clearSelectedField,
                            onConfirmClick = {
                                if (phoneText.isBlank()) {
                                    showAlert = true
                                } else if (passwordText.length == 4) {
                                    verifyPin(phoneText, passwordText) { isCorrect ->
                                        if (!isCorrect) {
                                            showAlert = true
                                        }
                                    }
                                } else {
                                    showAlert = true
                                }
                            }
                        )
                    }
                }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavigationWithBackAndTimer(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isAdmin = false,
                timerViewModel = null,
                showAd2 = showAd2,
                onBack = onBack
            )
        }
    }

    if (showAlert) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(screenWidth)
                .height(screenHeight)
                .background(dimBackground)
                .clickable {
                    showAlert = false
                    phoneText = ""
                    passwordText = ""
                    selectedField = 0
                }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(0.02f * screenWidth))
                    .background(color = Color.White)
                    .width(0.8f * screenWidth)
                    .height(0.16f * screenHeight),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.incorrect_password),
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.03f).sp,
                            color = primaryDark
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.incorrect_password_msg),
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.022f).sp,
                            color = hints
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp)
                            .clip(RoundedCornerShape((screenHeight.value * 0.011f).dp))
                            .background(brush = gradientBrush)
                            .padding(
                                start = 16.dp,
                                top = (screenHeight.value * 0.01f).dp,
                                end = 16.dp,
                                bottom = (screenHeight.value * 0.01f).dp
                            )
                            .fillMaxWidth()
                            .clickable {
                                showAlert = false
                                phoneText = ""
                                passwordText = ""
                                selectedField = 0
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.ok),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (screenWidth.value * 0.028f).sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumericKeypad(
    screenWidth: Dp,
    screenHeight: Dp,
    gradientBrush: Brush,
    onNumberClick: (String) -> Unit,
    onClearClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Column {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .padding(start = 40.dp, end = 40.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { num ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = numbersBackground,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .width(0.2f * screenWidth)
                            .height(0.08f * screenHeight)
                            .clickable { onNumberClick(num) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num,
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = (screenWidth.value * 0.05f).sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Last row with Clear, 0, Confirm
        Row(
            modifier = Modifier
                .padding(start = 40.dp, end = 40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = numbersBackground,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .width(0.2f * screenWidth)
                    .height(0.08f * screenHeight)
                    .clickable { onClearClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.clear),
                    style = TextStyle(
                        color = clearText,
                        fontSize = (screenWidth.value * 0.03f).sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = numbersBackground,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .width(0.2f * screenWidth)
                    .height(0.08f * screenHeight)
                    .clickable { onNumberClick("0") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "0",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = (screenWidth.value * 0.05f).sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .background(
                        brush = gradientBrush,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .width(0.2f * screenWidth)
                    .height(0.08f * screenHeight)
                    .clickable { onConfirmClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.confirm),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.03f).sp
                    )
                )
            }
        }
    }
}
