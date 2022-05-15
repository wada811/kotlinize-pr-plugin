package com.wada811.kotlinizepr.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.CurrentContentRevision
import com.intellij.openapi.vfs.VirtualFile
import com.wada811.kotlinizepr.action.CommitAndPushAction
import com.wada811.kotlinizepr.action.CreatePullRequestAction

object Notifications {
    fun Project.notifyCommitAndPush(
        targetFiles: List<VirtualFile>,
        beforeRevisions: Map<VirtualFile, CurrentContentRevision>
    ) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Kotlinize PR")
            .createNotification("", NotificationType.INFORMATION)
        notification.addAction(CommitAndPushAction(notification, targetFiles, beforeRevisions))
        notification.notify(this)
    }

    fun Project.notifyCreatePullRequest() {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Kotlinize PR")
            .createNotification("", NotificationType.INFORMATION)
        notification.addAction(CreatePullRequestAction(notification))
        notification.notify(this)
    }
}