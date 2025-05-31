package com.ownicn.extensions;

import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;

public class Module {

    private final com.intellij.openapi.module.Module module;

    public Module(com.intellij.openapi.module.Module module) {
        this.module = module;
    }

    public String getName() {
        return module.getName();
    }

    public String getModulePath() {
        // 获取模块的根管理器
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);

        // 获取内容根路径（实际源代码所在的路径）
        VirtualFile[] contentRoots = rootManager.getContentRoots();
        if (contentRoots.length > 0) {
            return contentRoots[0].getPath();
        }

        //noinspection UnstableApiUsage
        VirtualFile moduleFile = module.getModuleFile();
        if (moduleFile != null) {
            return moduleFile.getParent().getPath();
        }

        return "";
    }

    @Override
    public String toString() {
        return String.format("MODULE{name=%s, modulePath=%s}", getName(), getModulePath());
    }
}
