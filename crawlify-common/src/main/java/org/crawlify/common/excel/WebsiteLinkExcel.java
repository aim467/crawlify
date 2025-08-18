package org.crawlify.common.excel;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.format.DateTimeFormat;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.HeadFontStyle;
import cn.idev.excel.annotation.write.style.HeadStyle;
import cn.idev.excel.enums.BooleanEnum;
import cn.idev.excel.enums.poi.FillPatternTypeEnum;
import cn.idev.excel.enums.poi.HorizontalAlignmentEnum;
import cn.idev.excel.enums.poi.VerticalAlignmentEnum;
import lombok.Data;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.crawlify.common.excel.handler.ExtLinkConverter;
import org.crawlify.common.excel.handler.UrlTypeConverter;

import java.time.LocalDateTime;

@Data
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER,
        verticalAlignment = VerticalAlignmentEnum.CENTER,
        fillForegroundColor = 22)
public class WebsiteLinkExcel {

    @ColumnWidth(20)
    @HeadStyle(fillForegroundColor = 16, fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
    @HeadFontStyle(color = 9, fontName = "宋体", fontHeightInPoints = 12, bold = BooleanEnum.TRUE)
    @ExcelProperty(value = "链接唯一标识", index = 0)
    private String id;

    @ColumnWidth(25)
    @HeadStyle(fillForegroundColor = 16, fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
    @HeadFontStyle(color = 9, fontName = "宋体", fontHeightInPoints = 12, bold = BooleanEnum.TRUE)
    @ExcelProperty(value = "网站名称", index = 1)
    private String websiteName;

    @ColumnWidth(50)
    @HeadStyle(fillForegroundColor = 16, fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
    @HeadFontStyle(color = 9, fontName = "宋体", fontHeightInPoints = 12, bold = BooleanEnum.TRUE)
    @ExcelProperty(value = "链接地址", index = 2)
    private String url;

    @ColumnWidth(25)
    @HeadStyle(fillForegroundColor = 16, fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
    @HeadFontStyle(color = 9, fontName = "宋体", fontHeightInPoints = 12, bold = BooleanEnum.TRUE)
    @ExcelProperty(value = "外部链接标识", index = 3, converter = ExtLinkConverter.class)
    private Boolean extLink;

    @ColumnWidth(25)
    @HeadStyle(fillForegroundColor = 16, fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
    @HeadFontStyle(color = 9, fontName = "宋体", fontHeightInPoints = 12, bold = BooleanEnum.TRUE)
    @ExcelProperty(value = "链接类型编码", index = 4, converter = UrlTypeConverter.class)
    private Integer urlType;

    @ColumnWidth(30)
    @HeadStyle(fillForegroundColor = 16, fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
    @HeadFontStyle(color = 9, fontName = "宋体", fontHeightInPoints = 12, bold = BooleanEnum.TRUE)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "记录创建时间", index = 5)
    private LocalDateTime createdAt;

    @ColumnWidth(30)
    @HeadStyle(fillForegroundColor = 16, fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
    @HeadFontStyle(color = 9, fontName = "宋体", fontHeightInPoints = 12, bold = BooleanEnum.TRUE)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "记录更新时间", index = 6)
    private LocalDateTime updatedAt;
}