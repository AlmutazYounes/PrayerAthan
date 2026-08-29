package com.mutazyounes.prayerathan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mutazyounes.prayerathan.shell.KeepAwake
import com.mutazyounes.prayerathan.shell.LocationFixer
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
                    app.locationFixer,
                    app.audioSettings,
                    app.wallClock,
                ),
            )
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

    override fun onDestroy() {
        val fixer = (application as PrayerAthanApp).locationFixer
        if (fixer.launchPermissionDialog === launchLocationPermission) {
            fixer.launchPermissionDialog = null
        }
        super.onDestroy()
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
