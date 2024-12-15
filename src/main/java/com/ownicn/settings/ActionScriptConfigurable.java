package com.ownicn.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

public class ActionScriptConfigurable implements Configurable {
    private final ActionScriptSettingsPanel settingsPanel;

    public ActionScriptConfigurable() {
        settingsPanel = new ActionScriptSettingsPanel();
        settingsPanel.initComponents();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Action Script";
    }

    @Override
    public JComponent createComponent() {
        return settingsPanel.getPanel();
    }

    @Override
    public boolean isModified() {
        return settingsPanel.isModified();
    }

    @Override
    public void apply() {
        settingsPanel.apply();
    }

    @Override
    public void reset() {
        settingsPanel.reset();
    }
}