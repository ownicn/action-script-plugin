package com.ownicn.settings;

import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.ownicn.actions.DefaultAnAction;
import com.ownicn.groovy.GroovyScriptRunner;
import com.ownicn.util.EditorActionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyLanguage;

import javax.swing.*;
import java.awt.*;

public class LanguageEditor extends JPanel implements Disposable, DataProvider {
    private final EditorTextField editor;
    private final Project project;
    private EditorEx editorEx;
    private String lastSavedContent;

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

        DefaultActionGroup actionGroup = getDefaultActionGroup();
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ActionScriptEditor", actionGroup, true);
        toolbar.setTargetComponent(this);

        // 设置编辑器的首选大小
        editor.setPreferredSize(new Dimension(600, 400));

        // 添加工具栏和编辑器到面板
        JPanel topPanel = JBUI.Panels.simplePanel().addToLeft(toolbar.getComponent());
        add(topPanel, BorderLayout.NORTH);
        add(editor, BorderLayout.CENTER);
        
        // 保存初始内容
        lastSavedContent = "";
    }

    private @NotNull DefaultActionGroup getDefaultActionGroup() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new RunScriptAction("Run selected", "Run the selected code snippet", AllIcons.Actions.Execute));
        actionGroup.add(new RunScriptAction("Run Script", "Execute the current script", AllIcons.Actions.RunAll, false));
        actionGroup.addSeparator();

        actionGroup.add(new DefaultAnAction("Comment", AllIcons.Actions.InlayRenameInComments,
                event -> EditorActionUtil.toggleLineComment(editorEx, editorEx.getCaretModel().getCurrentCaret())));

        actionGroup.add(new DefaultAnAction("Revert", AllIcons.Diff.Revert, event -> {
            if (!editor.getText().equals(lastSavedContent)) {
                editor.setText(lastSavedContent);
            }
        }));

        actionGroup.add(new DefaultAnAction(() -> editorEx.getCaretModel().getCurrentCaret().hasSelection() ? "Reformat selected text" : "Reformat Code",
                AllIcons.Actions.ReformatCode, event -> EditorActionUtil.reformatCode(project, editorEx)));

        return actionGroup;
    }

    static class RunScriptAction extends AnAction {
        private boolean snippets = true;

        public RunScriptAction(String text, String description, Icon icon) {
            super(text, description, icon);
        }

        public RunScriptAction(String text, String description, Icon icon, boolean snippets) {
            super(text, description, icon);
            this.snippets = snippets;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (editor != null) {
                Caret caret = editor.getCaretModel().getCurrentCaret();
                String scriptContent = snippets && caret.hasSelection() ? caret.getSelectedText() : editor.getDocument().getText();

                GroovyScriptRunner scriptRunner = new GroovyScriptRunner(e.getProject());
                scriptRunner.additionalCapabilities(e);
                scriptRunner.executeScript(scriptContent);
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(true);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (editor != null) {
            editor.setEnabled(enabled);
            if (editorEx != null) {
                editorEx.setViewer(!enabled);
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
        lastSavedContent = text;
        ((UndoManagerImpl) UndoManager.getInstance(project)).dropHistoryInTests();
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

    @Override
    public @Nullable Object getData(@NotNull String dataId) {
        if (CommonDataKeys.EDITOR.is(dataId) && editorEx != null) {
            return editorEx;
        }
        if (CommonDataKeys.PROJECT.is(dataId)) {
            return project;
        }
        if (CommonDataKeys.CARET.is(dataId) && editorEx != null) {
            return editorEx.getCaretModel().getCurrentCaret();
        }
        return null;
    }
}
