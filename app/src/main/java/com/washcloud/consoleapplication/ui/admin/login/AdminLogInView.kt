package com.washcloud.consoleapplication.ui.admin.login



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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.washcloud.consoleapplication.ui.common.SelectedView
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.clearText
import com.washcloud.consoleapplication.utils.dimBackground
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.utils.numbersBackground
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun AdminLogInView(
    showAd2: () -> Unit,
    showAdminScreens: () -> Unit,
    onBack: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
) {

    val viewModel: AdminLoginViewModel = hiltViewModel();
    val isPasswordCorrect by viewModel.isPasswordCorrect.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()
    val passwordLoginForm by viewModel.passwordText.collectAsState()
    val passwordSelectedLoginForm by remember { mutableStateOf(true) }

    val timerViewModel: TimerViewModel = hiltViewModel()


    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    LaunchedEffect(isPasswordCorrect) {
        if (isPasswordCorrect) {
            showAdminScreens()
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


    Box {

        Column(
            modifier = Modifier
                .width(screenWidth)
                .then(interactionModifier),
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
                    text = stringResource(id = R.string.admin_login),
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
                                        .clickable {

                                        }
                                ) {
                                    Text(
                                        text = if (passwordLoginForm.isEmpty()) {
                                            "XXXX-XXXX-XXXX"
                                        } else {
                                            "*".repeat(passwordLoginForm.length)
                                        },
                                        style = TextStyle(
                                            color = hints,
                                            fontSize = (screenWidth.value * 0.025f).sp

                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height((screenHeight.value * 0.01f).dp))
                                Box(
                                    modifier = Modifier
                                        .padding(start = 24.dp, end = 24.dp)
                                        .clip(
                                            RoundedCornerShape((screenHeight.value * 0.011f).dp)
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
                                            top = (screenHeight.value * 0.01f).dp,
                                            end = 16.dp,
                                            bottom = (screenHeight.value * 0.01f).dp
                                        )
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 24.dp)
                                        .clickable {
                                            viewModel.verifyPassword()
                                            if (isPasswordCorrect) {
                                                showAdminScreens()
                                            }

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
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                blueGradient,
                                                secondaryColor,
                                            ),
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )

                                    .width(0.2 * screenWidth)
                                    .height(0.08 * screenHeight)
                                    .clickable {

                                        viewModel.verifyPassword()
                                        if (isPasswordCorrect) {
                                            showAdminScreens()
                                        }
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
                BottomNavigationWithBackAndTimer(screenWidth, screenHeight,  isAdmin = false, timerViewModel ,showAd2, onBack)
            }


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
                    viewModel.dismissAlert()
                }

        ) {
            Box(
                modifier =
                Modifier
                    .clip(
                        RoundedCornerShape(0.02 * screenWidth)
                    )
                    .background(color = Color.White)
                    .width(0.8 * screenWidth)
                    .height(0.16 * screenHeight),
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
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        blueGradient,
                                        secondaryColor,
                                    ),
                                )
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





