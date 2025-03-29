package com.washcloud.consoleapplication.ui.pickup

import android.util.Log
import android.view.KeyEvent
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key


import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.utils.FileLogger
import com.washcloud.consoleapplication.utils.OutlinedInputField
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.clearText
import com.washcloud.consoleapplication.utils.fieldsTitles
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.utils.numbersColor
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor
import kotlinx.coroutines.delay

@Composable
fun PickupView(
    screenWidth: Dp,
    screenHeight: Dp,
    showStaffStart: () -> Unit,
    showAd2: () -> Unit,
) {
    val viewModel: PickupViewModel = hiltViewModel()
    val transactions = viewModel.transactions.collectAsState().value
    val staffPickupResponse by viewModel.staffPickupResponse.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var wayBillNo by remember { mutableStateOf("") }
    var wayBillNoHidden by remember { mutableStateOf("") }
    val context = LocalContext.current

    val timerViewModel: TimerViewModel = hiltViewModel()


//    val interactionModifier = Modifier.pointerInput(Unit) {
//        detectTapGestures(onTap = {
//            timerViewModel.pauseTimer()
//            timerViewModel.resumeTimerAfterDelay(1000)
//        })
//    }

    val keyboardController = LocalSoftwareKeyboardController.current
    var isKeyboardVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
  //  var isInputEnabled by remember { mutableStateOf(true) }
     LaunchedEffect(Unit) {
        focusRequester.requestFocus()
         delay(10L)
         keyboardController?.hide();
    }

//    LaunchedEffect(Unit) {
//       delay(10000L)
//        wayBillNo = "O112503280003-1"
//        viewModel.sendMockDoorScannerDataBrodcast()
//    }


//    LaunchedEffect(isInputEnabled) {
//        if (!isInputEnabled) {
//            delay(5000L)
//            isInputEnabled = true
//        }
//    }
    LaunchedEffect(wayBillNo) {
        if (isValidSerialNumber(wayBillNo)) {
            viewModel.staffPickup(wayBillNo)
            wayBillNo = ""
        }
    }

    LaunchedEffect(wayBillNoHidden) {
        if (isValidSerialNumber(wayBillNoHidden)) {
            viewModel.staffPickup(wayBillNoHidden)
            wayBillNoHidden = ""
        }
    }

    var barcodeData by remember { mutableStateOf("") }


    val interactionModifier = Modifier
        .fillMaxSize()
        .onKeyEvent { event ->



            FileLogger.log(context, "PickupView", "onKeyEvent: ${event.nativeKeyEvent.action} ${event.nativeKeyEvent.keyCode} ${event.nativeKeyEvent.unicodeChar.toChar()}");
            if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN || event.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                val unicodeChar = event.nativeKeyEvent.unicodeChar.toChar()

                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    Log.e("PickupView", "Barcode data: $barcodeData")

                    FileLogger.log(context, "PickupView", "Barcode data: $barcodeData")

                    wayBillNoHidden =  barcodeData.trim()

                    barcodeData = ""
                    true
                } else {
                    barcodeData += unicodeChar
                    false
                }
            } else {
                false
            }
        }
    Box {
        Column(
            modifier = Modifier
                .width(screenWidth)
                .then(interactionModifier)

            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.pickup),
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.04f).sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryDark
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))

            //pickup list
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
                    .height(0.65 * screenHeight)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(top = 0.006 * screenHeight)
                ) {
                    items(transactions.size) { index ->
                        val transaction = transactions[index]
                        Column {
                            pickUpItem(screenWidth, screenHeight, transaction, viewModel)
                            if (index != transactions.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(
                                            top = 0.01 * screenHeight,
                                            bottom = 0.01 * screenHeight
                                        )
                                ) {
                                    PickUpListDivider(screenWidth, screenHeight)
                                }

                            }

                        }

                    }

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
                    .height(0.12 * screenHeight)
            ){
                Column (
                    modifier = Modifier.padding(top = 0.02 * screenHeight,
                        start = 0.05*screenWidth, bottom = 0.02 * screenHeight)
                ){
                    Text(text = stringResource(id = R.string.scan_order_number),
                        style = TextStyle(
                            color = fieldsTitles,
                            fontSize = (screenWidth.value * 0.03f).sp
                        ))
                    Spacer(modifier = Modifier.height(0.015 * screenHeight))
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ){
                        Spacer(modifier = Modifier.height(0.01 * screenHeight))
                        OutlinedInputField(

                            value = wayBillNo,
                            hintText = "XXXX-XXXX-XXXX",
                            onValueChange = { newValue ->
                                if (isLoading){
                                    wayBillNo = ""
                                }else{
                                    wayBillNo = newValue
                                }

                            },
                            hintTextSize = (screenWidth.value * 0.035f).sp,
                            fontSize = (screenWidth.value * 0.035f).sp,
                            cornerRadius = (screenWidth.value * 0.06f),
                           enabled = true,
                            modifier = Modifier
                                .width(0.66 * screenWidth)

                            ,
                            trailingIcon = {
                                val iconRes = if (isKeyboardVisible) R.drawable.keyboard_hide else R.drawable.keyboard_show
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = "Toggle Keyboard",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable {
                                            isKeyboardVisible = !isKeyboardVisible
                                            if (isKeyboardVisible) {
                                                keyboardController?.show()
                                                focusRequester.requestFocus()
                                            } else {
                                                keyboardController?.hide()
                                            }
                                        }
                                )
                            },
                              //  .focusRequester(focusRequester)
                              /*  .onKeyEvent { event ->
                                    if (event.key.keyCode.toInt() == KeyEvent.KEYCODE_ENTER) {
                                        if (isValidSerialNumber(wayBillNo)) {
                                            viewModel.staffPickup(wayBillNo)
                                            //wayBillNo = ""
                                        } else
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Invalid order number",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                    }
                                    false
                                }*/
                        )




                        Spacer(modifier = Modifier.width(0.04*screenWidth))
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(12.dp)
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
                                    top = (screenHeight.value * 0.005f).dp,
                                    end = (screenWidth.value * 0.04f).dp,
                                    bottom = (screenHeight.value * 0.005f).dp,
                                    start = (screenWidth.value * 0.04f).dp
                                )
                                .clickable {

                                    if (!URLUtil.isValidUrl(wayBillNo)) {
                                        viewModel.staffPickup(wayBillNo)
                                        wayBillNo = ""
                                    } else
                                        Toast
                                            .makeText(
                                                context,
                                                "Invalid order number",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()


                                }
                        ) {
                            Text(
                                text = stringResource(id = R.string.confirm),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (screenWidth.value * 0.03f).sp
                                )
                            )
                        }
                    }

                    OutlinedInputField(

                        value = wayBillNoHidden,
                        hintText = "XXXX-XXXX-XXXX",
                        onValueChange = { newValue ->
                            if (isLoading){
                                wayBillNoHidden = ""
                            }else{
                                wayBillNoHidden = newValue
                            }

                        },
                        hintTextSize = (screenWidth.value * 0.035f).sp,
                        fontSize = (screenWidth.value * 0.035f).sp,
                        cornerRadius = (screenWidth.value * 0.06f),
                        enabled = true,
                        modifier = Modifier
                            .width(0.66 * screenWidth)
                            .height(5.dp)
                            .alpha(0f)
                            .focusRequester(focusRequester)

                    )

                }


            }




            Box(
                modifier =
                Modifier.weight(1f)
            )




            BottomNavigationWithBackAndTimer(screenWidth, screenHeight,  isAdmin = false, timerViewModel ,showAd2, showStaffStart)
        }
    }

    if(isLoading){
        PickupDialog(screenWidth = screenWidth, screenHeight = screenHeight);
    }
}

