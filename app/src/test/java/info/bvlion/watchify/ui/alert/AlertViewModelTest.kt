package info.bvlion.watchify.ui.alert

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertViewModelTest {
  private lateinit var viewModel: AlertViewModel
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @Before
  fun setup() {
    viewModel = AlertViewModel()
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun cleanup() {
    Dispatchers.resetMain()
  }

  @Test
  fun `startTimeout should call onTimeout after specified delay`() = testScope.runTest {
    var isTimedOut = false
    viewModel.startTimeout(5) { isTimedOut = true }

    assertTrue(viewModel.isAlertStarted)
    advanceTimeBy(5100)
    assertTrue(isTimedOut)
  }

  @Test
  fun `cancelTimeout should prevent onTimeout from being called`() = testScope.runTest {
    var isTimedOut = false
    viewModel.startTimeout(5) { isTimedOut = true }
    viewModel.cancelTimeout()

    advanceTimeBy(5100)
    assertFalse(isTimedOut)
  }
}