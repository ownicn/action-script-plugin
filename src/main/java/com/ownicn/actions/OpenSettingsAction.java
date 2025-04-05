package com.ownicn.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.ownicn.settings.ActionScriptSettingsDialog;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
public class OpenSettingsAction extends AnAction {

    public OpenSettingsAction() {
    }

    public OpenSettingsAction(String text) {
        super(text);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        ActionScriptSettingsDialog dialog = new ActionScriptSettingsDialog(project);
        dialog.show();
    }
} 