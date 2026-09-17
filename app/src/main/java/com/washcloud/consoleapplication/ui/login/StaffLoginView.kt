package com.washcloud.consoleapplication.ui.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.ui.common.timerView
import com.washcloud.consoleapplication.ui.startStaff.DateTimeViewModel
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.clearText
import com.washcloud.consoleapplication.utils.dimBackground
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.utils.numbersBackground
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor
import kotlinx.coroutines.delay

//staff login form
@Composable
fun LoginForm(
    showAd2: () -> Unit,
    showDriverLogin: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
) {
    val viewModel: StaffLoginViewModel = hiltViewModel()

    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                blueGradient,
                secondaryColor,
            )
        )
    }
    val timerViewModel: TimerViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        timerViewModel.startTimeWatcher()
    }

    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    val clearSelectedField = {
        viewModel.accountText.value = ""
        viewModel.passwordText.value = ""
    }

    val updateSelectedField = { value: String -> //update selected field
        if (viewModel.selectedField.value == 1) {
            viewModel.passwordText.value += value
        } else {
            viewModel.accountText.value += value
        }
    }

    val selectField = { field: Int -> //select field 0 for mobile 1 for password
        viewModel.selectedField.value = field
    }
    val screenState by viewModel.uiState.collectAsState()
    val isLoading = screenState is LoginState.Loading
    if (screenState is LoginState.Success) {
        showDriverLogin()
        viewModel.resetLoadingToInitial()

    }

    LaunchedEffect(Unit) {
        viewModel.resetLoadingToInitial()
    }

    LaunchedEffect(screenState is LoginState.Error ) {
        Log.d("Login", "Error  $screenState")

        if(screenState is LoginState.Error){
            delay(10000L)
            viewModel.resetLoadingToInitial()
        }
    }

    Box {

        Column(
            modifier = Modifier
                .width(screenWidth)
                .then(interactionModifier)
//                .clickable {
//                    viewModel.resetLoadingToInitial()
//                }
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
                        .height(0.72 * screenHeight)
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
                                StaffAccountInputBox(viewModel = viewModel, screenWidth = screenWidth, screenHeight = screenHeight, selectField = selectField)
                                Text(
                                    text = stringResource(id = R.string.password),
                                    style = TextStyle(
                                        fontSize = (screenWidth.value * 0.025f).sp,
                                        color = Color.Black
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                                StaffPasswordInputBox(viewModel = viewModel, screenWidth = screenWidth, screenHeight = screenHeight, selectField = selectField)
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
                                        .padding(start = 24.dp, end = 24.dp)
                                        .clickable {
//                                            showDriverLogin()
                                            viewModel.login()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.login),
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = (screenWidth.value * 0.028f).sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height((screenHeight.value * 0.02f).dp))
                            }
                        }
                        Spacer(modifier = Modifier.height((screenHeight.value * 0.03f).dp))
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
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("1")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.one),
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
                                        color = numbersBackground,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("2")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.two),
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
                                        color = numbersBackground,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("3")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.three),
                                    style = TextStyle(
                                        color = Color.Black,
                                        fontSize = (screenWidth.value * 0.05f).sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
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
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("4")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.four),
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
                                        color = numbersBackground,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("5")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.five),
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
                                        color = numbersBackground,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("6")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.six),
                                    style = TextStyle(
                                        color = Color.Black,
                                        fontSize = (screenWidth.value * 0.05f).sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
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
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("7")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.seven),
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
                                        color = numbersBackground,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("8")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.eight),
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
                                        color = numbersBackground,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("9")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.nine),
                                    style = TextStyle(
                                        color = Color.Black,
                                        fontSize = (screenWidth.value * 0.05f).sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
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
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        clearSelectedField()
                                    },
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
                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        updateSelectedField("0")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.zero),
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

                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {
                                        viewModel.login()
                                    },
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
                Box(
                    modifier =
                    Modifier.weight(1f)
                )
                BottomNavigationWithBackAndTimer(screenWidth, screenHeight,   isAdmin = false, timerViewModel,showAd2, showAd2)
            }


        }

        if (isLoading || screenState is LoginState.Error)
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(screenWidth)
                    .height(screenHeight)
                    .background(dimBackground)
                    .clickable { if (screenState is LoginState.Error) viewModel.resetLoadingToInitial() }
            ) {
                Box(
                    modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(0.02 * screenWidth)
                        )
                        .background(color = Color.White)
                        .width(0.8 * screenWidth)
                        .height(0.3 * screenHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = hints,
                            modifier = Modifier
                                .width(0.15 * screenWidth)
                                .height(0.15 * screenWidth)
                        )
                        Spacer(modifier = Modifier.height(0.02 * screenHeight))
                        Text(
                            text = if (screenState is LoginState.Error) stringResource(id = R.string.wrong_credentials)  else stringResource(id = R.string.verify_wait) ,
                            style = TextStyle(
                                fontSize = (screenWidth.value * 0.04f).sp,
                                color = if (screenState is LoginState.Error) Color.Red else hints
                            )
                        )
                    }
                }
            }
    }
}

@Composable
fun StaffAccountDisplay(viewModel: StaffLoginViewModel, screenWidth: Dp) {
    val accountText by viewModel.accountText.collectAsState()
    Text(
        text = accountText,
        style = TextStyle(
            color = hints,
            fontSize = (screenWidth.value * 0.025f).sp
        )
    )
}

@Composable
fun StaffPasswordDisplay(viewModel: StaffLoginViewModel, screenWidth: Dp) {
    val password by viewModel.passwordText.collectAsState()
    Text(
        text =  if (password.isEmpty()) {
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

@Composable
fun StaffAccountInputBox(viewModel: StaffLoginViewModel, screenWidth: Dp, screenHeight: Dp, selectField: (Int) -> Unit) {
    val selectedField by viewModel.selectedField.collectAsState()
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
                selectField(0)
            }
    ) {
        StaffAccountDisplay(viewModel = viewModel, screenWidth = screenWidth)
    }
}

@Composable
fun StaffPasswordInputBox(viewModel: StaffLoginViewModel, screenWidth: Dp, screenHeight: Dp, selectField: (Int) -> Unit) {
    val selectedField by viewModel.selectedField.collectAsState()
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
                selectField(1)
            }
    ) {
        StaffPasswordDisplay(viewModel = viewModel, screenWidth = screenWidth)
    }
}
