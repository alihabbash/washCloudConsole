package com.washcloud.consoleapplication.ui.admin.ads

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
//import coil.compose.rememberImagePainter
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.ui.common.TimerViewModel
import com.washcloud.consoleapplication.ui.common.dateAndTimeView
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor
import kotlinx.coroutines.flow.collect

@Composable
fun AdsManagementScreen(
    screenWidth: Dp,
    screenHeight: Dp,
    showAd2: () -> Unit,
    onBack: () -> Unit,
    viewModel: AdsManagementViewModel = hiltViewModel()
) {
    val adsList by viewModel.adsList.collectAsState()

    val context = LocalContext.current
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addFile(it)
        }
    }

    val timerViewModel: TimerViewModel = hiltViewModel()


    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            timerViewModel.pauseTimer()
            timerViewModel.resumeTimerAfterDelay(1000)
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(interactionModifier)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.ads_management),
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = primaryDark
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        dateAndTimeView(screenWidth = screenWidth)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.1 * screenHeight)
                    .padding(16.dp)
                    .padding(32.dp)
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
                        pickFileLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.add_file),
                        contentDescription = null,
                        modifier = Modifier
                            .size(0.05 * screenWidth)

                        )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.add_file),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (screenWidth.value * 0.032f).sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(1.dp, secondaryColor, RoundedCornerShape(16.dp))
        ) {
            items(adsList) { uri ->
                AdItem(uri = uri, screenWidth = screenWidth, onDelete = {
                    viewModel.removeFile(uri)
                })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.1 * screenHeight)
            .padding(16.dp)
            .padding(32.dp)
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
                viewModel.saveAds()
            },
        contentAlignment = Alignment.Center
    ) {


        Text(
            text = stringResource(id = R.string.save),
            style = TextStyle(
                color = Color.White,
                fontSize = (screenWidth.value * 0.032f).sp,
                fontWeight = FontWeight.Bold
            )
        )

    }
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .padding(bottom = 32.dp)
//                .background(
//                    brush = Brush.horizontalGradient(
//                        colors = listOf(
//                            blueGradient,
//                            secondaryColor,
//                        ),
//                    ),
//                    shape = RoundedCornerShape(8.dp)
//                )
//                .clickable { },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = stringResource(id = R.string.save),
//                color = Color.White,
//                fontWeight = FontWeight.Bold
//            )
//        }

        Spacer(modifier = Modifier.height(16.dp))

        BottomNavigationWithBackAndTimer(screenWidth, screenHeight,  isAdmin = false, timerViewModel,showAd2, onBack)
    }
}

@Composable
fun AdItem(uri: Uri, screenWidth: Dp, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, secondaryColor, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Image(
//            painter = rememberImagePainter(uri),
//            contentDescription = null,
//            modifier = Modifier
//                .size(60.dp)
//                .padding(8.dp)
//        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "File Name: ${uri.lastPathSegment}",
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Rank: 1",
                style = TextStyle(color = Color.Gray)
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(R.drawable.delete),
                contentDescription = null,
                tint = Color.Red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAdsManagementScreen() {
    ConsoleApplicationTheme {
        AdsManagementScreen(
            screenWidth = 360.dp,
            screenHeight = 640.dp,
            showAd2 = {},
            onBack = {}
        )
    }
}