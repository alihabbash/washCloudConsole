package com.washcloud.consoleapplication.ui.dropoff

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.pickup.isValidSerialNumber
import com.washcloud.consoleapplication.utils.*
import kotlinx.coroutines.delay

@Composable
fun SelectLockerView(
    screenWidth: Dp,
    screenHeight: Dp,
    onBack: () -> Unit,
    showAd2: () -> Unit,
) {
    val viewModel: DropOffViewModel = hiltViewModel()
    var selectedLocker by remember { mutableStateOf<String?>(null) }
    val lockers by viewModel.lockers.collectAsState()
    val context = LocalContext.current
    var wayBillNo by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("Selection Required") }
    var alertMessage by remember { mutableStateOf("Please select a locker before proceeding.") }
    LaunchedEffect(Unit) {
        viewModel.fetchLockers()
    }
   /* LaunchedEffect(Unit) {
    delay(10000L)
        wayBillNo = "4442408170005-1"
   }*/

    LaunchedEffect(wayBillNo) {
     if (isValidSerialNumber(wayBillNo)) {


         if (selectedLocker == null) {
             showAlert = true
                alertTitle = "Selection Required"
                alertMessage = "Please select a locker before proceeding."

         } else if (isValidSerialNumber(wayBillNo)) {
             showAlert = true
             alertTitle = "Drop Off Clothes"
             alertMessage = "Please drop off clothes in locker $selectedLocker."
             viewModel.dropoff(wayBillNo, selectedLocker!!)
             wayBillNo = ""

         }

     }
    }
    Box {


            Column(
                modifier = Modifier
                    .width(screenWidth)
            ) {

        Column(
            modifier = Modifier
                .height(screenHeight)
                .width(screenWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {


            Header(screenWidth)
            if (lockers.isEmpty()) {
                NoLockersMessage(screenWidth)
            } else {
                LockerList(lockers, screenWidth, screenHeight, selectedLocker) {
                    selectedLocker = it
                }
            }



            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .border(
                        color = borderColor,
                        width = 1.dp,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .fillMaxWidth()

            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    text = stringResource(id = R.string.drop_off_clothes),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.04f).sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryDark
                    ),
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(16.dp))



                DropOffSection(
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    selectedLocker,
                    wayBillNo = wayBillNo,
                    onWayBillNoChange = { wayBillNo = it },
                    onConfirm = {

                        if (selectedLocker == null) {
                            showAlert = true
                            alertTitle = "Selection Required"
                            alertMessage = "Please select a locker before proceeding."

                        } else if (isValidSerialNumber(wayBillNo)) {
                            Log.e("DropOffViewModel", "Drop off clothes in locker $selectedLocker")
                            viewModel.dropoff(wayBillNo, selectedLocker!!)
                            showAlert = true
                            alertTitle = "Drop Off Clothes"
                            alertMessage = "Please drop off clothes in locker $selectedLocker."
                            wayBillNo = ""
                        } else {
                            showAlert = true
                            alertTitle = "Invalid Serial Number"
                            alertMessage = "Please enter a valid serial number."
                        }

                    })
            }

            Spacer(modifier = Modifier.height(0.05 * screenHeight))


            BottomNavigationWithBackAndTimer(screenWidth, screenHeight, showAd2, onBack)


        }
        }

        if (showAlert) {

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
                        .height(0.15 * screenHeight)
                        .padding(start = 16.dp, end = 16.dp),

                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {

                        Text(
                            alertTitle,
                            fontSize = (screenWidth.value * 0.04f).sp,
                            color = secondaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = alertMessage,
                            style = TextStyle(
                                fontSize = (screenWidth.value * 0.025f).sp,
                                fontWeight = FontWeight.Bold
                            ),
                        )

                        Spacer(modifier = Modifier.height(0.01 * screenHeight))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            blueGradient,
                                            secondaryColor,
                                        ),
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    showAlert = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.confirm),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (screenWidth.value * 0.024f).sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(16.dp)

                            )
                        }

                    }
                }

            }
        }
    }
}

