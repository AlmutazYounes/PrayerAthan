package com.mutazyounes.prayerathan.shell

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import com.mutazyounes.prayerathan.engine.LocationStore
import com.mutazyounes.prayerathan.engine.SavedLocation
import java.util.Locale
import java.util.TimeZone

class LocationFixer(
    context: Context,
    private val locationStore: LocationStore,
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var inFlight: InFlight? = null
    private var permissionCallback: ((Outcome) -> Unit)? = null

    @Volatile
    var launchPermissionDialog: (() -> Unit)? = null

    fun hasPermission(): Boolean {
        val fine = appContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = appContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED ||
            coarse == PackageManager.PERMISSION_GRANTED
    }

    fun requestOneFix(onResult: (Outcome) -> Unit) {
        cancel()
        val previousPermission = permissionCallback
        permissionCallback = null
        previousPermission?.invoke(Outcome.Failed)
        if (hasPermission()) {
            takeOneFix(onResult)
            return
        }
        val launch = launchPermissionDialog
        if (launch == null) {
            onResult(Outcome.Failed)
            return
        }
        permissionCallback = onResult
        launch()
    }

    fun onPermissionResult(granted: Boolean) {
        val callback = permissionCallback ?: return
        permissionCallback = null
        if (granted && hasPermission()) {
            takeOneFix(callback)
        } else {
            callback(Outcome.Failed)
        }
    }

    fun cancel() {
        val state = inFlight ?: return
        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        complete(state, Outcome.Failed, manager)
    }

    @SuppressLint("MissingPermission")
    private fun takeOneFix(onResult: (Outcome) -> Unit) {
        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            onResult(Outcome.Failed)
            return
        }
        val provider = pickProvider(manager)
        if (provider == null) {
            onResult(Outcome.Failed)
            return
        }
        val state = InFlight(onResult)
        inFlight = state
        val timeout = Runnable { finishWithLastKnownOrFail(manager, state) }
        state.timeout = timeout
        mainHandler.postDelayed(timeout, TIMEOUT_MS)
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                val cancel = CancellationSignal()
                state.cancellation = cancel
                manager.getCurrentLocation(
                    provider,
                    cancel,
                    appContext.mainExecutor,
                ) { location ->
                    onFreshLocation(manager, state, location)
                }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        onFreshLocation(manager, state, location)
                    }
                }
                state.listener = listener
                manager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper(),
                )
            }
        } catch (_: SecurityException) {
            complete(state, Outcome.Failed, manager)
        }
    }

    private fun onFreshLocation(
        manager: LocationManager,
        state: InFlight,
        location: Location?,
    ) {
        if (inFlight !== state) return
        if (location != null) {
            complete(state, saveFix(location.latitude, location.longitude), manager)
        } else {
            finishWithLastKnownOrFail(manager, state)
        }
    }

    @SuppressLint("MissingPermission")
    private fun finishWithLastKnownOrFail(manager: LocationManager, state: InFlight) {
        if (inFlight !== state) return
        val last = lastKnown(manager)
        if (last != null) {
            complete(state, saveFix(last.latitude, last.longitude), manager)
        } else {
            complete(state, Outcome.Failed, manager)
        }
    }

    private fun saveFix(latitude: Double, longitude: Double): Outcome {
        val label = String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
        val parsed = SavedLocation.parse(label, latitude, longitude, TimeZone.getDefault().id)
            ?: return Outcome.Failed
        locationStore.write(parsed)
        return Outcome.Saved
    }

    private fun complete(state: InFlight, outcome: Outcome, manager: LocationManager?) {
        if (inFlight !== state) return
        inFlight = null
        mainHandler.removeCallbacks(state.timeout)
        state.cancellation?.cancel()
        val listener = state.listener
        if (listener != null) {
            try {
                manager?.removeUpdates(listener)
            } catch (_: RuntimeException) {
            }
        }
        state.onResult(outcome)
    }

    private fun pickProvider(manager: LocationManager): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            manager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
        ) {
            return LocationManager.FUSED_PROVIDER
        }
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private fun lastKnown(manager: LocationManager): Location? {
        val candidates = ArrayList<Location>(3)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { manager.getLastKnownLocation(LocationManager.FUSED_PROVIDER) }
                .getOrNull()
                ?.let(candidates::add)
        }
        runCatching { manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }
            .getOrNull()
            ?.let(candidates::add)
        runCatching { manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }
            .getOrNull()
            ?.let(candidates::add)
        return candidates.maxByOrNull { it.elapsedRealtimeNanos }
    }

    enum class Outcome { Saved, Failed }

    private class InFlight(
        val onResult: (Outcome) -> Unit,
    ) {
        var timeout: Runnable = Runnable {}
        var cancellation: CancellationSignal? = null
        var listener: LocationListener? = null
    }

    companion object {
        private const val TIMEOUT_MS = 20_000L
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
}
