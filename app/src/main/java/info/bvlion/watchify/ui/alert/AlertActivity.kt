package info.bvlion.watchify.ui.alert

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import info.bvlion.watchify.service.AlarmService
import info.bvlion.watchify.ui.theme.WatchifyTheme

class AlertActivity : ComponentActivity() {

  private val viewModel by viewModels<AlertViewModel>()

  private val finishReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == ACTION_FINISH_ALERT_ACTIVITY) {
        finish()
      }
    }
  }

  @SuppressLint("InlinedApi")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val title = intent.getStringExtra(EXTRA_TITLE) ?: return
    val body = intent.getStringExtra(EXTRA_BODY) ?: return

    registerReceiver(
      finishReceiver, IntentFilter(ACTION_FINISH_ALERT_ACTIVITY),
      RECEIVER_NOT_EXPORTED
    )
    if (!viewModel.isAlertStarted) {
      startService(Intent(this, AlarmService::class.java))
    }
    viewModel.startTimeout { finish() }

    setContent {
      WatchifyTheme {
        AlertDialog(
          onDismissRequest = {},
          title = { Text(title) },
          text = { Text(body) },
          confirmButton = {
            TextButton(
              onClick = {
                finish()
              }
            ) {
              Text("停止")
            }
          }
        )
      }
    }
  }

  override fun onStart() {
    super.onStart()
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  }

  override fun onStop() {
    super.onStop()
    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  }

  override fun finish() {
    super.finish()
    stopService(Intent(this, AlarmService::class.java))
    viewModel.cancelTimeout()
    unregisterReceiver(finishReceiver)
  }


  companion object {
    private const val EXTRA_TITLE = "title"
    private const val EXTRA_BODY = "body"

    const val ACTION_FINISH_ALERT_ACTIVITY = "action_finish_alert_activity"

    fun createIntent(context: Context, title: String, body: String): Intent =
      Intent(context, AlertActivity::class.java).apply {
        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(EXTRA_TITLE, title)
        putExtra(EXTRA_BODY, body)
      }
  }
}