@Composable
fun NoLockersMessage(screenWidth: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.no_lockers_available),
            style = TextStyle(
                fontSize = (screenWidth.value * 0.04f).sp,
                fontWeight = FontWeight.Medium,
                color = primaryDark
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LockerItem(
    boxId: Long,
    type: String,
    abbreviation: String,
    availableNumber: Int,
    itemWidth: Dp,
    isSelected: Boolean,
    onSelect: () -> Unit,
    screenWidth: Dp,
) {
    val backgroundColor = if (isSelected) secondaryColor.copy(alpha = 0.3F) else Color.White
    val borderColor = secondaryColor
    val isEnabled = availableNumber > 0

    Row(
        modifier = Modifier
            .padding(8.dp)
            .width(itemWidth)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(enabled = isEnabled) { onSelect() }
            .padding(16.dp)
            .padding(vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (isEnabled) Brush.horizontalGradient(
                        colors = listOf(blueGradient, secondaryColor)
                    ) else SolidColor(Color.Gray)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = abbreviation,
                color = Color.White,
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.03f).sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = type,
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.032f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) secondaryColor else Color.Gray,
                )
            )
            Text(
                text = if (isEnabled) stringResource(id = R.string.available
                )  else stringResource(id = R.string.occupied),
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.03f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Color.Black else Color.Gray
                )
            )
            Text(
                text = "0$boxId",
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.03f).sp,
                    color = if (isEnabled) secondaryColor else Color.Gray,
                )
            )
        }
    }
}

@Composable
fun LockerList(
    lockers: List<BoxDto>,
    screenWidth: Dp,
    screenHeight: Dp,
    selectedLocker: String?,
    onLockerSelect: (String) -> Unit
) {
    val itemWidth = (screenWidth - 144.dp) / 3

    Column(
        modifier = Modifier
            .padding(24.dp)
            .border(
                color = borderColor,
                width = 1.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
            .fillMaxWidth()
            .height(0.45 * screenHeight.value.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(id = R.string.select_locker),
            style = TextStyle(
                fontSize = (screenWidth.value * 0.04f).sp,
                fontWeight = FontWeight.Bold,
                color = primaryDark
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .padding(horizontal = 16.dp)
        ) {
            items(lockers.chunked(3)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { locker ->
                        LockerItem(
                            boxId = locker.boxId,
                            type = locker.boxSize.name,
                            abbreviation = locker.boxSize.name.getAbbreviation(),
                            availableNumber = if (locker.boxState == BoxState.AVAILABLE) 1 else 0,
                            itemWidth = itemWidth,
                            isSelected = selectedLocker == locker.boxId.toString(),
                            onSelect = { onLockerSelect(locker.boxId.toString()) },
                            screenWidth = screenWidth
                        )

                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.width(itemWidth))
                    }
                }
            }
        }

    }
}

@Composable
fun DropOffSection(
    screenWidth: Dp,
    screenHeight: Dp,
    selectedLocker: String?,
    wayBillNo: String,
    onWayBillNoChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Box(
        modifier = Modifier
            .padding(24.dp)
            .border(
                color = borderColor,
                width = 1.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
            .fillMaxWidth()
            .height(0.22 * screenHeight),
    ) {
        Column(
            modifier = Modifier.padding(
                top = 0.02 * screenHeight,
                start = 0.05 * screenWidth,
                bottom = 0.02 * screenHeight
            ),
        ) {
            Text(
                text = stringResource(id = R.string.scan_order_number),
                style = TextStyle(
                    color = fieldsTitles,
                    fontSize = (screenWidth.value * 0.03f).sp,
                    textAlign = TextAlign.Start
                )
            )
            Spacer(modifier = Modifier.height(0.01 * screenHeight))
            Column {
                Spacer(modifier = Modifier.height(0.01 * screenHeight))
                OutlinedInputField(
                    value = wayBillNo,
                    hintText = "XXXX-XXXX-XXXX",
                    onValueChange = onWayBillNoChange,
                    hintTextSize = (screenWidth.value * 0.035f).sp,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    fontSize = (screenWidth.value * 0.035f).sp,
                    cornerRadius = (screenWidth.value * 0.06f),
                    modifier = Modifier.width(0.8 * screenWidth)
                        .focusRequester(focusRequester)

                )
                Spacer(modifier = Modifier.height(0.03 * screenWidth))
                ConfirmButton(screenWidth, screenHeight, onConfirm)
            }
        }
    }
}

@Composable
fun ConfirmButton(screenWidth: Dp, screenHeight: Dp, onClick: () -> Unit) {
    val context = LocalContext.current
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
            .padding(
                top = (screenHeight.value * 0.01f).dp,
                end = (screenWidth.value * 0.0f).dp,
                bottom = (screenHeight.value * 0.005f).dp,
                start = (screenWidth.value * 0.04f).dp
            )
            .width(0.75 * screenWidth)
            .height(0.05 * screenHeight)
            .clickable {


                onClick()

            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.confirm),
            style = TextStyle(
                color = Color.White,
                fontSize = (screenWidth.value * 0.03f).sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

fun String.getAbbreviation(): String {
    return when (this) {
        "X-small" -> "XS"
        "Small" -> "S"
        "Medium" -> "M"
        "Large" -> "L"
        "X-Large" -> "XL"
        "2X-Large" -> "2XL"
        "3X-Large" -> "3XL"
        "4X-Large" -> "4XL"
        "Conveyor" -> "C"
        else -> this.first().toString()
    }
}
