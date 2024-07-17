package com.washcloud.consoleapplication.ui.admin.settings

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer

@Composable
fun PCSettingsScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    onSaveLocker: (String) -> Unit,
    onSaveRebootSchedule: (String) -> Unit,
    showAd2: () -> Unit
) {
    var lockerName by remember { mutableStateOf("") }
    var rebootTime by remember { mutableStateOf("13:00") }
    var isRebootEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current
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

          Column {
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
                      .height(0.22 * screenHeight)
                      .padding(16.dp)
                      .padding(top = 32.dp, bottom = 32.dp),


                      verticalArrangement = Arrangement.SpaceBetween) {
                      Text(
                          text = "Locker S/N: 22222213703.001",
                          style = TextStyle(
                              fontSize = (screenWidth.value * 0.035f).sp,
                              color = Color.Black
                          )
                      )
                      Spacer(modifier = Modifier.height(8.dp))
                      OutlinedTextField(
                          value = lockerName,
                          onValueChange = { lockerName = it },
                          label = { Text("Locker Name",
                              style =  TextStyle(fontSize = (screenWidth.value * 0.035f).sp)) },
                          shape = RoundedCornerShape(16.dp),
                          modifier = Modifier.fillMaxWidth()
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
                                  onSaveLocker(lockerName)
                              }
                              .fillMaxWidth(),
                          contentAlignment = Alignment.Center
                      ) {
                          Text(
                              text = "Save",
                              style = TextStyle(
                                  color = Color.White,
                                  fontSize = (screenWidth.value * 0.035f).sp,
                                  textAlign = TextAlign.Center
                              )
                          )
                      }
                  }
              }
              Spacer(modifier = Modifier.height(24.dp))


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
                          .height(if (isRebootEnabled) 0.22 * screenHeight else 0.08 * screenHeight)
                          .padding(16.dp)
                          .padding(top = 32.dp, bottom = 32.dp),


                      verticalArrangement = Arrangement.SpaceBetween,
                      horizontalAlignment = Alignment.CenterHorizontally,
                  ) {
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                          Text(
                              text = "Daily Reboot Schedule",
                              style = TextStyle(
                                  fontSize = (screenWidth.value * 0.035f).sp,
                                  color = Color.Black,
                              )
                          )
                          Switch(
                              checked = isRebootEnabled,
                              onCheckedChange = { isRebootEnabled = it },
                              colors = SwitchDefaults.colors(
                                  checkedThumbColor = secondaryColor,
                                  uncheckedThumbColor = Color.Gray
                              )
                          )
                      }
                      Spacer(modifier = Modifier.height(16.dp))

                      if (isRebootEnabled) {

                          Column(horizontalAlignment = Alignment.CenterHorizontally) {

                              Text(
                                  text = rebootTime,
                                  style = TextStyle(
                                      fontSize = (screenWidth.value * 0.035f).sp,
                                      color = Color.Black
                                  ),
                                  modifier = Modifier.clickable {
                                      val timeParts = rebootTime.split(":")
                                      val hour = timeParts[0].toInt()
                                      val minute = timeParts[1].toInt()

                                      TimePickerDialog(context, { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                                          rebootTime = String.format("%02d:%02d", selectedHour, selectedMinute)
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
                                          onSaveRebootSchedule(rebootTime)
                                      }
                                      .fillMaxWidth(),
                                  contentAlignment = Alignment.Center
                              ) {
                                  Text(
                                      text = "Save",
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
            BottomNavigationWithBackAndTimer(screenWidth, screenHeight, showAd2, showAd2)
        }
    }
}

