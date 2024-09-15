package com.washcloud.consoleapplication.ui.admin.settings

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.remote.config.BASE_URL_DEV
import com.washcloud.consoleapplication.remote.config.BASE_URL_PROD
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer

@Composable
fun PCSettingsScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    showAd2: () -> Unit
) {

    val context = LocalContext.current
    val viewModel: PCSettingsViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .height(screenHeight)
            .width(screenWidth)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(id = R.string.pc_setting),
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = primaryDark
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))


        dateAndTimeView(screenWidth)
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier =

        Modifier
            .fillMaxWidth()
            .weight(1f),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,


            ) {

            LazyColumn(modifier = Modifier
                .weight(1f)) {

                item {
                    SettingItem(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        label =  stringResource(R.string.locker_sn),
                        value =  viewModel.lockerName.value,
                        onValueChange = { viewModel.lockerName.value = it },
                    ) {
                        viewModel.saveLockerName()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }


                item {
                    SettingItem(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        label =  stringResource(R.string.api_key),
                        value =  viewModel.apiKey.value,
                        onValueChange = { viewModel.apiKey.value = it },
                    ) {
                        viewModel.saveApiKey()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }


                item {
                    SettingItem(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        label =  stringResource(R.string.terminal_sn),
                        value =  viewModel.terminalSn.value,
                        onValueChange = { viewModel.terminalSn.value = it },
                    ) {
                        viewModel.saveTerminalSn()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {

                    ServerSettingItem(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        serverOption = viewModel.serverOption.value,
                        customServer = viewModel.customServer.value,
                        onOptionSelected = { option ->
                            viewModel.serverOption.value = option
                            if (option != "Other") {
                                viewModel.customServer.value = ""
                            }
                        },
                        onCustomServerChange = { viewModel.customServer.value = it },
                        onSave = { viewModel.saveServerOption() }
                    )


                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }


                item {    SettingItem(
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    label =  stringResource(R.string.branch_id),
                    value =  viewModel.branchId.value,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    onValueChange = { viewModel.branchId.value = it },
                ) {
                    viewModel.saveBranchId()
                } }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }


                item {
                    SettingItem(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        label =  stringResource(R.string.delay_millis),
                        value =  viewModel.delayMillis.value,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        onValueChange = { viewModel.delayMillis.value = it },
                    ) {
                        viewModel.saveDelayMillis()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }


                item {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .border(
                                color = borderColor,
                                width = 1.dp,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .shadow(3.dp, shape = RoundedCornerShape(24.dp))
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column( modifier =
                        Modifier
                            .height(0.10 * screenHeight)
                            .padding(8.dp)
                            .padding(top = 8.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = stringResource(R.string.reset_database_instruction),
                                style = TextStyle(
                                    fontSize = (screenWidth.value * 0.035f).sp,
                                    color = secondaryColor,
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                blueGradient,
                                                secondaryColor,
                                            ),
                                        )
                                    )
                                    .padding(16.dp)
                                    .clickable {
                                        viewModel.resetDatabase()

                                    }
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.reset_database),
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = (screenWidth.value * 0.035f).sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                }

                item {

                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .border(
                                color = borderColor,
                                width = 1.dp,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .shadow(3.dp, shape = RoundedCornerShape(24.dp))
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier =
                            Modifier
                                .height(if (viewModel.isRebootEnabled.value) 0.15 * screenHeight else 0.08 * screenHeight)
                                .padding(16.dp)
                                .padding(top = 8.dp, bottom = 8.dp),


                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.daily_reboot_schedule),
                                    style = TextStyle(
                                        fontSize = (screenWidth.value * 0.035f).sp,
                                        color = secondaryColor,
                                    )
                                )
                                Switch(
                                    checked = viewModel.isRebootEnabled.value,
                                    onCheckedChange  = {
                                        viewModel.isRebootEnabled.value = it
                                        viewModel.saveIsRebootEnabled()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = secondaryColor,
                                        uncheckedThumbColor = Color.Gray
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (viewModel.isRebootEnabled.value) {

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                    Text(
                                        text = viewModel.rebootTime.value,
                                        style = TextStyle(
                                            fontSize = (screenWidth.value * 0.035f).sp,
                                            color = Color.Black
                                        ),
                                        modifier = Modifier.clickable {
                                            val timeParts = viewModel.rebootTime.value.split(":")
                                            val hour = timeParts[0].toInt()
                                            val minute = timeParts[1].toInt()

                                            TimePickerDialog(context, { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                                                viewModel.rebootTime.value = String.format("%02d:%02d", selectedHour, selectedMinute)

                                            }, hour, minute, true).show()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        blueGradient,
                                                        secondaryColor,
                                                    ),
                                                )
                                            )
                                            .padding(16.dp)
                                            .clickable {
                                                viewModel.saveRebootTime()
                                            }
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.save),
                                            style = TextStyle(
                                                color = Color.White,
                                                fontSize = (screenWidth.value * 0.035f).sp,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }
                            }



                        }
                    }
                }



            }
            BottomNavigationWithBackAndTimer(screenWidth, screenHeight, showAd2, showAd2)
        }
    }


}

