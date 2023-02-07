package com.wada811.kotlinizepr.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import git4idea.GitUtil
import git4idea.branch.GitBrancher

class CheckoutPreviousBranchAction(
    private val notification: Notification
) : AnAction("Checkout Previous Branch") {
    override fun actionPerformed(e: AnActionEvent) {
        KotlinizeAction.logger.info("Checkout Previous Branch")
        notification.hideBalloon()
        val project = e.project ?: return
        checkoutPreviousBranch(project)
    }

    private fun checkoutPreviousBranch(project: Project) {
        val repositories = GitUtil.getRepositories(project).toList()
        val git = GitBrancher.getInstance(project)
        git.checkout("-", false, repositories) {}
    }
}