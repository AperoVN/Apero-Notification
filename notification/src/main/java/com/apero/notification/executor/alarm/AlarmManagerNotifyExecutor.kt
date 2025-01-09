import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.apero.notification.Logger
import com.apero.notification.NotificationContent
import com.apero.notification.ReminderType
import com.apero.notification.executor.NotificationExecutor
import com.apero.notification.receiver.NotificationReceiver
import java.util.Calendar
import java.util.Locale

/**
 * Created by KO Huyn on 14/08/2023.
 */
internal class AlarmManagerNotifyExecutor private constructor() : NotificationExecutor {
    override fun pushAfter(
        context: Context,
        type: NotificationContent,
        reminderType: ReminderType.OneTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).let { intent ->
            intent.putExtra(NotificationReceiver.ARG_TYPE_NOTIFICATION, type)
            intent.putExtra(NotificationReceiver.ARG_TYPE_REMINDER, reminderType)
            PendingIntent.getBroadcast(
                context,
                type.requestCodeReminder,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val calendarCurrentTime =
            Calendar.getInstance(Locale.ROOT).apply { set(Calendar.SECOND, 0) }.timeInMillis
        val timeNotifyAfter = reminderType.notifyTimeUnit.toMillis(reminderType.notifyTime)
        val timeToSetAlarm = calendarCurrentTime + timeNotifyAfter
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeToSetAlarm, intent)
        Logger.d(
            "push after $type with $timeNotifyAfter milis trigger show at (${
                Calendar.getInstance().apply {
                    timeInMillis = (timeToSetAlarm)
                }.time
            })"
        )
    }

    override fun pushInterval(
        context: Context,
        type: NotificationContent,
        reminderType: ReminderType.Schedule
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent =
            Intent(context.applicationContext, NotificationReceiver::class.java).let { intent ->
                intent.putExtra(NotificationReceiver.ARG_TYPE_NOTIFICATION, type)
                intent.putExtra(NotificationReceiver.ARG_TYPE_REMINDER, reminderType)
                PendingIntent.getBroadcast(
                    context,
                    type.requestCodeReminder,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        val hour = reminderType.notifyTimeHour
        val minute = reminderType.notifyTimeMinute
        val calendarSetInDay = Calendar.getInstance(Locale.ROOT).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        val calendarCurrentTime = Calendar.getInstance(Locale.ROOT).apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
        }
        if (calendarCurrentTime.timeInMillis - calendarSetInDay.timeInMillis > reminderType.minimumTimeCurrentToNotify) {
            calendarSetInDay.add(Calendar.DATE, reminderType.stepDay)
        }

        val exactlyScheduleAction = {
            intent.let {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendarSetInDay.timeInMillis,
                    it
                )
            }
        }

        val scheduleAction = {
            intent.let {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendarSetInDay.timeInMillis,
                    it
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                exactlyScheduleAction()
            } else {
                scheduleAction()
            }
        } else {
            exactlyScheduleAction()
        }
        Logger.d("push interval $type with ${calendarSetInDay.time} and repeat after one day")
    }

    override fun cancel(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        alarmManager.cancel(intent)
        Logger.d("cancel with id: $reminderId")
    }

    companion object {
        @Volatile
        private var instance: AlarmManagerNotifyExecutor? = null

        @Synchronized
        fun getInstance() = AlarmManagerNotifyExecutor()
    }
}