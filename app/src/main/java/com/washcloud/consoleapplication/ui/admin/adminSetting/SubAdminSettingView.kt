package com.washcloud.consoleapplication.ui.admin.adminSetting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.primaryDark

@Composable
fun SubAdminSettingsScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    onChangePassword: () -> Unit,
    onHelpPhoneNumber: () -> Unit,
    showAd2: () -> Unit
) {

    val timerViewModel: TimerViewModel = hiltViewModel()


    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(interactionModifier)
        ,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.sub_admin_setting),
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = primaryDark
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        dateAndTimeView(screenWidth = screenWidth)
        Spacer(modifier = Modifier.height(24.dp))


      Column(modifier = Modifier.weight(1f) ,verticalArrangement = Arrangement.SpaceBetween) {

         Column(modifier = Modifier.padding(32.dp)) {
             MenuItem(
                 iconRes = R.drawable.password,
                 text = stringResource(id = R.string.change_password),
                 onClick = onChangePassword,
                 screenWidth = screenWidth,
                 screenHeight = screenHeight,
             )

             MenuItem(
                 iconRes = R.drawable.change_phone,
                 text = stringResource(id = R.string.help_phone_number),
                 onClick = onHelpPhoneNumber,
                 screenWidth = screenWidth,
                 screenHeight = screenHeight,
             )
         }

          BottomNavigationWithBackAndTimer(screenWidth, screenHeight, timerViewModel ,showAd2, showAd2)
      }
    }
}

@Composable
fun MenuItem(
    iconRes: Int,
    text: String,
    onClick: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp
) {
    val configuration = LocalConfiguration.current
    val isRtl by rememberUpdatedState(newValue = configuration.layoutDirection == android.util.LayoutDirection.RTL)

    Row(
        modifier = Modifier
            .padding(8.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(0.05 * screenWidth)
                    .padding(end = 16.dp)
            )
            Text(
                text = text,
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.035f).sp,
                    color = primaryDark,
                )
            )
        }
        Image(
            painter = painterResource(id = R.drawable.left_arrow),
            contentDescription = null,
            modifier = Modifier.size(0.05 * screenWidth)
                .rotate(if (isRtl) 180f else 0f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSubAdminSettingsScreen() {
    ConsoleApplicationTheme {
        SubAdminSettingsScreen(
            screenWidth = 360.dp,
            screenHeight = 640.dp,
            onChangePassword = {},
            onHelpPhoneNumber = {},
            showAd2 = {}
        )
    }
}