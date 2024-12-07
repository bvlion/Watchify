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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.bvlion.watchify.BuildConfig
import info.bvlion.watchify.ui.theme.WatchifyTheme

class MainActivity : ComponentActivity() {

  private val viewModel by viewModels<MainViewModel>()

  private val registerOverlaysPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    viewModel.updateCanDrawOverlays()
  }
  private val registerIgnoringBatteryPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    viewModel.updateIgnoringBatteryOptimizations()
  }

  @SuppressLint("HardwareIds", "BatteryLife")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val canDrawOverlays = viewModel.canDrawOverlays.collectAsState()
      val isIgnoringBatteryOptimizations = viewModel.isIgnoringBatteryOptimizations.collectAsState()
      WatchifyTheme {
        MainScreen(
          isIgnoringBatteryOptimizations.value,
          Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID),
          {
            registerIgnoringBatteryPermission.launch(
              Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                setData(Uri.parse("package:$packageName"))
              }
            )
          },
          {
            startActivity(
              Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
              )
            )
          },
          {
            startActivity(
              Intent(
                Intent.ACTION_VIEW,
                Uri.parse(BuildConfig.CONTACT_URL)
              )
            )
          },
          {},
          {}
        )
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
        }
      }
    }
  }
}

@SuppressLint("HardwareIds")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  isIgnoringBatteryOptimizations: Boolean,
  androidId: String,
  onBatteryOptimizationSettingsClick: () -> Unit,
  onReviewClick: () -> Unit,
  onFeedbackClick: () -> Unit,
  onTermsClick: () -> Unit,
  onPrivacyClick: () -> Unit
) {
  val showMenu = remember { mutableStateOf(false) }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = { showMenu.value = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      FcmCard(
        title = "アラート設定用 FCM 登録",
        curlCommand =  """
            curl -X PUT "${BuildConfig.RT_DB_URL}notifier/$androidId.json" -d '{ "title":"緊急アラート", "body":"緊急アラートが発生しました", "wait_seconds":"30", "alert_seconds":"300", "timestamp":${System.currentTimeMillis()} }'
          """.trimIndent(),
        description = "表示したいタイトルと内容をセットにしてアラームを登録します。\n登録するとパラメータの wait_seconds で指定した秒後にアラートが alert_seconds で指定した秒数間、警告音と共に表示されます。"
      )

      Spacer(modifier = Modifier.height(16.dp))

      FcmCard(
        title = "アラート設定解除用 FCM 登録",
        curlCommand = """
            curl -X PUT "${BuildConfig.RT_DB_URL}stop/$androidId.json" -d '{ "timestamp":${System.currentTimeMillis()} }'
          """.trimIndent(),
        description = "アラートの設定解除や鳴っているアラートを止められます。"
      )
    }
  }

  // Modal Bottom Sheet for Menu
  if (showMenu.value) {
    ModalBottomSheet(
      onDismissRequest = { showMenu.value = false }
    ) {
      if (!isIgnoringBatteryOptimizations) {
        MenuItem(
          "バッテリー最適化から除外",
          "※ 動作が安定しない場合にお試しください",
          onClick = { onBatteryOptimizationSettingsClick(); showMenu.value = false }
        )
      }
      MenuItem("利用規約", onClick = { onTermsClick(); showMenu.value = false })
      MenuItem("プライバシーポリシー", onClick = { onPrivacyClick(); showMenu.value = false })
      MenuItem("レビューする", onClick = { onReviewClick(); showMenu.value = false })
      MenuItem("ご意見", onClick = { onFeedbackClick(); showMenu.value = false })
    }
  }
}

@Composable
fun FcmCard(title: String, curlCommand: String, description: String) {
  val context = LocalContext.current
  val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    elevation = CardDefaults.cardElevation(4.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Text(text = title, style = MaterialTheme.typography.headlineSmall)
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = description, style = MaterialTheme.typography.bodyMedium)
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = curlCommand, style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(8.dp))
      Button(onClick = {
        clipboardManager.setPrimaryClip(
          ClipData.newPlainText("label", curlCommand)
        )
        Toast.makeText(context, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
      }) {
        Text("コマンドをクリップボードにコピー")
      }
    }
  }
}

@Composable
fun MenuItem(title: String, body: String = "", onClick: () -> Unit) {
  TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(title, style = MaterialTheme.typography.bodyLarge)
      if (body.isNotEmpty()) {
        Text(
          body,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  WatchifyTheme {
    MainScreen(true, "android_id", {},{},{},{},{})
  }
}