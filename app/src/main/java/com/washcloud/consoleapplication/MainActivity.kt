package com.washcloud.consoleapplication

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.preference.PreferenceManager
import coil.compose.rememberAsyncImagePainter
import com.washcloud.consoleapplication.local.preferences.ADMIN_PASSWORD
import com.washcloud.consoleapplication.local.preferences.ADS_SECONDARY_ARRAY
import com.washcloud.consoleapplication.local.preferences.DELAY_MILLIS
import com.washcloud.consoleapplication.local.preferences.PHONE_NUMBER
import com.washcloud.consoleapplication.ui.admin.adminSetting.SubAdminSettingsScreen
import com.washcloud.consoleapplication.ui.admin.ads.AdsManagementScreen
import com.washcloud.consoleapplication.ui.admin.exit.ExitAdminView
import com.washcloud.consoleapplication.ui.admin.locker.AddLockerScreen
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
import com.washcloud.consoleapplication.ui.pickup.PickupView
import com.washcloud.consoleapplication.ui.pickup.StaticQrLoginScreen
import com.washcloud.consoleapplication.ui.pickup.SetPasswordScreen
import com.washcloud.consoleapplication.ui.startStaff.DriverLoginForm
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.FileLogger
import com.washcloud.consoleapplication.utils.screenBackground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.Locale


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var screenHeight = 0.0.dp
    private var screenWidth = 0.0.dp
    private val barcodeData = StringBuilder()
    private var phoneNumber by mutableStateOf("")


    private val scannerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            FileLogger.log(context, "MainActivity", "Received scanner data broadcast")

            if (intent.action == "com.washcloud.scanner_data") {
                val scannerData = intent.getStringExtra("scannerData")
                scannerData?.let {
                    FileLogger.log(context, "MainActivity", "Scanner Data: $it")
//                    Toast.makeText(context, "Scanned Data: $it", Toast.LENGTH_SHORT).show()

                    if (it.startsWith("https")) {
                        val adIntent = Intent(context, MainAdActivity::class.java).apply {
                            putExtra("barcode", it)
                        }
                        context.startActivity(adIntent)
                        finish()
                    }
                }
            }
        }
    }




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
        startSystemAlertWindowPermission()


        if (actionBar != null) {
            actionBar!!.hide()
        }



        window.decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
            if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                hideSystemUI()
            }
        }

        if(dLocale == null) {
            dLocale = Locale("ar")
        }
        val config = resources.configuration
        Locale.setDefault(dLocale)
        config.setLocale(dLocale)

        resources.updateConfiguration(config, resources.displayMetrics)



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


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
 
        FileLogger.log(this, "MainActivity", "keyCode $keyCode")
        return if (keyCode == KeyEvent.KEYCODE_ENTER) {
            Log.e("MainActivity", "keyCode ${KeyEvent.KEYCODE_ENTER}")
            FileLogger.log(this, "MainActivity", "keyCode ${KeyEvent.KEYCODE_ENTER}")
            val barcode = barcodeData.toString().trim().replace(Regex("\\s"), "").replace("\\","/").replace("\u0000", "")
            FileLogger.log(this, "MainActivity", "Barcode scanned: $barcode")
            Log.e("MainActivity", "Barcode scanned: $barcode")
            if (barcode.isNotEmpty()) {
                // Broadcast to all listeners (Pickup, DropOff, etc.)
                val broadcastIntent = Intent("com.washcloud.scanner_data").apply {
                    putExtra("scannerData", barcode)
                }
                sendBroadcast(broadcastIntent)
                FileLogger.log(this, "MainActivity", "Broadcasted scanner data: $barcode")

                if (barcode.startsWith("https")) {
                    FileLogger.log(this, "MainActivity", "Valid barcode: $barcode")
                    Log.e("MainActivity", "Valid barcode: $barcode")
                    val intent = Intent(this, MainAdActivity::class.java).apply {
                        putExtra("barcode", barcode)
                    }
                    startActivity(intent)
                    Log.e("MainActivity", "finish")
                    finish()
                }
            }
            barcodeData.setLength(0)
            true
        } else {
            barcodeData.append(event.unicodeChar.toChar())
            FileLogger.log(this, "MainActivity", "barcodeData: ${barcodeData.toString()}")
            super.onKeyDown(keyCode, event)
        }
    }

    private fun startSystemAlertWindowPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Log.i(
                        TAG,
                        "[startSystemAlertWindowPermission] requesting system alert window permission."
                    )
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                                "package:$packageName"
                            )
                        )
                    )
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "[startSystemAlertWindowPermission] error:", e)
        }
    }

    override fun onPause() {
        super.onPause()
        FileLogger.log(this, "MainActivity", "onPause")
    }

    override fun onStop() {
        super.onStop()
        FileLogger.log(this, "MainActivity", "onStop")
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
        val onTimeout: () -> Unit = {
            val intent = Intent(this@MainActivity, MainAdActivity::class.java)
            startActivity(intent)
            finish()
        }

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
              /*  LaunchedEffect(Unit) {
                    delay(DELAY_MILLIS)
                    finish()
                }*/

                HelpForm(
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    phoneNumber = phoneNumber,
                    quickOpen = { serialOrder  ->
                        val intent = Intent(this, MainAdActivity::class.java).apply {
                            putExtra("serialOrder", serialOrder)
                        }
                        startActivity(intent)
                        finish()
                    },
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
                showAd2 = onTimeout,
                showAdminScreens = { mainViewModel.addToStack(SelectedView.AdminMenuView) },
                onBack = { mainViewModel.popStack() },
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            is SelectedView.AdminMenuView -> AdminMenuView(
                screenWidth = screenWidth,
                screenHeight =  screenHeight,
                showSetting = { mainViewModel.addToStack(SelectedView.PCSettingsScreen) },
                showAdminSetting = { mainViewModel.addToStack(SelectedView.SubAdminSettingsScreen) },
                showAdsSetting = { mainViewModel.addToStack(SelectedView.AdsManagementScreen) },
                showLockerManagement = { mainViewModel.addToStack(SelectedView.AdminLockerScreen) },
                showAd2 = onTimeout,
                onBack = { mainViewModel.addToStack(SelectedView.ExitAdminView) }
            )

            is SelectedView.PCSettingsScreen -> PCSettingsScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                showAd2 = onTimeout,
                onBack = { mainViewModel.popStack() }
            )

            is SelectedView.SubAdminSettingsScreen -> SubAdminSettingsScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onChangePassword = { mainViewModel.addToStack(SelectedView.ChangePasswordScreen) },
                onHelpPhoneNumber = {  mainViewModel.addToStack(SelectedView.UpdatePhoneNumberScreen) },
                showAd2 = onTimeout,
                onBack = { mainViewModel.popStack() }
            )

            is SelectedView.ChangePasswordScreen -> ChangePasswordScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onSave = {  },
                showAd2 = onTimeout,
                onBack = { mainViewModel.popStack() }
            )

            is SelectedView.UpdatePhoneNumberScreen -> UpdatePhoneNumberScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onSave = {
                    loadPhoneNumber()
                },
                showAd2 = onTimeout,
                onBack = { mainViewModel.popStack() }
            )

            is SelectedView.AdsManagementScreen -> AdsManagementScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                showAd2 = onTimeout,
                onBack = { mainViewModel.popStack() }
            )

            is SelectedView.AdminLockerScreen -> AdminLockerScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onAddLockerClick = {
                    mainViewModel.addToStack(SelectedView.AddLockerScreen)
                },
                onBack = { mainViewModel.popStack()},
                showAd2 = onTimeout
            )

            is  SelectedView.ExitAdminView ->
                ExitAdminView(
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    exitToAndroid = {
                        finishAffinity();
                        System.exit(0)
                    },
                    rebootAndroid = {
                        try {
                            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
                            process.waitFor()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    shutdown = {
                        try {

                            val process =
                                Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot -p"))
                            process.waitFor()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    },
                    logoutAdmin = { mainViewModel.resetStack() },
                    showAd2 = onTimeout,
                    onBack = { mainViewModel.popStack() }
                )
            is SelectedView.AddLockerScreen -> AddLockerScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onBack = { mainViewModel.popStack() },
                showAd2 = onTimeout
            )
            is SelectedView.SetPasswordScreen -> SetPasswordScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                showAd2 = onTimeout,
                onBack = { mainViewModel.popStack() },
                onPasswordSetSuccess = { phone, newPin ->
                    mainViewModel.popStack()
                    mainViewModel.addToStack(SelectedView.StaticQrLoginScreen)
                },
                onPhoneValidate = { phone, callback ->
                    callback(true) // Mock validation for MainActivity demo
                }
            )
            is SelectedView.StaticQrLoginScreen -> StaticQrLoginScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                showAd2 = onTimeout,
                onBack = { mainViewModel.popStack() },
                verifyPin = { phone, pin, onResult -> 
                    val isCorrect = (pin == "1234")
                    onResult(isCorrect)
                    if (isCorrect) {
                        mainViewModel.resetStack()
                    }
                }
            )




        }
    }
    @Composable
    fun MainScreen(
        showAd2: () -> Unit,
        showStaffLogin: () -> Unit,
        showAdminLogin: () -> Unit,
        showHelpForm: () -> Unit,
        phoneNumber: String
    ) {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
        val adsSecondaryString: Set<String>? = try {
            sharedPreferences.getStringSet(ADS_SECONDARY_ARRAY, emptySet())
        } catch (e: Exception) {
            Log.e("MainAdViewModel", "Error retrieving ADS_SECONDARY_ARRAY from SharedPreferences", e)
            emptySet()
        }

        val adsSecondaryList = adsSecondaryString?.map { Uri.parse(it) } ?: emptyList()
        var currentIndex by remember { mutableStateOf(0) }

        LaunchedEffect(adsSecondaryList) {
            while (adsSecondaryList.isNotEmpty()) {
                delay(5000L)
                currentIndex = (currentIndex + 1) % adsSecondaryList.size
            }
        }

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
                if (adsSecondaryList.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(adsSecondaryList[currentIndex]),
                        contentDescription = "Ad Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds


                    )
                } else {
                    Image(
                        painterResource(R.drawable.empty_image),
                        contentDescription = "Empty Ad",
                        modifier = Modifier
                            .width(screenWidth * 0.3f)
                            .height(screenWidth * 0.3f)
                    )
                }
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
            sharedPreferences.edit().putString(ADMIN_PASSWORD, "589188").apply()
        }
    }


    private fun changeLanguage() {
        val currentLang = dLocale?.language ?: Locale.getDefault().language
        val newLang = if (currentLang == "ar") "en" else "ar"
        dLocale = Locale(newLang)
        val config = resources.configuration
        Locale.setDefault(dLocale)
        config.setLocale(dLocale)
        MainAdActivity.dLocale = dLocale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            createConfigurationContext(config)

        resources.updateConfiguration(config, resources.displayMetrics)

        recreate()
    }



}


