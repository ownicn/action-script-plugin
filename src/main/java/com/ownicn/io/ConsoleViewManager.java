package com.ownicn.io;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.project.Project;

import static com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;

public class ConsoleViewManager {
    private final Project project;
    private ConsoleView consoleView;
    private static final String TOOL_WINDOW_TITLE = "Action Script";

    public ConsoleViewManager(Project project) {
        this.project = project;
        initializeConsoleView();
    }

    private void initializeConsoleView() {
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        RunContentDescriptor descriptor = new RunContentDescriptor(
                consoleView,
                null,
                consoleView.getComponent(),
                TOOL_WINDOW_TITLE
        );
        descriptor.setActivateToolWindowWhenAdded(true);
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        RunContentManager.getInstance(project).showRunContent(executor, descriptor);
    }

    public void print(String message) {
        print(message, false);
    }

    public void printErr(String message) {
        print(message, true);
    }

    public void print(String message, boolean isError) {
        if (consoleView != null) {
            consoleView.print(message, isError ? ERROR_OUTPUT : NORMAL_OUTPUT);
        }
    }

    public void clear() {
        if (consoleView != null) {
            consoleView.clear();
        }
    }
}
