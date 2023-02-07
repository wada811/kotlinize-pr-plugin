package com.wada811.kotlinizepr.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.commit.AmendCommitAware
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
        KotlinizeAction.logger.info("rootFile: ${rootFile.path}")
        val childrenFiles = rootFile.children
        KotlinizeAction.logger.info("childrenFiles: ${childrenFiles.map { it.name }}")
        BackgroundTask.doBackgroundTask(
            project,
            "Commit files",
            {
                fun List<VirtualFile>.findChildren(): List<VirtualFile> {
                    return flatMap {
                        if (it.isDirectory && !listOf(".git", ".idea", ".gradle", "build", "gradle").contains(it.name)) {
                            it.children.toList().findChildren()
                        } else if (it.path.contains("src") && VcsUtil.isFileUnderVcs(project, it.path)) {
                            listOf(it)
                        } else {
                            emptyList()
                        }
                    }
                }

                val files = rootFile.children.toList().findChildren()
                GitFileUtils.addFiles(project, rootFile, files)
                val changes = files.map { ChangeListManager.getInstance(project).getChange(it) }
                KotlinizeAction.logger.info("changes: $changes")
                VcsUtil.getVcsFor(project, rootFile)?.checkinEnvironment?.commit(
                    changes,
                    "Kotlinize more better"
                )
            }, {
//                ActionManager.getInstance().getAction(PUSH_ACTION_ID)?.actionPerformed(e)
                ActionManager.getInstance().getAction(CREATE_PULL_REQUEST_ACTION_ID)?.actionPerformed(e)
                project.notifyCheckoutPreviousBranch()
            }
        )
    }

    companion object {
        //        private const val PUSH_ACTION_ID = "Vcs.Push"
        private const val CREATE_PULL_REQUEST_ACTION_ID = "Github.Create.Pull.Request"
    }
}