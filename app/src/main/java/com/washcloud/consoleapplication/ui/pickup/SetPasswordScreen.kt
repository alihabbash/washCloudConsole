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
import androidx.compose.ui.platform.LocalContext
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.utils.*

@Composable
fun SetPasswordScreen(
    showAd2: () -> Unit,
    onBack: () -> Unit,
    onPasswordSetSuccess: (String, String) -> Unit,
    onPhoneValidate: (String, (Boolean) -> Unit) -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
) {
    var phoneText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var selectedField by remember { mutableIntStateOf(0) }
    var isConfirmStage by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(blueGradient, secondaryColor)
        )
    }

    val updateSelectedField = { value: String ->
        if (!isConfirmStage) {
            if (selectedField == 0) {
                if (phoneText.length < 9) phoneText += value
            } else {
                if (passwordText.length < 4) passwordText += value
            }
        } else {
            if (confirmPasswordText.length < 4) confirmPasswordText += value
        }
    }

    val clearSelectedField = {
        if (!isConfirmStage) {
            if (selectedField == 0) phoneText = ""
            else passwordText = ""
        } else {
            confirmPasswordText = ""
        }
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
                text = stringResource(id = R.string.setup_offline_pin),
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

                            if (!isConfirmStage) {
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
                                    text = stringResource(id = R.string.create_4_digit_pin),
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
                            } else {
                                Text(
                                    text = stringResource(id = R.string.confirm_4_digit_pin),
                                    style = TextStyle(
                                        fontSize = (screenWidth.value * 0.025f).sp,
                                        color = Color.Black
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                                CustomerPasswordInputBox(
                                    passwordText = confirmPasswordText,
                                    selectedField = 1,
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight,
                                    selectField = { } // Cannot change focus in confirm stage
                                )
                            }
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
                            if (!isConfirmStage) {
                                if (phoneText.isBlank()) {
                                    alertMessage = context.getString(R.string.mobile_number_cannot_be_empty) // Using a string resource if available, or just fallback
                                    showAlert = true
                                } else if (passwordText.length == 4) {
                                    onPhoneValidate(phoneText) { isValid ->
                                        if (isValid) {
                                            isConfirmStage = true
                                        } else {
                                            alertMessage = context.getString(R.string.invalid_phone_number)
                                            showAlert = true
                                        }
                                    }
                                } else {
                                    alertMessage = context.getString(R.string.pin_must_be_4_digits)
                                    showAlert = true
                                }
                            } else {
                                if (confirmPasswordText == passwordText) {
                                    onPasswordSetSuccess(phoneText, passwordText)
                                } else {
                                    alertMessage = context.getString(R.string.pins_do_not_match)
                                    showAlert = true
                                }
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
                    confirmPasswordText = ""
                    isConfirmStage = false
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
                        text = alertMessage,
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.03f).sp,
                            color = primaryDark
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
                                confirmPasswordText = ""
                                isConfirmStage = false
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
fun CustomerPhoneInputBox(phoneText: String, selectedField: Int, screenWidth: Dp, screenHeight: Dp, selectField: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(24.dp)
            .border(
                color = if (selectedField == 0) primaryDark else borderColor,
                width = 1.dp,
                shape = RoundedCornerShape(36.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(36.dp)
            )
            .fillMaxWidth()
            .padding(
                top = (screenHeight.value * 0.01f).dp,
                bottom = (screenHeight.value * 0.01f).dp,
                start = 24.dp
            )
            .clickable {
                selectField()
            }
    ) {
        Text(
            text = phoneText,
            style = TextStyle(
                color = hints,
                fontSize = (screenWidth.value * 0.025f).sp
            )
        )
    }
}

@Composable
fun CustomerPasswordInputBox(passwordText: String, selectedField: Int, screenWidth: Dp, screenHeight: Dp, selectField: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(24.dp)
            .border(
                color = if (selectedField == 1) primaryDark else borderColor,
                width = 1.dp,
                shape = RoundedCornerShape(36.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(36.dp)
            )
            .fillMaxWidth()
            .padding(
                top = (screenHeight.value * 0.01f).dp,
                bottom = (screenHeight.value * 0.01f).dp,
                start = 24.dp
            )
            .clickable {
                selectField()
            }
    ) {
        Text(
            text = if (passwordText.isEmpty()) {
                "XXXX-XXXX-XXXX"
            } else {
                "*".repeat(passwordText.length)
            },
            style = TextStyle(
                color = hints,
                fontSize = (screenWidth.value * 0.025f).sp
            )
        )
    }
}

@Composable
fun PasswordDisplay(passwordText: String, screenWidth: Dp) {
    Text(
        text = if (passwordText.isEmpty()) "XXXX" else "*".repeat(passwordText.length),
        style = TextStyle(
            color = hints,
            fontSize = (screenWidth.value * 0.025f).sp
        )
    )
}
