package com.wada811.kotlinizepr.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import git4idea.GitUtil
import git4idea.branch.GitBrancher

class CreatePullRequestAction(
    private val notification: Notification
) : AnAction("Create Kotlinize PR") {
    override fun actionPerformed(e: AnActionEvent) {
        KotlinizeAction.logger.info("Create Kotlinize PR")
        notification.hideBalloon()
        ActionManager.getInstance().getAction(PUSH_ACTION_ID)?.actionPerformed(e)
        ActionManager.getInstance().getAction(CREATE_PULL_REQUEST_ACTION_ID)?.actionPerformed(e)
        val project = e.project ?: return
        checkoutPreviousBranch(project)
    }

    private fun checkoutPreviousBranch(project: Project) {
        val repositories = GitUtil.getRepositories(project).toList()
        val git = GitBrancher.getInstance(project)
        git.checkout("-", false, repositories) {}
    }

    companion object {
        private const val PUSH_ACTION_ID = "Vcs.Push"
        private const val CREATE_PULL_REQUEST_ACTION_ID = "Github.Create.Pull.Request"
    }
}