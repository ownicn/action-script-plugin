package com.ownicn.extensions;

import com.ownicn.extensions.impl.DefaultClipboard;
import com.ownicn.util.ProjectHolder;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

public final class BindingMaps {

    private final Map<String, Object> bindingMap = new HashMap<>();
    public static final BindingMaps INSTANCE = new BindingMaps();

    public Map<String, Object> getBindingMap() {
        return bindingMap;
    }

    private BindingMaps() {
        Project PROJECT = ProjectHolder.getProject();
        bindingMap.put("PROJECT", PROJECT);
        bindingMap.put("CLIPBOARD", new DefaultClipboard());
        //bindingMap.put("FILES", FILES);
    }
}
