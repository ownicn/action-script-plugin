package com.ownicn.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.ownicn.groovy.GroovyScriptRunner;
import com.ownicn.settings.ActionScriptSettings;
import com.ownicn.settings.ActionScriptSettings.ScriptEntry;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.util.List;
import java.util.Objects;

public class ExecuteGroovyScriptAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // 使用 invokeLater 确保在 EDT 线程中显示弹出窗口
        ApplicationManager.getApplication().invokeLater(() -> JBPopupFactory.getInstance()
                .createActionGroupPopup("Execute Action Script", createActionGroup(project),
                        e.getDataContext(), JBPopupFactory.ActionSelectionAid.MNEMONICS, true)
                .showInBestPositionFor(e.getDataContext()), ModalityState.nonModal());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    private static @NotNull DefaultActionGroup createActionGroup(Project project) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        DefaultActionGroup scriptsGroup = new DefaultActionGroup();
        List<ScriptEntry> scripts = Objects.requireNonNull(ActionScriptSettings.getInstance().getState()).getScripts();

        if (scripts.isEmpty()) {
            scriptsGroup.add(new EmptyScriptAction("No scripts configured"));
        } else {
            for (int i = 0; i < scripts.size(); i++) {
                scriptsGroup.add(getScriptAction(scripts, i, project));
            }
        }

        actionGroup.add(Separator.create());
        actionGroup.add(scriptsGroup);
        actionGroup.add(Separator.create("More"));
        actionGroup.add(new OpenSettingsAction("_0 Action Script Settings"));

        return actionGroup;
    }

    private static @NotNull AnAction getScriptAction(List<ScriptEntry> scripts, int i, Project project) {
        ScriptEntry script = scripts.get(i);
        String name = (i < 9) ? String.format("_%d ", i + 1) + script.getName() : "   " + script.getName();
        return new DefaultAnAction(name, e -> new GroovyScriptRunner(project).additionalCapabilities(e).executeScript(script.getContent()));
    }

    static class EmptyScriptAction extends AnAction {
        public EmptyScriptAction(String text) {
            super(text);
        }

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
    }
} 