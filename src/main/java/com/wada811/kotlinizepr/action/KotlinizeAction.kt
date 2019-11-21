package com.wada811.kotlinizepr.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.CurrentContentRevision
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.vcsUtil.VcsUtil
import com.wada811.kotlinizepr.util.BackgroundableTask
import com.wada811.kotlinizepr.util.contentRevision
import git4idea.GitUtil
import git4idea.branch.GitBrancher
import java.util.concurrent.CountDownLatch

class KotlinizeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        logger.info("KotlinizeAction: actionPerformed: ${e.presentation.text}")
        val project = e.project ?: return
        val files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY) ?: return
        val beforeRevisions = mutableMapOf<VirtualFile, CurrentContentRevision>()
        val targetFiles = selectedJavaFiles(project, files).toList()
        if (targetFiles.none()) {
            return
        }
        checkoutNewBranch(project, "kotlinize/" + targetFiles.joinToString("-") { it.nameWithoutExtension.toLowerCase() })
        val filesLatch = CountDownLatch(targetFiles.size)
        BackgroundableTask.doBackgroundTask(
            project,
            "Rename files",
            {
                targetFiles.forEach { file ->
                    val fileLatch = CountDownLatch(1)
                    logger.info("KotlinizeAction: ${file.path}")
                    if (!VcsUtil.isFileUnderVcs(project, file.path)) {
                        logger.info("File `${file.name}` is not under VCS, skip kotlinize")
                        return@forEach
                    }
                    val before = file.contentRevision()
                    beforeRevisions[file] = before
                    WriteCommandAction.runWriteCommandAction(project) {
                        logger.info("File `${file.name}` rename to kt")
                        file.rename(this, file.nameWithoutExtension + ".kt")
                        val after = file.contentRevision()
                        BackgroundableTask.doBackgroundTask(
                            project,
                            "Commit files",
                            {
                                VcsUtil.getVcsFor(project, file)?.checkinEnvironment?.commit(
                                    listOf(Change(before, after)),
                                    "Rename ${file.nameWithoutExtension}.java to ${file.name}"
                                )
                            }, {
                                WriteCommandAction.runWriteCommandAction(project) {
                                    logger.info("File `${file.name}` rename to java")
                                    file.rename(this, file.nameWithoutExtension + ".java")
                                    fileLatch.countDown()
                                    filesLatch.countDown()
                                }
                            }
                        )
                    }
                    fileLatch.await()
                }
                filesLatch.await()
            },
            {
                logger.info("Call: Convert Java File to Kotlin File")
                ActionManager.getInstance().getAction(CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID)?.actionPerformed(e)
                val notification = Notification(
                    "Kotlinize PR",
                    "Kotlinize PR",
                    "",
                    NotificationType.INFORMATION
                )
                notification.addAction(CommitAndPushAction(notification, targetFiles, beforeRevisions))
                    .notify(project)
            }
        )
    }

    override fun update(e: AnActionEvent) {
        var isEnabledAndVisible = false
        val project = e.project
        val files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)
        if (project != null && files != null) {
            isEnabledAndVisible = isJavaFileSelected(project, files)
        }
        e.presentation.isEnabledAndVisible = isEnabledAndVisible
    }

    private fun isJavaFileSelected(
        project: Project,
        files: Array<VirtualFile>
    ): Boolean = selectedJavaFiles(project, files).any()

    private fun selectedJavaFiles(
        project: Project,
        files: Array<VirtualFile>,
        manager: PsiManager = PsiManager.getInstance(project)
    ): Sequence<VirtualFile> {
        return files.asSequence()
            .flatMap { if (it.isDirectory) selectedJavaFiles(project, it.children, manager) else sequenceOf(it) }
            .filter { it.isWritable }
            .map { manager.findFile(it) }
            .filterIsInstance<PsiJavaFile>()
            .map { it.virtualFile }
    }

    private fun checkoutNewBranch(project: Project, branchName: String) {
        val repositories = GitUtil.getRepositories(project).toList()
        val git = GitBrancher.getInstance(project)
        git.checkoutNewBranch(branchName, repositories)
    }

    companion object {
        val logger = Logger.getInstance(KotlinizeAction::class.java)
        private const val CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID = "ConvertJavaToKotlin"
    }
}