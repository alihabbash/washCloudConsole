package com.washcloud.consoleapplication.ui.pickup

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.clearText
import com.washcloud.consoleapplication.utils.numbersColor
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.secondaryColor

@Composable
fun PickupView(
    screenWidth: Dp,
    screenHeight: Dp,
    showStaffStart: () -> Unit,
) {
    val viewModel: PickupViewModel = hiltViewModel()

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
                   .height(0.59 * screenHeight)
           ){
               Column {


                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)
                   Spacer(
                       modifier =
                       Modifier
                           .height(1.dp)
                           .background(color = borderColor)
                           .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                           .width(screenWidth - (0.2 * screenWidth))
                           .align(Alignment.CenterHorizontally)
                           .padding(start = 24.dp, end = 24.dp)
                   )
                   pickUpItem(screenWidth, screenHeight)

               }
           }
       }
    }
}

@Composable
fun pickUpItem(
    screenWidth: Dp,
    screenHeight: Dp,
) {
    Row(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
    ){
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
                    text = stringResource(id = R.string.order_number),
                    style = TextStyle(
                        color = clearText,
                        fontSize = (screenWidth.value * 0.01f).sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.box_number),
                    style = TextStyle(
                        color = clearText,
                        fontSize = (screenWidth.value * 0.01f).sp
                    )
                )
            }
            Column {
                Text(
                    text = "930859037",
                    style = TextStyle(
                        color = numbersColor,
                        fontSize = (screenWidth.value * 0.01f).sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "16",
                    style = TextStyle(
                        color = numbersColor,
                        fontSize = (screenWidth.value * 0.01f).sp
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
                    top = (screenHeight.value * 0.01f).dp,
                    end = 16.dp,
                    bottom = (screenHeight.value * 0.01f).dp,
                    start = 16.dp
                )
                .clickable {
                    //todo print
                }
        ){
            Text(
                text = stringResource(id = R.string.print),
                style = TextStyle(
                    color = Color.White,
                    fontSize = (screenWidth.value * 0.03f).sp
                )
            )
        }
    }
}