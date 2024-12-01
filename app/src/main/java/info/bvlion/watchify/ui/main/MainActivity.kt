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

    setContent {
      val canDrawOverlays = viewModel.canDrawOverlays.collectAsState()
      WatchifyTheme {
        MainScreen(
          Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID),
          {},
          {},
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
  androidId: String,
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
      FcmCard(
        title = "アラート設定用 FCM 登録",
        curlCommand =  """
            curl -X PUT "${BuildConfig.RT_DB_URL}$androidId.json" -d '{"title":"緊急アラート","body":"緊急アラートが発生しました", "type":"start", "timestamp":${System.currentTimeMillis()}}
          """.trimIndent(),
        description = "表示したいタイトルと内容をセットにしてアラームを登録します。\n登録して1分後にアラートが5分間、警告音と共に表示されます。"
      )

      Spacer(modifier = Modifier.height(16.dp))

      FcmCard(
        title = "アラート設定解除用 FCM 登録",
        curlCommand = """
            curl -X PUT "${BuildConfig.RT_DB_URL}$androidId.json" -d '{"type":"stop", "timestamp":${System.currentTimeMillis()}}
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
      Text(text = description, style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = curlCommand, style = MaterialTheme.typography.bodyMedium)
      Spacer(modifier = Modifier.height(8.dp))
      Button(onClick = {
        clipboardManager.setPrimaryClip(
          ClipData.newPlainText("label", curlCommand)
        )
        Toast.makeText(context, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
      }) {
        Text("クリップボードにコマンドをコピー")
      }
    }
  }
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
  TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  WatchifyTheme {
    MainScreen("android_id", {},{},{},{})
  }
}