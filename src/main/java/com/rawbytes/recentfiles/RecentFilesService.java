package com.rawbytes.recentfiles;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service(Service.Level.PROJECT)
@State(name = "RecentFilesState", storages = @Storage("recentFiles.xml"))
public final class RecentFilesService implements PersistentStateComponent<RecentFilesService.State> {
    private static final int MAX_SIZE = 50;
    private final LinkedList<VirtualFile> recentFiles = new LinkedList<>();
    private final Project project;
    private State myState = new State();

    public static class State {
        public List<String> recentFileUrls = new ArrayList<>();
    }

    public RecentFilesService(Project project) {
        this.project = project;
    }

    public void fileOpened(VirtualFile file) {
        synchronized (recentFiles) {
            recentFiles.remove(file);
            recentFiles.addFirst(file);
            if (recentFiles.size() > MAX_SIZE) {
                recentFiles.removeLast();
            }
        }
        MessageBus messageBus = project.getMessageBus();
        messageBus.syncPublisher(RecentFilesUpdateListener.TOPIC).filesUpdated(getRecentFiles());
    }

    public void fileClosed(VirtualFile file) {
        // Optionally remove from list or keep for history
    }

    public List<VirtualFile> getRecentFiles() {
        synchronized (recentFiles) {
            return new LinkedList<>(recentFiles);
        }
    }

    @Nullable
    @Override
    public State getState() {
        synchronized (recentFiles) {
            myState.recentFileUrls.clear();
            for (VirtualFile file : recentFiles) {
                myState.recentFileUrls.add(file.getUrl());
            }
        }
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
        VirtualFileManager fileManager = VirtualFileManager.getInstance();
        synchronized (recentFiles) {
            recentFiles.clear();
            myState.recentFileUrls.stream()
                    .map(fileManager::findFileByUrl)
                    .filter(Objects::nonNull)
                    .forEach(recentFiles::add);
        }
    }
}