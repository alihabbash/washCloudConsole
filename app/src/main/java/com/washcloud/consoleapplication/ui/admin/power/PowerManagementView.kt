package com.washcloud.consoleapplication.ui.admin.power

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.washcloud.consoleapplication.ui.admin.login.AdminLoginViewModel
import com.washcloud.consoleapplication.ui.common.AdminPasswordPad
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.utils.*

@Composable
fun PowerManagementView(
    screenWidth: Dp,
    screenHeight: Dp,
    rebootAndroid: () -> Unit,
    shutdown: () -> Unit,
    showAd2: () -> Unit,
    onBack: () -> Unit
) {
    val timerViewModel: TimerViewModel = hiltViewModel()
    val adminLoginViewModel: AdminLoginViewModel = hiltViewModel()
    
    var showPasswordPad by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<() -> Unit>({}) }

    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    Column(
        modifier = Modifier
            .width(screenWidth)
            .then(interactionModifier)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        TitleText(stringResource(id = R.string.shutdown_and_restart), screenWidth)
        Spacer(modifier = Modifier.height(24.dp))

        dateAndTimeView(screenWidth = screenWidth)

        PowerMenuGrid(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            rebootAndroid = {
                pendingAction = rebootAndroid
                showPasswordPad = true
            },
            shutdown = {
                pendingAction = shutdown
                showPasswordPad = true
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomNavigationWithBackAndTimer(screenWidth, screenHeight, isAdmin = true, timerViewModel, showAd2, onBack)
    }

    if (showPasswordPad) {
        Box(
            modifier = Modifier
                .width(screenWidth)
                .height(screenHeight)
                .background(dimBackground)
                .clickable { showPasswordPad = false },
            contentAlignment = Alignment.Center
        ) {
            AdminPasswordPad(
                modifier = Modifier.width(0.8f * screenWidth),
                viewModel = adminLoginViewModel,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onSuccess = { 
                    pendingAction()
                    showPasswordPad = false 
                },
                onCancel = { showPasswordPad = false }
            )
        }
    }
}

@Composable
private fun TitleText(text: String, screenWidth: Dp) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = primaryDark
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PowerMenuGrid(
    screenWidth: Dp,
    screenHeight: Dp,
    rebootAndroid: () -> Unit,
    shutdown: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(64.dp)
            .shadow(3.dp, RoundedCornerShape(24.dp))
            .border(1.dp, lightGrey, RoundedCornerShape(24.dp))
            .background(Color.White, shape = RoundedCornerShape(24.dp)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier
                .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            MenuItem(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                title = stringResource(id = R.string.shutdown),
                icon = R.drawable.shutdown,
                action = { shutdown() }
            )
            Spacer(modifier = Modifier.width((screenWidth.value * 0.04f).dp))
            MenuItem(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                title = stringResource(id = R.string.reboot),
                icon = R.drawable.reboot,
                action = { rebootAndroid() }
            )
        }
    }
}

@Composable
private fun MenuItem(
    screenWidth: Dp,
    screenHeight: Dp,
    title: String,
    icon: Int,
    action: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(0.4 * screenWidth)
            .height(0.23 * screenHeight)
            .border(1.dp, secondaryColor, RoundedCornerShape(24.dp))
            .padding(top = 24.dp, bottom = 24.dp)
            .clickable { action() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
                painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = secondaryColor),
                modifier = Modifier
                    .width((screenWidth.value * 0.11f).dp)
                    .height((screenWidth.value * 0.11f).dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = TextStyle(
                color = secondaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = (screenWidth.value * 0.035f).sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
