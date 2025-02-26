package com.washcloud.consoleapplication.ui.dropoff

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.local.database.dto.BoxDto
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.database.utils.BoxSizeType
import com.washcloud.consoleapplication.local.database.utils.TransactionType
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.utils.*
import java.util.Date

@Composable
fun DropOffView(
    screenWidth: Dp,
    screenHeight: Dp,
    showStaffStart: () -> Unit,
    showContinueToDropOff: () -> Unit,
    showAd2: () -> Unit,
) {
    val viewModel: DropOffViewModel = hiltViewModel()
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val transactions by viewModel.transactions.collectAsState()
    val selectionRequiredText = stringResource(id = R.string.selection_required)
    val selectOrderBeforeProceedingText = stringResource(id = R.string.select_order_before_proceeding)

    var alertTitle by remember { mutableStateOf(selectionRequiredText) }
    var alertMessage by remember { mutableStateOf(selectOrderBeforeProceedingText) }
    var showAlert by remember { mutableStateOf(false) }

    val timerViewModel: TimerViewModel = hiltViewModel()


    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    LaunchedEffect(Unit) {
        Log.e("DropOffView", "LaunchedEffect")
        viewModel.fetchTransactions()
    }
    Box {
        Column(
            modifier = Modifier
                .height(screenHeight)
                .width(screenWidth).then(interactionModifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Header(screenWidth)
            if(transactions.isEmpty()) {
                NoTransactionsMessage(screenWidth)
            }else{
                DropOffList(screenWidth, screenHeight, transactions, selectedIndex) { index ->
                    selectedIndex = index
                }
            }

            BottomButtons(screenWidth, screenHeight, showContinueToDropOff) {

                if (selectedIndex == -1) {
                    showAlert = true

                }else{
                    val currentTransaction = transactions[selectedIndex]
                    viewModel.recall(currentTransaction.orderSerial, currentTransaction.boxId.toString(), currentTransaction.boxType.name)
                }


            }
            Spacer(modifier = Modifier.weight(1f))
            BottomNavigationWithBackAndTimer(screenWidth, screenHeight,  isAdmin = false, timerViewModel ,showAd2, showStaffStart)
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
fun Header(screenWidth: Dp) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(id = R.string.drop_off),
        style = TextStyle(
            fontSize = (screenWidth.value * 0.04f).sp,
            fontWeight = FontWeight.Bold,
            color = primaryDark
        ),
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun DropOffList(screenWidth: Dp, screenHeight: Dp, transactions: List<BoxDto>,selectedIndex: Int, onItemSelected: (Int) -> Unit) {
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
            .height(0.7 * screenHeight.value.dp)
    ) {
        LazyRow(modifier = Modifier.padding(top = 0.006 * screenHeight.value.dp)) {
            items(transactions.size) { index ->
                Column {
                    DropOffItem(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        transaction = transactions[index],
                        isSelected = index == selectedIndex
                    ) {
                        onItemSelected(index)
                    }
                    if (index != transactions.size - 1) {
                        DropOffListDivider(screenWidth)
                    }
                }
            }
        }
    }
}

@Composable
fun DropOffItem(screenWidth: Dp, screenHeight: Dp,transaction: BoxDto ,isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) lightGrey else Color.White
    val textColor = if (isSelected) secondaryColor else primaryDark

    Row(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() },

        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(R.drawable.drop_off_circle),
            "drop_off_circle",
            modifier = Modifier
                .width(0.1 * screenWidth.value.dp)
                .height(0.1 * screenWidth.value.dp),
        )
        Spacer(modifier = Modifier.width(24.dp))
        OrderDetails(screenWidth, textColor, transaction)
        Box(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(24.dp))
    }
}

@Composable
fun OrderDetails(screenWidth: Dp, textColor: Color, transaction: BoxDto) {
    Row {
        Column {
            Text(
                text = stringResource(id = R.string.order_number),
                style = TextStyle(
                    color = textColor,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.box_number),
                style = TextStyle(
                    color = textColor,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
        }
        Column {
            Text(
                text = transaction.orderSerial,
                style = TextStyle(
                    color = textColor,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = transaction.boxId.toString(),
                style = TextStyle(
                    color = textColor,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
        }
        Column(modifier = Modifier.padding(start = 0.05 * screenWidth.value.dp)) {
//            Text(
//                text = stringResource(id = R.string.mobile),
//                style = TextStyle(
//                    color = textColor,
//                    fontSize = (screenWidth.value * 0.03f).sp
//                )
//            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.time),
                style = TextStyle(
                    color = textColor,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
        }
        Column {
//            Text(
//                text = "0552516789",
//                style = TextStyle(
//                    color = textColor,
//                    fontSize = (screenWidth.value * 0.03f).sp
//                )
//            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = transaction.trnasDate.toString(),
                style = TextStyle(
                    color = textColor,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
        }
    }
}

@Composable
fun DropOffListDivider(screenWidth: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        VerticalDivider(
            modifier = Modifier
                .height(1.dp)
                .width(screenWidth - (0.15 * screenWidth))
                .background(color = borderColor)
                .padding(horizontal = 24.dp)
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun NoTransactionsMessage(screenWidth: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.no_transactions_available),
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
fun BottomButtons(screenWidth: Dp, screenHeight: Dp, showContinueToDropOff: () -> Unit, recall: () -> Unit){
    Row(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(32.dp)
    ) {
        val buttonModifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        blueGradient,
                        secondaryColor,
                    ),
                )
            )
            .padding(
                top = (screenHeight.value * 0.02f).dp,
                bottom = (screenHeight.value * 0.02f).dp,
                start = (screenWidth.value * 0.04f).dp,
                end = (screenWidth.value * 0.04f).dp
            )

        Box(
            modifier = buttonModifier.clickable {

                recall()

            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.recall_clothes),
                style = TextStyle(
                    color = Color.White,
                    fontSize = (screenWidth.value * 0.03f).sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = buttonModifier.clickable {
                showContinueToDropOff()
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.continue_to_drop_off),
                style = TextStyle(
                    color = Color.White,
                    fontSize = (screenWidth.value * 0.03f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDropOffView() {
    DropOffView(
        screenWidth = 360.dp,
        screenHeight = 640.dp,
        showStaffStart = {},
        showContinueToDropOff = {},
        showAd2 = {}
    )
}

