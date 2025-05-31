package com.ownicn.extensions.impl;

import com.ownicn.extensions.Clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class DefaultClipboard implements Clipboard {
    @Override
    public String getText() {
        // 获取剪贴板内容
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            return clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
        } catch (UnsupportedFlavorException | IOException e) {
            return null;
        }
    }

    @Override
    public void setText(String text) {
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    @Override
    public String toString() {
        return String.format("CLIPBOARD{text=%s}", getText());
    }
}
