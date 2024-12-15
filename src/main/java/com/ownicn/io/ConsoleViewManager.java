package com.ownicn.io;

import com.ownicn.util.ProjectHolder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.Writer;

public class ConsoleViewManager {

    private final Project project;
    private ConsoleView consoleView;
    private static final String CONSOLE_WINDOW_ID = "Action Script Console";
    private static final ConsoleViewManager INSTANCE = new ConsoleViewManager(ProjectHolder.getProject());

    private final Writer stdoutWriter;

    public static ConsoleViewManager getInstance() {
        return INSTANCE;
    }

    private ConsoleViewManager(Project project) {
        this.project = project;
        initConsoleView();

        // 创建标准输出的 Writer
        stdoutWriter = new Writer() {
            @Override
            public void write(char @NotNull [] buff, int off, int len) {
                print(new String(buff, off, len));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        // 创建错误输出的 Writer
        Writer stderrWriter = new Writer() {
            @Override
            public void write(char @NotNull [] buff, int off, int len) {
                printErr(new String(buff, off, len));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        System.setOut(new PrintStream(new WriterOutputStream(stdoutWriter)));
        System.setErr(new PrintStream(new WriterOutputStream(stderrWriter)));
    }


    private void initConsoleView() {
        if (project != null && !project.isDisposed()) {
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            ToolWindow toolWindow = toolWindowManager.getToolWindow(CONSOLE_WINDOW_ID);

            if (toolWindow == null) {
                // 创建新的工具窗口，添加图标
                //noinspection deprecation
                toolWindow = toolWindowManager.registerToolWindow(CONSOLE_WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
                toolWindow.setIcon(AllIcons.Nodes.Console);

                // 创建控制台视图
                consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();

                // 创建内容
                Content content = ContentFactory.getInstance().createContent(consoleView.getComponent(), "", false);
                toolWindow.getContentManager().addContent(content);
            } else {
                // 获取已存在的控制台视图
                Content content = toolWindow.getContentManager().getSelectedContent();
                if (content != null) {
                    consoleView = (ConsoleView) content.getComponent();
                } else {
                    // 如果没有内容，创建新的控制台视图
                    consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
                    Content newContent = ContentFactory.getInstance().createContent(consoleView.getComponent(), "", false);
                    toolWindow.getContentManager().addContent(newContent);
                }
            }
        }
    }

    public void clear() {
        if (consoleView != null) {
            consoleView.clear();
        }
    }

    public void print(String message) {
        printToConsole(message, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public void printErr(String message) {
        printToConsole(message, ConsoleViewContentType.ERROR_OUTPUT);
    }

    public void printToConsole(String message, ConsoleViewContentType contentType) {
        if (project != null && !project.isDisposed() && consoleView != null) {
            consoleView.print(message, contentType);
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(CONSOLE_WINDOW_ID);
            if (toolWindow != null) {
                toolWindow.show();
            }
        }
    }

    public Writer getStdoutWriter() {
        return stdoutWriter;
    }

}
