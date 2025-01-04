package info.bvlion.watchify

import android.view.accessibility.AccessibilityEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import info.bvlion.watchify.ui.main.MainActivity
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Rule

class InstrumentedTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun uiTest() {
    // textbox is visible
    composeTestRule.onNodeWithText("アラート設定用 FCM 登録").assertIsDisplayed()

    // button is enable
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val events = mutableListOf<AccessibilityEvent>()
    instrumentation.uiAutomation.setOnAccessibilityEventListener {
      if (it.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
        events.add(it)
      }
    }

    try {
      composeTestRule.onNodeWithContentDescription("アラート設定用 FCM 登録 コマンドをクリップボードにコピー")
        .assertIsDisplayed()
        .performClick()

      runBlocking {
        repeat(10) {
          if (events.isNotEmpty()) return@runBlocking
          delay(100)
        }
      }

      assertTrue(events.any { it.text.contains("クリップボードにコピーしました") })
    } finally {
      instrumentation.uiAutomation.setOnAccessibilityEventListener(null)
    }
  }
}