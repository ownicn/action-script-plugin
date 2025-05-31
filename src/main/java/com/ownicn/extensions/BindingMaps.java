package com.ownicn.extensions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.ownicn.extensions.impl.DefaultClipboard;
import com.intellij.openapi.project.Project;
import com.ownicn.extensions.impl.DefaultSelection;

import java.util.HashMap;
import java.util.Map;

public final class BindingMaps {

    private final Map<String, Object> bindingMap = new HashMap<>();

    public static BindingMaps create(Project PROJECT) {
        return new BindingMaps(PROJECT, null);
    }

    public static BindingMaps create(Project PROJECT, AnActionEvent actionEvent) {
        return new BindingMaps(PROJECT, actionEvent);
    }

    public Map<String, Object> getBindingMap() {
        return bindingMap;
    }

    private BindingMaps(Project PROJECT, AnActionEvent actionEvent) {
        bindingMap.put("CLIPBOARD", new DefaultClipboard());
        bindingMap.put("PROJECT", new com.ownicn.extensions.Project(PROJECT));
        if (actionEvent != null) {
            bindingMap.put("SELECTION", new DefaultSelection(actionEvent));
        }
        // bindingMap.put("FILES", FILES);
    }
}
