package com.washcloud.consoleapplication.ui.admin.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.local.database.dto.CustomerUserDto
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor

@Composable
fun StoredCustomersScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    showAd2: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: StoredCustomersViewModel = hiltViewModel()
    val timerViewModel: TimerViewModel = hiltViewModel()
    val customers by viewModel.customers.collectAsState()

    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    Column(
        modifier = Modifier
            .height(screenHeight)
            .width(screenWidth)
            .fillMaxWidth()
            .then(interactionModifier),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.stored_customers_title),
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            if (customers.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.no_customers_found),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.04f).sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(customers) { customer ->
                        CustomerCard(customer = customer, screenWidth = screenWidth)
                    }
                }
            }
        }

        BottomNavigationWithBackAndTimer(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            isAdmin = true,
            timerViewModel = timerViewModel,
            onBack = onBack,
            showAd2 = showAd2
        )
    }
}

@Composable
fun CustomerCard(customer: CustomerUserDto, screenWidth: Dp) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    color = borderColor,
                    width = 1.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .shadow(3.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer.userId.toString(),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.035f).sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryDark
                    ),
                    modifier = Modifier.padding(end = 32.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = customer.customerName ?: "N/A",
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.035f).sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryColor
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = customer.phoneNumber ?: "N/A",
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.03f).sp,
                            color = Color.Gray
                        )
                    )
                }
            }
        }
    }
}
