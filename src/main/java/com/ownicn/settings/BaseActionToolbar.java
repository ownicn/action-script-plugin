package com.ownicn.settings;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class BaseActionToolbar extends AnAction {

    private Consumer<AnActionEvent> action;
    private Function<AnActionEvent, Boolean> enableFunction;

    public BaseActionToolbar(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    public BaseActionToolbar(String text, String description, Icon icon, Consumer<AnActionEvent> action) {
        super(text, description, icon);
        this.action = action;
    }

    public BaseActionToolbar(String text, String description, Icon icon, Consumer<AnActionEvent> action, Function<AnActionEvent, Boolean> enableFunction) {
        super(text, description, icon);
        this.action = action;
        this.enableFunction = enableFunction;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (action != null) {
            action.accept(e);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(enableFunction != null ? enableFunction.apply(e) : true);
    }

}
