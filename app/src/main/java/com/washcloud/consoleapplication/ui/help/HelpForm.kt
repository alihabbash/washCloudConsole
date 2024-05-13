package com.washcloud.consoleapplication.ui.help

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.washcloud.consoleapplication.ui.common.BottomNavigation
import com.washcloud.consoleapplication.utils.OutlinedInputField
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.lightGreen
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor

@Composable
fun HelpForm(
    screenWidth: Dp,
    screenHeight: Dp,
    changeLanguage: () -> Unit,
    showAd2: () -> Unit
) {

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
            Spacer(modifier = Modifier.height(36.dp))


            Row {
                Spacer(modifier = androidx.compose.ui.Modifier.width(0.02 * screenWidth))
                Column {
                    Spacer(modifier = Modifier.padding(top = 12.dp))
                    Image(
                        painterResource(R.drawable.arrow_back),
                        "arrow_back",
                        colorFilter = ColorFilter.tint(color = primaryDark),
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
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
                        fontSize = (screenWidth.value * 0.04f).sp,
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
                    Spacer(modifier = androidx.compose.ui.Modifier.height(0.02 * screenHeight))
                    Image(
                        painterResource(R.drawable.steps),
                        "steps",
                        contentScale = ContentScale.FillWidth,
                        modifier = androidx.compose.ui.Modifier
                            .width(screenWidth - 50.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = androidx.compose.ui.Modifier.width(0.05 * screenWidth))
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .padding(0.008 * screenWidth)
                                .width(0.08 * screenWidth)
                                .height(0.08 * screenWidth)
                                .background(lightGreen, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(R.drawable.outline_phone),
                                "phone",
                                colorFilter = ColorFilter.tint(color = secondaryColor),
                                modifier = androidx.compose.ui.Modifier
                                    .width(0.05 * screenWidth)
                                    .height(0.05 * screenWidth)
                            )
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            text = "920031915",
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = (screenWidth.value * 0.025f).sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = androidx.compose.ui.Modifier.width(0.05 * screenWidth))
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .padding(0.008 * screenWidth)
                                .width(0.08 * screenWidth)
                                .height(0.08 * screenWidth)
                                .background(lightGreen, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(R.drawable.outline_email),
                                "email",
                                colorFilter = ColorFilter.tint(color = secondaryColor),
                                modifier = androidx.compose.ui.Modifier
                                    .width(0.05 * screenWidth)
                                    .height(0.05 * screenWidth)
                            )

                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            text = "info@horizonscloud.net",
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = (screenWidth.value * 0.025f).sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(
                        modifier =
                        Modifier
                            .height(1.dp)
                            .background(color = borderColor)
                            .padding(start = 24.dp, end = 24.dp)
                            .width(screenWidth - (0.2 * screenWidth))
                            .align(Alignment.CenterHorizontally)
                            .padding(start = 24.dp, end = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(id = R.string.quick_open),
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.03f).sp,
                            color = Color.Black
                        ),
                        textAlign = TextAlign.Center,
                        modifier = androidx.compose.ui.Modifier.width(screenWidth)
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
                    ) {
                        Column {
                            Spacer(modifier = androidx.compose.ui.Modifier.height((screenHeight.value * 0.02f).dp))
                            Text(
                                text = stringResource(id = R.string.enter_security),
                                style = TextStyle(
                                    fontSize = (screenWidth.value * 0.025f).sp,
                                    color = Color.Black
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = (screenHeight.value * 0.01f).dp)
                            )
                            OutlinedInputField(
                                value = "",
                                hintText = "XXXX-XXXX-XXXX",
                                onValueChange = {

                                },
                                hintTextSize = (screenWidth.value * 0.02f).sp,
                                cornerRadius = (screenWidth.value * 0.06f),
                                modifier = androidx.compose.ui.Modifier
                                    .padding((screenHeight.value * 0.02f).dp)
                                    .fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 24.dp, end = 24.dp)
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
                                        top = (screenWidth.value * 0.02f).dp,
                                        end = 16.dp,
                                        bottom = (screenWidth.value * 0.02f).dp
                                    )
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.open),
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = (screenWidth.value * 0.03f).sp
                                    )
                                )
                            }
                            Spacer(modifier = androidx.compose.ui.Modifier.height((screenHeight.value * 0.02f).dp))

                        }
                    }
                }
            }
            Box(
                modifier =
                Modifier.weight(1f)
            )
            BottomNavigation(screenWidth, screenHeight, changeLanguage, showAd2, {}, true)
        }


    }
}