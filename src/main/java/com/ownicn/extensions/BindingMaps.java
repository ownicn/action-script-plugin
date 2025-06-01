package com.ownicn.extensions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.ownicn.extensions.impl.DefaultClipboard;
import com.intellij.openapi.project.Project;
import com.ownicn.extensions.impl.DefaultSelection;

import java.util.HashMap;
import java.util.Map;

public final class BindingMaps {

    private final Map<String, Object> bindingMap = new HashMap<>();

    public static BindingMaps create(Project project) {
        return new BindingMaps(project, null);
    }

    public static BindingMaps create(Project project, AnActionEvent actionEvent) {
        return new BindingMaps(project, actionEvent);
    }

    public Map<String, Object> getBindingMap() {
        return bindingMap;
    }

    private BindingMaps(Project project, AnActionEvent actionEvent) {
        bindingMap.put("CLIPBOARD", new DefaultClipboard());
        bindingMap.put("PROJECT", new com.ownicn.extensions.Project(project));
        bindingMap.put("SELECTION", actionEvent == null ? null : new DefaultSelection(actionEvent));
        // bindingMap.put("FILES", FILES);
    }
}
