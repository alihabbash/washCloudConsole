package com.washcloud.consoleapplication.ui.startStaff

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.ColorFilter
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
import com.washcloud.consoleapplication.ui.common.timerView
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.utils.lightGreen
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor

@Composable
fun DriverLoginForm(
    screenWidth: Dp,
    screenHeight: Dp,
    showPickUp: () -> Unit,
    showDropOff: () -> Unit,
    showAd2: () -> Unit,
) {
    val viewModel: DateTimeViewModel = hiltViewModel()
    val hoursText by viewModel.hoursText.collectAsState()
    val fullDateText by viewModel.fullDateText.collectAsState()
    Column(
        modifier = Modifier
            .width(screenWidth)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.login_driver),
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = primaryDark
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = hoursText,
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.02f).sp,
                    color = hints
                ),
                textAlign = TextAlign.Start,
            )
            Box(
                modifier =
                Modifier.weight(1f)
            )
            Text(
                text = fullDateText,
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.02f).sp,
                    color = hints
                ),
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.width(24.dp))

        }

        Column(
            modifier =
            Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                Modifier
                    .padding(start = 24.dp, end = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .border(
                            color = secondaryColor,
                            width = 1.dp,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                        .clickable {
                            showDropOff()
                        },

                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .width(0.22 * screenWidth)
                            .height(0.22 * screenWidth)
                            .background(lightGreen, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painterResource(R.drawable.drop_off),
                            "drop_off",
                            colorFilter = ColorFilter.tint(color = secondaryColor),
                            modifier = Modifier
                                .width((screenWidth.value * 0.11f).dp)
                                .height((screenWidth.value * 0.11f).dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(id = R.string.drop_off),
                        style = TextStyle(
                            color = secondaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenWidth.value * 0.035f).sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width((screenWidth.value * 0.04f).dp))
                Column(
                    modifier = Modifier
                        .border(
                            color = secondaryColor,
                            width = 1.dp,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                        .clickable {
                                   showPickUp()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .width(0.22 * screenWidth)
                            .height(0.22 * screenWidth)
                            .background(lightGreen, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painterResource(R.drawable.pickup),
                            "pickup",
                            colorFilter = ColorFilter.tint(color = secondaryColor),
                            modifier = Modifier
                                .width((screenWidth.value * 0.11f).dp)
                                .height((screenWidth.value * 0.11f).dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(id = R.string.pickup),
                        style = TextStyle(
                            color = secondaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenWidth.value * 0.035f).sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

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
                        .padding(
                            top = (screenHeight.value * 0.01f).dp,
                            end = 16.dp,
                            bottom = (screenHeight.value * 0.01f).dp,
                            start = 16.dp
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
                        Spacer(modifier = Modifier.width((screenWidth.value * 0.02f).dp))
                        Text(
                            text = stringResource(id = R.string.back),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (screenWidth.value * 0.03f).sp
                            )
                        )
                        Spacer(modifier = Modifier.width((screenWidth.value * 0.02f).dp))
                    }
                }
                Box(
                    modifier = Modifier.weight(1f)
                )
                timerView(screenWidth, showAd2)
            }
        }
    }
}