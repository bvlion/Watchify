package info.bvlion.watchify.service

import android.annotation.SuppressLint
import android.provider.Settings
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import info.bvlion.watchify.ui.alert.AlertActivity

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
    startActivity(AlertActivity.createIntent(this, message.data["title"], message.data["body"]))
  }
}