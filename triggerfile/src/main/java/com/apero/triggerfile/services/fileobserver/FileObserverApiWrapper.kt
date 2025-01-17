package com.apero.triggerfile.services.fileobserver

import android.os.Build
import android.os.FileObserver
import java.io.File

internal open class FileObserverApiWrapper(
    val path: String,
    mask: Int,
) {
    private var fileObserver: FileObserver? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fileObserver =
                object : FileObserver(File(path), mask) {
                    override fun onEvent(
                        event: Int,
                        path: String?,
                    ) {
                        this@FileObserverApiWrapper.onEvent(event, path)
                    }
                }
        } else {
            @Suppress("DEPRECATION")
            fileObserver =
                object : FileObserver(path, mask) {
                    override fun onEvent(
                        event: Int,
                        path: String?,
                    ) {
                        this@FileObserverApiWrapper.onEvent(event, path)
                    }
                }
        }
    }

    open fun onEvent(
        event: Int,
        path: String?,
    ) {
    }

    open fun startWatching() {
        fileObserver?.startWatching()
    }

    open fun stopWatching() {
        fileObserver?.stopWatching()
    }
}