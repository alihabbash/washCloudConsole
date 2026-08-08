package com.washcloud.consoleapplication.ui.mainad

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import coil.compose.rememberAsyncImagePainter
import com.washcloud.consoleapplication.HeartbeatReceiver
import com.washcloud.consoleapplication.MainActivity
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.hardware.SerialPortService
import com.washcloud.consoleapplication.local.database.utils.BoxSeeder
import com.washcloud.consoleapplication.local.database.utils.BoxType

import com.washcloud.consoleapplication.local.preferences.IS_REBOOT_ENABLED_KEY
import com.washcloud.consoleapplication.local.preferences.PrefsManager
import com.washcloud.consoleapplication.local.preferences.REBOOT_TIME_KEY
import com.washcloud.consoleapplication.remote.config.LOCKER_API
import com.washcloud.consoleapplication.remote.config.PREFIX
import com.washcloud.consoleapplication.remote.config.VERIFICATION
import com.washcloud.consoleapplication.ui.bagCounter.BagCounterView
import com.washcloud.consoleapplication.ui.bagCounter.BagCounterViewModel
import com.washcloud.consoleapplication.ui.pickup.SetPasswordScreen
import com.washcloud.consoleapplication.ui.pickup.StaticQrLoginScreen

import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.FileLogger
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.dimBackground
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.numbersColor
import com.washcloud.consoleapplication.utils.primaryDark
import com.washcloud.consoleapplication.utils.screenBackground
import com.washcloud.consoleapplication.utils.secondaryColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tp.xmaihh.serialport.SerialHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class MainAdActivity : ComponentActivity() {
    private var screenHeight = 0.0.dp
    private var screenWidth = 0.0.dp
    private val barcodeData = StringBuilder()

    private val viewModel: MainAdViewModel by viewModels()
    private val bagCounterViewModel: BagCounterViewModel by viewModels()

    private lateinit var rebootTime: String
    private var isRebootEnabled: Boolean = false


    private val scannerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            FileLogger.log(context, "MainAdActivity", "Received scanner data broadcast")

            if (intent.action == "com.washcloud.scanner_data") {
                val scannerData = intent.getStringExtra("scannerData")
                scannerData?.let {
                    FileLogger.log(context, "MainAdActivity", "Scanner Data: $it")
                 //   Toast.makeText(context, "Scanned Data: $it", Toast.LENGTH_SHORT).show()

                    viewModel.handleBarcode(it)

                }
            }
        }
    }


    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            FileLogger.log(context,  "onReceive"   ,"onReceive intent: ${intent.action}")
            Log.e("MainAdViewModel", "onReceive intent: ${intent.action}")
            if (intent.action == "com.washcloud.door_status") {

                val isOpen  = intent.getBooleanExtra("status", false)
                val stationId = intent.getStringExtra("stationId")
                val boxId = intent.getStringExtra("boxId")

                FileLogger.log(context,  "MainActivity onReceive"   ,"stationId: $stationId, boxId: $boxId, isOpen: $isOpen")

                viewModel.handCheckDoorStatusResponse(stationId, boxId, isOpen)
            }
        }
    }

    private  val converyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            FileLogger.log(context,  "onReceive"   ,"onReceive intent: ${intent.action}")
            Log.e("MainAdViewModel", "onReceive intent: ${intent.action}")
            if (intent.action == "com.washcloud.conveyor_door_status") {

                val status = intent.getStringExtra("status")
                FileLogger.log(context,  "MainActivity onReceive com.washcloud.conveyor_door_status"   ,"status: $status");
                val isOpen = status == "open"
                viewModel.handCheckDoorStatusResponse(isOpen = isOpen);
            }else if (intent.action ==    "com.washcloud.conveyor_move"){
                    val status = intent.getStringExtra("status")
                    FileLogger.log(context, "MainAdActivity", "Conveyor move status: $status")

                    if (status == "open") {
                        FileLogger.log(context, "MainAdActivity", "Conveyor is arrived to DES Please open the door and status is  ${status}")
                         viewModel.openConveyorDoor()
                    }

            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerScannerReceiver() {
        val filter = IntentFilter("com.washcloud.scanner_data")
        registerReceiver(scannerReceiver, filter)
        FileLogger.log(this, "MainAdActivity", "Scanner receiver registered")
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private  fun registerConveyorReceiver() {
        println("registerReceiver com.washcloud.conveyor_door_status")

        val filter = IntentFilter().apply {
            addAction("com.washcloud.conveyor_move")
            addAction("com.washcloud.conveyor_door_status")
        }
        FileLogger.log(this,  "registerReceiver"   ,"registerReceiver com.washcloud.conveyor_door_status")
        registerReceiver(converyReceiver, filter)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerReceiver() {
        println("registerReceiver com.washcloud.door_status")
        val filter = IntentFilter("com.washcloud.door_status")
        FileLogger.log(this,  "registerReceiver"   ,"registerReceiver com.washcloud.door_status")
        registerReceiver(dataReceiver, filter)
    }


    companion object {
        public var dLocale: Locale? = Locale("ar")

        fun getBaseUrl(context: Context): String {
            val url = PrefsManager.getBaseURL(context)
            return url.ifBlank {
                "https://api.washcloud.net/"
            }
        }
    }

    init {
        updateConfig(this)
    }


    private fun updateConfig(wrapper: ContextThemeWrapper) {
        dLocale = Locale("ar")
        Locale.setDefault(dLocale)
        val configuration = Configuration()
        configuration.setLocale(dLocale)
        MainActivity.dLocale = dLocale
        wrapper.applyOverrideConfiguration(configuration)
    }




    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiverSafe(scannerReceiver)
        unregisterReceiverSafe(dataReceiver)
        unregisterReceiverSafe(converyReceiver)
        FileLogger.log(this, "MainAdActivity onDestroy", "Receivers unregistered")
    }


    private fun checkRebootStatus() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        rebootTime = sharedPreferences.getString(REBOOT_TIME_KEY, "13:00") ?: "13:00"
        isRebootEnabled = sharedPreferences.getBoolean(IS_REBOOT_ENABLED_KEY, true)
        FileLogger.log(this, "MainAdActivity", "Reboot time: $rebootTime, isRebootEnabled: $isRebootEnabled")
        startRebootWatcher()
    }




    private fun startRebootWatcher() {
        // Check every minute if it's time to reboot
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            while (true) {
                if (isRebootEnabled) {
                    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    if (currentTime == rebootTime) {
                        rebootDevice()
                        break
                    }
                }
                delay(60000L)
            }
        }
    }
  private  fun rebootDevice() {

        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
            FileLogger.log(this, "MainAdActivity", "Rebooting device")
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//
//        FileLogger.log(this, "MainAdActivity", "keyCode $keyCode")
//
//        return if (keyCode == KeyEvent.KEYCODE_ENTER) {
//            FileLogger.log(this, "MainAdActivity", "keyCode ${KeyEvent.KEYCODE_ENTER}")
//            val barcode = barcodeData.toString().trim().replace(Regex("\\s"), "").replace("\\","/").replace("\u0000", "")
//            FileLogger.log(this, "MainAdActivity", "Barcode scanned: $barcode")
//            if (barcode.isNotEmpty()) {
//              //  Toast.makeText(this, "Barcode scanned: $barcode", Toast.LENGTH_LONG).show()
//                viewModel.handleBarcode(barcode)
//                FileLogger.log(this, "MainAdActivity", "Sending barcode to DropOffAndPickup: $barcode")
//                broadcastScannedBarcodeToListeners(barcode)
//                barcodeData.setLength(0)
//            }
//            true
//        } else {
//            barcodeData.append(event.unicodeChar.toChar())
//            super.onKeyDown(keyCode, event)
//        }
//    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCode = event.keyCode
            
            // Re-add keyCode logging for debugging
            FileLogger.log(this, "MainAdActivity", "keyCode (dispatchKeyEvent) $keyCode")

            // Allow system keys to pass through normally
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || 
                keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || 
                keyCode == KeyEvent.KEYCODE_BACK ||
                keyCode == KeyEvent.KEYCODE_HOME) {
                return super.dispatchKeyEvent(event)
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val barcode = barcodeData.toString().trim().replace(Regex("\\s"), "").replace("\\","/").replace("\u0000", "")
                if (barcode.isNotEmpty()) {
                    FileLogger.log(this, "MainAdActivity", "Barcode scanned (dispatchKeyEvent): $barcode")
                    viewModel.handleBarcode(barcode)
                    FileLogger.log(this, "MainAdActivity", "Sending barcode to DropOffAndPickup: $barcode")
                    broadcastScannedBarcodeToListeners(barcode)
                }
                barcodeData.setLength(0)
                return true // Consume ENTER so it doesn't trigger UI elements
            } else {
                val char = event.unicodeChar
                if (char != 0) {
                    barcodeData.append(char.toChar())
                    FileLogger.log(this, "MainAdActivity", "barcodeData (dispatchKeyEvent): ${barcodeData.toString()}")
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onStop() {
        super.onStop()
        FileLogger.log(this, "MainAdActivity", "onStop")
    }

    override fun onPause() {
        super.onPause()
        Log.e("MainAdActivity", "onPause");
        FileLogger.log(this, "MainAdActivity", "onPause")


        unregisterReceiverSafe(scannerReceiver)
        unregisterReceiverSafe(dataReceiver)
        unregisterReceiverSafe(converyReceiver)
    }

    override fun onResume() {
        super.onResume()

        FileLogger.log(this, "MainAdActivity", "onResume - Registering receivers again")

        registerScannerReceiver()
        registerReceiver()
        registerConveyorReceiver()

        viewModel.loadAdsFromStorage()
        Log.e("MainAdActivity", "onResume");
        FileLogger.log(this, "MainAdActivity", "Loading ads from storage ")
    }

    private fun unregisterReceiverSafe(receiver: BroadcastReceiver) {
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Receiver was not registered or another framework exception occurred
            FileLogger.log(this,"MainAdActivity", "Receiver not registered: ${receiver.javaClass.simpleName} - ${e.message}")
        }
    }

    private fun broadcastScannedBarcodeToListeners(barcode: String) {
        val broadcastIntent = Intent("com.washcloud.scanner_data")
        broadcastIntent.putExtra("scannerData", barcode)
        sendBroadcast(broadcastIntent)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val barcode = intent.getStringExtra("barcode")

        if (barcode != null) {
            FileLogger.log(this, "MainAdActivity", "Received barcode: $barcode")
            Log.e("MainAdActivity", "Received barcode: $barcode")

                viewModel.handleBarcode(barcode)

        } else {
            Log.e("MainAdActivity", "No barcode received")
        }


        val  serialOrder = intent.getStringExtra("serialOrder");
        if(serialOrder != null) {
            FileLogger.log(this, "MainAdActivity", "Received serialOrder: $serialOrder")
            Log.e("MainAdActivity", "Received serialOrder: $serialOrder")
            handleSerialOrder(serialOrder)

        }


        /*CoroutineScope(Dispatchers.Main).launch {
            delay(50_000L)
            val testBarcode = "O112505170001-1"
            FileLogger.log(this@MainAdActivity, "MainAdActivity", "Auto-calling sendBarcodToDropOffAndPickup after 50s with barcode: $testBarcode")
            sendBarcodeToDropOffAndPickup(testBarcode)
        }*/


        /* registerConveyorReceiver()
         registerReceiver()
         registerScannerReceiver()*/
        startPortService()

        requestPermissionsIfNeeded()
        checkRebootStatus()
        viewModel.apiResponse.observe(this, Observer { response ->
            handleApiResponse(response)
        })

        viewModel.error.observe(this, Observer { errorMessage ->
           // Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            FileLogger.log(this, "MainAdActivity", "Error: $errorMessage")
            println("Error: $errorMessage")
        })

//        GlobalScope.launch {
//            delay(1000 * 5 )
//            val url = "https://devwashcloud.azurewebsites.net/api/LockerIntegration/Verification/HH12511170002-4/123456789?ApiKey=123456789"
//            FileLogger.log(this@MainAdActivity, "MainActivity", "Fetching data from $url");
//            viewModel.handleBarcode(url)
////            //  viewModel.handleBarcode("https://devwashcloud.azurewebsites.net/api/LockerIntegration/Verification/4442503220002-1/21222213701A-001")
////
//////            delay(30000)
//////            val url2 = "https://devwashcloud.azurewebsites.net/api/LockerIntegration/Verification/4442408250004-1/21222213701A-001"
//////            FileLogger.log(this@MainAdActivity, "MainActivity", "Fetching data from $url2");
//////            viewModel.fetchfetchDirectlyDirectly(url2)
////
//        }

       scheduleHeartbeat(this)


        setContent {


            ConsoleApplicationTheme {
                screenHeight = LocalConfiguration.current.screenHeightDp.dp
                screenWidth = LocalConfiguration.current.screenWidthDp.dp
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = screenBackground
                ) {
                    val context = LocalContext.current

                    val isBagScanMode by viewModel.isBagScanMode.collectAsState()

                    if (isBagScanMode) {
                        // Show bag counter instead of ads/lockers
                        BagCounterView (
                            screenWidth = screenWidth,
                            screenHeight = screenHeight,
                            viewModel = bagCounterViewModel,
                            onBack = {
                                viewModel.exitBagScanMode()
                                // Optional: go back to MainActivity if you prefer:
                                // startActivity(Intent(context, MainActivity::class.java))
                                // finish()
                            }
                        )
                    } else if (viewModel.offlineQrSetPinCustomerId.collectAsState().value != null) {
                        val customerId = viewModel.offlineQrSetPinCustomerId.collectAsState().value!!
                        SetPasswordScreen(
                            showAd2 = {},
                            onBack = { viewModel.clearOfflineQrMode() },
                            onPasswordSetSuccess = { phone, newPin ->
                                viewModel.savePinAndUnlockBoxes(customerId, phone, newPin) { success ->
                                    if (!success) {
                                        Toast.makeText(context, "Verification failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onPhoneValidate = { phone, callback ->
                                viewModel.validatePhoneForOfflineQr(customerId, phone, callback)
                            },
                            screenWidth = screenWidth,
                            screenHeight = screenHeight
                        )
                    } else if (viewModel.offlineQrLoginCustomerId.collectAsState().value != null) {
                        val customerId = viewModel.offlineQrLoginCustomerId.collectAsState().value!!
                        StaticQrLoginScreen(
                            showAd2 = {},
                            onBack = { viewModel.clearOfflineQrMode() },
                            verifyPin = { phone, pin, onResult ->
                                viewModel.verifyPinAndUnlockBoxes(customerId, phone, pin, onResult)
                            },
                            screenWidth = screenWidth,
                            screenHeight = screenHeight
                        )
                    } else {

                        val apiData by viewModel.showDialog.observeAsState()
                        val isDoorOpen by viewModel.isDoorOpen.collectAsState()

                        var showDialog by remember { mutableStateOf(false) }
                        val adsList by viewModel.adsList.collectAsState()


                        LaunchedEffect(apiData) {
                            apiData?.let {
                                val box = viewModel.getBox(it.doorNo)
                                val boxType =
                                    if (it.boxes.size > 1) it.boxes.first().type.uppercase(Locale.ENGLISH) else it.type.uppercase(
                                        Locale.ENGLISH
                                    )
                                if (isDoorOpen) {
                                    showDialog = true
                                    FileLogger.log(
                                        context,
                                        "MainAdActivity",
                                        "Showing dialog for door 0${it.doorNo} and station ${box?.stationId}"
                                    )


                                    while (isDoorOpen && apiData != null && boxType == BoxType.BOX.name) {
                                        delay(3000L)


                                        viewModel.sendCheckDoorStatusCommand(
                                            box?.stationId.toString(),
                                            "0${it.doorNo}"
                                        )
                                        FileLogger.log(
                                            context,
                                            "MainAdActivity",
                                            "Sending check door status command for door 0${it.doorNo} and station ${box?.stationId}"
                                        )
                                    }

                                } else {
                                    FileLogger.log(
                                        context,
                                        "MainAdActivity",
                                        "Door is closed for that not showing dialog door 0${it.doorNo} and station ${box?.stationId}"
                                    )
                                }
                            }
                        }


                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            // finish()
                                            MainActivity.dLocale = Locale("ar")
                                            val intent = Intent(context, MainActivity::class.java)
                                            context.startActivity(intent)


                                        }
                                    )
                                }
                        ) {

                            AdDisplay(adsList, context, screenWidth, screenHeight)
//                        Image(
//                            painterResource(R.drawable.empty_image),
//                            "ad_1",
//                            modifier = Modifier
//                                .width(screenWidth * 0.3f)
//                                .height(screenWidth * 0.3f)
//                        )
//                        Text(
//                            text = stringResource(id = R.string.ad1),
//                            style = TextStyle(
//                                fontSize = (screenWidth.value * 0.07f).sp,
//                                fontWeight = FontWeight.Medium,
//                                color = lightGrey
//                            ),
//                            textAlign = TextAlign.Center,
//                        )

//                        VideoPlayer(context, Modifier.fillMaxSize())
                        }


                        if (showDialog && isDoorOpen) {

                            apiData?.let { data ->

                                /*DropOffDialog(
                                doorNo = data.doorNo,
                                onDismiss = { showDialog = false },
                                onConfirm = {
                                   viewModel.insertTransaction(data)
                                    showDialog = false
                                    viewModel.setCloseDoor()
                                }
                            )*/


                                val boxType =
                                    if (data.boxes.size > 1) data.boxes.first().type.uppercase(
                                        Locale.ENGLISH
                                    ) else data.type.uppercase(Locale.ENGLISH)

                                var timer by remember { mutableStateOf(180) }
                                var currentBoxIndex by remember { mutableStateOf(0) }
                                var hideNextButton by remember { mutableStateOf(false) }

                                LaunchedEffect(isDoorOpen) {
                                    while (timer > 0 && isDoorOpen) {
                                        delay(1000L)
                                        timer--
                                    }
                                    FileLogger.log(
                                        context,
                                        "MainAdActivity",
                                        "Door is still open after 60 seconds"
                                    )
                                    if (isDoorOpen && boxType == BoxType.BOX.name) {
                                        showDialog = false
                                    } else {
                                        viewModel.closeConveyorDoor()
                                    }

                                    //     viewModel.insertTransaction(data)


                                    if (data.boxes.size > 1) {
                                        viewModel.checkOperationType(
                                            boxType = data.boxes[currentBoxIndex].type,
                                            doorNumber = data.boxes[currentBoxIndex].doorNo
                                        )
                                    } else {
                                        viewModel.checkOperationType()
                                    }

                                }

                                if (data.boxes.size > 1) {
                                    ClothesPickupDialog(
                                        currentIndex = currentBoxIndex + 1,
                                        totalOrders = data.boxes.size,
                                        doorNo = data.boxes[currentBoxIndex].doorNo,
                                        type = data.boxes[currentBoxIndex].type,
                                        remainingTime = timer,
                                        hideNextButton = hideNextButton,
                                        onNext = {


                                            if (currentBoxIndex < data.boxes.size - 1) {
                                                FileLogger.log(
                                                    context,
                                                    "MainAdActivity",
                                                    "Moving to next box index ${currentBoxIndex + 1}"
                                                )
                                                viewModel.checkOperationType(
                                                    boxType = data.boxes[currentBoxIndex].type,
                                                    doorNumber = data.boxes[currentBoxIndex].doorNo,
                                                    hideDialog = false
                                                )
                                                currentBoxIndex++
                                                timer = 180;
                                                val nextBox = data.boxes[currentBoxIndex]
                                                val nextType =
                                                    nextBox.type.uppercase(Locale.ENGLISH)

                                                if (nextType == BoxType.CONVEYOR.name) {
                                                    FileLogger.log(
                                                        context,
                                                        "MainAdActivity",
                                                        "The next Is Conveyor, Opening conveyor door for door no ${nextBox.doorNo}"
                                                    );

                                                    viewModel.openConveyor(
                                                        nextBox.doorNo.padStart(
                                                            2,
                                                            '0'
                                                        )
                                                    )
                                                } else {

                                                    FileLogger.log(
                                                        context,
                                                        "MainAdActivity",
                                                        "The next Is Box, Sending command to open box door no ${nextBox.doorNo}"
                                                    )
                                                    viewModel.sendCommand(
                                                        nextBox.doorNo.padStart(
                                                            2,
                                                            '0'
                                                        )
                                                    )
                                                }
                                                hideNextButton =
                                                    currentBoxIndex == data.boxes.size - 1
                                            } else {
                                                showDialog = false
                                                if (data.boxes.any { it.type.uppercase(Locale.ENGLISH) == BoxType.CONVEYOR.name }) {
                                                    FileLogger.log(
                                                        context,
                                                        "MainAdActivity",
                                                        "Finishing pickup with conveyor box involved"
                                                    );
                                                    viewModel.closeConveyorDoor()
                                                }

                                                viewModel.checkOperationType(
                                                    boxType = data.boxes[currentBoxIndex].type,
                                                    doorNumber = data.boxes[currentBoxIndex].doorNo
                                                )
                                            }
                                        },
                                        onFinish = {
                                            showDialog = false

                                            if (data.boxes.any { it.type.uppercase(Locale.ENGLISH) == BoxType.CONVEYOR.name }) {
                                                FileLogger.log(
                                                    context,
                                                    "MainAdActivity",
                                                    "Finishing pickup with conveyor box involved"
                                                );
                                                viewModel.closeConveyorDoor()
                                            }


                                            viewModel.checkOperationType(
                                                boxType = data.boxes[currentBoxIndex].type,
                                                doorNumber = data.boxes[currentBoxIndex].doorNo
                                            )
                                        }
                                    )

                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(screenWidth)
                                            .height(screenHeight)
                                            .background(dimBackground)
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .clip(
                                                        RoundedCornerShape(0.02 * screenWidth)
                                                    )
                                                    .background(color = Color.White)
                                                    .width(0.8 * screenWidth)
                                                    .height(0.22 * screenHeight)
                                                    .padding(start = 16.dp, end = 16.dp),

                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.Start
                                            ) {

                                                Text(
                                                    text = if (data.operationType == "PickUp") stringResource(
                                                        id = R.string.pickup_clothes
                                                    ) else stringResource(id = R.string.dropoff_clothes),
                                                    style = TextStyle(
                                                        fontSize = (screenWidth.value * 0.033f).sp,
                                                        fontWeight = FontWeight.Bold,

                                                        color = secondaryColor
                                                    ),
                                                )


                                                Text(
                                                    text = if (data.operationType == "PickUp" && boxType == BoxType.BOX.name) stringResource(
                                                        id = R.string.locker_pickup_message,
                                                        data.doorNo
                                                    ) else if (data.operationType == "PickUp" && boxType == BoxType.CONVEYOR.name) stringResource(
                                                        id = R.string.conveyor_moving_customer
                                                    ) else stringResource(
                                                        id = R.string.locker_dropoff_message,
                                                        data.doorNo
                                                    ),
                                                    style = TextStyle(
                                                        fontSize = (screenWidth.value * 0.025f).sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                )

                                                Spacer(modifier = Modifier.height(0.03 * screenHeight))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 16.dp, end = 16.dp)
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
                                                            //onConfirm()
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = stringResource(id = R.string.time_remaining) + ": " + timer + " " + stringResource(
                                                            id = R.string.seconds
                                                        ),
                                                        style = TextStyle(
                                                            color = Color.White,
                                                            fontSize = (screenWidth.value * 0.024f).sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 16.dp, end = 16.dp)
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
                                                            if (isDoorOpen && boxType == BoxType.BOX.name) {
                                                                showDialog = false
                                                            } else {
                                                                viewModel.closeConveyorDoor()

                                                            }

                                                            //      viewModel.insertTransaction(data)
                                                            viewModel.checkOperationType()
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = stringResource(id = R.string.finsih),
                                                        style = TextStyle(
                                                            color = Color.White,
                                                            fontSize = (screenWidth.value * 0.024f).sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(16.dp)

                                                    )
                                                }
                                            }
                                        }
                                    }
                                }


                            }
                        }

                    }

                }
            }
        }
    }

    @Composable
    fun AdDisplay(adsList: List<Uri>, context: Context, screenWidth: Dp, screenHeight: Dp) {
        var currentIndex by remember { mutableStateOf(0) }
        val contentResolver = context.contentResolver

        LaunchedEffect(adsList, currentIndex) {
            if (adsList.isNotEmpty()) {
                val currentAd = adsList[currentIndex]
                val mimeType = contentResolver.getType(currentAd)

                if (mimeType?.startsWith("image/") == true) {
                    delay(10_000L)
                    currentIndex = (currentIndex + 1) % adsList.size
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (adsList.isNotEmpty()) {
                val currentAd = adsList[currentIndex]
                val mimeType = contentResolver.getType(currentAd)

                if (mimeType?.startsWith("image/") == true) {
                    Image(
                        painter = rememberAsyncImagePainter(currentAd),
                        contentDescription = "Ad Image",
                        modifier = Modifier
                            .width(screenWidth * 0.3f)
                            .height(screenWidth * 0.3f)
                    )
                } else if (mimeType?.startsWith("video/") == true) {
                    VideoPlayer(
                        context = context,
                        videoUri = currentAd,
                        modifier = Modifier.fillMaxSize(),
                        onVideoEnded = {
                            currentIndex = (currentIndex + 1) % adsList.size
                        }
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_ad_image),
                    contentDescription = "Default Ad Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }

    @Composable
    fun ClothesPickupDialog(
        currentIndex: Int,
        totalOrders: Int,
        doorNo: String,
        type: String, // "Conveyor" or "Box"
        remainingTime: Int,
        hideNextButton: Boolean,
        onNext: () -> Unit,
        onFinish: () -> Unit
    ) {
        val isConveyor = type.equals("Conveyor", ignoreCase = true)
        val titleText = stringResource(id = R.string.pickup_clothes)
        val boxLabel = if (isConveyor)
            stringResource(id = R.string.hanging_clothes_door)
        else
            stringResource(id = R.string.box_label)
        val imageRes = if (isConveyor) R.drawable.hanging else R.drawable.box

        val instructionText = stringResource(id = R.string.pickup_instruction)
        val minutes = remainingTime / 60
        val seconds = remainingTime % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        val remainingText = stringResource(id = R.string.remaining_time_format, formattedTime)

        var nextEnabled by remember { mutableStateOf(true) }
        var startCooldown by remember { mutableStateOf(false) }


        LaunchedEffect(startCooldown) {
            if (startCooldown) {
                nextEnabled = false
                delay(8_000)
                nextEnabled = true
                startCooldown = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dimBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 32.dp, vertical = 32.dp)
                    .width(0.85f * LocalConfiguration.current.screenWidthDp.dp),
            ) {
                Text(
                    text = titleText,
                    style = TextStyle(
                        color = secondaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = (screenWidth.value * 0.036f).sp,
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(90.dp)
                    )

                    Spacer(modifier = Modifier.width(24.dp))
                    Column {
                        Row {
                            Text(
                                text = stringResource(id = R.string.order_pickup_label),
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    fontSize = (screenWidth.value * 0.033f).sp,
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(
                                    id = R.string.order_progress,
                                    currentIndex,
                                    totalOrders
                                ),
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = numbersColor,
                                    fontSize = (screenWidth.value * 0.033f).sp,
                                )
                            )
                        }

                        Row {
                            Text(
                                text = stringResource(id = R.string.opened_label),
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    fontSize = (screenWidth.value * 0.033f).sp,
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = boxLabel,
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = numbersColor,
                                    fontSize = (screenWidth.value * 0.033f).sp,
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.number_label),
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    fontSize = (screenWidth.value * 0.033f).sp,
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = doorNo,
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = numbersColor,
                                    fontSize = (screenWidth.value * 0.033f).sp,
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = buildAnnotatedString {
                        append(instructionText)
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFD86060),
                                fontSize = (screenWidth.value * 0.033f).sp,
                            )
                        ) {
                            append(remainingText)
                        }
                    },
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = (screenWidth.value * 0.033f).sp,
                        textAlign = TextAlign.Center
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!hideNextButton)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (nextEnabled) secondaryColor
                                    else secondaryColor.copy(alpha = 0.5f)
                                )
                                .clickable(enabled = nextEnabled) {
                                    onNext()
                                    startCooldown = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.next_order_button),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (screenWidth.value * 0.033f).sp,
                                ),
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF57B48C))
                            .clickable { onFinish() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.finish_button),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (screenWidth.value * 0.033f).sp,
                            ),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }


    /*private  fun insertBoxes() {

        val database = DatabaseModule.provideConsoleDatabase(this)
        val boxDao = database.getBoxDao()
        CoroutineScope(Dispatchers.IO).launch {
            BoxSeeder.seed(boxDao)
        }
    }*/


    private  fun handleSerialOrder(serialOrder: String){

        val url = getBaseUrl(this) + LOCKER_API + VERIFICATION + serialOrder + "/" + PrefsManager.getTerminalSN(this);

        FileLogger.log(this, "MainAdActivity", "Open box quick way using data from $url")
        viewModel.handleBarcode(url)
    }
    private fun startPortService() {
        try {
            startService(Intent(this, SerialPortService::class.java))
            FileLogger.log(this, "MainAdActivity", "SerialPortService started successfully")
        } catch (e: Exception) {
            Log.e("MainAdActivity", "Error starting SerialPortService", e)
            FileLogger.log(this, "MainAdActivity", "Error starting SerialPortService: ${e.message}")
        }
    }
    private fun handleApiResponse(response: ApiResponse) {

        response.data?.forEach {
            Log.d("MainAdActivity", "Operation Type: ${it.operationType}")

        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val isGranted = it.value
            if (isGranted) {
                Log.d("MainActivity", "Permission granted: ${it.key}")
                // Handle the USB device if permission was granted
                val intent = intent
                if (intent != null && UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                }
            } else {
                Log.d("MainActivity", "Permission denied: ${it.key}")
            }
        }
    }


    private fun requestPermissionsIfNeeded() {
        val permissionsNeeded = listOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.MANAGE_DOCUMENTS,
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()


        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsNeeded)
        }
    }

    private fun scheduleHeartbeat(context: Context) {
        val intent = Intent(context, HeartbeatReceiver::class.java).apply {
            putExtra("API_KEY", PrefsManager.getApiKey(context))
            putExtra("TERMINAL_SN", PrefsManager.getTerminalSN(context))
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            60 * 2000,
            pendingIntent
        )
    }

