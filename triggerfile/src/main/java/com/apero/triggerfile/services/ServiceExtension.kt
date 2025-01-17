package com.apero.triggerfile.services

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

fun <T : Service> Context.startTriggerService(serviceClass: Class<T>) {
    if (isServiceRunning(serviceClass)) {
        return
    }
    startServiceWithAction(ActionsService.START.name, serviceClass)
}

fun <T : Service> Context.stopTriggerService(serviceClass: Class<T>) {
    startServiceWithAction(ActionsService.STOP.name, serviceClass)
}

fun <T : Service> Context.isServiceRunning(serviceClass: Class<T>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun <T : Service> Context.restartTriggerServiceAfterGrantFilePermission(serviceClass: Class<T>) {
    if (isServiceRunning(serviceClass)) {
        startServiceWithAction(ActionsService.RESTART_OBSERVER.name, serviceClass)
    } else {
        startTriggerService(serviceClass)
    }
}

private fun <T : Service> Context.startServiceWithAction(
    action: String,
    serviceClass: Class<T>
) {
    kotlin.runCatching {
        val intentService = Intent(this, serviceClass).apply {
            this.action = action
        }
        Log.d("startServiceWithAction", "startServiceWithAction: $intentService")
        ContextCompat.startForegroundService(this, intentService)
    }.onFailure {
        Log.e("startServiceWithAction", "catch throwable service exception ", it)
    }.getOrNull()
}