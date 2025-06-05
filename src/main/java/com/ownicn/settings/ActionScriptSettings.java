package com.ownicn.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(
    name = "ActionScriptSettings",
    storages = @Storage("action-scripts.xml")
)
@SuppressWarnings("unused")
public class ActionScriptSettings implements PersistentStateComponent<ActionScriptSettings.State> {
    private final State scriptState = new State();

    public static class State {
        private List<ScriptEntry> scripts = new ArrayList<>();
        private boolean softWrapEnabled = false;

        public State() {
        }

        public List<ScriptEntry> getScripts() {
            return scripts;
        }

        public void setScripts(List<ScriptEntry> scripts) {
            this.scripts = scripts;
        }

        public boolean isSoftWrapEnabled() {
            return softWrapEnabled;
        }

        public void setSoftWrapEnabled(boolean softWrapEnabled) {
            this.softWrapEnabled = softWrapEnabled;
        }
    }

    public static class ScriptEntry {
        private String name;
        private String content;
        private LanguageSupports language;

        public ScriptEntry() {
            this("", "", LanguageSupports.Groovy);
        }

        public ScriptEntry(@NotNull String name, @NotNull String content, @NotNull LanguageSupports language) {
            this.name = name;
            this.content = content;
            this.language = language;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public LanguageSupports getLanguage() {
            return language;
        }

        public void setLanguage(LanguageSupports language) {
            this.language = language;
        }

        @Override
        public String toString() {
            return "ScriptEntry{name:" + name + ", content:" + content + ", language:" + language + "}";
        }
    }

    public static ActionScriptSettings getInstance() {
        return ApplicationManager.getApplication().getService(ActionScriptSettings.class);
    }

    @Override
    public @Nullable State getState() {
        return scriptState;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, scriptState);
    }

    public void setSoftWrapEnabled(boolean enabled) {
        scriptState.setSoftWrapEnabled(enabled);
    }

    public boolean isSoftWrapEnabled() {
        return scriptState.isSoftWrapEnabled();
    }
} 