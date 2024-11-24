package info.bvlion.watchify.ui.main

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _canDrawOverlays = MutableStateFlow(Settings.canDrawOverlays(application))
    val canDrawOverlays = _canDrawOverlays.asStateFlow()

    fun updateCanDrawOverlays() {
        _canDrawOverlays.value = Settings.canDrawOverlays(getApplication())
    }
}