//    @Composable
//    fun DropOffDialog(doorNo: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
//        var timer by remember { mutableStateOf(60) }
//
//        LaunchedEffect(Unit) {
//            while (timer > 0) {
//                delay(1000L)
//                timer--
//            }
//
//            onConfirm()
//        }
//        AlertDialog(
//            onDismissRequest = {},
//            title = { Text(
//                text = "Drop Off Clothes",
//                style = TextStyle(
//                    fontSize = (screenWidth.value * 0.033f).sp,
//                    fontWeight = FontWeight.Bold,
//                    color = secondaryColor
//                ),
//            ) },
//            text = { Text(
//                text = "Locker Number: $doorNo\nPlease drop off your clothes in the locker.",
//                style = TextStyle(
//                    fontSize = (screenWidth.value * 0.025f).sp,
//                    fontWeight = FontWeight.Bold
//                ),
//            ) },
//            confirmButton = {
////                Button(onClick = onConfirm) {
////                    Text("OK")
////                }
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(start = 16.dp, end = 16.dp)
//                        .background(
//                            brush = Brush.horizontalGradient(
//                                colors = listOf(
//                                    blueGradient,
//                                    secondaryColor,
//                                ),
//                            ),
//                            shape = RoundedCornerShape(8.dp)
//                        )
//                        .clickable {
//                            //onConfirm()
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "Time remaining: $timer seconds",
//                        style = TextStyle(
//                            color = Color.White,
//                            fontSize = (screenWidth.value * 0.024f).sp,
//                            fontWeight = FontWeight.Bold
//                        ),
//                        modifier = Modifier.padding(16.dp)
//                    )
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .wrapContentHeight()
//                .padding(16.dp)
//
//
//        )
//    }



}

