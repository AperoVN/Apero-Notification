package com.apero.notification.fullscreen

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apero.notification.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

abstract class NotificationFullscreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.FullscreenReminderTheme)
        turnScreenOnAndKeyguardOff()
    }

    override fun onStart() {
        super.onStart()
        triggerUpdateTimeDisplay()
    }

    fun navigateTo(activityClazz: Class<*>, arguments: Bundle.() -> Unit = {}) {
        val intent = Intent(this, activityClazz)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val bundle = Bundle()
        bundle.arguments()
        intent.putExtras(bundle)
        finish()
        dismissKeyguard()
        startActivity(intent)
    }


    private fun dismissKeyguard() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguardManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }

        with(getSystemService(KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@NotificationFullscreenActivity, null)
            }
        }
    }

    private fun turnScreenOffAndKeyguardOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }
    }

    data class TimeFullScreen(
        val hourDisplay: String,
        val minuteDisplay: String,
        val dateDisplay: String
    )

    abstract fun updateDateTimeDisplayFullScreen(dateTimeFullScreen: TimeFullScreen)

    private fun triggerUpdateTimeDisplay() = kotlin.runCatching {
        lifecycleScope.launch {
            while (lifecycleScope.isActive) {
                val cal: Calendar = Calendar.getInstance()
                val currentHour: Int = cal.get(Calendar.HOUR_OF_DAY)
                val currentMinute: Int = cal.get(Calendar.MINUTE)
                val formattedHour = String.format(Locale.ROOT, "%02d", currentHour)
                val formattedMinute = String.format(Locale.ROOT, "%02d", currentMinute)
                val dateFormat = SimpleDateFormat(
                    "EEEE, MMMM dd",
                    Resources.getSystem().configuration.locales.get(0)
                )
                val date = dateFormat.format(Date())
                updateDateTimeDisplayFullScreen(
                    TimeFullScreen(
                        hourDisplay = formattedHour,
                        minuteDisplay = formattedMinute,
                        dateDisplay = date
                    )
                )
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOffAndKeyguardOn()
    }
}