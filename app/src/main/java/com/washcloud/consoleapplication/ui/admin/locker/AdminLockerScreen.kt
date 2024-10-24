package com.washcloud.consoleapplication.ui.admin.locker


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.secondaryColor
import kotlin.random.Random
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.utils.OutlinedInputField
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.dimBackground

@Composable
fun AdminLockerScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    onAddLockerClick: () -> Unit,
    onBack: () -> Unit,
    showAd2: () -> Unit
) {

    val viewModel: LockerViewModel = hiltViewModel()
    var selectedConveyor by remember { mutableStateOf("") }
    var selectedLocker by remember { mutableStateOf("") }

    val lockers by viewModel.lockers.collectAsState(initial = emptyList())

    val timerViewModel: TimerViewModel = hiltViewModel()

    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var userClickOnTheDeleteion by remember { mutableStateOf(false) }
    var locker_deleted_success = stringResource(id = R.string.locker_deleted_success)
    var locker_deleted_failed = stringResource(id = R.string.locker_deleted_failed)
    var sure_to_delete = stringResource(id = R.string.are_you_sure_delete)

    var userConfirmedDeletion by remember { mutableStateOf(false) }
    var lockerToDelete by remember { mutableStateOf<Int?>(null) }
    var confirmationMessage by remember { mutableStateOf("") }

    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    val showConfirmationDialog: (String, Int) -> Unit = { message, lockerNumber ->
        confirmationMessage = message
        userConfirmedDeletion = true
        lockerToDelete = lockerNumber
        showAlert = true
    }


    LaunchedEffect(Unit) {
        viewModel.fetchLockers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(interactionModifier)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Admin Locker",
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        dateAndTimeView(screenWidth = screenWidth)
        Spacer(modifier = Modifier.height(24.dp))

//        Row(
//            modifier = Modifier
//                .padding(16.dp)
//                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
//                .background(Color.White, RoundedCornerShape(16.dp))
//                .shadow(4.dp, RoundedCornerShape(16.dp))
//                .padding(16.dp)
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically,
//
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement =  Arrangement.Center,
//
//            ) {
//                Text(text = "Conveyor #", style = TextStyle(fontSize = 32.sp))
//                Spacer(modifier = Modifier.width(8.dp))
//                OutlinedInputField(
//                    value = selectedConveyor,
//                    hintText = "00",
//                    onValueChange = { selectedConveyor = it },
//                    enabled = true
//                )
//                Spacer(modifier = Modifier.width(16.dp))
//
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Column {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center,
//                ) {
//                    TextButton("Close Door") { /* Handle Close Door */ }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    TextButton("Clear Status") { /* Handle Clear Status */ }
//                }
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center,
//                ) {
//                    TextButton("Close Door") { /* Handle Close Door */ }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    TextButton("Clear Status") { /* Handle Clear Status */ }
//                }
//            }
//
//        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row( modifier = Modifier.width(0.25 * screenWidth),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,){
                    Text(text = stringResource(id = R.string.locker), style = TextStyle(fontSize =   (screenWidth.value * 0.032f).sp,))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedInputField(
                        value = selectedLocker,
                        hintText = "00",
                        onValueChange = { selectedLocker = it },
                        enabled = true,
                        fontSize = (screenWidth.value * 0.032f).sp,
                        hintTextSize = (screenWidth.value * 0.032f).sp,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
               Row ( verticalAlignment = Alignment.CenterVertically,
                   horizontalArrangement = Arrangement.Center,){
                   TextButton(stringResource(id = R.string.open)) {
                       if(selectedLocker.isNotEmpty()) {

                       viewModel.sendCommand("com.washcloud.open_door", "02", selectedLocker)
                   }
                   }
                   TextButton(stringResource(id = R.string.reset)) { /* Handle Reset */ }
               }
            }
        }


        Column(
            modifier = Modifier
                .padding(16.dp)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(stringResource(id = R.string.open_all_empty_lockers), secondaryColor) { viewModel.openAllEmptyLockers() }
                TextButton(stringResource(id = R.string.open_all_accuity_lockers), Color.Red) {  viewModel.openAllOccupiedLockers()}
                TextButton(stringResource(id = R.string.check_locker_status), secondaryColor) {  }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn( modifier = Modifier
                .fillMaxWidth()
                .height(0.5 * screenHeight)) {
                items(lockers.chunked(10)) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowItems.forEachIndexed { index, box ->
                            LockerGridItem(
                                lockerNumber = box.boxId.toInt(),
                                isOccupied = box.boxState == BoxState.OCCUPIED,
                                onDeleteClick = { lockerNumber ->
                                    showConfirmationDialog(  sure_to_delete + " #$lockerNumber?", lockerNumber)
                                },
                                showErrorMessage = {->
                                    alertMessage = locker_deleted_failed
                                    showAlert = true
                                }
                            )
                        }
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .background(secondaryColor, RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable { onAddLockerClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(id = R.string.add_more_lokers),
                color = Color.White,
                style = TextStyle(fontSize = 50.sp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        BottomNavigationWithBackAndTimer(screenWidth, screenHeight,  isAdmin = false, timerViewModel, showAd2, onBack)
    }

    if (showAlert) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(screenWidth)
                .height(screenHeight)
                .background(dimBackground)
                .clickable { showAlert = false }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(0.02 * screenWidth))
                    .background(color = Color.White)
                    .width(0.8 * screenWidth)
                    .height(0.16 * screenHeight),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (lockerToDelete == null) alertMessage else confirmationMessage,
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.03f).sp,
                            color = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp)
                            .clip(RoundedCornerShape((screenHeight.value * 0.011f).dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        blueGradient,
                                        secondaryColor,
                                    ),
                                )
                            )
                            .padding(
                                start = 16.dp,
                                top = (screenHeight.value * 0.01f).dp,
                                end = 16.dp,
                                bottom = (screenHeight.value * 0.01f).dp
                            )
                            .fillMaxWidth()
                            .clickable {

                                if (lockerToDelete != null) {
                                    viewModel.deleteLocker(lockerToDelete!!)
                                    lockerToDelete = null
                                    alertMessage = locker_deleted_success
                                    showAlert = true
                                } else {
                                    showAlert = false
                                }


                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.ok),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (screenWidth.value * 0.028f).sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp)
                            .clip(RoundedCornerShape((screenHeight.value * 0.011f).dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        blueGradient,
                                        secondaryColor,
                                    ),
                                )
                            )
                            .padding(
                                start = 16.dp,
                                top = (screenHeight.value * 0.01f).dp,
                                end = 16.dp,
                                bottom = (screenHeight.value * 0.01f).dp
                            )
                            .fillMaxWidth()
                            .clickable {
                                showAlert = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (screenWidth.value * 0.028f).sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockerGridItem(
    lockerNumber: Int,
    isOccupied: Boolean,
    onDeleteClick: (Int) -> Unit,
    showErrorMessage: () -> Unit
) {
    val backgroundColor = if (isOccupied) Color.Red else Color(0xFFEDEDEF)
    val textColor = if (isOccupied) Color.White else secondaryColor
    val confirmationMessage = stringResource(id = R.string.are_you_sure_delete)
    Box(
        modifier = Modifier
            .padding(4.dp)
            .padding(top = 16.dp)
            .size(80.dp)
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (isOccupied) {
                            showErrorMessage()
                        } else {
                            onDeleteClick(lockerNumber)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(24.dp)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = lockerNumber.toString().padStart(2, '0'),
                color = textColor,
                style = TextStyle(fontSize = 28.sp)
            )
        }
    }
}
@Composable
fun OutlinedInput(
    value: String,
    hintText: String,
    onValueChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .background(Color.White)
    ) {
        OutlinedInputField(
            value = value,
            onValueChange = onValueChange,
            hintText = hintText,
        )

    }
}

@Composable
fun TextButton(text: String, bgColor: Color = Color(0xFF1DA0B4), onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = TextStyle( fontSize = 32.sp)
        )
    }
}

