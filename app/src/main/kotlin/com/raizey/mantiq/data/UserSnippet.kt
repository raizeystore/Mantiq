package com.raizey.mantiq.data

import com.raizey.mantiq.core.Snippet

data class UserSnippet(
    val id: String,
    val trigger: String,
    val template: String,
    val enabled: Boolean,
    val allowedPackages: Set<String>,
    val createdAt: Long,
) {
    fun isAllowedIn(packageName: String?): Boolean =
        enabled && packageName != null && packageName in allowedPackages

    fun asCoreSnippet(): Snippet = Snippet(trigger = trigger, template = template)
}

data class InstalledApp(
    val label: String,
    val packageName: String,
)
