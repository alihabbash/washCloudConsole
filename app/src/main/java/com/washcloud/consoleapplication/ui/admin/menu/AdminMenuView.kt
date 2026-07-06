package com.washcloud.consoleapplication.ui.admin.menu

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.washcloud.consoleapplication.ui.startStaff.DateTimeViewModel
import com.washcloud.consoleapplication.utils.*

@Composable
fun AdminMenuView(
    screenWidth: Dp,
    screenHeight: Dp,
    showSetting: () -> Unit,
    showAdminSetting: () -> Unit,
    showAdsSetting: () -> Unit,
    showLockerManagement: () -> Unit,
    showAd2: () -> Unit,
    onBack: () -> Unit
) {

    val timerViewModel: TimerViewModel = hiltViewModel()


    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    val context = LocalContext.current
    val version = getAppVersionName(context)

    Column(
        modifier = Modifier
            .width(screenWidth)
            .then(interactionModifier)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        TitleText(stringResource(id = R.string.admin_menu), screenWidth)
        Spacer(modifier = Modifier.height(24.dp))

        dateAndTimeView(screenWidth = screenWidth)

        MenuGrid(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            showSetting = showSetting,
            showAdminSetting = showAdminSetting,
            showAdsSetting = showAdsSetting,
            showLockerManagement = showLockerManagement
        )

        Spacer(modifier = Modifier.weight(1f))


        Text(
            text = "Version: $version",
            style = TextStyle(
                color = Color.White,
                fontSize = (screenWidth.value * 0.06f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Footer(screenWidth)

        BottomNavigationWithBackAndTimer(screenWidth, screenHeight,  isAdmin = true, timerViewModel, showAd2, onBack)
    }
}

@Composable
fun TitleText(text: String, screenWidth: Dp) {
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
fun MenuGrid(
    screenWidth: Dp,
    screenHeight: Dp,
    showSetting: () -> Unit,
    showAdminSetting: () -> Unit,
    showAdsSetting: () -> Unit,
    showLockerManagement: () -> Unit
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
        MenuRow(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            title1 = stringResource(id = R.string.pc_setting),
            icon1 = R.drawable.admin_pc_setting,
            action1 = showSetting,
            title2 = stringResource(id = R.string.admin_setting),
            icon2 = R.drawable.admin_setting,
            action2 = showAdminSetting
        )
        MenuRow(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            title1 = stringResource(id = R.string.ads_management),
            icon1 = R.drawable.admin_ads,
            action1 = showAdsSetting,
            title2 = stringResource(id = R.string.locket_management),
            icon2 = R.drawable.admin_lockers,
            action2 = showLockerManagement
        )
    }
}

@Composable
fun MenuRow(
    screenWidth: Dp,
    screenHeight: Dp,
    title1: String,
    icon1: Int,
    action1: () -> Unit,
    title2: String,
    icon2: Int,
    action2: () -> Unit
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
            title = title1,
            icon = icon1,
            action = action1
        )
        Spacer(modifier = Modifier.width((screenWidth.value * 0.04f).dp))
        MenuItem(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            title = title2,
            icon = icon2,
            action = action2
        )
    }
}

@Composable
fun MenuItem(
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

@Composable
fun Footer(screenWidth: Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SN:12433455333",
            style = TextStyle(
                color = Color.White,
                fontSize = (screenWidth.value * 0.025f).sp
            )
        )
        Text(
            text = "WD1.04.202345325333",
            style = TextStyle(
                color = Color.White,
                fontSize = (screenWidth.value * 0.025f).sp
            )
        )
    }
}