@Composable
fun pickUpItem(
    screenWidth: Dp,
    screenHeight: Dp,
    transaction: BoxDto,
    viewModel: PickupViewModel,
) {
    Row(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(R.drawable.pickup_circle),
            "pickup_circle",
            modifier = Modifier
                .width(0.1 * screenWidth)
                .height(0.1 * screenWidth),
        )

        Spacer(modifier = Modifier.width(24.dp))
        Row {
            Column {
                Text(
                    text = stringResource(id = R.string.order_number) ,
                    style = TextStyle(
                        color = clearText,
                        fontSize = (screenWidth.value * 0.03f).sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.box_number),
                    style = TextStyle(
                        color = clearText,
                        fontSize = (screenWidth.value * 0.03f).sp
                    )
                )
            }
            Column {
                Text(
                    text = transaction.orderSerial,
                    style = TextStyle(
                        color = numbersColor,
                        fontSize = (screenWidth.value * 0.03f).sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =  transaction.boxId.toString(),
                    style = TextStyle(
                        color = numbersColor,
                        fontSize = (screenWidth.value * 0.03f).sp
                    )
                )
            }
        }
        Box(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(12.dp)
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
                    top = (screenHeight.value * 0.005f).dp,
                    end = (screenWidth.value * 0.04f).dp,
                    bottom = (screenHeight.value * 0.005f).dp,
                    start = (screenWidth.value * 0.04f).dp
                )
                .clickable {
                    viewModel.printTransaction(transaction)
                }
        ) {
            Text(
                text = stringResource(id = R.string.print),
                style = TextStyle(
                    color = Color.White,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
        }
        Box(modifier = Modifier.width(24.dp))
    }
}
//
//@Composable
//fun BarcodeScannerView(
//    onBarcodeScanned: (String) -> Unit
//) {
//    var barcodeData by remember { mutableStateOf("") }
//
//
//    val serialPattern = Regex("^\\d{13}-\\d{1}\$")
//
//    val focusRequester = remember { FocusRequester() }
//    val context = LocalContext.current
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .focusRequester(focusRequester)
//            .onKeyEvent { event ->
//                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
//                    val unicodeChar = event.nativeKeyEvent.unicodeChar.toChar()
//                    Log.e("unicodeChar", unicodeChar.toString())
//
//                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
//                        FileLogger.log(context, "BarcodeScannerView barcodeData", "barcodeData: $barcodeData")
//                        val barcode = "4442408120002-1"
//                            //barcodeData
//                           // .trim()
//
//
//                        FileLogger.log(context, "BarcodeScannerView barcode", "barcode: $barcodeData")
//
//                        Log.e("barcode", barcode)
//
//                        if (serialPattern.matches(barcode)) {
//
//                            onBarcodeScanned(barcode)
//                        }
//                        barcodeData = ""
//                    } else {
//                        barcodeData += unicodeChar
//                    }
//                    true
//                } else {
//                    false
//                }
//            }
//    ) {
//
//    }
//
//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//    }
//}


@Composable
fun PickUpListDivider(
    screenWidth: Dp,
    screenHeight: Dp,
) {
    VerticalDivider(
        modifier =
        Modifier
            .height(1.dp)
            .background(color = borderColor)
            .padding(start = 24.dp, end = 24.dp)
            .width(screenWidth - (0.15 * screenWidth))
            .padding(start = 24.dp, end = 24.dp),

        )
}

fun isValidSerialNumber(serial: String): Boolean {
    //  "4442408120002-1"
    val serialPattern = Regex("^[A-Za-z0-9]{13}(-\\d{1})?$")
    return serial.matches(serialPattern)
}

