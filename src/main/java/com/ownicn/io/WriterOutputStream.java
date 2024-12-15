package com.ownicn.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class WriterOutputStream extends OutputStream {
    private final Writer writer;
    private final StringBuilder buffer = new StringBuilder();

    public WriterOutputStream(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void write(int b) throws IOException {
        char c = (char) b;
        buffer.append(c);
        if (c == '\n') {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        if (!buffer.isEmpty()) {
            writer.write(buffer.toString());
            writer.flush();
            buffer.setLength(0);
        }
    }
} 