package com.ownicn.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.ownicn.groovy.GroovyScriptRunner;
import com.ownicn.settings.ActionScriptSettings;
import com.ownicn.settings.ActionScriptSettings.ScriptEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ExecuteGroovyScriptAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        DefaultActionGroup scriptsGroup = new DefaultActionGroup();
        List<ScriptEntry> scripts = Objects.requireNonNull(ActionScriptSettings.getInstance().getState()).getScripts();

        if (scripts.isEmpty()) {
            scriptsGroup.add(new AnAction("No Scripts Available") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                }

                @Override
                public void update(@NotNull AnActionEvent e) {
                    e.getPresentation().setEnabled(false);
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }
            });
        } else {
            for (int i = 0; i < scripts.size(); i++) {
                scriptsGroup.add(getScriptAction(scripts, i, project));
            }
        }

        actionGroup.add(Separator.create());
        actionGroup.add(scriptsGroup);
        actionGroup.add(Separator.create("More"));
        actionGroup.add(new OpenSettingsAction("_0 Action Script Settings"));

        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                "Operations",
                actionGroup,
                DataManager.getInstance().getDataContext(Objects.requireNonNull(event.getInputEvent()).getComponent()),
                JBPopupFactory.ActionSelectionAid.MNEMONICS,
                true
        );

        popup.showCenteredInCurrentWindow(project);
    }

    private static @NotNull AnAction getScriptAction(List<ScriptEntry> scripts, int i, Project project) {
        ScriptEntry script = scripts.get(i);
        String name = (i < 9) ? String.format("_%d ", i + 1) + script.getName() : "   " + script.getName();
        return new DefaultAnAction(name, e -> new GroovyScriptRunner(project).additionalCapabilities(e).executeScript(script.getContent()));
    }
} 