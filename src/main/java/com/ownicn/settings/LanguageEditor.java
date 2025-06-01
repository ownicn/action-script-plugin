package com.ownicn.settings;

import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class LanguageEditor extends JPanel implements Disposable {
    private final EditorTextField editor;
    private final Project project;
    private EditorEx editorEx;

    public LanguageEditor(Project project, LanguageSupports languageSupport) {
        super(new BorderLayout());
        this.project = project;

        Language language = languageSupport == LanguageSupports.Groovy ? GroovyLanguage.INSTANCE : PlainTextLanguage.INSTANCE;
        // 创建编辑器
        editor = new LanguageTextField(language, project, "", false) {
            @Override
            protected @NotNull EditorEx createEditor() {
                editorEx = super.createEditor();

                // 获取 IDEA 的全局编辑器配色方案
                EditorColorsScheme colorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
                editorEx.setColorsScheme(colorsScheme);
                editorEx.setBackgroundColor(null);

                // 设置语法高亮
                editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(getFileType(), colorsScheme, project));

                // 配置基本编辑器设置
                EditorSettings settings = editorEx.getSettings();
                settings.setLineNumbersShown(true);
                settings.setIndentGuidesShown(true);
                settings.setFoldingOutlineShown(true);
                settings.setUseSoftWraps(false);

                // 显示滚动条
                editorEx.setVerticalScrollbarVisible(true);
                editorEx.setHorizontalScrollbarVisible(true);

                return editorEx;
            }
        };

        // 设置编辑器的首选大小
        editor.setPreferredSize(new Dimension(600, 400));

        // 添加编辑器到面板
        add(editor, BorderLayout.CENTER);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (editor != null) {
            editor.setEnabled(enabled);
            if (editorEx != null) {
                editorEx.setViewer(!enabled);
                // 更新编辑器的背景色
                if (enabled) {
                    editorEx.setBackgroundColor(null);
                } else {
                    editorEx.setBackgroundColor(UIUtil.getPanelBackground());
                }
            }
        }
    }

    public void setText(String text) {
        editor.setText(text);
        // 清空撤销历史
        ((UndoManagerImpl)UndoManager.getInstance(project)).dropHistoryInTests();
    }

    public String getText() {
        return editor.getText();
    }

    public EditorTextField getEditor() {
        return editor;
    }

    @Override
    public void dispose() {
        if (editorEx != null && !editorEx.isDisposed()) {
            EditorFactory.getInstance().releaseEditor(editorEx);
            editorEx = null;
        }
        removeAll();
    }
}
