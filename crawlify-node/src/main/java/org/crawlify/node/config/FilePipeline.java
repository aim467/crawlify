package org.crawlify.node.config;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FilePipeline implements Pipeline {
    private final String filePath;
    private final BufferedWriter writer;
    private final Lock lock = new ReentrantLock();

    public FilePipeline(String filePath) {
        this.filePath = filePath;
        try {
            this.writer = new BufferedWriter(new FileWriter(filePath, true)); // 追加模式
        } catch (IOException e) {
            throw new RuntimeException("Failed to create file writer", e);
        }
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        String url = resultItems.getRequest().getUrl();
        try {
            lock.lock(); // 确保线程安全
            writer.write(url);
            writer.newLine();
            writer.flush(); // 立即写入文件
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}