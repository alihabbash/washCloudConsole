package com.washcloud.consoleapplication.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.utils.secondaryColor


@Composable
fun timerView(screenWidth: Dp, back: () -> Unit){
    val timerViewModel: TimerViewModel = hiltViewModel()
    val timerText by timerViewModel.timerText.collectAsState()
    if(timerText == "0"){
        timerViewModel.resetTimer()
        back()
    } else if(timerText.isEmpty()){
        timerViewModel.startTimeWatcher()
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.timer),
                "timer",
                modifier = Modifier
                    .width(0.05 * screenWidth)
                    .height(0.05 * screenWidth)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timerText,
                style = TextStyle(
                    color = secondaryColor,
                    fontSize = (screenWidth.value * 0.025f).sp
                )
            )
        }
    }
}