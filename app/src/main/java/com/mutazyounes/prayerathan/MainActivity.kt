package com.mutazyounes.prayerathan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mutazyounes.prayerathan.audio.AthanLockScreen
import com.mutazyounes.prayerathan.audio.AthanVolumeKeys
import com.mutazyounes.prayerathan.shell.KeepAwake
import com.mutazyounes.prayerathan.shell.LocationFixer
import com.mutazyounes.prayerathan.shell.ShowOverLock
import com.mutazyounes.prayerathan.ui.WallScreen
import com.mutazyounes.prayerathan.ui.WallViewModel

class MainActivity : ComponentActivity() {
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        (application as PrayerAthanApp).locationFixer.onPermissionResult(granted)
    }

    private val launchLocationPermission: () -> Unit = {
        locationPermissionLauncher.launch(LocationFixer.PERMISSIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KeepAwake.apply(this)
        applyOverLock(intent)
        hideSystemBars()
        requestNotificationPermission()
        val app = application as PrayerAthanApp
        app.locationFixer.launchPermissionDialog = launchLocationPermission
        val now = app.wallClock.now()
        app.athanController.schedule(app.prayerEngine.day(now), now)
        setContent {
            val wallViewModel: WallViewModel = viewModel(
                factory = WallViewModel.factory(
                    app.prayerEngine,
                    app.athanController,
                    app.wallSettings,
                    app.weatherClient,
                    app.locationStore,
                    app.offsetStore,
                    app.locationFixer,
                    app.audioSettings,
                    app.medicineSettings,
                    app.wallClock,
                ),
            )
            val athanPlaying by app.athanController.playback.collectAsState()
            val athkarPlaying by app.athanController.athkarPlayback.collectAsState()
            val medicinePlaying by app.athanController.medicinePlayback.collectAsState()
            val demoId by app.athanController.demoId.collectAsState()
            var hadAthan by remember { mutableStateOf(false) }
            val alarmAudio = athanPlaying != null ||
                athkarPlaying != null ||
                medicinePlaying != null ||
                demoId != null
            LaunchedEffect(athanPlaying) {
                if (athanPlaying != null) {
                    hadAthan = true
                    ShowOverLock.apply(this@MainActivity, true)
                } else if (hadAthan) {
                    ShowOverLock.apply(this@MainActivity, false)
                    hadAthan = false
                }
            }
            LaunchedEffect(alarmAudio) {
                volumeControlStream = if (alarmAudio) {
                    AudioManager.STREAM_ALARM
                } else {
                    AudioManager.USE_DEFAULT_STREAM_TYPE
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .keepScreenOn(),
            ) {
                WallScreen(
                    viewModel = wallViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyOverLock(intent)
    }

    override fun onDestroy() {
        val fixer = (application as PrayerAthanApp).locationFixer
        if (fixer.launchPermissionDialog === launchLocationPermission) {
            fixer.launchPermissionDialog = null
        }
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!alarmAudioPlaying()) {
            return super.onKeyDown(keyCode, event)
        }
        if (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP) {
            return super.onKeyDown(keyCode, event)
        }
        volumeControlStream = AudioManager.STREAM_ALARM
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.repeatCount > 0) {
            (application as PrayerAthanApp).athanController.stop()
            return true
        }
        val handled = super.onKeyDown(keyCode, event)
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val manager = getSystemService(AudioManager::class.java) ?: return handled
            val volume = manager.getStreamVolume(AudioManager.STREAM_ALARM)
            if (AthanVolumeKeys.stopAfterDown(event.repeatCount, volume)) {
                (application as PrayerAthanApp).athanController.stop()
            }
        }
        return handled
    }

    private fun alarmAudioPlaying(): Boolean {
        val controller = (application as PrayerAthanApp).athanController
        return controller.playback.value != null ||
            controller.athkarPlayback.value != null ||
            controller.medicinePlayback.value != null ||
            controller.demoId.value != null
    }

    private fun applyOverLock(intent: Intent?) {
        if (intent?.getBooleanExtra(AthanLockScreen.EXTRA_OVER_LOCK, false) == true) {
            ShowOverLock.apply(this, true)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
