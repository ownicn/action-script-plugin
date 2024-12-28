package com.ownicn.io;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.project.Project;

public class ConsoleViewManager {
    private final Project project;
    private ConsoleView consoleView;
    private ProcessHandler processHandler;
    private final AnsiEscapeDecoder ansiEscapeDecoder = new AnsiEscapeDecoder();
    private static final String TOOL_WINDOW_TITLE = "Action Script";
    private boolean isConsoleShown = false;

    public ConsoleViewManager(Project project) {
        this.project = project;
        initializeConsoleView();
    }

    private void initializeConsoleView() {
        processHandler = new ConsoleProcessHandler(this);
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();

        if (consoleView instanceof ConsoleViewImpl) {
            ((ConsoleViewImpl) consoleView).setUpdateFoldingsEnabled(false);
        }

        consoleView.attachToProcess(processHandler);
        processHandler.startNotify();
    }

    public void showConsoleWindow() {
        if (!isConsoleShown) {
            isConsoleShown = true;
            RunContentDescriptor descriptor = new RunContentDescriptor(consoleView, processHandler, consoleView.getComponent(), TOOL_WINDOW_TITLE);
            descriptor.setActivateToolWindowWhenAdded(true);
            Executor executor = DefaultRunExecutor.getRunExecutorInstance();
            RunContentManager.getInstance(project).showRunContent(executor, descriptor);
        }
    }

    public void print(String message, boolean isError) {
        if (processHandler != null && message != null) {
            showConsoleWindow();
            ansiEscapeDecoder.escapeText(message, isError ? ProcessOutputTypes.STDERR : ProcessOutputTypes.STDOUT,
                    (text, key) -> processHandler.notifyTextAvailable(text, key));
        }
    }

    public void clear() {
        if (consoleView != null) {
            consoleView.clear();
        }
    }
}
