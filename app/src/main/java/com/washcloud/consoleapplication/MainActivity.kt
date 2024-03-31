package com.washcloud.consoleapplication

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.OutlinedInputField
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.utils.clearText
import com.washcloud.consoleapplication.utils.hints
import com.washcloud.consoleapplication.local.preferences.language
import com.washcloud.consoleapplication.ui.login.LoginState
import com.washcloud.consoleapplication.ui.login.StaffLoginViewModel
import com.washcloud.consoleapplication.utils.lightGreen
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.numbersBackground
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.screenBackground
import com.washcloud.consoleapplication.utils.secondaryColor
import com.washcloud.consoleapplication.utils.unSelectedTextColor
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import com.washcloud.consoleapplication.ui.login.LoginForm
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var screenHeight = 0.0.dp
    private var screenWidth = 0.0.dp

    companion object {
        public var dLocale: Locale? = null
    }

    init {
        updateConfig(this)
    }

    private fun updateConfig(wrapper: ContextThemeWrapper) {
        if (dLocale == Locale("")) // Do nothing if dLocale is null
            return

        Locale.setDefault(dLocale)
        val configuration = Configuration()
        configuration.setLocale(dLocale)
        wrapper.applyOverrideConfiguration(configuration)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsoleApplicationTheme {
                screenHeight = LocalConfiguration.current.screenHeightDp.dp
                screenWidth = LocalConfiguration.current.screenWidthDp.dp

                var showLoginForm by remember { mutableStateOf(false) }
                var showAd2Form by remember { mutableStateOf(true) }
                var showHelpForm by remember { mutableStateOf(false) }
                var showDriverLoginForm by remember { mutableStateOf(false) }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Box {
                        Image(
                            painterResource(R.drawable.background),
                            "background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillWidth
                        )
                        if (showAd2Form)
                            MainScreen(
                                {
                                    showAd2Form = true
                                    showHelpForm = false
                                    showLoginForm = false
                                },
                                {//show login form
                                    showLoginForm = true
                                    showAd2Form = false
                                },
                                {//show help form
                                    showAd2Form = false
                                    showHelpForm = true
                                },
                            )
                        if (showLoginForm)
                            LoginForm({
                                showLoginForm = false
                                showAd2Form = true
                            }, {
                                showLoginForm = false
                                showDriverLoginForm = true
                            },
                                screenWidth,
                                screenHeight)
                        if (showHelpForm)
                            HelpForm {
                                showAd2Form = true
                                showHelpForm = false
                            }
                        if (showDriverLoginForm)
                            DriverLoginForm {
                                showAd2Form = true
                                showDriverLoginForm = false
                            }
                    }
                }
            }
        }
    }


    @Composable
    fun MainScreen(
        showAd2: () -> Unit,
        showStaffLogin: () -> Unit,
        showHelpForm: () -> Unit,
    ) {

        Column(
            modifier = Modifier
                .width(screenWidth)
        ) {

            Row(
                Modifier
                    .height(0.08 * screenHeight)
                    .width(screenWidth)
            ) {
                Box(
                    modifier =
                    Modifier
                        .weight(1f, true)
                        .fillMaxHeight()
                )
                Box(
                    modifier =
                    Modifier
                        .weight(1f, true)
                        .fillMaxHeight()
                        .clickable {
                            showStaffLogin()
                        }
                )
            }


            Column(
                modifier = Modifier
                    .height(0.78 * screenHeight)
                    .width(screenWidth)
                    .background(color = screenBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painterResource(R.drawable.empty_image),
                    "ad_2",
                    modifier = Modifier
                        .width(screenWidth * 0.3f)
                        .height(screenWidth * 0.3f)
                )
                Text(
                    text = stringResource(id = R.string.ad2),
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.07f).sp,
                        fontWeight = FontWeight.Medium,
                        color = lightGrey
                    ),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(0.04 * screenHeight))

            BottomNavigation(showAd2, showHelpForm, false)

        }
    }

    @Composable
    fun BottomNavigation(
        showAd2: () -> Unit, showHelpForm: () -> Unit,
        isHelpFormShown: Boolean
    ) {
        Box(
            modifier = Modifier
                .width(screenWidth)
                .height(0.1 * screenHeight)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.06 * screenWidth, end = 0.06 * screenWidth),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!isHelpFormShown) {
                                showHelpForm()
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(R.drawable.help),
                        "help",
                        colorFilter = if (isHelpFormShown) ColorFilter.tint(color = secondaryColor) else null,
                        modifier = Modifier
                            .width(0.04 * screenHeight)
                            .height(0.04 * screenHeight)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.help),
                        style = TextStyle(
                            color = if (isHelpFormShown) secondaryColor else unSelectedTextColor,
                            fontSize = (screenWidth.value * 0.023f).sp
                        )
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(2f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(0.05 * screenWidth)
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
                                start = 0.04 * screenWidth,
                                top = 0.01 * screenHeight,
                                end = 0.04 * screenWidth,
                                bottom = 0.01 * screenHeight
                            )
                    ) {
                        Row {
                            Image(
                                painterResource(R.drawable.phone),
                                "phone",
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(48.dp)
                            )
                            Spacer(modifier = Modifier.width(0.01 * screenWidth))
                            Text(
                                text = "920031915",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (screenWidth.value * 0.03f).sp
                                )
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            changeLanguage()
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(R.drawable.language),
                        "language",
                        modifier = Modifier
                            .width(0.04 * screenHeight)
                            .height(0.04 * screenHeight)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.language),
                        style = TextStyle(
                            color = unSelectedTextColor,
                            fontSize = (screenWidth.value * 0.023f).sp
                        )
                    )
                }
            }
        }
    }

    private fun changeLanguage() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var lang = sharedPreferences.getString(language, "")
        lang = if (lang == "ar") "en" else "ar"

        sharedPreferences.edit().putString(language, lang).apply()
        dLocale = Locale(lang)
        MainAdActivity.dLocale = Locale(lang)
        val config = resources.configuration
        Locale.setDefault(dLocale)
        config.setLocale(dLocale)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            createConfigurationContext(config)

        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }



    @Composable
    fun HelpForm(showAd2: () -> Unit) {

        Column(
            modifier = Modifier
                .width(screenWidth)
        ) {


            Column(
                modifier = Modifier
                    .height(screenHeight)
                    .width(screenWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(modifier = Modifier.height(36.dp))


                Row {
                    Spacer(modifier = Modifier.width(0.02 * screenWidth))
                    Column {
                        Spacer(modifier = Modifier.padding(top = 12.dp))
                        Image(
                            painterResource(R.drawable.arrow_back),
                            "arrow_back",
                            colorFilter = ColorFilter.tint(color = primaryDark),
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp)
                                .clickable {
                                    showAd2()
                                }
                        )
                    }
                    Box(
                        modifier =
                        Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(id = R.string.help),
                        style = TextStyle(
                            fontSize = (screenWidth.value * 0.04f).sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryDark
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Box(
                        modifier =
                        Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(24.dp))

                }
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
                        .height(0.72 * screenHeight)
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(0.02 * screenHeight))
                        Image(
                            painterResource(R.drawable.steps),
                            "steps",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .width(screenWidth - 50.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(0.05 * screenWidth))
                            Box(
                                modifier = Modifier
                                    .padding(0.008 * screenWidth)
                                    .width(0.08 * screenWidth)
                                    .height(0.08 * screenWidth)
                                    .background(lightGreen, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painterResource(R.drawable.outline_phone),
                                    "phone",
                                    colorFilter = ColorFilter.tint(color = secondaryColor),
                                    modifier = Modifier
                                        .width(0.05 * screenWidth)
                                        .height(0.05 * screenWidth)
                                )
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Text(
                                text = "920031915",
                                style = TextStyle(
                                    color = Color.Black,
                                    fontSize = (screenWidth.value * 0.025f).sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(0.05 * screenWidth))
                            Box(
                                modifier = Modifier
                                    .padding(0.008 * screenWidth)
                                    .width(0.08 * screenWidth)
                                    .height(0.08 * screenWidth)
                                    .background(lightGreen, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painterResource(R.drawable.outline_email),
                                    "email",
                                    colorFilter = ColorFilter.tint(color = secondaryColor),
                                    modifier = Modifier
                                        .width(0.05 * screenWidth)
                                        .height(0.05 * screenWidth)
                                )

                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Text(
                                text = "info@horizonscloud.net",
                                style = TextStyle(
                                    color = Color.Black,
                                    fontSize = (screenWidth.value * 0.025f).sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Spacer(
                            modifier =
                            Modifier
                                .height(1.dp)
                                .background(color = borderColor)
                                .padding(start = 24.dp, end = 24.dp)
                                .width(screenWidth - (0.2 * screenWidth))
                                .align(CenterHorizontally)
                                .padding(start = 24.dp, end = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.quick_open),
                            style = TextStyle(
                                fontSize = (screenWidth.value * 0.03f).sp,
                                color = Color.Black
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(screenWidth)
                        )
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
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height((screenHeight.value * 0.02f).dp))
                                Text(
                                    text = stringResource(id = R.string.enter_security),
                                    style = TextStyle(
                                        fontSize = (screenWidth.value * 0.025f).sp,
                                        color = Color.Black
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = (screenHeight.value * 0.01f).dp)
                                )
                                OutlinedInputField(
                                    value = "",
                                    hintText = "XXXX-XXXX-XXXX",
                                    onValueChange = {

                                    },
                                    hintTextSize = (screenWidth.value * 0.02f).sp,
                                    cornerRadius = (screenWidth.value * 0.06f),
                                    modifier = Modifier
                                        .padding((screenHeight.value * 0.02f).dp)
                                        .fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(start = 24.dp, end = 24.dp)
                                        .clip(
                                            RoundedCornerShape((screenWidth.value * 0.02f).dp)
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
                                            start = 16.dp,
                                            top = (screenWidth.value * 0.02f).dp,
                                            end = 16.dp,
                                            bottom = (screenWidth.value * 0.02f).dp
                                        )
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.open),
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = (screenWidth.value * 0.03f).sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height((screenHeight.value * 0.02f).dp))

                            }
                        }
                    }
                }
                Box(
                    modifier =
                    Modifier.weight(1f)
                )
                BottomNavigation(showAd2, {}, true)
            }


        }
    }


    @Composable
    fun DriverLoginForm(showAd2: () -> Unit) {
        Column(
            modifier = Modifier
                .width(screenWidth)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.login_driver),
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryDark
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "02:30",
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
                    text = "02/03/2024",
                    style = TextStyle(
                        fontSize = (screenWidth.value * 0.02f).sp,
                        color = borderColor
                    ),
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.width(24.dp))

            }

            Column(
                modifier =
                Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    Modifier
                        .padding(start = 24.dp, end = 24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .border(
                                color = secondaryColor,
                                width = 1.dp,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .width(0.22 * screenWidth)
                                .height(0.22 * screenWidth)
                                .background(lightGreen, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(R.drawable.drop_off),
                                "drop_off",
                                colorFilter = ColorFilter.tint(color = secondaryColor),
                                modifier = Modifier
                                    .width((screenWidth.value * 0.11f).dp)
                                    .height((screenWidth.value * 0.11f).dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.drop_off),
                            style = TextStyle(
                                color = secondaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = (screenWidth.value * 0.035f).sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width((screenWidth.value * 0.04f).dp))
                    Column(
                        modifier = Modifier
                            .border(
                                color = secondaryColor,
                                width = 1.dp,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .width(0.22 * screenWidth)
                                .height(0.22 * screenWidth)
                                .background(lightGreen, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(R.drawable.pickup),
                                "pickup",
                                colorFilter = ColorFilter.tint(color = secondaryColor),
                                modifier = Modifier
                                    .width((screenWidth.value * 0.11f).dp)
                                    .height((screenWidth.value * 0.11f).dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.pickup),
                            style = TextStyle(
                                color = secondaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = (screenWidth.value * 0.035f).sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(screenWidth)
                    .height(0.1 * screenHeight)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 0.06 * screenWidth, end = 0.06 * screenWidth),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
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
                                showAd2()
                            }
                    ) {
                        Row {
                            Image(
                                painterResource(R.drawable.arrow_back),
                                "arrow_back",
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                            )
                            Spacer(modifier = Modifier.width((screenWidth.value * 0.02f).dp))
                            Text(
                                text = stringResource(id = R.string.back),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (screenWidth.value * 0.03f).sp
                                )
                            )
                            Spacer(modifier = Modifier.width((screenWidth.value * 0.02f).dp))
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


