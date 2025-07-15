package com.rawbytes.recentfiles;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class RecentFilesListener implements FileEditorManagerListener {
    private final RecentFilesService recentFilesService;

    public RecentFilesListener(Project project) {
        this.recentFilesService = project.getService(RecentFilesService.class);
    }

    @Override
    public void fileOpened(@NotNull com.intellij.openapi.fileEditor.FileEditorManager source, @NotNull VirtualFile file) {
        // recentFilesService.fileOpened(file);
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile file = event.getNewFile();
        if (file != null) {
            recentFilesService.fileOpened(file);
        }
    }
}