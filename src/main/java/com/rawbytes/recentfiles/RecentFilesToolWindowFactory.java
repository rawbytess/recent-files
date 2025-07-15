package com.rawbytes.recentfiles;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.openapi.fileEditor.FileEditorManager;
import org.jetbrains.annotations.NotNull;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
/**
 * Factory class for creating the Recent Files tool window.
 */
public class RecentFilesToolWindowFactory implements ToolWindowFactory {

    /**
     * Creates the content for the tool window.
     *
     * @param project    The current project.
     * @param toolWindow The tool window instance.
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Get the list of recently opened files
        RecentFilesService recentFilesService = project.getService(RecentFilesService.class);

        // Create a list model to hold the files
        DefaultListModel<VirtualFile> listModel = new DefaultListModel<>();
        listModel.addAll(recentFilesService.getRecentFiles());

        // Create a JBList to display the files
        JBList<VirtualFile> fileList = new JBList<>(listModel);
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof VirtualFile file) {
                    setText(file.getPath());
                    setIcon(file.getFileType().getIcon());
                    setToolTipText(file.getPath());
                }
                return this;
            }
        });

        // Add a mouse listener to open the file on double-click
        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && !evt.isConsumed()) {
                    evt.consume();
                    VirtualFile fileToOpen = fileList.getSelectedValue();
                    if (fileToOpen != null) FileEditorManager.getInstance(project).openFile(fileToOpen, true);
                }
            }
        });

        // Create a scroll pane for the list. This is the main component of our tool window.
        JBScrollPane scrollPane = new JBScrollPane(fileList);

        // Create a single content panel and add it to the tool window. This is done only once.
        ContentFactory contentFactory = ContentFactory.getInstance();
        // Use an empty string for the display name to avoid showing a tab header for a single panel.
        Content content = contentFactory.createContent(scrollPane, "", false);
        toolWindow.getContentManager().addContent(content);

        // Subscribe to file updates to refresh the list model.
        // The UI components are already created; this will just update the data in the JBList.
        project.getMessageBus().connect(toolWindow.getDisposable()).subscribe(RecentFilesUpdateListener.TOPIC, new RecentFilesUpdateListener() {
            @Override
            public void filesUpdated(List<VirtualFile> files) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    listModel.clear();
                    listModel.addAll(files);
                });
        };
    });
}}


