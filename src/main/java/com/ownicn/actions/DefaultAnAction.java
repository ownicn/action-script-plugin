package com.ownicn.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultAnAction extends AnAction {

    Supplier<String> dynamicText;

    final Consumer<AnActionEvent> actionEventConsumer;

    public DefaultAnAction(String text, Consumer<AnActionEvent> actionEventConsumer) {
        super(text);
        this.actionEventConsumer = actionEventConsumer;
    }

    public DefaultAnAction(String text, Icon icon, Consumer<AnActionEvent> actionEventConsumer) {
        super(() -> text, icon);
        this.actionEventConsumer = actionEventConsumer;
    }

    public DefaultAnAction(Supplier<String> dynamicText, Icon icon, Consumer<AnActionEvent> actionEventConsumer) {
        super(dynamicText, icon);
        this.dynamicText = dynamicText;
        this.actionEventConsumer = actionEventConsumer;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (dynamicText != null) {
            e.getPresentation().setText(dynamicText.get());
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (actionEventConsumer != null) {
            actionEventConsumer.accept(anActionEvent);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
