package com.apero.triggerfile.services.fileobserver

import android.os.FileObserver.ALL_EVENTS
import java.io.File
import java.util.Stack

internal open class RecursiveFileObserver
@JvmOverloads
constructor(
    private var mPath: String,
    private var mMask: Int = ALL_EVENTS,
) : FileObserverApiWrapper(mPath, mMask) {
    private var observers: MutableList<SingleFileObserver>? = null

    override fun startWatching() {
        if (observers != null) return
        observers = mutableListOf()
        val stack = Stack<String>()
        stack.push(mPath)
        while (!stack.empty()) {
            val parent = stack.pop()
            observers?.add(SingleFileObserver(parent, mMask))
            val path = File(parent)
            val files: Array<File> = path.listFiles() ?: continue
            for (i in files.indices) {
                if (files[i].isDirectory &&
                    !files[i].name.equals(".") &&
                    !files[i].name.equals("..")
                    && !files[i].name.startsWith(".")
                ) {
                    stack.push(files[i].path)
                }
            }
        }
        observers?.let {
            for (i in it.indices) it[i].startWatching()
        }
    }

    override fun stopWatching() {
        if (observers == null) return
        for (i in observers!!.indices) observers!![i].stopWatching()
        observers!!.clear()
        observers = null
    }

    override fun onEvent(
        event: Int,
        path: String?,
    ) {
    }

    inner class SingleFileObserver(
        private val mPath: String,
        mask: Int,
    ) : FileObserverApiWrapper(mPath, mask) {
        override fun onEvent(
            event: Int,
            path: String?,
        ) {
            val newPath = "$mPath/$path"
            this@RecursiveFileObserver.onEvent(event, newPath)
        }
    }
}