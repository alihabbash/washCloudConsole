package com.washcloud.consoleapplication.ui.pickup

import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.local.database.dto.TransactionDto
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
import com.washcloud.consoleapplication.remote.model.pickup.StaffPickupRequest
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.utils.OutlinedInputField
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.clearText
import com.washcloud.consoleapplication.utils.fieldsTitles
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.utils.numbersColor
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor

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
    var wayBillNo by remember { mutableStateOf("") }
 val context = LocalContext.current
    Box {
        Column(
            modifier = Modifier
                .height(screenHeight)
                .width(screenWidth),
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
                    .height(0.7 * screenHeight)
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
                                wayBillNo = newValue
                            },
                            hintTextSize = (screenWidth.value * 0.035f).sp,
                            fontSize = (screenWidth.value * 0.035f).sp,
                            cornerRadius = (screenWidth.value * 0.06f),
                            modifier = Modifier
                                .width(0.66 * screenWidth)
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

                                    if (!URLUtil.isValidUrl(wayBillNo))
                                        viewModel.staffPickup(wayBillNo)
                                    else
                                        Toast.makeText(
                                            context,
                                            "Invalid order number",
                                            Toast.LENGTH_SHORT
                                        ).show()


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

                }
            }



            Box(
                modifier =
                Modifier.weight(1f)
            )
            BottomNavigationWithBackAndTimer(screenWidth, screenHeight, showAd2, showStaffStart)
        }
    }
}

@Composable
fun pickUpItem(
    screenWidth: Dp,
    screenHeight: Dp,
    transaction: TransactionDto,
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
                    text = transaction.orderSerial.toString(),
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