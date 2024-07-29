package com.washcloud.consoleapplication.ui.mainad

import android.app.AlarmManager
import android.app.PendingIntent
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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.washcloud.consoleapplication.HeartbeatReceiver
import com.washcloud.consoleapplication.MainActivity
import com.washcloud.consoleapplication.R
import com.washcloud.consoleapplication.hardware.UsbBroadcastReceiver
import com.washcloud.consoleapplication.local.preferences.API_KEY
import com.washcloud.consoleapplication.local.preferences.TERMINAL_SN
import com.washcloud.consoleapplication.ui.theme.ConsoleApplicationTheme
import com.washcloud.consoleapplication.utils.blueGradient
import com.washcloud.consoleapplication.utils.lightGrey
import com.washcloud.consoleapplication.utils.screenBackground
import com.washcloud.consoleapplication.utils.secondaryColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainAdActivity : ComponentActivity() {
    private var screenHeight = 0.0.dp
    private var screenWidth = 0.0.dp

    private lateinit var usbManager: UsbManager
    private lateinit var usbReceiver: UsbBroadcastReceiver
    private val viewModel: MainAdViewModel by viewModels()



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
        unregisterReceiver(usbReceiver)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        usbReceiver = UsbBroadcastReceiver(usbManager, this)
        IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(UsbBroadcastReceiver.ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, this)
        }

        requestPermissionsIfNeeded()

        viewModel.apiResponse.observe(this, Observer { response ->
            handleApiResponse(response)
        })


        viewModel.error.observe(this, Observer { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            println("Error: $errorMessage")
        })

        /*GlobalScope.launch {
            delay(100)
            val url = "https://devwashcloud.azurewebsites.net/api/LockerIntegration/Verification/4442407280012-1/21222213701A-001"
            viewModel.fetchDirectly(url)
        }*/

     //  scheduleHeartbeat(this)
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


                    var showDialog by remember { mutableStateOf(false) }

                    LaunchedEffect(apiData) {
                        apiData?.let {
                            showDialog = true
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clickable {
                                finish()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(
                                    intent
                                )
                            }
                    ) {
                        Image(
                            painterResource(R.drawable.empty_image),
                            "ad_1",
                            modifier = Modifier.width(screenWidth * 0.3f)
                                .height(screenWidth*0.3f)
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


                    if (showDialog) {

                        apiData?.let { data ->
                            viewModel.sendCommand("2", data.doorNo)
                            DropOffDialog(
                                doorNo = data.doorNo,
                                onDismiss = { showDialog = false },
                                onConfirm = {
                                   viewModel.insertTransaction(data)
                                    showDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleApiResponse(response: ApiResponse) {

        response.data.forEach {
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
                    device?.let { handleDeviceConnection(it) }
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
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val action = intent.action
        if (UsbBroadcastReceiver.ACTION_USB_PERMISSION == action) {
            synchronized(this) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.let { handleDeviceConnection(it) }
                } else {
                    Log.d("MainActivity", "Permission denied for device $device")
                }
            }
        }
    }

    fun handleDeviceConnection(device: UsbDevice) {
        val usbInterface = device.getInterface(0)
        val endpoint = usbInterface.getEndpoint(0)
        val connection = usbManager.openDevice(device)

        val buffer = ByteArray(64)
        connection.bulkTransfer(endpoint, buffer, buffer.size, 0)
        val barcode = String(buffer).trim()

        if (barcode != null) {
            viewModel.handleBarcode(barcode)
        }

    }

    fun handleDeviceDisconnection(device: UsbDevice) {

        Log.d("MainActivity", "Device disconnected: $device")
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
            60 * 1000,
            pendingIntent
        )
    }

    @Composable
    fun DropOffDialog(doorNo: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Drop Off Clothes",  style = TextStyle(
                fontSize = (screenWidth.value * 0.032f).sp,
                fontWeight = FontWeight.Bold,
                color = secondaryColor
            ),) },
            text = { Text(text = "Locker Number: $doorNo\nPlease drop off your clothes in the locker.",  style = TextStyle(
                fontSize = (screenWidth.value * 0.025f).sp,
                fontWeight = FontWeight.Bold
            ),) },
            confirmButton = {
//                Button(onClick = onConfirm) {
//                    Text("OK")
//                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start =  16.dp, end = 16.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    blueGradient,
                                    secondaryColor,
                                ),
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.ok),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (screenWidth.value * 0.025f).sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },

        )
    }



}

