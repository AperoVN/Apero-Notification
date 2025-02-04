package com.apero.triggerfile.services

import android.app.ActivityManager
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.apero.triggerfile.services.fileobserver.RecursiveFileObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


abstract class TriggerFileService : Service() {

    companion object {
        internal const val ID_NOTIFICATION_SERVICE = 669966
    }
    private val iBinder: IBinder = LocalBinder()
    private var fileObservers = arrayListOf<RecursiveFileObserver>()
    private val coroutineScope by lazy {
        CoroutineScope(Dispatchers.Default)
    }

    abstract fun onNewFileDetected(file: File)
    abstract fun createNotificationService(): Notification
    abstract fun setListDirectoriesToWatch(): List<String>
    open fun setIdNotificationService(): Int = ID_NOTIFICATION_SERVICE
    open fun setConditionTrigger(): Boolean = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ActionsService.START.name -> {
                startForeground(
                    setIdNotificationService(),
                    createNotificationService(),
                )
                observerFileInDispatchersIO()
            }

            ActionsService.RESTART_OBSERVER.name -> {
                unregisterFileObserver()
                observerFileInDispatchersIO()
            }

            ActionsService.STOP.name -> {
                unregisterFileObserver()
                stopSelf()
            }
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            START_NOT_STICKY
        } else
            START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

    protected open fun handleOnEvent(event: Int, path: String?) {
        if (!path.isNullOrEmpty()) {
            val fileName = path.substring(path.lastIndexOf("/") + 1, path.length)
            if (!fileName.contains(".") || fileName.startsWith(".pending")) {
                return
            } else {
                coroutineScope.launch {
                    delay(2000)
                    val file = File(path)
                    if (file.exists() && file.length() > 0) {
                        onNewFileDetected(file)
                    }
                }
            }
        }
    }

    inner class LocalBinder : Binder() {
        val service: TriggerFileService
            get() = this@TriggerFileService
    }

    private fun registerFileObserver() {
        setListDirectoriesToWatch().forEach { path ->
            fileObservers.add(
                object : RecursiveFileObserver(
                    path,
                    FileObserver.CREATE or FileObserver.MOVED_TO
                ) {
                    override fun onEvent(
                        event: Int,
                        path: String?,
                    ) {
                        handleOnEvent(event, path)
                    }
                }.also {
                    it.startWatching()
                },
            )
        }
    }

    private fun unregisterFileObserver() {
        fileObservers.forEach {
            it.stopWatching()
        }
        fileObservers.clear()
    }

    private fun observerFileInDispatchersIO() {
        if (setConditionTrigger()) {
            coroutineScope.launch(Dispatchers.IO) {
                registerFileObserver()
            }
        }
    }
}