package com.washcloud.consoleapplication.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.clearText
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.utils.numbersBackground
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor

//staff login form
@Composable
fun LoginForm(
    showAd2: () -> Unit, showDriverLogin: () -> Unit,
    clearSelectedField: () -> Unit,
    updateSelectedField: (String) -> Unit,
    selectField: (Int) -> Unit,
    mobileSelectedLoginForm: Boolean,
    passwordSelectedLoginForm: Boolean,
    mobileLoginForm: String,
    passwordLoginForm: String,
    screenWidth: Dp,
    screenHeight: Dp,
) {
    val viewModel: StaffLoginViewModel = hiltViewModel()
    val screenState by viewModel.uiState.collectAsState()
    val isLoading = screenState is LoginState.Loading
    Column(
        modifier = androidx.compose.ui.Modifier
            .width(screenWidth)
    ) {


        Column(
            modifier = androidx.compose.ui.Modifier
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

            Row {
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "02:30",
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.02f).sp,
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
                        fontSize = (screenWidth.value * 0.02f).sp,
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
                            Spacer(modifier = androidx.compose.ui.Modifier.height((screenHeight.value * 0.02f).dp))
                            Text(
                                text = stringResource(id = R.string.mobile_number),
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
                                        color = if(mobileSelectedLoginForm)
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
                                        bottom = (screenHeight.value * 0.01f).dp,
                                        start = 24.dp
                                    )
                                    .clickable {
                                        selectField(0)
                                    }
                            ) {
                                Text(
                                    text = mobileLoginForm,
                                    style = TextStyle(
                                        color = hints,
                                        fontSize = (screenWidth.value * 0.025f).sp
                                    )
                                )
                            }
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
                                        color = if(passwordSelectedLoginForm)
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
                                        selectField(1)
                                    }
                            ) {
                                Text(
                                    text = passwordLoginForm.ifEmpty { "XXXX-XXXX-XXXX" },
                                    style = TextStyle(
                                        color = hints,
                                        fontSize = (screenWidth.value * 0.025f).sp

                                    )
                                )
                            }
                            Spacer(modifier = androidx.compose.ui.Modifier.height((screenHeight.value * 0.01f).dp))
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
                                        showDriverLogin()
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
                            Spacer(modifier = androidx.compose.ui.Modifier.height((screenHeight.value * 0.02f).dp))
                        }
                    }
                    Spacer(modifier = androidx.compose.ui.Modifier.height((screenHeight.value * 0.03f).dp))
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
                                .height(0.08 * screenHeight),
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
            Box(
                modifier = androidx.compose.ui.Modifier
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
                                RoundedCornerShape((screenWidth.value * 0.02f).dp)
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
                            .clickable {
                                showAd2()
                            }
                    ) {
                        Row {
                            Image(
                                painterResource(R.drawable.arrow_back),
                                "arrow_back",
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                            )
                            Spacer(modifier = androidx.compose.ui.Modifier.width((screenWidth.value * 0.02f).dp))
                            Text(
                                text = stringResource(id = R.string.back),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (screenWidth.value * 0.03f).sp
                                )
                            )
                            Spacer(modifier = androidx.compose.ui.Modifier.width((screenWidth.value * 0.02f).dp))
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
                            modifier = androidx.compose.ui.Modifier
                                .width(0.05 * screenWidth)
                                .height(0.05 * screenWidth)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "03:59",
                            style = TextStyle(
                                color = secondaryColor,
                                fontSize = (screenWidth.value * 0.025f).sp
                            )
                        )
                    }
                }
            }
        }


    }
}