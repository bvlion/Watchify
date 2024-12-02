package info.bvlion.watchify.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import info.bvlion.watchify.ui.alert.AlertActivity

class TimerWorker(
  context: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
  override suspend fun doWork(): Result {
    val title = inputData.getString(TITLE_PARAMETER) ?: return Result.failure()
    val body = inputData.getString(BODY_PARAMETER) ?: return Result.failure()
    val seconds = inputData.getString(SECONDS_PARAMETER) ?: return Result.failure()
    applicationContext.startActivity(AlertActivity.createIntent(applicationContext, title, body, seconds))
    return Result.success()
  }

  companion object {
    const val TITLE_PARAMETER = "title_parameter"
    const val BODY_PARAMETER = "body_parameter"
    const val SECONDS_PARAMETER = "seconds_parameter"
  }
}