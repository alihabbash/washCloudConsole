package com.washcloud.consoleapplication.ui.bagCounter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.numbersColor
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.screenBackground
import com.washcloud.consoleapplication.utils.secondaryColor
import kotlinx.coroutines.delay

@Composable
fun BagCounterView(
    screenWidth: Dp,
    screenHeight: Dp,
    viewModel: BagCounterViewModel,
    onBack: () -> Unit,

) {
//    val viewModel: BagCounterViewModel = hiltViewModel()

    val sessionCount by viewModel.sessionCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    // Inactivity countdown (seconds)
    var inactivityLeft by remember { mutableIntStateOf(25) }



    //  Register / unregister scanner receiver based on screen visibility
    DisposableEffect(Unit) {
        viewModel.registerScannerDataReceiver()
        onDispose {
            viewModel.unregisterScannerDataReceiver()
        }
    }

    LaunchedEffect(sessionCount) {
        inactivityLeft = 25
        while (inactivityLeft > 0) {
            delay(1000L)
            inactivityLeft--
        }
        // 25 seconds passed with no new scan
        onBack()
    }

    Box(
        modifier = Modifier
            .width(screenWidth)
//            .fillMaxWidth()
//            .fillMaxHeight()
            .height(screenHeight)
            .background(screenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top bar with title + total counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.bag_counter_title), // e.g. "Scan Bags"
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.04f).sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryDark
                    )
                )

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(id = R.string.total_bags_label), // "Total scanned"
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.025f).sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryColor
                        )
                    )
                    Text(
                        text = totalCount.toString(),
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.05f).sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryColor
                        )
                    )
                }
            }

            // Center big session counter
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sessionCount.toString(),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.20f).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = numbersColor
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.session_bags_label), // "Bags this session"
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.03f).sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryDark
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.scan_bag_hint), // "Scan bags to increase the counter"
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.025f).sp,
                        color = Color.Gray
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Bottom: back button (or you can reuse your existing BottomNavigationWithBack…)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(numbersColor, secondaryColor)
                        )
                    )
                    .clickable { onBack() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.back),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.03f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewBagCounterView() {
    BagCounterView(
        screenWidth = 360.dp,
        screenHeight = 640.dp,
        onBack = {},
        viewModel = BagCounterViewModel(
            broadcastReceiverRepository = TODO(),
            application = TODO()
        ),

    )
}