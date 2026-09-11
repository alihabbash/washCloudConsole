package com.washcloud.consoleapplication.ui.common

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.admin.login.AdminLoginViewModel
import com.washcloud.consoleapplication.utils.*

@Composable
fun AdminPasswordPad(
    modifier: Modifier = Modifier,
    viewModel: AdminLoginViewModel,
    screenWidth: Dp,
    screenHeight: Dp,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val isPasswordCorrect by viewModel.isPasswordCorrect.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()
    val passwordSelectedLoginForm by remember { mutableStateOf(true) }

    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                blueGradient,
                secondaryColor,
            )
        )
    }

    LaunchedEffect(isPasswordCorrect) {
        if (isPasswordCorrect) {
            onSuccess()
            viewModel.resetPasswordCorrectState()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.resetPasswordCorrectState()
    }

    val clearSelectedField = {
        viewModel.passwordText.value = ""
    }

    val updateSelectedField = { value: String ->
        viewModel.passwordText.value += value
    }

    Box(
        modifier = modifier
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
    ) {
            Column {
                Spacer(modifier = Modifier.height((screenHeight.value * 0.02f).dp))

                Text(
                    text = stringResource(id = R.string.password),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.025f).sp,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 24.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .border(
                            color = if (passwordSelectedLoginForm)
                                primaryDark else borderColor,
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
                            bottom = (screenHeight.value * 0.01f).dp, start = 24.dp
                        )
                ) {
                    AdminPasswordDisplay(viewModel = viewModel, screenWidth = screenWidth)
                }
                Spacer(modifier = Modifier.height((screenHeight.value * 0.01f).dp))
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp)
                        .clip(
                            RoundedCornerShape((screenHeight.value * 0.011f).dp)
                        )
                        .background(
                            brush = gradientBrush
                        )
                        .padding(
                            start = 16.dp,
                            top = (screenHeight.value * 0.01f).dp,
                            end = 16.dp,
                            bottom = (screenHeight.value * 0.01f).dp
                        )
                        .fillMaxWidth()
                        .clickable {
                            viewModel.verifyPassword()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.open),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (screenWidth.value * 0.028f).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height((screenHeight.value * 0.02f).dp))
                
                // Numpad rows
                val buttonWidth = 0.2f * screenWidth
                val buttonHeight = 0.08f * screenHeight
                
                @Composable
                fun NumpadRow(texts: List<String>, values: List<String>, isLastRow: Boolean = false) {
                    Row(
                        modifier = Modifier
                            .padding(start = 40.dp, end = 40.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in texts.indices) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = if (isLastRow && i == 2) gradientBrush else Brush.linearGradient(listOf(numbersBackground, numbersBackground)),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .width(buttonWidth)
                                    .height(buttonHeight)
                                    .clickable {
                                        if (isLastRow && i == 0) {
                                            clearSelectedField()
                                        } else if (isLastRow && i == 2) {
                                            viewModel.verifyPassword()
                                        } else {
                                            updateSelectedField(values[i])
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = texts[i],
                                    style = TextStyle(
                                        color = if (isLastRow && i == 0) clearText else if (isLastRow && i == 2) Color.White else Color.Black,
                                        fontSize = if (isLastRow && i == 0) (screenWidth.value * 0.03f).sp else if (isLastRow && i == 2) (screenWidth.value * 0.03f).sp else (screenWidth.value * 0.05f).sp
                                    )
                                )
                            }
                            if (i < texts.size - 1) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                    }
                }

                NumpadRow(
                    texts = listOf(stringResource(id = R.string.one), stringResource(id = R.string.two), stringResource(id = R.string.three)),
                    values = listOf("1", "2", "3")
                )
                Spacer(modifier = Modifier.height(20.dp))
                NumpadRow(
                    texts = listOf(stringResource(id = R.string.four), stringResource(id = R.string.five), stringResource(id = R.string.six)),
                    values = listOf("4", "5", "6")
                )
                Spacer(modifier = Modifier.height(20.dp))
                NumpadRow(
                    texts = listOf(stringResource(id = R.string.seven), stringResource(id = R.string.eight), stringResource(id = R.string.nine)),
                    values = listOf("7", "8", "9")
                )
                Spacer(modifier = Modifier.height(20.dp))
                NumpadRow(
                    texts = listOf(stringResource(id = R.string.clear), stringResource(id = R.string.zero), stringResource(id = R.string.confirm)),
                    values = listOf("", "0", ""),
                    isLastRow = true
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

    if (showAlert) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(dimBackground)
                .clickable {
                    viewModel.dismissAlert()
                }

        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(0.02 * screenWidth)
                    )
                    .background(color = Color.White)
                    .width(0.8 * screenWidth)
                    .height(0.16 * screenHeight)
                    .clickable(enabled = false) {},
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
                            .clip(
                                RoundedCornerShape((screenHeight.value * 0.011f).dp)
                            )
                            .background(
                                brush = gradientBrush
                            )
                            .padding(
                                start = 16.dp,
                                top = (screenHeight.value * 0.01f).dp,
                                end = 16.dp,
                                bottom = (screenHeight.value * 0.01f).dp
                            )
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp)
                            .clickable {
                                viewModel.dismissAlert()

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
fun AdminPasswordDisplay(viewModel: AdminLoginViewModel, screenWidth: Dp) {
    val password by viewModel.passwordText.collectAsState()
    Text(
        text = if (password.isEmpty()) {
            "XXXX-XXXX-XXXX"
        } else {
            "*".repeat(password.length)
        },
        style = TextStyle(
            color = hints,
            fontSize = (screenWidth.value * 0.025f).sp
        )
    )
}
