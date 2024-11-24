package info.bvlion.watchify.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlertViewModel : ViewModel() {
  private var timeoutJob: Job? = null

  var isAlertStarted: Boolean = false
    private set

  fun startTimeout(onTimeout: () -> Unit) {
    isAlertStarted = true
    timeoutJob?.cancel()
    timeoutJob = viewModelScope.launch {
      delay(5 * 60 * 1000)
      onTimeout()
    }
  }

  fun cancelTimeout() {
    timeoutJob?.cancel()
  }
}