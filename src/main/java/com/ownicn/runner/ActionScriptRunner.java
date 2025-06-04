package com.ownicn.runner;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.ownicn.extensions.BindingMaps;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActionScriptRunner implements Disposable {
    private final Project project;
    private final ExecutorService executorService;
    private final ConsoleView consoleView;
    private final GroovyShell shell;
    public static final String SCRIPT_NAME = "ActionScript";
    private volatile boolean isConsoleShown = false;
    private final AnsiEscapeDecoder ansiEscapeDecoder = new AnsiEscapeDecoder();

    public ActionScriptRunner(Project project) {
        this.project = project;
        this.executorService = Executors.newSingleThreadExecutor();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");

        ClassLoader classLoader = ActionScriptRunner.class.getClassLoader();
        this.shell = new GroovyShell(classLoader, new Binding(), config);
        this.consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    }

    private void setupConsoleView() {
        if (!isConsoleShown) {
            isConsoleShown = true;
            RunContentDescriptor descriptor = new RunContentDescriptor(consoleView, null, consoleView.getComponent(), "Groovy Script");
            descriptor.setActivateToolWindowWhenAdded(true);
            Executor executor = DefaultRunExecutor.getRunExecutorInstance();
            RunContentManager.getInstance(project).showRunContent(executor, descriptor);
        }
    }

    public void additionalCapabilities(AnActionEvent actionEvent) {
        Binding binding = this.shell.getContext();
        Map<String, Object> bindingMap = BindingMaps.create(project, actionEvent).getBindingMap();
        for (Map.Entry<String, Object> variable : bindingMap.entrySet()) {
            binding.setVariable(variable.getKey(), variable.getValue());
        }
    }

    public void executeScript(String scriptContent) {
        executorService.submit(() -> {
            consoleView.clear();
            PrintStream originalOut = System.out, originalErr = System.err;
            PrintStream stdoutStream = createConsolePrintStream(ConsoleViewContentType.NORMAL_OUTPUT);
            PrintStream stderrStream = createConsolePrintStream(ConsoleViewContentType.ERROR_OUTPUT);

            try (stdoutStream; stderrStream) {
                System.setOut(stdoutStream);
                System.setErr(stderrStream);

                Object result = shell.evaluate(scriptContent, SCRIPT_NAME);
                if (result != null && scriptContent.trim().matches("(?s).*\\breturn\\b.*$")) {
                    consoleView.print(result + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
                }

            } catch (Exception e) {
                /// String errorMessage = ScriptErrorHandler.formatError(e, scriptContent);
                /// printToConsole(errorMessage, ConsoleViewContentType.ERROR_OUTPUT);

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                printToConsole(sw.toString(), ConsoleViewContentType.ERROR_OUTPUT);
            } finally {
                // 恢复标准输出和错误输出
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        });
    }

    private PrintStream createConsolePrintStream(ConsoleViewContentType contentType) {
        return new PrintStream(new OutputStream() {
            private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                buffer.write(b);
                if (b == '\n') {
                    flush();
                }
            }

            @Override
            public void flush() {
                if (buffer.size() > 0) {
                    printToConsole(buffer.toString(StandardCharsets.UTF_8), contentType);
                    buffer.reset();
                }
            }

            @Override
            public void close() {
                flush();
            }
        }, true, StandardCharsets.UTF_8);
    }

    private void printToConsole(String message, ConsoleViewContentType contentType) {
        ApplicationManager.getApplication().invokeLater(() -> {
            this.setupConsoleView();
            Key<?> outputType;
            if (contentType == ConsoleViewContentType.ERROR_OUTPUT) {
                outputType = ProcessOutputTypes.STDERR;
            } else if (contentType == ConsoleViewContentType.SYSTEM_OUTPUT) {
                outputType = ProcessOutputTypes.SYSTEM;
            } else {
                outputType = ProcessOutputTypes.STDOUT;
            }

            ansiEscapeDecoder.escapeText(message, outputType, (text, key) -> {
                consoleView.print(text, ConsoleViewContentType.getConsoleViewType(key));
            });
        });
    }

    @Override
    public void dispose() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (consoleView != null) {
            Disposer.dispose(consoleView);
        }
    }
}