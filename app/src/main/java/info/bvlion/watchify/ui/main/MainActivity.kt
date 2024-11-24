package info.bvlion.watchify.ui.main

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import info.bvlion.watchify.BuildConfig
import info.bvlion.watchify.ui.theme.WatchifyTheme

class MainActivity : ComponentActivity() {

  private val viewModel by viewModels<MainViewModel>()

  private val registerOverlaysPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    viewModel.updateCanDrawOverlays()
  }

  @SuppressLint("HardwareIds")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    setContent {
      val canDrawOverlays = viewModel.canDrawOverlays.collectAsState()
      WatchifyTheme {
        if (!canDrawOverlays.value) {
          AlertDialog(
            onDismissRequest = {},
            title = { Text("オーバーレイ権限") },
            text = { Text("当アプリではオーバーレイの権限が必要です。\n設定画面でオーバーレイ権限を許可してください。") },
            confirmButton = {
              TextButton(
                onClick = {
                  registerOverlaysPermission.launch(
                    Intent(
                      Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                      Uri.parse("package:${packageName}")
                    )
                  )
                }
              ) {
                Text("設定画面へ")
              }
            }
          )
        } else {
          val curl = """
            curl -X PUT "${BuildConfig.RT_DB_URL}${Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)}.json" -d '{"title":"緊急アラート","body":"緊急アラートが発生しました", "timestamp":${System.currentTimeMillis()}
          """.trimIndent()

          AlertDialog(
            onDismissRequest = { finish() },
            title = { Text("通知方法") },
            text = { Text("以下の curl でデータを登録すると緊急アラートを音と振動付きでこの端末に表示できます\n\n$curl") },
            dismissButton = {
              TextButton(
                onClick = { finish() }
              ) {
                Text("終了")
              }
            },
            confirmButton = {
              TextButton(
                onClick = {
                  clipboardManager.setPrimaryClip(
                    ClipData.newPlainText("label", curl)
                  )
                  Toast.makeText(this, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
                }
              ) {
                Text("クリップボードにコピー")
              }
            }
          )
        }
      }
    }
  }
}