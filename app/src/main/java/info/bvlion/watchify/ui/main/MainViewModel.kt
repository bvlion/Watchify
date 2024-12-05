package info.bvlion.watchify.ui.main

import android.app.Application
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    private val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val _canDrawOverlays = MutableStateFlow(Settings.canDrawOverlays(application))
    val canDrawOverlays = _canDrawOverlays.asStateFlow()

    private val _isIgnoringBatteryOptimizations = MutableStateFlow(powerManager.isIgnoringBatteryOptimizations(application.packageName))
    val isIgnoringBatteryOptimizations = _isIgnoringBatteryOptimizations.asStateFlow()

    fun updateCanDrawOverlays() {
        _canDrawOverlays.value = Settings.canDrawOverlays(getApplication())
    }

    fun updateIgnoringBatteryOptimizations() {
        _isIgnoringBatteryOptimizations.value = powerManager.isIgnoringBatteryOptimizations(application.packageName)
    }
}