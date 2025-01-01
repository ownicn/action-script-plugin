package com.ownicn.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DefaultAnAction extends AnAction {

    final Consumer<AnActionEvent> actionEventConsumer;

    public DefaultAnAction(String text, Consumer<AnActionEvent> actionEventConsumer) {
        super(text);
        this.actionEventConsumer = actionEventConsumer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (actionEventConsumer != null) {
            actionEventConsumer.accept(anActionEvent);
        }
    }
}
