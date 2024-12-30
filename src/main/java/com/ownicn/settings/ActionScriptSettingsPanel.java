package com.ownicn.settings;


import com.ownicn.settings.ActionScriptSettings.ScriptEntry;
import com.ownicn.util.ProjectHolder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;

public class ActionScriptSettingsPanel {
    private JPanel mainPanel;
    private JBList<ScriptEntry> scriptsList;
    private LanguageEditor editorTextField;
    private JTextField nameField;
    private JComboBox<LanguageSupports> languageComboBox;
    private DefaultListModel<ScriptEntry> listModel;
    private boolean isModified = false;
    private boolean isUpdatingUI = false;
    private final Map<ScriptEntry, ScriptEntry> modifiedEntries = new HashMap<>();

    public ActionScriptSettingsPanel() {
    }

    public void initComponents() {
        // 获取当前活动的项目
        Project project = ProjectHolder.getProject();

        mainPanel = new JPanel(new BorderLayout());

        // 创建工具栏
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.add(createToolbar().getComponent(), BorderLayout.WEST);

        // 左侧面板：设置最小和首选宽度
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension(185, size.height);
            }

            @Override
            public Dimension getMinimumSize() {
                // 设置最小宽度为150
                Dimension size = super.getMinimumSize();
                return new Dimension(150, size.height);
            }
        };

        // 脚本列表
        scriptsList = new JBList<>();
        listModel = new DefaultListModel<>();
        scriptsList.setModel(listModel);
        scriptsList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scriptsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ScriptEntry) {
                    setText(((ScriptEntry) value).getName());
                }
                return this;
            }
        });

        leftPanel.add(new JBScrollPane(scriptsList), BorderLayout.CENTER);

        // 右侧面板：使用 BorderLayout
        JPanel rightPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 0);
            }
        };

        // 创建元数据编辑面板，使用固定高度
        JPanel metaPanel = new JPanel(new GridBagLayout());
        metaPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        // 设置固定高度
        metaPanel.setPreferredSize(new Dimension(-1, 40));
        metaPanel.setMinimumSize(new Dimension(-1, 40));
        metaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(2);

        // 名称标签和输入框
        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        metaPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        metaPanel.add(nameField, gbc);

        // 类型标签和下拉框
        JLabel typeLabel = new JLabel("Language:");
        languageComboBox = new ComboBox<>(new LanguageSupports[]{LanguageSupports.Groovy});

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(2, 8, 2, 2); // 左边增加间距
        metaPanel.add(typeLabel, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.5;
        metaPanel.add(languageComboBox, gbc);

        // 添加编辑器
        editorTextField = new LanguageEditor(project, LanguageSupports.Groovy);

        // 创建一个面板来容纳编辑器，使用 BorderLayout
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(editorTextField, BorderLayout.CENTER);

        // 组装右侧面板
        rightPanel.add(metaPanel, BorderLayout.NORTH);
        rightPanel.add(editorPanel, BorderLayout.CENTER);

        // 使用分隔面板，设置初始比例为0.2（20%）
        Splitter splitter = new Splitter(false, 0.22f);
        splitter.setFirstComponent(new JBScrollPane(scriptsList));
        splitter.setSecondComponent(rightPanel);

        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        mainPanel.add(splitter, BorderLayout.CENTER);

        // 添加监听器
        setupListeners();
        
        // 加载设置并确保选中第一项
        loadSettings();
    }

    private ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new BaseActionToolbar("Add", "Add new action", AllIcons.General.Add, (e) -> addNewScript()));
        Function<AnActionEvent, Boolean> hasSelected = (e) -> scriptsList != null && !scriptsList.isSelectionEmpty();
        group.add(new BaseActionToolbar("Remove", "Remove selected action", AllIcons.General.Remove,
                (e) -> removeSelectedScript(), hasSelected));
        group.add(new BaseActionToolbar("Copy", "Copy selected action", AllIcons.Actions.Copy,
                (e) -> copySelectedScript(), hasSelected));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ActionScriptToolbar", group, true);
        toolbar.setTargetComponent(scriptsList);

        return toolbar;
    }

    private void addNewScript() {
        ScriptEntry newEntry = new ScriptEntry("New Action", "", LanguageSupports.Groovy);
        listModel.addElement(newEntry);
        // 确保新添加的项被选中
        scriptsList.setSelectedValue(newEntry, true);
        isModified = true;
        // 请求焦点到名称字段
        nameField.requestFocus();
        nameField.selectAll();  // 选中全部文本以便直接编辑
    }

    private void removeSelectedScript() {
        int selectedIndex = scriptsList.getSelectedIndex();
        List<ScriptEntry> selectedValuesList = scriptsList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty()) {
            selectedValuesList.forEach(listModel::removeElement);
            scriptsList.setSelectedIndex(Math.min(selectedIndex, listModel.size() - 1));
            isModified = true;
        }
    }

    private void copySelectedScript() {
        ScriptEntry selected = scriptsList.getSelectedValue();
        if (selected != null) {
            ScriptEntry copy = new ScriptEntry(selected.getName() + " (copy)", selected.getContent(), selected.getLanguage());
            listModel.addElement(copy);
            scriptsList.setSelectedValue(copy, true);
            nameField.requestFocus();
            isModified = true;
        }
    }

    private void setupListeners() {
        // 列表选择监听器
        scriptsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateEditor4Selected();
                updateControlsEditableState();
            }
        });

        // 编辑器文本变化监听器
        editorTextField.getEditor().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (!isUpdatingUI) {
                    String newText = event.getDocument().getText();
                    updateScriptFromEditor(entry -> {
                        // 创建或获取临时副本
                        ScriptEntry tempEntry = modifiedEntries.computeIfAbsent(entry, 
                            original -> new ScriptEntry(original.getName(), original.getContent(), original.getLanguage()));
                        tempEntry.setContent(newText);
                    });
                    isModified = true;
                }
            }
        });

        javax.swing.event.DocumentListener textChangeListener = new javax.swing.event.DocumentListener() {
            private void handleUpdate(javax.swing.event.DocumentEvent e) {
                if (!isUpdatingUI) {
                    try {
                        String newText = e.getDocument().getText(0, e.getDocument().getLength());
                        if (e.getDocument() == nameField.getDocument()) {
                            updateScriptFromEditor(entry -> {
                                // 创建或获取临时副本
                                ScriptEntry tempEntry = modifiedEntries.computeIfAbsent(entry,
                                    original -> new ScriptEntry(original.getName(), original.getContent(), original.getLanguage()));
                                tempEntry.setName(newText);
                            });
                        }
                        isModified = true;
                    } catch (BadLocationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                handleUpdate(e);
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                handleUpdate(e);
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        };

        nameField.getDocument().addDocumentListener(textChangeListener);
    }

    // 从编辑器更新脚本内容
    private void updateScriptFromEditor(Consumer<ScriptEntry> entry) {
        ScriptEntry selected = scriptsList.getSelectedValue();
        if (selected != null) {
            entry.accept(selected);
            scriptsList.repaint();
        }
    }

    // 从脚本更新编辑器内容
    private void updateEditor4Selected() {
        isUpdatingUI = true;
        try {
            ScriptEntry selected = scriptsList.getSelectedValue();
            if (selected != null) {
                // 显示临时修改的内容（如果有）
                ScriptEntry tempEntry = modifiedEntries.get(selected);
                if (tempEntry != null) {
                    nameField.setText(tempEntry.getName());
                    editorTextField.setText(tempEntry.getContent());
                    languageComboBox.setSelectedItem(tempEntry.getLanguage());
                } else {
                    nameField.setText(selected.getName());
                    editorTextField.setText(selected.getContent());
                    languageComboBox.setSelectedItem(selected.getLanguage());
                }
                nameField.requestFocus();
            } else {
                nameField.setText("");
                editorTextField.setText("");
                languageComboBox.setSelectedItem(LanguageSupports.Groovy);
            }
        } finally {
            isUpdatingUI = false;
        }
    }

    public boolean isModified() {
        return isModified;
    }

    public void apply() {
        ActionScriptSettings settings = ActionScriptSettings.getInstance();
        if (settings.getState() != null) {
            // 应用所有修改
            for (Map.Entry<ScriptEntry, ScriptEntry> entry : modifiedEntries.entrySet()) {
                ScriptEntry original = entry.getKey();
                ScriptEntry modified = entry.getValue();
                original.setName(modified.getName());
                original.setContent(modified.getContent());
                original.setLanguage(modified.getLanguage());
            }
            
            // 清空修改记录
            modifiedEntries.clear();
            
            // 保存到设置
            settings.getState().getScripts().clear();
            for (int i = 0; i < listModel.size(); i++) {
                settings.getState().getScripts().add(listModel.getElementAt(i));
            }
        }
        isModified = false;
    }

    public void reset() {
        listModel.clear();
        modifiedEntries.clear();
        ActionScriptSettings settings = ActionScriptSettings.getInstance();
        if (settings.getState() != null) {
            for (ScriptEntry entry : settings.getState().getScripts()) {
                listModel.addElement(entry);
            }
        }
        isModified = false;
        updateEditor4Selected();
    }

    public void loadSettings() {
        ActionScriptSettings settings = ActionScriptSettings.getInstance();
        listModel.clear();
        modifiedEntries.clear();
        
        if (settings.getState() != null) {
            for (ScriptEntry entry : settings.getState().getScripts()) {
                listModel.addElement(entry);
            }
        }
        // 确保在加载设置后更新控件状态
        updateControlsEditableState();
        // 如果列表为空，确保添加按钮可用
        if (listModel.isEmpty()) {
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    private void updateControlsEditableState() {
        boolean hasSelection = scriptsList != null && !scriptsList.isSelectionEmpty();
        nameField.setEnabled(hasSelection);
        editorTextField.setEnabled(hasSelection);
        languageComboBox.setEnabled(hasSelection);

        // 如果有选中项但没有设置选中索引，则设置第一项为选中
        if (scriptsList.getSelectedIndex() == -1) {
            scriptsList.setSelectedIndex(0);
        }
    }

    public JComponent getPanel() {
        return mainPanel;
    }

    public void dispose() {
        if (editorTextField != null) {
            Disposer.dispose(editorTextField);
            editorTextField = null;
        }
    }
}