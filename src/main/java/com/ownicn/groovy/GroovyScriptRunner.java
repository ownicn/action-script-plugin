package com.ownicn.groovy;

import com.ownicn.extensions.BindingMaps;
import com.ownicn.io.ConsoleViewManager;
import com.ownicn.io.ScriptErrorHandler;
import com.intellij.openapi.project.Project;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.PrintWriter;
import java.util.Map;

public class GroovyScriptRunner {
    private final GroovyShell shell;
    private final Project project;
    public static final String SCRIPT_NAME = "ActionScript";
    ConsoleViewManager consoleViewManager = ConsoleViewManager.getInstance();

    public GroovyScriptRunner(Project project) {
        this.project = project;

        // 创建编译器配置
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");

        // 创建绑定对象
        Binding binding = new Binding();
        binding.setProperty("project", project);
        binding.setProperty("out", new PrintWriter(consoleViewManager.getStdoutWriter(), true));

        // 使用当前类加载器创建 GroovyShell
        ClassLoader classLoader = GroovyScriptRunner.class.getClassLoader();
        this.shell = new GroovyShell(classLoader, binding, config);
    }


    public void putVariable(String name, Object value) {
        shell.setVariable(name, value);
    }

    public Object executeScript(String scriptContent) {
        try {
            consoleViewManager.clear();
            // 为每次执行创建新的 GroovyShell，以便使用不同的脚本名
            CompilerConfiguration config = new CompilerConfiguration();
            config.setSourceEncoding("UTF-8");

            Binding binding = shell.getContext();
            Map<String, Object> bindingMap = BindingMaps.INSTANCE.getBindingMap();
            for (Map.Entry<String, Object> variable : bindingMap.entrySet()) {
                binding.setVariable(variable.getKey(), variable.getValue());
            }
            binding.setVariable("project", project);
            binding.setProperty("out", shell.getContext().getProperty("out"));

            GroovyShell newShell = new GroovyShell(GroovyScriptRunner.class.getClassLoader(), binding, config);
            return newShell.parse(scriptContent, SCRIPT_NAME).run();
        } catch (Exception e) {
            String errorMessage = ScriptErrorHandler.formatError(e, scriptContent);
            consoleViewManager.printErr(errorMessage);
        }
        return null;
    }
} 