package com.washcloud.consoleapplication.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.ui.startStaff.DateTimeViewModel
import com.washcloud.consoleapplication.utils.borderColor

@Composable
fun dateAndTimeView(screenWidth: Dp){
    val dateTimeViewModel: DateTimeViewModel = hiltViewModel()
    val hoursText by dateTimeViewModel.hoursText.collectAsState()
    val fullDateText by dateTimeViewModel.fullDateText.collectAsState()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = hoursText,
            style = TextStyle(
                fontSize = (screenWidth.value * 0.02f).sp,
                color = borderColor
            ),
            textAlign = TextAlign.Start,
        )
        Box(
            modifier =
            Modifier.weight(1f)
        )
        Text(
            text = fullDateText,
            style = TextStyle(
                fontSize = (screenWidth.value * 0.02f).sp,
                color = borderColor
            ),
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.width(24.dp))

    }
}