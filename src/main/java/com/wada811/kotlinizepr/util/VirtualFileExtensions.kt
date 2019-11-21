package com.wada811.kotlinizepr.util

import com.intellij.openapi.vcs.actions.VcsContextFactory
import com.intellij.openapi.vcs.changes.CurrentContentRevision
import com.intellij.openapi.vfs.VirtualFile

fun VirtualFile.contentRevision(): CurrentContentRevision {
    val contextFactory = VcsContextFactory.SERVICE.getInstance()
    val path = contextFactory.createFilePathOn(this)
    return CurrentContentRevision(path)
}