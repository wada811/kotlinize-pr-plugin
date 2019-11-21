package com.wada811.kotlinizepr.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CreatePullRequestAction(
    private val notification: Notification
) : AnAction("Create Kotlinize PR") {
    override fun actionPerformed(e: AnActionEvent) {
        KotlinizeAction.logger.info("Create Kotlinize PR")
        notification.hideBalloon()
        ActionManager.getInstance().getAction(CREATE_PULL_REQUEST_ACTION_ID)?.actionPerformed(e)
    }

    companion object {
        private const val CREATE_PULL_REQUEST_ACTION_ID = "Github.Create.Pull.Request"
    }
}