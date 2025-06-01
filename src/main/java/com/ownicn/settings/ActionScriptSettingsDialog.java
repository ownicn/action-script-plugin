package com.ownicn.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.DimensionService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ActionScriptSettingsDialog extends DialogWrapper {
    private final Project project;
    private final ActionScriptSettingsPanel settingsPanel;
    private static final String DIMENSION_KEY = "#com.ownicn.settings.ActionScriptSettingsDialog";
    private static final Dimension DEFAULT_SIZE = new Dimension(1000, 600);
    private final Action applyAction;

    public ActionScriptSettingsDialog(Project project) {
        super(project, true, IdeModalityType.MODELESS);
        this.project = project;
        settingsPanel = new ActionScriptSettingsPanel();
        settingsPanel.initComponents();

        // 创建 Apply 按钮
        applyAction = new AbstractAction("Apply") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doApplyAction();
            }
        };

        init();
        setTitle("Action Script Settings");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(settingsPanel.getPanel(), BorderLayout.CENTER);
        dialogPanel.setMinimumSize(new Dimension(800, 500));
        return dialogPanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction(), getCancelAction(), applyAction};
    }

    @Override
    protected void doOKAction() {
        settingsPanel.apply();
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        settingsPanel.reset();
        super.doCancelAction();
    }

    protected void doApplyAction() {
        settingsPanel.apply();
        settingsPanel.getPanel().repaint();
    }

    @Override
    protected String getDimensionServiceKey() {
        return DIMENSION_KEY;
    }

    @Nullable
    @Override
    public Point getInitialLocation() {
        Point storedLocation = DimensionService.getInstance().getLocation(DIMENSION_KEY, project);
        if (storedLocation == null) {
            Window window = getWindow();
            if (window != null) {
                window.setSize(DEFAULT_SIZE);
                return getLocationRelativeToParent();
            }
        }
        return storedLocation;
    }

    @Override
    public Dimension getInitialSize() {
        Dimension storedSize = DimensionService.getInstance().getSize(DIMENSION_KEY, project);
        return storedSize != null ? storedSize : DEFAULT_SIZE;
    }

    private Point getLocationRelativeToParent() {
        Window window = getWindow();
        if (window != null && window.getParent() != null) {
            Point parentLocation = window.getParent().getLocation();
            Dimension parentSize = window.getParent().getSize();
            Dimension windowSize = window.getSize();

            int x = parentLocation.x + (parentSize.width - windowSize.width) / 2;
            int y = parentLocation.y + (parentSize.height - windowSize.height) / 2;

            return new Point(x, y);
        }
        return null;
    }

    @Override
    public void dispose() {
        settingsPanel.dispose();
        super.dispose();
    }
} 