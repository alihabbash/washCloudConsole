package com.washcloud.consoleapplication.ui.admin.password


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.OutlinedInputField
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor

import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor
import androidx.compose.ui.platform.LocalContext

@Composable
fun ChangePasswordScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    onSave: () -> Unit,
    showAd2: () -> Unit
) {
    val  viewModel: ChangePasswordViewModel = hiltViewModel()
    val currentPassword by viewModel.currentPassword.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val passwordHasChanged by viewModel.passwordHasChanged.collectAsState()
    val context = LocalContext.current


        if(passwordHasChanged) {
            LaunchedEffect(passwordHasChanged) {
                Toast.makeText(context, "Password successfully changed", Toast.LENGTH_SHORT).show()
                onSave()
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

       Column(modifier = Modifier.padding(16.dp)) {
           Text(
               text = stringResource(id = R.string.change_password),
               style = TextStyle(
                   fontSize = (screenWidth.value * 0.035f).sp,
                   fontWeight = FontWeight.Bold,
                   color = primaryDark
               ),
               textAlign = TextAlign.Start,
               modifier = Modifier.fillMaxWidth()
           )
           Spacer(modifier = Modifier.height(16.dp))
       }

     Column(modifier = Modifier.weight(1f) ,verticalArrangement = Arrangement.SpaceBetween){
         Column(
             modifier = Modifier.padding(start = 32.dp, end = 32.dp),
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             Column(
                 modifier = Modifier
                     .border(
                         width = 1.dp,
                         color = borderColor,
                         shape = RoundedCornerShape(16.dp)
                     )
                     .padding(16.dp)
                     .fillMaxWidth(),
                 horizontalAlignment = Alignment.CenterHorizontally
             ) {

                 Text(
                     text = stringResource(id = R.string.current_password),
                     style = TextStyle(
                         fontSize = (screenWidth.value * 0.025f).sp,
                         color = primaryDark
                     ),
                     textAlign = TextAlign.Start,
                     modifier = Modifier.fillMaxWidth()
                         .padding(start = 16.dp, top = 32.dp)
                 )
                 OutlinedInputField(
                     value = currentPassword,
                     hintText = stringResource(id = R.string.current_password),
                     onValueChange = { viewModel.onCurrentPasswordChange(it) },
                     hintTextSize = (screenWidth.value * 0.035f).sp,
                     fontSize = (screenWidth.value * 0.025f).sp,
                     cornerRadius = (screenWidth.value * 0.06f),
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(16.dp)
                         .padding(bottom = 32.dp)
                 )

                 Text(
                     text = stringResource(id = R.string.new_password),
                     style = TextStyle(
                         fontSize = (screenWidth.value * 0.025f).sp,
                         color = primaryDark
                     ),
                     textAlign = TextAlign.Start,
                     modifier = Modifier.fillMaxWidth()
                         .padding(start = 16.dp)
                 )
                 OutlinedInputField(
                     value = newPassword,
                     hintText = stringResource(id = R.string.new_password),
                     onValueChange = { viewModel.onNewPasswordChange(it)},
                     hintTextSize = (screenWidth.value * 0.035f).sp,
                     fontSize = (screenWidth.value * 0.025f).sp,
                     cornerRadius = (screenWidth.value * 0.06f),
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(16.dp)
                         .padding(bottom = 32.dp)
                 )

                 Text(
                     text = stringResource(id = R.string.confirm_password),
                     style = TextStyle(
                         fontSize = (screenWidth.value * 0.025f).sp,
                         color = primaryDark
                     ),
                     textAlign = TextAlign.Start,
                     modifier = Modifier.fillMaxWidth()
                         .padding(start = 16.dp)
                 )
                 OutlinedInputField(
                     value = confirmPassword,
                     hintText = stringResource(id = R.string.confirm_password),
                     onValueChange = { viewModel.onConfirmPasswordChange(it)},
                     hintTextSize = (screenWidth.value * 0.035f).sp,
                     fontSize = (screenWidth.value * 0.025f).sp,
                     cornerRadius = (screenWidth.value * 0.06f),
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(16.dp)
                         .padding(bottom = 32.dp)
                 )

                 Box(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(16.dp)
                         .padding(bottom = 32.dp)
                         .background(
                             brush = Brush.horizontalGradient(
                                 colors = listOf(
                                     blueGradient,
                                     secondaryColor,
                                 ),
                             ),
                             shape = RoundedCornerShape(8.dp)
                         )
                         .clickable { viewModel.changePassword(onSave) },
                     contentAlignment = Alignment.Center
                 ) {
                     Text(
                         text = stringResource(id = R.string.save),
                         style = TextStyle(
                             color = Color.White,
                             fontSize = (screenWidth.value * 0.025f).sp,
                             fontWeight = FontWeight.Bold
                         ),
                         modifier = Modifier.padding(16.dp)
                     )
                 }

                 errorMessage?.let {
                     Spacer(modifier = Modifier.height(16.dp))
                     Text(
                         text = it,
                         style = TextStyle(
                             color = Color.Red,
                             fontSize = (screenWidth.value * 0.025f).sp
                         ),
                         modifier = Modifier.padding(16.dp)
                     )
                 }
             }
         }
         BottomNavigationWithBackAndTimer(screenWidth, screenHeight, showAd2, showAd2)
     }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChangePasswordScreen() {
    ConsoleApplicationTheme {
        ChangePasswordScreen(
            screenWidth = 360.dp,
            screenHeight = 640.dp,
            onSave = {},
            showAd2 = {}
        )
    }
}
