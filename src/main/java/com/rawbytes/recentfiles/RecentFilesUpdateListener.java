package com.rawbytes.recentfiles;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;
import java.util.List;

public interface RecentFilesUpdateListener extends EventListener {
    Topic<RecentFilesUpdateListener> TOPIC = Topic.create("Recent Files Updated", RecentFilesUpdateListener.class);
    void filesUpdated(List<VirtualFile> files);
}