package com.ownicn.groovy;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.ownicn.extensions.BindingMaps;
import com.ownicn.extensions.impl.DefaultClipboard;
import com.ownicn.extensions.impl.DefaultSelection;
import com.ownicn.io.ConsoleOutputStream;
import com.ownicn.io.ConsoleViewManager;
import com.ownicn.io.ScriptErrorHandler;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.PrintStream;
import java.util.Map;

public class GroovyScriptRunner {
    private final GroovyShell shell;
    private final Project project;
    public static final String SCRIPT_NAME = "ActionScript";

    public GroovyScriptRunner(Project project) {
        this.project = project;

        // 创建编译器配置
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");

        // 使用当前类加载器创建 GroovyShell
        ClassLoader classLoader = GroovyScriptRunner.class.getClassLoader();
        this.shell = new GroovyShell(classLoader, new Binding(), config);
    }

    public void setVariable(String name, Object value) {
        shell.setVariable(name, value);
    }

    public GroovyScriptRunner additionalCapabilities(AnActionEvent actionEvent) {
        Binding binding = this.shell.getContext();
        Map<String, Object> bindingMap = BindingMaps.create(project).getBindingMap();
        for (Map.Entry<String, Object> variable : bindingMap.entrySet()) {
            binding.setVariable(variable.getKey(), variable.getValue());
        }
        this.setVariable("CLIPBOARD", new DefaultClipboard());
        this.setVariable("SELECTION", new DefaultSelection(actionEvent));
        this.setVariable("PROJECT", new com.ownicn.extensions.Project(project));
        return this;
    }

    public void executeScript(String scriptContent) {
        PrintStream oldOut = System.out, oldErr = System.err;
        ConsoleViewManager consoleViewManager = new ConsoleViewManager(project);
        try {
            consoleViewManager.clear();

            // 重定向标准输出到 ConsoleView
            System.setOut(new PrintStream(new ConsoleOutputStream(consoleViewManager)));
            System.setErr(new PrintStream(new ConsoleOutputStream(consoleViewManager, true)));

            Object result = shell.parse(scriptContent, SCRIPT_NAME).run();
            if (result != null && scriptContent.trim().matches("(?s).*\\breturn\\b.*$")) {
                consoleViewManager.print(result + "\n", false);
            }
        } catch (Exception e) {
            String errorMessage = ScriptErrorHandler.formatError(e, scriptContent);
            consoleViewManager.print(errorMessage, true);
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }

}