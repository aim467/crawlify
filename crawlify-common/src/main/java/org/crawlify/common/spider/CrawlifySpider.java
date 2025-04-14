package org.crawlify.common.spider;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class CrawlifySpider extends Spider {

    /**
     * create a spider with pageProcessor.
     *
     * @param pageProcessor pageProcessor
     */
    public CrawlifySpider(PageProcessor pageProcessor) {
        super(pageProcessor);
    }


    @Override
    public void run() {
        super.run();
        // 添加回调
    }


    @Override
    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }
}
