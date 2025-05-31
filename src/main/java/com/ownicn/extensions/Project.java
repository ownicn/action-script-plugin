package com.ownicn.extensions;


import com.ownicn.util.ProjectHolder;

import java.util.List;

public class Project {

    private final com.intellij.openapi.project.Project project;

    public Project(com.intellij.openapi.project.Project project) {
        this.project = project;
    }

    public String getName() {
        return project.getName();
    }

    public String getBasePath() {
        return project.getBasePath();
    }

    public List<Module> getModules() {
        return ProjectHolder.getModules(project);
    }

    @Override
    public String toString() {
        return String.format("PROJECT{name=%s, basePath=%s, modules=%s}", getName(), getBasePath(), getModules());
    }

}
