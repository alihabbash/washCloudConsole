package com.washcloud.consoleapplication.ui.admin.login



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.SelectedView
import com.washcloud.consoleapplication.ui.common.AdminPasswordPad
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.clearText
import com.washcloud.consoleapplication.utils.dimBackground
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.utils.numbersBackground
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun AdminLogInView(
    showAd2: () -> Unit,
    showAdminScreens: () -> Unit,
    onBack: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
) {

    val viewModel: AdminLoginViewModel = hiltViewModel();
    val isPasswordCorrect by viewModel.isPasswordCorrect.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()
    val passwordSelectedLoginForm by remember { mutableStateOf(true) }
    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                blueGradient,
                secondaryColor,
            )
        )
    }

    val timerViewModel: TimerViewModel = hiltViewModel()


    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    LaunchedEffect(isPasswordCorrect) {
        if (isPasswordCorrect) {
            showAdminScreens()
            viewModel.resetPasswordCorrectState()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.resetPasswordCorrectState()
    }

    val clearSelectedField = {

        viewModel.passwordText.value = ""

    }

    val updateSelectedField = { value: String ->

        viewModel.passwordText.value += value

    }


    Box {

        Column(
            modifier = Modifier
                .width(screenWidth)
                .then(interactionModifier),
        ) {


            Column(
                modifier = Modifier
                    .height(screenHeight)
                    .width(screenWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.admin_login),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.04f).sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryDark
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))

                dateAndTimeView(screenWidth)
                Spacer(modifier = Modifier.height(24.dp))
                AdminPasswordPad(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.72 * screenHeight),
                    viewModel = viewModel,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    onSuccess = { showAdminScreens() },
                    onCancel = { onBack() }
                )
                Box(
                    modifier =
                    Modifier.weight(1f)
                )
                BottomNavigationWithBackAndTimer(screenWidth, screenHeight,  isAdmin = false, timerViewModel ,showAd2, onBack)
            }


        }


    }
}
