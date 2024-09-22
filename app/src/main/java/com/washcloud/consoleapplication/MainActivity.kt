package com.washcloud.consoleapplication

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.local.preferences.ADMIN_PASSWORD
import com.washcloud.consoleapplication.local.preferences.DELAY_MILLIS
import com.washcloud.consoleapplication.local.preferences.PHONE_NUMBER
import com.washcloud.consoleapplication.local.preferences.language
import com.washcloud.consoleapplication.ui.admin.adminSetting.SubAdminSettingsScreen
import com.washcloud.consoleapplication.ui.admin.ads.AdsManagementScreen
import com.washcloud.consoleapplication.ui.admin.locker.AdminLockerScreen
import com.washcloud.consoleapplication.ui.admin.login.AdminLogInView
import com.washcloud.consoleapplication.ui.admin.menu.AdminMenuView
import com.washcloud.consoleapplication.ui.admin.password.ChangePasswordScreen
import com.washcloud.consoleapplication.ui.admin.phoneNumber.UpdatePhoneNumberScreen
import com.washcloud.consoleapplication.ui.admin.settings.PCSettingsScreen
import com.washcloud.consoleapplication.ui.common.BottomNavigation
import com.washcloud.consoleapplication.ui.common.MainViewModel
import com.washcloud.consoleapplication.ui.common.SelectedView
import com.washcloud.consoleapplication.ui.dropoff.DropOffView
import com.washcloud.consoleapplication.ui.dropoff.SelectLockerView
import com.washcloud.consoleapplication.ui.help.HelpForm
import com.washcloud.consoleapplication.ui.login.LoginForm
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity
import com.washcloud.consoleapplication.ui.mainad.MainAdActivity.Companion
import com.washcloud.consoleapplication.ui.pickup.PickupView
import com.washcloud.consoleapplication.ui.startStaff.DriverLoginForm
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.FileLogger
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.screenBackground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.io.DataOutputStream
import java.util.Locale


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var screenHeight = 0.0.dp
    private var screenWidth = 0.0.dp
    private val barcodeData = StringBuilder()
    private var phoneNumber by mutableStateOf("")

    companion object {
         var dLocale: Locale? = null
    }

    init {
        updateConfig(this)
    }

    private fun updateConfig(wrapper: ContextThemeWrapper) {

        if(dLocale == null) {
            dLocale = Locale("ar")
        }
        Locale.setDefault(dLocale)
        val configuration = Configuration()
        configuration.setLocale(dLocale)
        wrapper.applyOverrideConfiguration(configuration)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAdminPassword()
        loadPhoneNumber()

       /* val p = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(p.outputStream)

       // os.writeBytes("yourCommand\n")

        //os.writeBytes("exit\n")

        os.flush()
        os.close()
        try {
            p.waitFor()
        } catch (e: InterruptedException) {
        }*/

       /* Handler(Looper.getMainLooper()).postDelayed({
            simulateKeyPressSequence("https://devwashcloud.azurewebsites.net/api/LockerIntegration/Verification/4442408250005-1/21222213701A-001")
        }, 3000)*/


        setContent {
            ConsoleApplicationTheme {

                screenHeight =  getRealScreenHeight().dp
                    //LocalConfiguration.current.screenHeightDp.dp
                screenWidth = getRealScreenWidth().dp

                //LocalConfiguration.current.screenWidthDp.dp
                val mainViewModel: MainViewModel = hiltViewModel()
                val stack by mainViewModel.stack.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),

                    color = Color.White
                ) {
                    Box {
                        BackgroundImage()
                        CurrentView(stack.last(), screenWidth, screenHeight, mainViewModel, phoneNumber)
                    }
                }
            }
        }
    }



    private fun loadPhoneNumber() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        phoneNumber = sharedPreferences.getString(PHONE_NUMBER, "920031915") ?: "920031915"
    }

    fun getRealScreenHeight(): Int {
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowManager.defaultDisplay.getRealMetrics(metrics)
        } else {
            windowManager.defaultDisplay.getMetrics(metrics)
        }
        return metrics.heightPixels
    }

    fun getRealScreenWidth(): Int {
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowManager.defaultDisplay.getRealMetrics(metrics)
        } else {
            windowManager.defaultDisplay.getMetrics(metrics)
        }
        return metrics.widthPixels
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        return if (keyCode == KeyEvent.KEYCODE_ENTER) {
            Log.e("MainActivity", "keyCode ${KeyEvent.KEYCODE_ENTER}")
            FileLogger.log(this, "MainActivity", "keyCode ${KeyEvent.KEYCODE_ENTER}")
            val barcode = barcodeData.toString().trim().replace(Regex("\\s"), "").replace("\\","/").replace("\u0000", "")
            FileLogger.log(this, "MainActivity", "Barcode scanned: $barcode")
            Log.e("MainActivity", "Barcode scanned: $barcode")
            if (barcode.startsWith("https")) {
               FileLogger.log(this, "MainActivity", "Valid barcode: $barcode")
                barcodeData.setLength(0)
                Log.e("MainActivity", "Valid barcode: $barcode")

                val intent = Intent(this, MainAdActivity::class.java).apply {
                    putExtra("barcode", barcode)
                }
                startActivity(intent)
                Log.e("MainActivity", "finish")
                finish()
            }
            true
        } else {
            barcodeData.append(event.unicodeChar.toChar())
            super.onKeyDown(keyCode, event)
        }
    }

    @Composable
    fun BackgroundImage() {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = "background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth
        )
    }

    @Composable
    fun CurrentView(
        selectedView: SelectedView,
        screenWidth: Dp,
        screenHeight: Dp,
        mainViewModel: MainViewModel,
        phoneNumber: String
    ) {
        when (selectedView) {
            is SelectedView.Ad2Form -> {
                LaunchedEffect(Unit) {
                    delay(DELAY_MILLIS)
                    finish()
                }

                MainScreen(
                    { mainViewModel.resetStack() },
                    { mainViewModel.addToStack(SelectedView.LoginForm) },
                    { mainViewModel.addToStack(SelectedView.AdminLogInView) },
                    { mainViewModel.addToStack(SelectedView.HelpForm) },
                    phoneNumber =  phoneNumber,
                )
            }
            is SelectedView.LoginForm -> LoginForm(
                { mainViewModel.resetStack() },
                { mainViewModel.addToStack(SelectedView.DriverLoginForm) },
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
            is SelectedView.HelpForm -> {
                LaunchedEffect(Unit) {
                    delay(DELAY_MILLIS)
                    finish()
                }

                HelpForm(
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    phoneNumber = phoneNumber,
                    { changeLanguage() },
                    { mainViewModel.resetStack() }
                )
            }
            is SelectedView.DriverLoginForm -> DriverLoginForm(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                { mainViewModel.addToStack(SelectedView.PickUpView) }, { mainViewModel.addToStack(SelectedView.DropOffView) },
                 { mainViewModel.resetStack() }
            )
            is SelectedView.PickUpView -> PickupView(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                 { mainViewModel.popStack() },
                { mainViewModel.resetStack() }
            )
            is SelectedView.DropOffView -> DropOffView(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                { mainViewModel.popStack() },
                { mainViewModel.addToStack(SelectedView.SelectLockerView) },
                { mainViewModel.resetStack() }
            )
            is SelectedView.SelectLockerView -> SelectLockerView(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onBack = { mainViewModel.popStack() },
                { mainViewModel.resetStack() }
            )
            is SelectedView.AdminLogInView -> AdminLogInView(
                { mainViewModel.resetStack() },
                { mainViewModel.addToStack(SelectedView.AdminMenuView) },
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            is SelectedView.AdminMenuView -> AdminMenuView(
                screenWidth = screenWidth,
                screenHeight =  screenHeight,
                showSetting = { mainViewModel.addToStack(SelectedView.PCSettingsScreen) },
                showAdminSetting = { mainViewModel.addToStack(SelectedView.SubAdminSettingsScreen) },
                showAdsSetting = { mainViewModel.addToStack(SelectedView.AdsManagementScreen) },
                showLockerManagement = { mainViewModel.addToStack(SelectedView.AdminLockerScreen) }) {
                mainViewModel.resetStack()
            }

            is SelectedView.PCSettingsScreen -> PCSettingsScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight) {

                mainViewModel.popStack()
            }

            is SelectedView.SubAdminSettingsScreen -> SubAdminSettingsScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onChangePassword = { mainViewModel.addToStack(SelectedView.ChangePasswordScreen) },
                onHelpPhoneNumber = {  mainViewModel.addToStack(SelectedView.UpdatePhoneNumberScreen) },
                showAd2 = {

                    mainViewModel.popStack()
                }
            )

            is SelectedView.ChangePasswordScreen -> ChangePasswordScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onSave = {  },
                showAd2 = { mainViewModel.popStack() }
            )

            is SelectedView.UpdatePhoneNumberScreen -> UpdatePhoneNumberScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onSave = {
                    loadPhoneNumber()
                },
                showAd2 = { mainViewModel.popStack() }
            )

            is SelectedView.AdsManagementScreen -> AdsManagementScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                showAd2 = { mainViewModel.popStack() },
                onBack = { mainViewModel.popStack() }
            )

            is SelectedView.AdminLockerScreen -> AdminLockerScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                numberOfLockers = 60,
                onBack = { mainViewModel.popStack()},
                showAd2 = { mainViewModel.popStack() }
            )

        }
    }
    @Composable
    fun MainScreen(
        showAd2: () -> Unit,
        showStaffLogin: () -> Unit,
        showAdminLogin: () -> Unit,
        showHelpForm: () -> Unit,
        phoneNumber: String,
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
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {

                                    showAdminLogin()


                                }
                            )
                        }

                )
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
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {

                                    showStaffLogin()


                                }
                            )
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
            }, showAd2, showHelpForm, false,  phoneNumber)

        }
    }

    private fun setAdminPassword() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val storedPassword = sharedPreferences.getString(ADMIN_PASSWORD, "")
        if (storedPassword == "") {
            sharedPreferences.edit().putString(ADMIN_PASSWORD, "123321").apply()
        }
    }







    private fun changeLanguage() {
        val currentLang = dLocale?.language ?: Locale.getDefault().language
        val newLang = if (currentLang == "ar") "en" else "ar"
        dLocale = Locale(newLang)
        val config = resources.configuration
        Locale.setDefault(dLocale)
        config.setLocale(dLocale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            createConfigurationContext(config)

        resources.updateConfiguration(config, resources.displayMetrics)

        recreate()
    }



}


