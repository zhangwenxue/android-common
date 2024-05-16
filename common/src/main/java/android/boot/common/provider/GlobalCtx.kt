package android.boot.common.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri

val globalContext: Context get() = GlobalCtxProvider.applicationContext

fun localPackageInfo(): PackageInfo? {
    return runCatching {
        globalContext.packageManager.getPackageInfo(
            globalContext.packageName,
            0
        )
    }.getOrNull()
}

private val finishedTasks = mutableSetOf<String>()
private val pendingTasks = mutableSetOf<Pair<String, () -> Unit>>()
fun lazyInit(tag: String, task: () -> Unit) {
    if (!appStarted) {
        if (pendingTasks.any { it.first == tag }) return
        pendingTasks.add(Pair(tag, task))
    } else {
        if (finishedTasks.contains(tag)) return
        task().also {
            finishedTasks.add(tag)
        }
    }
}

@Volatile
private var appStarted = false

class GlobalCtxProvider : ContentProvider() {

    companion object {
        lateinit var applicationContext: Context
    }

    override fun onCreate(): Boolean {
        return context?.applicationContext?.let {
            applicationContext = it
            appStarted = true
            pendingTasks.forEach { taskPair ->
                taskPair.second()
                finishedTasks.add(taskPair.first)
            }
            true
        } ?: false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ) = null

    override fun getType(uri: Uri) = null

    override fun insert(uri: Uri, values: ContentValues?) = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ) = 0

}