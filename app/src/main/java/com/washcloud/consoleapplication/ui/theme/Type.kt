package com.washcloud.consoleapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.washcloud.consoleapplication.R

val ArialMT = FontFamily(
    Font(R.font.arial_mt_normal, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.arial_mt_italic, FontWeight.Normal, FontStyle.Italic),

    Font(R.font.arial_mt_bold, FontWeight.Bold, FontStyle.Normal),

    Font(R.font.arial_mt_light, FontWeight.Light, FontStyle.Normal),

    Font(R.font.arial_mt_medium, FontWeight.Medium, FontStyle.Normal),

)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = ArialMT,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)