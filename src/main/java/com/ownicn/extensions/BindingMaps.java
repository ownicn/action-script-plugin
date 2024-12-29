package com.ownicn.extensions;

import com.ownicn.extensions.impl.DefaultClipboard;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

public final class BindingMaps {

    private final Map<String, Object> bindingMap = new HashMap<>();

    public static BindingMaps create(Project PROJECT) {
        return new BindingMaps(PROJECT);
    }

    public Map<String, Object> getBindingMap() {
        return bindingMap;
    }

    private BindingMaps(Project PROJECT) {
        bindingMap.put("PROJECT", PROJECT);
        bindingMap.put("CLIPBOARD", new DefaultClipboard());
        //bindingMap.put("FILES", FILES);
    }
}
