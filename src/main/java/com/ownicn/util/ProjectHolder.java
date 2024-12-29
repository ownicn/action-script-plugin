package com.ownicn.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;

public class ProjectHolder {

    public static Project getProject() {
        // 先获取当前焦点窗口
        WindowManager windowManager = WindowManager.getInstance();
        Window focusedWindow = windowManager.getMostRecentFocusedWindow();

        // 获取所有打开的项目
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length > 0 && focusedWindow != null) {
            // 遍历所有打开的项目，找到匹配当前焦点窗口的项目
            for (Project project : openProjects) {
                if (project.isDisposed()) {
                    continue;
                }

                IdeFrame frame = windowManager.getIdeFrame(project);
                if (frame != null && SwingUtilities.isDescendingFrom(frame.getComponent(), focusedWindow)) {
                    return project;
                }
            }

            // 如果没有找到活动的项目，返回第一个非默认且未关闭的项目
            for (Project project : openProjects) {
                if (!project.isDefault() && !project.isDisposed()) {
                    return project;
                }
            }
        }

        // 如果没有找到合适的项目，返回默认项目
        return ProjectManager.getInstance().getDefaultProject();
    }
}
