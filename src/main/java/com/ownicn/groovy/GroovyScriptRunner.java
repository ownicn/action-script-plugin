package com.ownicn.groovy;

import com.intellij.openapi.project.Project;
import com.ownicn.extensions.BindingMaps;
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

        // 创建绑定对象
        Binding binding = new Binding();
        binding.setProperty("project", project);

        // 使用当前类加载器创建 GroovyShell
        ClassLoader classLoader = GroovyScriptRunner.class.getClassLoader();
        this.shell = new GroovyShell(classLoader, binding, config);
    }

    public void putVariable(String name, Object value) {
        shell.setVariable(name, value);
    }

    public void executeScript(String scriptContent) {
        PrintStream oldOut = System.out, oldErr = System.err;
        ConsoleViewManager consoleViewManager = new ConsoleViewManager(project);
        try {
            consoleViewManager.clear();
            
            // 重定向标准输出到 ConsoleView
            System.setOut(new PrintStream(new ConsoleOutputStream(consoleViewManager)));
            System.setErr(new PrintStream(new ConsoleOutputStream(consoleViewManager, true)));

            // 为每次执行创建新的 GroovyShell，以便使用不同的脚本名
            CompilerConfiguration config = new CompilerConfiguration();
            config.setSourceEncoding("UTF-8");

            Binding binding = shell.getContext();
            Map<String, Object> bindingMap = BindingMaps.INSTANCE.getBindingMap();
            for (Map.Entry<String, Object> variable : bindingMap.entrySet()) {
                binding.setVariable(variable.getKey(), variable.getValue());
            }
            binding.setVariable("project", project);

            GroovyShell newShell = new GroovyShell(GroovyScriptRunner.class.getClassLoader(), binding, config);
            Object result = newShell.parse(scriptContent, SCRIPT_NAME).run();
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