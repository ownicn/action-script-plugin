package com.ownicn.io;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.ownicn.groovy.GroovyScriptRunner.SCRIPT_NAME;

public class ScriptErrorHandler {
    private record ErrorLocation(int lineNumber, int columnNumber) {
    }

    public static String formatError(Exception e, String scriptContent) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Error: ").append(e.getMessage());

        // 获取错误位置
        ErrorLocation errorLocation;

        if (e instanceof MissingPropertyException) {
            errorLocation = handleMissingPropertyException((MissingPropertyException) e, scriptContent, errorMsg);
        } else if (e instanceof MissingMethodException mme) {
            errorLocation = handleMissingMethodException(mme, scriptContent, errorMsg);
        } else {
            errorLocation = handleGenericException(e, errorMsg);
        }

        // 添加错误上下文
        addErrorContext(scriptContent, errorMsg, errorLocation);

        // 添加堆栈跟踪
        errorMsg.append(sw);
        return errorMsg.toString();
    }

    private static ErrorLocation handleMissingPropertyException(MissingPropertyException e,
                                                                String scriptContent, StringBuilder errorMsg) {
        String className = e.getMessage().replaceAll(".*class: (\\S+).*", "$1");
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().contains(className)) {
                int lineNumber = element.getLineNumber();
                String line = scriptContent.split("\n")[lineNumber - 1];
                String propertyName = e.getMessage().split(":")[1].trim().split(" ")[0];
                int columnNumber = line.indexOf(propertyName) + 1;
                errorMsg.append(" at line ").append(lineNumber)
                        .append(", column ").append(columnNumber)
                        .append(" in script\n");
                return new ErrorLocation(lineNumber, columnNumber);
            }
        }
        return new ErrorLocation(-1, -1);
    }

    private static ErrorLocation handleMissingMethodException(MissingMethodException mme,
                                                              String scriptContent, StringBuilder errorMsg) {
        for (StackTraceElement element : mme.getStackTrace()) {
            if (element.getFileName() != null && element.getFileName().contains(SCRIPT_NAME)) {
                int lineNumber = element.getLineNumber();
                String line = scriptContent.split("\n")[lineNumber - 1];
                String methodName = mme.getMethod();
                int columnNumber = line.indexOf(methodName) + 1;
                errorMsg.append(" at line ").append(lineNumber)
                        .append(", column ").append(columnNumber)
                        .append(" in script\n");
                return new ErrorLocation(lineNumber, columnNumber);
            }
        }
        return new ErrorLocation(-1, -1);
    }

    private static ErrorLocation handleGenericException(Exception e, StringBuilder errorMsg) {
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getFileName() != null && element.getFileName().contains(SCRIPT_NAME)) {
                int lineNumber = element.getLineNumber();
                errorMsg.append(" at line ").append(lineNumber).append(" in script\n");
                return new ErrorLocation(lineNumber, -1);
            }
        }
        return new ErrorLocation(-1, -1);
    }

    private static void addErrorContext(String scriptContent, StringBuilder errorMsg, ErrorLocation errorLocation) {
        int lineNumber = errorLocation.lineNumber;
        int columnNumber = errorLocation.columnNumber;
        if (lineNumber > 0) {
            String[] scriptLines = scriptContent.split("\n");
            if (lineNumber <= scriptLines.length) {
                if (lineNumber > 1) {
                    errorMsg.append(scriptLines[lineNumber - 2]).append("\n");
                }
                errorMsg.append(scriptLines[lineNumber - 1]).append("\n");
                if (columnNumber > 0) {
                    errorMsg.append(" ".repeat(columnNumber - 1)).append("^\n");
                } else {
                    errorMsg.append("^\n");
                }
                if (lineNumber < scriptLines.length) {
                    errorMsg.append(scriptLines[lineNumber]).append("\n");
                }
            }
        }
    }
} 