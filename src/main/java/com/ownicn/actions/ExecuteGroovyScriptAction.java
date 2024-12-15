package com.ownicn.actions;

import com.ownicn.extensions.impl.DefaultSelection;
import com.ownicn.groovy.GroovyScriptRunner;
import com.ownicn.io.ConsoleViewManager;
import com.ownicn.settings.ActionScriptSettings;
import com.ownicn.settings.ActionScriptSettings.ScriptEntry;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExecuteGroovyScriptAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        ActionScriptSettings settings = ActionScriptSettings.getInstance();
        List<ScriptEntry> scripts = null;
        if (settings.getState() != null) {
            scripts = settings.getState().getScripts();
        }

        if (scripts != null && scripts.isEmpty()) {
            Messages.showWarningDialog(project, "No scripts configured", "Execute Action Script");
            return;
        }

        chooseScriptRun(project, e, scripts);
    }

    private void chooseScriptRun(Project project, AnActionEvent event, List<ScriptEntry> scripts) {
        JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>("Choose Script", scripts) {
            @Override
            public @NotNull String getTextFor(ScriptEntry value) {
                return value.getName();
            }

            @Override
            public PopupStep<?> onChosen(ScriptEntry selectedValue, boolean finalChoice) {
                if (finalChoice && selectedValue != null) {
                    GroovyScriptRunner runner = new GroovyScriptRunner(project);
                    String scriptContent = selectedValue.getContent();
                    runner.putVariable("SELECTION", new DefaultSelection(event));
                    runner.executeScript(scriptContent);
                }
                return FINAL_CHOICE;
            }
        }).showCenteredInCurrentWindow(project);
    }
} 