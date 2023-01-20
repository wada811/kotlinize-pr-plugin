package com.wada811.kotlinizepr.util

import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project

object BackgroundTask {
    fun doBackgroundTask(project: Project, taskName: String, doOnAction: () -> Unit, doOnSuccess: () -> Unit) {
        val backgroundTask = object : Task.Backgroundable(
            project,
            taskName,
            false,
            PerformInBackgroundOption.ALWAYS_BACKGROUND
        ) {
            override fun run(indicator: ProgressIndicator) {
                doOnAction()
            }

            override fun onSuccess() {
                super.onSuccess()
                doOnSuccess()
            }
        }
        val indicator = BackgroundableProcessIndicator(backgroundTask)
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(backgroundTask, indicator)
    }
}