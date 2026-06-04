package org.autojs.autojs6.jetbrains

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object AutoJs6Notifier {
    private const val GROUP = "AutoJs6 Notifications"

    fun info(project: Project?, message: String) = notify(project, message, NotificationType.INFORMATION)
    fun warn(project: Project?, message: String) = notify(project, message, NotificationType.WARNING)
    fun error(project: Project?, message: String) = notify(project, message, NotificationType.ERROR)

    private fun notify(project: Project?, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP)
            .createNotification(message, type)
            .notify(project)
    }
}
