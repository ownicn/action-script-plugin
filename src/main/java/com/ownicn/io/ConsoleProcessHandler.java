package com.ownicn.io;

import com.intellij.execution.process.BaseOSProcessHandler;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ConsoleProcessHandler extends BaseOSProcessHandler {

    public ConsoleProcessHandler(ConsoleViewManager console) {
        super(createProcess(console), null, StandardCharsets.UTF_8);
    }

    private static Process createProcess(ConsoleViewManager console) {
        return new Process() {
            private final ConsoleOutputStream stdout = new ConsoleOutputStream(console, false);

            @Override
            public ConsoleOutputStream getOutputStream() {
                return stdout;
            }

            @Override
            public InputStream getInputStream() {
                return new InputStream() {
                    @Override
                    public int read() {
                        return -1;
                    }
                };
            }

            @Override
            public InputStream getErrorStream() {
                return getInputStream();
            }

            @Override
            public int waitFor() {
                return 0;
            }

            @Override
            public int exitValue() {
                return 0;
            }

            @Override
            public void destroy() {
            }
        };
    }

    @Override
    public boolean isSilentlyDestroyOnClose() {
        return true;
    }
} 