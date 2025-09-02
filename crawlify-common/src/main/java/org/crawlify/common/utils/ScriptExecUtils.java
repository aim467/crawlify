package org.crawlify.common.utils;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ScriptExecUtils {


    /**
     * 运行 groovy 脚本
     * @param scriptPath
     */
    public static void runGroovy(String scriptPath) throws IOException {
        Process process = new ProcessExecutor()
                .command("")
                .start()
                .getProcess();

        long pid = process.pid(); // Java 9+ 支持
        System.out.println("PID: " + pid);
    }

    public static void main(String[] args) throws IOException {
        runGroovy("");
    }
}
