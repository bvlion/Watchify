package info.bvlion.watchify.service

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import info.bvlion.watchify.ui.alert.AlertActivity
import info.bvlion.watchify.worker.TimerWorker
import java.util.concurrent.TimeUnit

class MessagingService : FirebaseMessagingService() {
  @SuppressLint("HardwareIds")
  override fun onNewToken(token: String) {
    Firebase.database.reference
      .child("fcmToken")
      .child(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
      .setValue(token)
      .addOnFailureListener {
        Firebase.crashlytics.recordException(it)
      }
  }

  override fun onMessageReceived(message: RemoteMessage) {
    val workManager = WorkManager.getInstance(this)
    when (message.data[MESSAGE_DATA_KEY_TYPE]) {
      MESSAGE_DATA_VALUE_TYPE_START -> workManager.enqueueUniqueWork(
        TIMER_WORKER_NAME,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<TimerWorker>()
          .setInitialDelay(
            message.data[MESSAGE_DATA_KEY_WAIT_SECONDS].let {
              it?.toLongOrNull() ?:
              throw IllegalArgumentException("Illegal wait_seconds parameter: $it")
            },
            TimeUnit.SECONDS
          )
          .setInputData(workDataOf(
            TimerWorker.TITLE_PARAMETER to message.data[MESSAGE_DATA_KEY_TITLE],
            TimerWorker.BODY_PARAMETER to message.data[MESSAGE_DATA_KEY_BODY],
            TimerWorker.SECONDS_PARAMETER to message.data[MESSAGE_DATA_KEY_ALERT_SECONDS],
          ))
          .build()
      )
      MESSAGE_DATA_VALUE_TYPE_STOP -> {
        workManager.cancelUniqueWork(TIMER_WORKER_NAME)
        sendBroadcast(Intent(AlertActivity.ACTION_FINISH_ALERT_ACTIVITY))
        stopService(Intent(this, AlarmService::class.java))
      }
      else -> Firebase.crashlytics.recordException(IllegalArgumentException("Unknown type parameter: ${message.data[MESSAGE_DATA_KEY_TYPE]}"))
    }
  }

  companion object {
    private const val TIMER_WORKER_NAME = "timer_work"
    private const val MESSAGE_DATA_KEY_TITLE = "title"
    private const val MESSAGE_DATA_KEY_BODY = "body"
    private const val MESSAGE_DATA_KEY_WAIT_SECONDS = "wait_seconds"
    private const val MESSAGE_DATA_KEY_ALERT_SECONDS = "alert_seconds"
    private const val MESSAGE_DATA_KEY_TYPE = "type"
    private const val MESSAGE_DATA_VALUE_TYPE_START = "start"
    private const val MESSAGE_DATA_VALUE_TYPE_STOP = "stop"
  }
}