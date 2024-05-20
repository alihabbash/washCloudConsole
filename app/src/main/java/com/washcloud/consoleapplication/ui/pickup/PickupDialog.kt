package com.washcloud.consoleapplication.ui.pickup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.utils.dimBackground
import com.washcloud.consoleapplication.utils.hints

@Composable
fun PickupDialog(
    screenWidth: Dp,
    screenHeight: Dp,
){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(screenWidth)
            .height(screenHeight)
            .background(dimBackground)
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
                    text = stringResource(id = R.string.verify_wait),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.04f).sp,
                        color = hints
                    )
                )
            }
        }
    }
}