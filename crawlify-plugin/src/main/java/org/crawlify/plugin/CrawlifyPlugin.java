package org.crawlify.plugin;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import java.util.Map;

@Extension
public interface CrawlifyPlugin extends ExtensionPoint {

    /**
     * 数据采集，插件采集到数据之后，统一返回数据，交由主程序处理
     * @return
     */
    Map<String, Object> crawl();


}
