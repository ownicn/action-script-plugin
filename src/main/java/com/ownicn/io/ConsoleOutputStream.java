package com.ownicn.io;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ConsoleOutputStream extends OutputStream {
    private final ConsoleViewManager console;
    private final boolean isError;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public ConsoleOutputStream(ConsoleViewManager console) {
        this(console, false);
    }

    public ConsoleOutputStream(ConsoleViewManager console, boolean isError) {
        this.console = console;
        this.isError = isError;
    }

    @Override
    public void write(int b) {
        buffer.write(b);
        flush();
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) {
        buffer.write(b, off, len);
        flush();
    }

    @Override
    public void flush() {
        if (buffer.size() > 0) {
            console.print(buffer.toString(StandardCharsets.UTF_8), isError);
            buffer.reset();
        }
    }
}