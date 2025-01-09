package com.apero.notification

import android.app.Notification
import android.content.Context
import android.os.Parcelable

/**
 * Created by KO Huyn on 14/08/2023.
 */
abstract class NotificationContent(open val requestCodeReminder: Int) : Parcelable {
    abstract fun getNotifyId(): Int

    abstract fun getBy(context: Context): Notification?

    open fun canShowNotify() : Boolean = true
}
