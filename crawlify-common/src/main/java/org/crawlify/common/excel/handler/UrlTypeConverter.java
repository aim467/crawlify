package org.crawlify.common.excel.handler;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 链接类型转换器
 */
public class UrlTypeConverter implements Converter<Integer> {

    private static Map<String, Integer> typeMapping = Map.of(
            "webpage", 1,
            "css", 2,
            "javascript", 3,
            "image", 4,
            "document", 5,
            "font", 6,
            "video", 7,
            "archive", 8,
            "data", 9,
            "unknown", 0
    );

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {
        return typeMapping.getOrDefault(cellData.getStringValue(), 0);

    }

    @Override
    public WriteCellData<String> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {

        Map<Integer, String> typeReverseMapping = typeMapping.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getValue,  // 新 key
                Map.Entry::getKey,    // 新 value
                (v1, v2) -> v1        // 如果 key 冲突，保留第一个（可自定义策略）
        ));
        return new WriteCellData<>(typeReverseMapping.get(value));
    }
}
