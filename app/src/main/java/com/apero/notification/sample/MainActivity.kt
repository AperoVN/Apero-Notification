package com.apero.notification.sample

import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.apero.notification.NotificationSDK
import com.apero.notification.ReminderType
import com.apero.notification.sample.notification.FullscreenNotificationContent
import com.apero.notification.sample.service.CustomTriggerFileService
import com.apero.triggerfile.services.startTriggerService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private var hourOfDay:Int? = null
    private var minute:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<TimePicker>(R.id.timePicker).let { timePicker ->
            timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
                this.hourOfDay = hourOfDay
                this.minute = minute
            }
        }
        findViewById<Button>(R.id.pushNotification).let { button ->
            button.setOnClickListener {
                if (hourOfDay == null || minute == null) {
                    NotificationSDK.pushNotificationReminder(
                        FullscreenNotificationContent(),
                        ReminderType.OneTime(15, TimeUnit.MINUTES)
                    )
                } else {
                    NotificationSDK.pushNotification(FullscreenNotificationContent())
                }
            }
        }
        startTriggerService(CustomTriggerFileService::class.java)
//        NotificationSDK.cancelNotificationReminder(DefaultNotificationType.REQUEST_CODE_PUSH_NOTIFICATION)
//
//        if (NotificationSDK.isNotificationActive(DefaultNotificationType.NOTIFICATION_ID)) {
//            NotificationSDK.cancelNotification(DefaultNotificationType.NOTIFICATION_ID)
//        }
    }
}