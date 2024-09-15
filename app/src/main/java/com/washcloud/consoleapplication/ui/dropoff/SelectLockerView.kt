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
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
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

    val lockers by viewModel.lockers.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val selectionRequiredText = stringResource(id = R.string.selection_required)
    val selectLockerText = stringResource(id = R.string.select_locker_before_proceeding)
    val dropOffClothesText = stringResource(id = R.string.drop_off_clothes)
    val dropOffMessageTemplate = stringResource(id = R.string.drop_off_message_template)
    val invalid_serial_number = stringResource(id = R.string.invalid_serial_number)
    val enter_valid_serial_number = stringResource(id = R.string.enter_valid_serial_number)
    var wayBillNo by remember { mutableStateOf("") }
    val showAlert by viewModel.showAlert.collectAsState()
    var alertTitle by remember { mutableStateOf(selectionRequiredText) }
    var alertMessage by remember { mutableStateOf("Please select a locker before proceeding.") }
    var selectedLocker by remember { mutableStateOf<BoxDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchLockers()
    }

    LaunchedEffect(error) {
        if (error != null) {
            alertTitle = "Error"
            alertMessage = error ?: "An error occurred"
            viewModel.setShowAlert()
        }
    }
    /*LaunchedEffect(Unit) {
    delay(10000L)
        wayBillNo = "4442408250004-1"
   }*/

    LaunchedEffect(wayBillNo) {
     if (isValidSerialNumber(wayBillNo)) {



         if (selectedLocker == null) {
             viewModel.setShowAlert()
             alertTitle = selectionRequiredText
             alertMessage = selectLockerText
             wayBillNo = ""

         } else if (isValidSerialNumber(wayBillNo)) {
             //viewModel.setShowAlert()
             alertTitle = dropOffClothesText
             alertMessage = dropOffMessageTemplate.format(selectedLocker?.boxId ?: "None")
             viewModel.dropoff(wayBillNo, selectedLocker!!.boxId.toString())
             wayBillNo = ""
             selectedLocker = null

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

                LockerListGrouped(lockers, screenWidth, screenHeight, selectedLocker) { locker ->
                    selectedLocker = locker
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
                    selectedLocker?.boxId.toString() ?: "",
                    wayBillNo = wayBillNo,
                    onWayBillNoChange = { wayBillNo = it },
                    onConfirm = {

                        if (selectedLocker == null) {

                            viewModel.setShowAlert()
                            alertTitle = selectionRequiredText
                            alertMessage = selectLockerText
                            wayBillNo = ""

                        } else if (isValidSerialNumber(wayBillNo)) {
                            Log.e("DropOffViewModel", "Drop off clothes in locker ${selectedLocker!!.boxId}.")
                            viewModel.dropoff(wayBillNo, selectedLocker!!.boxId.toString())

                            alertTitle = dropOffClothesText
                            alertMessage = dropOffMessageTemplate.format(selectedLocker?.boxId ?: "None")
                            wayBillNo = ""
                            selectedLocker = null
                        } else {
                            viewModel.setShowAlert()
                            alertTitle = invalid_serial_number
                            alertMessage = enter_valid_serial_number
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
                        .height(0.2 * screenHeight)
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
                                    viewModel.setShowAlert(false)
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
fun LockerListGrouped(
    lockers: List<BoxDto>,
    screenWidth: Dp,
    screenHeight: Dp,
    selectedLocker: BoxDto? = null,
    onLockerSelect: (BoxDto) -> Unit,

) {
    val lockersGrouped = lockers.groupBy { it.boxSize }
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
            lockersGrouped.forEach { (boxSize, lockers) ->
                item {
                    LockerTypeItem(
                        boxSize = boxSize,
                        availableNumber = lockers.count { it.boxState == BoxState.AVAILABLE },
                        itemWidth = itemWidth,
                        onSelect = {
                            val firstAvailableLocker = lockers.firstOrNull { it.boxState == BoxState.AVAILABLE }
                            if (firstAvailableLocker != null) {
                                onLockerSelect(firstAvailableLocker)
                            }
                        },
                        screenWidth = screenWidth,
                        selectedLocker = selectedLocker
                    )
                }
            }
        }
    }
}

@Composable
fun LockerTypeItem(
    boxSize: BoxSizeType,
    availableNumber: Int,
    itemWidth: Dp,
    onSelect: () -> Unit,
    screenWidth: Dp,
    selectedLocker: BoxDto? = null

) {
    val backgroundColor = if (selectedLocker?.boxSize == boxSize ) secondaryColor.copy(alpha = 0.3F) else Color.White
    val borderColor = secondaryColor

    Row(
        modifier = Modifier
            .padding(8.dp)
            .width(itemWidth)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(enabled = availableNumber > 0) { onSelect() }
            .padding(16.dp)
            .padding(vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (availableNumber > 0) Brush.horizontalGradient(
                        colors = listOf(blueGradient, secondaryColor)
                    ) else SolidColor(Color.Gray)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = boxSize.name.getAbbreviation(),
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
                text = boxSize.name,
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.032f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (availableNumber > 0) secondaryColor else Color.Gray,
                )
            )
            Text(
                text = if (availableNumber > 0) stringResource(id = R.string.available) + " $availableNumber"
                else stringResource(id = R.string.occupied),
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.03f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (availableNumber > 0) Color.Black else Color.Gray
                )
            )
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

   /* LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }*/

    var isInputEnabled by remember { mutableStateOf(true) }
    LaunchedEffect(isInputEnabled) {
        if (!isInputEnabled) {
            delay(5000L)
            isInputEnabled = true
        }
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
                    onValueChange = {
                        onWayBillNoChange(it)
                        if (isValidSerialNumber(it)) {
                            isInputEnabled = false
                        }
                    },
                    hintTextSize = (screenWidth.value * 0.035f).sp,
                    enabled = isInputEnabled,
                    fontSize = (screenWidth.value * 0.035f).sp,
                    cornerRadius = (screenWidth.value * 0.06f),

                    modifier = Modifier.width(0.8 * screenWidth)

//                        .focusRequester(focusRequester)

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
