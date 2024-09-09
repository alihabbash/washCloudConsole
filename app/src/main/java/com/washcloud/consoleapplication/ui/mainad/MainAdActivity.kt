package com.washcloud.consoleapplication.ui.mainad

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
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.KeyEvent
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.washcloud.consoleapplication.HeartbeatReceiver
import com.washcloud.consoleapplication.MainActivity
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.di.DatabaseModule
import com.washcloud.consoleapplication.hardware.SerialPortService
import com.washcloud.consoleapplication.local.database.utils.BoxSeeder
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.FileLogger
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.dimBackground
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.screenBackground
import com.washcloud.consoleapplication.utils.secondaryColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tp.xmaihh.serialport.SerialHelper
import java.util.Locale


@AndroidEntryPoint
class MainAdActivity : ComponentActivity() {
    private var screenHeight = 0.0.dp
    private var screenWidth = 0.0.dp
    private val barcodeData = StringBuilder()

    private val viewModel: MainAdViewModel by viewModels()


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

    private fun registerReceiver() {
        println("registerReceiver com.washcloud.door_status")
        val filter = IntentFilter("com.washcloud.door_status")
        FileLogger.log(this,  "registerReceiver"   ,"registerReceiver com.washcloud.door_status")
        registerReceiver(dataReceiver, filter)
    }


    companion object {
        public var dLocale: Locale? = null
    }

    init {
        updateConfig(this)
    }

    private fun updateConfig(wrapper: ContextThemeWrapper) {
        if(dLocale == Locale("") ) // Do nothing if dLocale is null
            return

        Locale.setDefault(dLocale)
        val configuration = Configuration()
        configuration.setLocale(dLocale)
        wrapper.applyOverrideConfiguration(configuration)
    }

