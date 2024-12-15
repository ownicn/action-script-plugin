package com.ownicn.io;


import java.io.OutputStream;

public class ConsoleOutputStream extends OutputStream {
    private final ConsoleViewManager console;
    private final boolean isError;
    private final StringBuilder buffer = new StringBuilder();

    public ConsoleOutputStream(ConsoleViewManager console) {
        this(console, false);
    }

    public ConsoleOutputStream(ConsoleViewManager console, boolean isError) {
        this.console = console;
        this.isError = isError;
    }

    @Override
    public void write(int b) {
        char c = (char) b;
        buffer.append(c);
        if (c == '\n') {
            flush();
        }
    }

    @Override
    public void flush() {
        if (!buffer.isEmpty()) {
            String text = buffer.toString();
            console.print(text, isError);
            buffer.setLength(0);
        }
    }
}