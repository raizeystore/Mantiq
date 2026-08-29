package com.raizey.mantiq.data

import android.content.Context
import android.content.Intent

class InstalledAppsRepository(private val context: Context) {
    fun listLaunchableApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .map { info ->
                InstalledApp(
                    label = info.loadLabel(packageManager)?.toString().orEmpty()
                        .ifBlank { info.activityInfo.packageName },
                    packageName = info.activityInfo.packageName,
                )
            }
            .distinctBy(InstalledApp::packageName)
            .sortedBy { it.label.lowercase() }
    }
}
