package com.ownicn.extensions.impl;

import com.ownicn.extensions.Selection;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;

public class DefaultSelection implements Selection {

    AnActionEvent event;

    public DefaultSelection(AnActionEvent event) {
        this.event = event;
    }

    @Override
    public String getText() {
        Caret caret = event.getData(PlatformDataKeys.CARET);
        if (caret != null && caret.hasSelection()) {
            return caret.getSelectedText();
        }
        if (caret == null) {
            return "";
        }

        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            Document document = editor.getDocument();
            return getAlphanumericTextInLine(document, caret.getOffset());
        }
        return "";
    }

    @Override
    public String getModule() {
        Module module = event.getData(LangDataKeys.MODULE);
        if (module != null) {
            return module.getName();
        }
        return "";
    }

    /**
     * 获取光标所在行内连续的非空字符文本
     *
     * @param document 文档对象
     * @param offset   光标位置
     * @return 当前行内的连续非空字符文本
     */
    private String getAlphanumericTextInLine(Document document, int offset) {
        String text = document.getText();
        int textLength = text.length();

        if (offset < 0 || offset >= textLength) {
            return "";
        }

        // 获取光标所在行的范围
        int lineNumber = document.getLineNumber(offset);
        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);

        int start = offset;
        while (start > lineStart && Character.isLetterOrDigit(text.charAt(start - 1))) {
            start--;
        }

        int end = offset;
        while (end < lineEnd && Character.isLetterOrDigit(text.charAt(end))) {
            end++;
        }

        if (start == end) {
            return "";
        }

        return text.substring(start, end);
    }
}
