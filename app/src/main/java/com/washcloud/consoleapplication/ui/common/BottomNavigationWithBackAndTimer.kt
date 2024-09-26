package com.washcloud.consoleapplication.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.secondaryColor

@Composable
fun BottomNavigationWithBackAndTimer(
    screenWidth: Dp,
    screenHeight: Dp,
    isAdmin : Boolean = false,
    timerViewModel: TimerViewModel? = null,
    showAd2: () -> Unit,
    onBack: () -> Unit,
){
    val configuration = LocalConfiguration.current
    val isRtl by rememberUpdatedState(newValue = configuration.layoutDirection == android.util.LayoutDirection.RTL)



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
                        onBack()
                    }
            ) {
                Row {
                    Image(
                        painterResource(R.drawable.arrow_back),
                        "arrow_back",
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .rotate(if (isRtl) 180f else 0f)
                    )
                    Spacer(modifier = Modifier.width((screenWidth.value * 0.02f).dp))
                    Text(
                        text = if (isAdmin) stringResource(id = R.string.exit) else stringResource(id = R.string.back),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (screenWidth.value * 0.03f).sp
                        )
                    )
                    Spacer(modifier = Modifier.width((screenWidth.value * 0.02f).dp))
                }
            }
            Box(
                modifier =
                Modifier.weight(1f)
            )
            timerView(screenWidth,timerViewModel!! ,showAd2)
        }
    }
}