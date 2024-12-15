package com.ownicn.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import org.jetbrains.annotations.Nullable;

public class ActionScriptConfigurableProvider extends ConfigurableProvider {
    @Override
    public @Nullable Configurable createConfigurable() {
        return new ActionScriptConfigurable();
    }
}