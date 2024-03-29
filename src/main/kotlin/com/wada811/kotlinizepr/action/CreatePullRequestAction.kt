package com.wada811.kotlinizepr.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil
import com.wada811.kotlinizepr.notification.Notifications.notifyCheckoutPreviousBranch
import com.wada811.kotlinizepr.util.BackgroundTask
import git4idea.util.GitFileUtils

class CreatePullRequestAction(
    private val notification: Notification,
    private val files: Array<VirtualFile>
) : AnAction("Create Kotlinize PR") {
    override fun actionPerformed(e: AnActionEvent) {
        KotlinizeAction.logger.info("Create Kotlinize PR")
        notification.hideBalloon()
        val project = e.project ?: return
        val rootFile = VcsUtil.getVcsRootFor(project, files[0])!!
        BackgroundTask.doBackgroundTask(
            project,
            "Commit files",
            {
                val files = ChangeListManager.getInstance(project).affectedFiles
                GitFileUtils.addFiles(project, rootFile, files)
                val changes = ChangeListManager.getInstance(project).getChangesIn(rootFile).toList()
                VcsUtil.getVcsFor(project, rootFile)?.checkinEnvironment?.commit(
                    changes,
                    "Kotlinize more better"
                )
            }, {
                ActionManager.getInstance().getAction(CREATE_PULL_REQUEST_ACTION_ID)?.actionPerformed(e)
                project.notifyCheckoutPreviousBranch()
            }
        )
    }

    companion object {
        private const val CREATE_PULL_REQUEST_ACTION_ID = "Github.Create.Pull.Request"
    }
}