    override fun onDestroy() {
        super.onDestroy()
    }





    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {


        return if (keyCode == KeyEvent.KEYCODE_ENTER) {
            FileLogger.log(this, "MainActivity", "keyCode ${KeyEvent.KEYCODE_ENTER}")
            val barcode = barcodeData.toString().trim().replace(Regex("\\s"), "").replace("\\","/").replace("\u0000", "")
            FileLogger.log(this, "MainActivity", "Barcode scanned: $barcode")
            if (barcode.isNotEmpty()) {
                Toast.makeText(this, "Barcode scanned: $barcode", Toast.LENGTH_LONG).show()
                viewModel.handleBarcode(barcode)
                barcodeData.setLength(0)
            }
            true
        } else {
            barcodeData.append(event.unicodeChar.toChar())
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcode = intent.getStringExtra("barcode")
        if (barcode != null) {
            Log.e("MainAdActivity", "Received barcode: $barcode")

                viewModel.handleBarcode(barcode)

        } else {
            Log.e("MainAdActivity", "No barcode received")
        }

        registerReceiver()
      //  startPortService()

        requestPermissionsIfNeeded()

        viewModel.apiResponse.observe(this, Observer { response ->
            handleApiResponse(response)
        })


        viewModel.error.observe(this, Observer { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            FileLogger.log(this, "MainAdActivity", "Error: $errorMessage")
            println("Error: $errorMessage")
        })

       /* GlobalScope.launch {
            delay(1000)
            val url = "https://devwashcloud.azurewebsites.net/api/LockerIntegration/Verification/4442408250005-1/21222213701A-001"
            FileLogger.log(this@MainAdActivity, "MainActivity", "Fetching data from $url");
            viewModel.fetchDirectly(url)

            delay(30000)
            val url2 = "https://devwashcloud.azurewebsites.net/api/LockerIntegration/Verification/4442408250004-1/21222213701A-001"
            FileLogger.log(this@MainAdActivity, "MainActivity", "Fetching data from $url2");
            viewModel.fetchDirectly(url2)

        }*/

       scheduleHeartbeat(this)

        insertBoxes()

        setContent {
            ConsoleApplicationTheme {
                screenHeight = LocalConfiguration.current.screenHeightDp.dp
                screenWidth = LocalConfiguration.current.screenWidthDp.dp
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = screenBackground
                ) {
                    val context = LocalContext.current
                    val apiData by viewModel.showDialog.observeAsState()
                    val isDoorOpen by viewModel.isDoorOpen.collectAsState()

                    var showDialog by remember { mutableStateOf(false) }

                    LaunchedEffect(apiData) {
                        apiData?.let {
                            if(isDoorOpen) {
                                showDialog = true
                                FileLogger.log(context, "MainAdActivity", "Showing dialog for door 0${it.doorNo} and station 02")

                                while (isDoorOpen && apiData != null) {
                                    delay(3000L)
                                    viewModel.sendCheckDoorStatusCommand("02", "0${it.doorNo}")
                                    FileLogger.log(context, "MainAdActivity", "Sending check door status command for door 0${it.doorNo} and station 02")
                                }

                            }else{
                                FileLogger.log(context, "MainAdActivity", "Door is closed for that not showing dialog door 0${it.doorNo} and station 02")
                            }
                        }
                    }

                  /*  LaunchedEffect(showDialog) {
                        while (showDialog && isDoorOpen) {
                            delay(3000L)
                            apiData?.let { data ->
                                FileLogger.log(context, "MainAdActivity", "Sending check door status command for door 0${data.doorNo} and station 02")
                                viewModel.sendCheckDoorStatusCommand("02", "0" + data.doorNo)
                            }
                        }
                    }*/

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                       // finish()
                                        val intent = Intent(context, MainActivity::class.java)
                                        context.startActivity(intent)


                                    }
                                )
                            }
                    ) {
                        Image(
                            painterResource(R.drawable.empty_image),
                            "ad_1",
                            modifier = Modifier
                                .width(screenWidth * 0.3f)
                                .height(screenWidth * 0.3f)
                        )
                        Text(
                            text = stringResource(id = R.string.ad1),
                            style = TextStyle(
                                fontSize = (screenWidth.value * 0.07f).sp,
                                fontWeight = FontWeight.Medium,
                                color = lightGrey
                            ),
                            textAlign = TextAlign.Center,
                        )

                      //  VideoPlayer(context, Modifier.fillMaxSize())
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


                            var timer by remember { mutableStateOf(60) }

                            LaunchedEffect(isDoorOpen) {
                                while (timer > 0 && isDoorOpen) {
                                    delay(1000L)
                                    timer--
                                }

                                if(isDoorOpen){
                                    FileLogger.log(context, "MainAdActivity", "Door is still open after 60 seconds")
                                    viewModel.insertTransaction(data)
                                    showDialog = false
                                    viewModel.checkOperationType()
                                }


                            }
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
                                            .height(0.15 * screenHeight)
                                            .padding(start = 16.dp, end = 16.dp),

                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.Start
                                        ) {

                                            Text(
                                                text = if (data.operationType == "PickUp")  "Pick Up Clothes" else "Drop Off Clothes",
                                                style = TextStyle(
                                                    fontSize = (screenWidth.value * 0.033f).sp,
                                                    fontWeight = FontWeight.Bold,

                                                    color = secondaryColor
                                                ),
                                            )


                                            Text(
                                                text = if (data.operationType == "PickUp") "Locker Number: ${data.doorNo}\nPlease pick up your clothes from the locker." else "Locker Number: ${data.doorNo}\nPlease drop off your clothes in the locker." ,
                                                style = TextStyle(
                                                    fontSize = (screenWidth.value * 0.025f).sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                            )

                                            Spacer(modifier = Modifier.height(0.02 * screenHeight))
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
                                                    text = "Time remaining: $timer seconds",
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


    private  fun insertBoxes() {

        val database = DatabaseModule.provideConsoleDatabase(this)
        val boxDao = database.getBoxDao()
        CoroutineScope(Dispatchers.IO).launch {
            BoxSeeder.seed(boxDao)
        }
    }

    private fun startPortService() {
        try {
            startService(Intent(this, SerialPortService::class.java))
            FileLogger.log(this, "MainActivity", "SerialPortService started successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting SerialPortService", e)
            FileLogger.log(this, "MainActivity", "Error starting SerialPortService: ${e.message}")
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
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsNeeded)
        }
    }


    private fun scheduleHeartbeat(context: Context) {
        val intent = Intent(context, HeartbeatReceiver::class.java).apply {
            putExtra("API_KEY", API_KEY)
            putExtra("TERMINAL_SN", TERMINAL_SN)
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            60 * 5000,
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

