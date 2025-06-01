package com.ownicn.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.ownicn.settings.LanguageEditor;
import org.jetbrains.annotations.NotNull;

public class EditorActionUtil {

    private static final String LINE_COMMENT = "//";

    public static void toggleLineComment(@NotNull Editor editor, @NotNull Caret caret) {
        Project project = editor.getProject();
        if (project == null) return;

        Document document = editor.getDocument();
        WriteCommandAction.runWriteCommandAction(project, "Toggle Line Comment", null, () -> {
            if (!caret.hasSelection()) {
                // 如果没有选择，处理当前行
                int lineNumber = document.getLineNumber(caret.getOffset());
                toggleSingleLineComment(document, lineNumber);
            } else {
                // 处理选中的多行
                int startLine = document.getLineNumber(caret.getSelectionStart());
                int endLine = document.getLineNumber(caret.getSelectionEnd());

                // 检查所有选中的行是否都已经被注释
                boolean allCommented = true;
                for (int i = startLine; i <= endLine; i++) {
                    if (!isLineCommented(document, i)) {
                        allCommented = false;
                        break;
                    }
                }

                // 根据检查结果，统一添加或删除注释
                for (int i = startLine; i <= endLine; i++) {
                    if (allCommented) {
                        removeLineComment(document, i);
                    } else {
                        addLineComment(document, i);
                    }
                }
            }
        });
    }

    private static void toggleSingleLineComment(Document document, int lineNumber) {
        if (isLineCommented(document, lineNumber)) {
            removeLineComment(document, lineNumber);
        } else {
            addLineComment(document, lineNumber);
        }
    }

    private static boolean isLineCommented(Document document, int lineNumber) {
        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);
        String lineText = document.getText(new TextRange(lineStart, lineEnd)).trim();
        return lineText.startsWith(LINE_COMMENT);
    }

    private static void addLineComment(Document document, int lineNumber) {
        int lineStart = document.getLineStartOffset(lineNumber);
        // int indent = getLineIndent(document, lineNumber);
        document.insertString(lineStart, LINE_COMMENT + " ");
    }

    private static void removeLineComment(Document document, int lineNumber) {
        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);
        String lineText = document.getText(new TextRange(lineStart, lineEnd));

        int commentStart = lineText.indexOf(LINE_COMMENT);
        if (commentStart >= 0) {
            // 删除注释符号和后面的一个空格（如果有）
            int deleteEnd = commentStart + LINE_COMMENT.length();
            if (deleteEnd < lineText.length() && lineText.charAt(deleteEnd) == ' ') {
                deleteEnd++;
            }
            document.deleteString(lineStart + commentStart, lineStart + deleteEnd);
        }
    }

    private static int getLineIndent(Document document, int lineNumber) {
        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);
        String lineText = document.getText(new TextRange(lineStart, lineEnd));

        int indent = 0;
        while (indent < lineText.length() && Character.isWhitespace(lineText.charAt(indent))) {
            indent++;
        }
        return indent;
    }

    public static void reformatCode(Project project, EditorEx editorEx) {
        if (project == null || editorEx == null) return;

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editorEx.getDocument());

        if (psiFile != null) {
            WriteCommandAction.runWriteCommandAction(project, "Reformat Code", null, () -> {
                try {
                    CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
                    Caret caret = editorEx.getCaretModel().getCurrentCaret();

                    if (caret.hasSelection()) {
                        // 格式化选中的文本范围
                        int start = caret.getSelectionStart();
                        int end = caret.getSelectionEnd();
                        codeStyleManager.reformatRange(psiFile, start, end);
                    } else {
                        // 格式化整个文件
                        codeStyleManager.reformat(psiFile);
                    }
                } catch (Exception e) {
                    // 忽略格式化异常
                }
            });
        }
    }
} 