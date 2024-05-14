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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.OutlinedInputField
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.borderColor
import com.washcloud.consoleapplication.local.preferences.language
import com.washcloud.consoleapplication.ui.common.BottomNavigation
import com.washcloud.consoleapplication.ui.common.MainViewModel
import com.washcloud.consoleapplication.ui.common.SelectedView
import com.washcloud.consoleapplication.ui.help.HelpForm
import com.washcloud.consoleapplication.utils.lightGreen
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.screenBackground
import com.washcloud.consoleapplication.utils.secondaryColor
import com.washcloud.consoleapplication.utils.unSelectedTextColor
import java.util.Locale
import com.washcloud.consoleapplication.ui.startStaff.DriverLoginForm
import com.washcloud.consoleapplication.ui.login.LoginForm
import com.washcloud.consoleapplication.ui.pickup.PickupView
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
                val mainViewModel: MainViewModel = hiltViewModel()
                val stack by mainViewModel.stack.collectAsState()


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
                        if (stack[stack.size-1] is SelectedView.Ad2Form)
                            MainScreen(
                                {
                                    mainViewModel.resetStack()
                                },
                                {//show login form
                                    mainViewModel.addToStack(SelectedView.LoginForm)
                                },
                                {//show help form
                                    mainViewModel.addToStack(SelectedView.HelpForm)
                                },
                            )
                        if (stack[stack.size-1] is SelectedView.LoginForm)
                            LoginForm({
                                mainViewModel.resetStack()
                            }, {
                                mainViewModel.addToStack(SelectedView.DriverLoginForm)
                            },
                                screenWidth,
                                screenHeight
                            )
                        if (stack[stack.size-1] is SelectedView.HelpForm)
                            HelpForm(screenWidth, screenHeight, {
                                changeLanguage()
                            }) {
                                mainViewModel.resetStack()
                            }
                        if (stack[stack.size-1] is SelectedView.DriverLoginForm)
                            DriverLoginForm(screenWidth, screenHeight,
                                {
                                    mainViewModel.addToStack(SelectedView.PickUpView)
                            }) {
                                mainViewModel.resetStack()
                            }
                        if (stack[stack.size-1] is SelectedView.PickUpView)
                            PickupView(screenWidth, screenHeight,{
                                mainViewModel.resetStack()
                            }){
                                mainViewModel.addToStack(SelectedView.DriverLoginForm)
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

            BottomNavigation(screenWidth, screenHeight, {
                changeLanguage()
            }, showAd2, showHelpForm, false)

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


}


