package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.entity.TemplateConfig;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.mapper.TemplateConfigMapper;
import org.crawlify.common.service.TemplateConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class TemplateConfigServiceImpl extends ServiceImpl<TemplateConfigMapper, TemplateConfig> implements TemplateConfigService {

    @Value("${crawlify.script-path-prefix}")
    private String scriptPathPrefix;

    @Override
    public R saveScript(TemplateConfig templateConfig) {
        TemplateConfig rawTemplateConfig = this.getById(templateConfig.getConfigId());
        if (rawTemplateConfig.getScriptPath() != null) {
            // 获得完整的脚本路径
            String fullScriptPath = scriptPathPrefix + templateConfig.getScriptPath();
            // 把 templateConfig 中的 scriptContent 写入到文件中，覆盖掉原来的内容
            try {
                FileWriter writer = new FileWriter(fullScriptPath, StandardCharsets.UTF_8);
                writer.write(templateConfig.getScriptContent());
                writer.close();
            } catch (IOException e) {
                return R.fail("写入脚本失败");
            }
            return R.ok("更新脚本文件成功");
        }
        // 创建新脚本
        String scriptPath = templateConfig.getConfigName() + "_" + templateConfig.getConfigId() + ".groovy";
        String fullScriptPath = scriptPathPrefix + scriptPath;
        templateConfig.setScriptPath(scriptPath);
        try {
            FileWriter writer = new FileWriter(fullScriptPath, StandardCharsets.UTF_8);
            writer.write(templateConfig.getScriptContent());
            writer.close();
        } catch (IOException e) {
            return R.fail("写入脚本失败");
        }
        templateConfig.setUpdatedAt(LocalDateTime.now());
        updateById(templateConfig);
        return R.ok("更新脚本文件成功");
    }

    @Override
    public R runScript(TemplateConfig templateConfig) {
        return null;
    }

    @Override
    public R stopScript(TemplateConfig templateConfig) {
        return null;
    }

    @Override
    public R<String> getScript(String configId) {
        TemplateConfig templateConfig = this.getById(configId);
        if (templateConfig == null) {
            return R.fail("Template config not found.");
        }

        if (templateConfig.getScriptPath() == null) {
            return R.ok();
        }

        String fullScriptPath = scriptPathPrefix + templateConfig.getScriptPath();
        try {
            FileReader reader = new FileReader(fullScriptPath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            bufferedReader.close();
            reader.close();
            templateConfig.setScriptContent(content.toString());
            return R.ok(content.toString());
        } catch (Exception e) {
            return R.fail("读取脚本文件失败");
        }
    }
}