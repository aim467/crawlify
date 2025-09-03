package org.crawlify.common.utils;


import org.crawlify.common.entity.TemplateConfig;
import org.crawlify.plugin.CrawliyPlugin;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrawlifyPluginUtils {

    private final PluginManager pluginManager;
    private final String pluginPath;


    public CrawlifyPluginUtils(String pluginPath) {
        this.pluginPath = pluginPath;
        pluginManager = new DefaultPluginManager(Paths.get(this.pluginPath));
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }


    /**
     * 列出所有插件
     */
    public List<CrawliyPlugin> listPlugins() {
        return pluginManager.getExtensions(CrawliyPlugin.class);
    }

    /**
     * 运行指定插件
     */
    public Map<String, Object> runPlugin(String pluginId, TemplateConfig templateConfig) {
        for (CrawliyPlugin plugin : listPlugins()) {
            if (plugin.getClass().getName().equals(pluginId)) {
                return plugin.crawl();
            }
        }
        return new HashMap<String, Object>(0);
    }


    /**
     * 卸载指定插件
     */
    public boolean unloadPlugin(String pluginId) {
        return pluginManager.unloadPlugin(pluginId);
    }

    /**
     * 重载指定插件
     */
    public boolean reloadPlugin(String pluginId) {
        return pluginManager.deletePlugin(pluginId) && pluginManager.loadPlugin(Paths.get(pluginId)) != null;
    }

    /**
     * 禁用指定插件
     * @param pluginId
     * @return
     */
    public boolean disablePlugin(String pluginId) {
        return pluginManager.disablePlugin(pluginId);
    }

    /**
     * 启用指定插件
     * @param pluginId
     * @return
     */
    public boolean enablePlugin(String pluginId) {
        return pluginManager.enablePlugin(pluginId);
    }

    /**
     * 重新加载所有插件
     */
    public void reloadPlugins() {
        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }


    /**
     * 删除插件
     */
    public boolean deleteJar(String fileName) {
        // 使用文件删除 jar
        String fullPath = pluginPath + "/" + fileName;
        return new java.io.File(fullPath).delete();
    }
}
