package com.washcloud.consoleapplication.ui.dropoff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.washcloud.consoleapplication.ui.common.BottomNavigationWithBackAndTimer
import com.washcloud.consoleapplication.utils.*

@Composable
fun SelectLockerView(
    screenWidth: Dp,
    screenHeight: Dp,
    onBack: () -> Unit,
    showAd2: () -> Unit,
) {
    val viewModel: DropOffViewModel = hiltViewModel()
    var selectedLocker by remember { mutableStateOf<String?>(null) }

    Box {
        Column(
            modifier = Modifier
                .height(screenHeight)
                .width(screenWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Header(screenWidth)
            LockerList(lockers, screenWidth, screenHeight, selectedLocker) { selectedLocker = it }
            Spacer(modifier = Modifier.weight(1f))
            BottomNavigationWithBackAndTimer(screenWidth, screenHeight, showAd2, onBack)
        }
    }
}

@Composable
fun LockerItem(
    type: String,
    abbreviation: String,
    availableNumber: Int,
    itemWidth: Dp,
    isSelected: Boolean,
    onSelect: () -> Unit,
    screenWidth: Dp,
) {
    val backgroundColor = if (isSelected) secondaryColor.copy(alpha = 0.3F) else Color.White
    val borderColor = secondaryColor
    val isEnabled = availableNumber > 0

    Row(
        modifier = Modifier
            .padding(8.dp)
            .width(itemWidth)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(enabled = isEnabled) { onSelect() }
            .padding(16.dp)
            .padding(vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (isEnabled) Brush.horizontalGradient(
                        colors = listOf(blueGradient, secondaryColor)
                    ) else SolidColor(Color.Gray)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = abbreviation,
                color = Color.White,
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.03f).sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = type,
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.03f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Color.Black else Color.Gray
                )
            )
            Text(
                text = "Available: $availableNumber",
                style = TextStyle(
                    fontSize = (screenWidth.value * 0.03f).sp,
                    color = if (isEnabled) secondaryColor else Color.Gray,
                )
            )
        }
    }
}

@Composable
fun LockerList(
    lockers: List<Pair<String, Int>>,
    screenWidth: Dp,
    screenHeight: Dp,
    selectedLocker: String?,
    onLockerSelect: (String) -> Unit
) {
    val itemWidth = (screenWidth - 144.dp) / 3

    Column(
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
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(id = R.string.select_locker),
            style = TextStyle(
                fontSize = (screenWidth.value * 0.04f).sp,
                fontWeight = FontWeight.Bold,
                color = primaryDark
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .padding(horizontal = 16.dp)
        ) {
            items(lockers.chunked(3)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { (type, availableNumber) ->
                        LockerItem(
                            type = type,
                            abbreviation = type.getAbbreviation(),
                            availableNumber = availableNumber,
                            itemWidth = itemWidth,
                            isSelected = selectedLocker == type,
                            onSelect = { onLockerSelect(type) },
                            screenWidth = screenWidth
                        )
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.width(itemWidth))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(0.05 * screenHeight))
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(id = R.string.drop_off_clothes),
            style = TextStyle(
                fontSize = (screenWidth.value * 0.04f).sp,
                fontWeight = FontWeight.Bold,
                color = primaryDark
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        DropOffSection(screenWidth, screenHeight)
    }
}

@Composable
fun DropOffSection(screenWidth: Dp, screenHeight: Dp) {
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
            .height(0.25 * screenHeight),
    ) {
        Column(
            modifier = Modifier.padding(
                top = 0.02 * screenHeight,
                start = 0.05 * screenWidth,
                bottom = 0.02 * screenHeight
            ),
        ) {
            Text(
                text = stringResource(id = R.string.scan_order_number),
                style = TextStyle(
                    color = fieldsTitles,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
            Spacer(modifier = Modifier.height(0.01 * screenHeight))
            Column {
                Spacer(modifier = Modifier.height(0.01 * screenHeight))
                OutlinedInputField(
                    value = "",
                    hintText = "XXXX-XXXX-XXXX",
                    onValueChange = {},
                    hintTextSize = (screenWidth.value * 0.035f).sp,
                    fontSize = (screenWidth.value * 0.035f).sp,
                    cornerRadius = (screenWidth.value * 0.06f),
                    modifier = Modifier.width(0.8 * screenWidth)
                )
                Spacer(modifier = Modifier.height(0.03 * screenWidth))
                ConfirmButton(screenWidth, screenHeight)
            }
        }
    }
}

@Composable
fun ConfirmButton(screenWidth: Dp, screenHeight: Dp) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        blueGradient,
                        secondaryColor,
                    ),
                )
            )
            .padding(
                top = (screenHeight.value * 0.01f).dp,
                end = (screenWidth.value * 0.0f).dp,
                bottom = (screenHeight.value * 0.005f).dp,
                start = (screenWidth.value * 0.04f).dp
            )
            .width(0.75 * screenWidth)
            .height(0.08 * screenHeight)
            .clickable {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.confirm),
            style = TextStyle(
                color = Color.White,
                fontSize = (screenWidth.value * 0.03f).sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

fun String.getAbbreviation(): String {
    return when (this) {
        "X-small" -> "XS"
        "Small" -> "S"
        "Medium" -> "M"
        "Large" -> "L"
        "X-Large" -> "XL"
        "2X-Large" -> "2XL"
        "3X-Large" -> "3XL"
        "4X-Large" -> "4XL"
        "Conveyor" -> "C"
        else -> this.first().toString()
    }
}

val lockers = listOf(
    "X-small" to 7,
    "Small" to 7,
    "Medium" to 7,
    "Large" to 7,
    "X-Large" to 7,
    "2X-Large" to 7,
    "3X-Large" to 7,
    "4X-Large" to 7,
    "Conveyor" to 0
)

@Preview(showBackground = true)
@Composable
fun PreviewLockerList() {
    var selectedLocker by remember { mutableStateOf<String?>(null) }
    LockerList(lockers, screenWidth = 1366.dp, screenHeight = 768.dp, selectedLocker) { selectedLocker = it }
}
