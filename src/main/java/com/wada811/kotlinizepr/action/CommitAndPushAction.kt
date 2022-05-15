package com.wada811.kotlinizepr.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.CurrentContentRevision
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil
import com.wada811.kotlinizepr.notification.Notifications.notifyCreatePullRequest
import com.wada811.kotlinizepr.util.BackgroundTask.doBackgroundTask
import com.wada811.kotlinizepr.util.contentRevision
import git4idea.util.GitFileUtils

class CommitAndPushAction(
    private val notification: Notification,
    private val targetFiles: List<VirtualFile>,
    private val beforeRevisions: Map<VirtualFile, CurrentContentRevision>
) : AnAction("Commit and Push") {
    override fun actionPerformed(e: AnActionEvent) {
        KotlinizeAction.logger.info("Commit and Push")
        notification.hideBalloon()
        val project = e.project ?: return
        doBackgroundTask(
            project = project,
            taskName = "Commit and Push",
            doOnAction = {
                targetFiles.forEach { file ->
                    KotlinizeAction.logger.info("File `${file.name}` had kotlinize")
                    val before = beforeRevisions[file] ?: return@forEach
                    val after = file.contentRevision()
                    VcsUtil.getVcsFor(project, file)?.checkinEnvironment?.commit(
                        listOf(Change(before, after)),
                        "Kotlinize ${file.nameWithoutExtension}"
                    )
                }
                GitFileUtils.addFiles(project, VcsUtil.getVcsRootFor(project, targetFiles[0])!!, targetFiles)
            },
            doOnSuccess = {
                ActionManager.getInstance().getAction(COMMIT_AND_PUSH_ACTION_ID)?.actionPerformed(e)
                project.notifyCreatePullRequest()
            }
        )
    }

    companion object {
        private const val COMMIT_AND_PUSH_ACTION_ID = "Vcs.Push"
    }
}