package com.washcloud.consoleapplication.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.secondaryColor
import com.washcloud.consoleapplication.utils.unSelectedTextColor

@Composable
fun BottomNavigation(
    screenWidth: Dp,
    screenHeight: Dp,
    changeLanguage: () -> Unit,
    showAd2: () -> Unit,
    showHelpForm: () -> Unit,
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
                        .width(0.04 * screenHeight)
                        .height(0.04 * screenHeight)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.help),
                    style = TextStyle(
                        color = if (isHelpFormShown) secondaryColor else unSelectedTextColor,
                        fontSize = (screenWidth.value * 0.023f).sp
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
                            RoundedCornerShape(0.05 * screenWidth)
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
                            start = 0.04 * screenWidth,
                            top = 0.01 * screenHeight,
                            end = 0.04 * screenWidth,
                            bottom = 0.01 * screenHeight
                        )
                ) {
                    Row {
                        Image(
                            painterResource(R.drawable.phone),
                            "phone",
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(0.01 * screenWidth))
                        Text(
                            text = "920031915",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (screenWidth.value * 0.03f).sp
                            )
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        changeLanguage()
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource(R.drawable.language),
                    "language",
                    modifier = Modifier
                        .width(0.04 * screenHeight)
                        .height(0.04 * screenHeight)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.language),
                    style = TextStyle(
                        color = unSelectedTextColor,
                        fontSize = (screenWidth.value * 0.023f).sp
                    )
                )
            }
        }
    }
}