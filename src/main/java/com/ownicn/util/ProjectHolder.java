package com.ownicn.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class ProjectHolder {

    public static Project getProject() {
        // 获取当前活动的项目
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        return (openProjects.length > 0) ? openProjects[0] : ProjectManager.getInstance().getDefaultProject();
    }
}
