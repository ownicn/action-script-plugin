package com.ownicn.extensions;


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

}
