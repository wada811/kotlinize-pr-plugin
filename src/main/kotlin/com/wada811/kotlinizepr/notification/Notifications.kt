package com.wada811.kotlinizepr.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.wada811.kotlinizepr.action.CheckoutPreviousBranchAction
import com.wada811.kotlinizepr.action.CreatePullRequestAction

object Notifications {
    private const val GROUP_ID = "Kotlinize PR"
    private const val CONTENT = "Kotlinize PR"
    fun Project.notifyCreatePullRequest() {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(CONTENT, NotificationType.INFORMATION)
        notification.addAction(CreatePullRequestAction(notification))
        notification.notify(this)
    }
    fun Project.notifyCheckoutPreviousBranch() {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(CONTENT, NotificationType.INFORMATION)
        notification.addAction(CheckoutPreviousBranchAction(notification))
        notification.notify(this)
    }

}