@Composable
fun SettingItem(
    screenWidth: Dp,
    screenHeight: Dp,
    label: String,
    value: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,

    ) {
    Box(
        modifier = Modifier
            .padding(24.dp)
            .border(
                color = borderColor,
                width = 1.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(3.dp, shape = RoundedCornerShape(24.dp))
            .background(
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column( modifier =
        Modifier
            .height(0.15 * screenHeight)
            .padding(8.dp)
            .padding(top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
//            Text(
//                text = label,
//                style = TextStyle(
//                    fontSize = (screenWidth.value * 0.035f).sp,
//                    color = Color.Black
//                )
//            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label, modifier = Modifier.padding(bottom = 16.dp),
                    style =  TextStyle(fontSize = (screenWidth.value * 0.042f).sp, color = secondaryColor)) },
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(
                    fontSize = (screenWidth.value * 0.032f).sp,
                ),
                keyboardOptions = keyboardOptions,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)

            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                blueGradient,
                                secondaryColor,
                            ),
                        )
                    )
                    .padding(16.dp)
                    .clickable {
                        onSave()
                    }
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.035f).sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Composable
fun ServerSettingItem(
    screenWidth: Dp,
    screenHeight: Dp,
    serverOption: String,
    customServer: String,
    onOptionSelected: (String) -> Unit,
    onCustomServerChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val devServerUrl = BASE_URL_DEV
    val prodServerUrl = BASE_URL_PROD

    Box(
        modifier = Modifier
            .padding(24.dp)
            .border(
                color = borderColor,
                width = 1.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(3.dp, shape = RoundedCornerShape(24.dp))
            .background(
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .padding(top = 8.dp, bottom = 8.dp)
                .height( if(serverOption == "Other") 0.18 * screenHeight else 0.12 * screenHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = stringResource(R.string.choose_server),
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.035f).sp,
                    color = secondaryColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = serverOption == devServerUrl,
                    onClick = { onOptionSelected(devServerUrl) }
                )
                Text(
                    text = stringResource(R.string.development),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.035f).sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.clickable { onOptionSelected(devServerUrl) }
                )

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = serverOption == prodServerUrl,
                    onClick = { onOptionSelected(prodServerUrl) }
                )
                Text(
                    text = stringResource(R.string.production),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.035f).sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.clickable { onOptionSelected(prodServerUrl) }
                )

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = serverOption == "Other",
                    onClick = { onOptionSelected("Other") }
                )
                Text(
                    text = stringResource(R.string.other),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.035f).sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.clickable { onOptionSelected("Other") }
                )
            }

            if (serverOption == "Other") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = customServer,
                    onValueChange = onCustomServerChange,
                    label = { Text(stringResource(R.string.enter_custom_server), modifier = Modifier.padding(bottom = 16.dp), style =  TextStyle(fontSize = (screenWidth.value * 0.042f).sp, color = secondaryColor)) },
                    textStyle = TextStyle(
                        fontSize = (screenWidth.value * 0.032f).sp,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()

                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                blueGradient,
                                secondaryColor,
                            ),
                        )
                    )
                    .padding(16.dp)
                    .clickable {
                        onSave()
                    }
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.035f).sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
