package com.alaje.learn.hb_flutter_image_gutter_viewer.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project


fun Project.showWarningNotification(message: String): Notification {
    return NotificationGroup.create(
        "someId",
        NotificationDisplayType.BALLOON,
        false,
        null,
        message,
        null
    ).createNotification(message, NotificationType.WARNING).apply {
        notify(this@showWarningNotification)
    }
}

fun Project.showInfoNotification(message: String): Notification {
    return NotificationGroup.create(
        "someId",
        NotificationDisplayType.BALLOON,
        false,
        null,
        message,
        null
    ).createNotification(message, NotificationType.INFORMATION).apply {
        notify(this@showInfoNotification)
    }
}
