package com.washcloud.consoleapplication.ui.admin.locker


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.times
import com.washcloud.consoleapplication.local.database.utils.BoxState
import com.washcloud.consoleapplication.utils.OutlinedInputField

@Composable
fun AdminLockerScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    numberOfLockers: Int,
    onBack: () -> Unit,
    showAd2: () -> Unit
) {

    val viewModel: LockerViewModel = hiltViewModel()
    var selectedConveyor by remember { mutableStateOf("") }
    var selectedLocker by remember { mutableStateOf("") }

    val lockers by viewModel.lockers.collectAsState(initial = emptyList())




    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    Text(text = "Locker #", style = TextStyle(fontSize =   (screenWidth.value * 0.032f).sp,))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedInputField(
                        value = selectedLocker,
                        hintText = "00",
                        onValueChange = { selectedLocker = it },
                        enabled = true,
                        fontSize = (screenWidth.value * 0.032f).sp,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
               Row ( verticalAlignment = Alignment.CenterVertically,
                   horizontalArrangement = Arrangement.Center,){
                   TextButton("Open") {
                       if(selectedLocker.isNotEmpty()) {

                       viewModel.sendCommand("com.washcloud.open_door", "02", selectedLocker)
                   }
                   }
                   TextButton("Reset") { /* Handle Reset */ }
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
                TextButton("Open All Empty", secondaryColor) { /* Handle Open All Empty */ }
                TextButton("Open All Occupied", secondaryColor) { /* Handle Open All Occupied */ }
                TextButton("Check Locker", secondaryColor) { /* Handle Check Locker */ }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(lockers.chunked(10)) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowItems.forEachIndexed { index, box ->
                            LockerGridItem(
                                lockerNumber = box.boxId.toInt(),
                                isOccupied = box.boxState == BoxState.OCCUPIED
                            )
                        }
                    }
                }
            }

        }

        Spacer(modifier = Modifier.weight(1f))
        BottomNavigationWithBackAndTimer(screenWidth, screenHeight, showAd2, onBack)
    }
}

@Composable
fun LockerGridItem(
    lockerNumber: Int,
    isOccupied: Boolean,
) {
    val backgroundColor = if (isOccupied) secondaryColor else Color(0xFFEDEDEF)
    val textColor = if (isOccupied) Color.White else secondaryColor

    Box(
        modifier = Modifier
            .padding(4.dp)
            .padding(top = 16.dp)
            .size(80.dp)
            .background(backgroundColor, RoundedCornerShape(6.dp)),
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
                style = TextStyle(fontSize = 40.sp)
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

@Preview(showBackground = true)
@Composable
fun PreviewAdminLockerScreen() {
    ConsoleApplicationTheme {
        LockerGridItem(
            lockerNumber = 1,
            isOccupied = true,
        )
    }
}
