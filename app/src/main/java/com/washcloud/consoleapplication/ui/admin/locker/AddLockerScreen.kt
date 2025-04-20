package com.washcloud.consoleapplication.ui.admin.locker

import android.net.Uri
import android.os.FileUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.secondaryColor

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.times

import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.BoxType

import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.utils.OutlinedInputField
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.dimBackground
import com.washcloud.consoleapplication.utils.primaryDark
import kotlinx.coroutines.delay

@Composable
fun AddLockerScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    onBack: () -> Unit,
    showAd2: () -> Unit
) {
    val viewModel: LockerViewModel = hiltViewModel()
    var boxId by remember { mutableStateOf("") }
    var branchId by remember { mutableStateOf("") }
    var stationId by remember { mutableStateOf("") }
    var portId by remember { mutableStateOf("") }
    var selectedBoxSize by remember { mutableStateOf(BoxSizeType.MEDIUM) }
    var selectedBoxType by remember { mutableStateOf(BoxType.BOX) }
    val timerViewModel: TimerViewModel = hiltViewModel()
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    val errorMessageStr = stringResource(id = R.string.error_fill_all_fields)
    val successMessage = stringResource(id = R.string.success_locker_added)
    val LockerStr = stringResource(id = R.string.locker)
    val boxesToInsert by viewModel.boxesToInsert.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    var operationDoneSuccessfully by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var selectedUri: Uri? by remember { mutableStateOf(null) }

        val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            viewModel.loadCsvFile(context, it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetInsertBoxes()
        showAlert = false
        operationDoneSuccessfully = false


    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(interactionModifier)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.add_locker),
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

        Column(
            modifier = Modifier
                .padding(16.dp)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.box_id),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenWidth.value * 0.032f).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontSize = (screenWidth.value * 0.032f).sp,
                hintTextSize = (screenWidth.value * 0.032f).sp,
                value = boxId,
                hintText = stringResource(id = R.string.box_id),
                onValueChange = { boxId = it },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.branch_id),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenWidth.value * 0.032f).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontSize = (screenWidth.value * 0.032f).sp,
                hintTextSize = (screenWidth.value * 0.032f).sp,
                value = branchId,
                hintText = stringResource(id = R.string.branch_id),
                onValueChange = { branchId = it },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.station_id),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenWidth.value * 0.032f).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontSize = (screenWidth.value * 0.032f).sp,
                hintTextSize = (screenWidth.value * 0.032f).sp,
                value = stationId,
                hintText = stringResource(id = R.string.station_id),
                onValueChange = { stationId = it },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.port_id),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenWidth.value * 0.032f).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontSize = (screenWidth.value * 0.032f).sp,
                hintTextSize = (screenWidth.value * 0.032f).sp,
                value = portId,
                hintText = stringResource(id = R.string.port_id),
                onValueChange = { portId = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.select_box_size),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenWidth.value * 0.032f).sp
                )
            )
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                BoxSizeType.values().forEach { size ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedBoxSize = size }
                    ) {
                        RadioButton(
                            selected = selectedBoxSize == size,
                            onClick = { selectedBoxSize = size },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = secondaryColor,
                                unselectedColor = primaryDark
                            )
                        )
                        Text(text = size.name, fontSize = (screenWidth.value * 0.02f).sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.select_box_type),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (screenWidth.value * 0.032f).sp
                )
            )

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                BoxType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedBoxType = type }
                    ) {
                        RadioButton(
                            selected = selectedBoxType == type,
                            onClick = { selectedBoxType = type },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = secondaryColor,
                                unselectedColor = primaryDark
                            )
                        )
                        Text(text = type.name, fontSize = (screenWidth.value * 0.02f).sp)
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(secondaryColor, RoundedCornerShape(16.dp))
                    .clickable {
                        if (boxId.isNotEmpty() && branchId.isNotEmpty() && stationId.isNotEmpty() && portId.isNotEmpty()) {
                            viewModel.addLocker(
                                boxId = boxId.toLong(),
                                boxNumber = boxId.toLong(),
                                branchId = branchId.toLong(),
                                stationId = stationId.toLong(),
                                portId = portId,
                                boxSize = selectedBoxSize,
                                boxType = selectedBoxType
                            )

                            alertMessage = successMessage + " 1 " + LockerStr

                            showAlert = true
                            operationDoneSuccessfully = true

                        } else {
                            alertMessage = errorMessageStr
                            showAlert = true
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.add_locker),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.034f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(secondaryColor, RoundedCornerShape(16.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .clickable {

                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(id = R.string.add_via_file),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.034f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(16.dp),
                style = TextStyle(fontSize = 32.sp)
            )
        }

        boxesToInsert?.let { boxes ->

            if(boxes.isNotEmpty() && !operationDoneSuccessfully){
                operationDoneSuccessfully = true
                viewModel.insertBoxes(boxes)
                alertMessage = successMessage + " " + boxes.size + " " + LockerStr
                showAlert = true

            }


        }



            Spacer(modifier = Modifier.weight(1f))
        BottomNavigationWithBackAndTimer(
            screenWidth,
            screenHeight,
            isAdmin = false,
            timerViewModel,
            showAd2,
            onBack
        )

    }
        if (showAlert) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(screenWidth)
                    .height(screenHeight)
                    .background(dimBackground)
                    .clickable {

                        if (operationDoneSuccessfully) {
                            onBack()
                        }
                        showAlert = false

                    }
            ) {
                Box(
                    modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(0.02 * screenWidth)
                        )
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
                            text = alertMessage,
                            style = TextStyle(
                                fontSize = (screenWidth.value * 0.03f).sp,
                                color = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Box(
                            modifier = Modifier
                                .padding(start = 24.dp, end = 24.dp)
                                .clip(
                                    RoundedCornerShape((screenHeight.value * 0.011f).dp)
                                )
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

                                    if (operationDoneSuccessfully) {
                                        viewModel.resetInsertBoxes()
                                        onBack()
                                    }
                                    showAlert = false
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
                    }
                }
            }
        }

}