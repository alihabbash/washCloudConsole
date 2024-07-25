package com.washcloud.consoleapplication

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.hilt.work.HiltWorkerFactory
import androidx.preference.PreferenceManager
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.hardware.SerialPortService
import com.washcloud.consoleapplication.local.database.utils.BoxSeeder
import com.washcloud.consoleapplication.local.preferences.ADMIN_PASSWORD
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
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
import com.washcloud.consoleapplication.ui.pickup.PickupView
import com.washcloud.consoleapplication.ui.startStaff.DriverLoginForm
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.screenBackground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


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
        setAdminPassword()
        insertBoxes()
        startPortService()



        setContent {
            ConsoleApplicationTheme {
                screenHeight = LocalConfiguration.current.screenHeightDp.dp
                screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val mainViewModel: MainViewModel = hiltViewModel()
                val stack by mainViewModel.stack.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Box {
                        BackgroundImage()
                        CurrentView(stack.last(), screenWidth, screenHeight, mainViewModel)
                    }
                }
            }
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
        mainViewModel: MainViewModel
    ) {
        when (selectedView) {
            is SelectedView.Ad2Form -> MainScreen(
                 { mainViewModel.resetStack() },
                 { mainViewModel.addToStack(SelectedView.LoginForm) },
                 { mainViewModel.addToStack(SelectedView.AdminLogInView) },
                 { mainViewModel.addToStack(SelectedView.HelpForm) }
            )
            is SelectedView.LoginForm -> LoginForm(
                { mainViewModel.resetStack() },
                { mainViewModel.addToStack(SelectedView.DriverLoginForm) },
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
            is SelectedView.HelpForm -> HelpForm(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                 { changeLanguage() },
                { mainViewModel.resetStack() }
            )
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
                screenHeight = screenHeight,
                onSaveLocker = {},
                onSaveRebootSchedule = {}) {
                mainViewModel.resetStack()
            }

            is SelectedView.SubAdminSettingsScreen -> SubAdminSettingsScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onChangePassword = { mainViewModel.addToStack(SelectedView.ChangePasswordScreen) },
                onHelpPhoneNumber = {  mainViewModel.addToStack(SelectedView.UpdatePhoneNumberScreen) },
                showAd2 = { mainViewModel.addToStack(SelectedView.Ad2Form) }
            )

            is SelectedView.ChangePasswordScreen -> ChangePasswordScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onSave = {  },
                showAd2 = { mainViewModel.addToStack(SelectedView.Ad2Form) }
            )

            is SelectedView.UpdatePhoneNumberScreen -> UpdatePhoneNumberScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onSave = {  },
                showAd2 = { mainViewModel.addToStack(SelectedView.Ad2Form) }
            )

            is SelectedView.AdsManagementScreen -> AdsManagementScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                showAd2 = { mainViewModel.addToStack(SelectedView.Ad2Form) },
                onBack = { mainViewModel.resetStack() }
            )

            is SelectedView.AdminLockerScreen -> AdminLockerScreen(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                numberOfLockers = 60,
                onBack = { mainViewModel.resetStack() },
                showAd2 = { mainViewModel.addToStack(SelectedView.Ad2Form) }
            )

        }
    }
    @Composable
    fun MainScreen(
        showAd2: () -> Unit,
        showStaffLogin: () -> Unit,
        showAdminLogin: () -> Unit,
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
                        .clickable {
                            showAdminLogin()
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

    private fun setAdminPassword() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val storedPassword = sharedPreferences.getString(ADMIN_PASSWORD, "")
        if (storedPassword == "") {
            sharedPreferences.edit().putString(ADMIN_PASSWORD, "123321").apply()
        }
    }


    private  fun insertBoxes() {

        val database = DatabaseModule.provideConsoleDatabase(this)
         val boxDao = database.getBoxDao()
        CoroutineScope(Dispatchers.IO).launch {
            BoxSeeder.seed(boxDao)
        }
    }

    private fun startPortService() {
        startService(Intent(this, SerialPortService::class.java))
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


