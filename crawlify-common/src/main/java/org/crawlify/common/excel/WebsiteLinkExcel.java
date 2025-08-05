package org.crawlify.common.excel;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.format.DateTimeFormat;
import lombok.Data;
import org.crawlify.common.excel.handler.ExtLinkConverter;
import org.crawlify.common.excel.handler.UrlTypeConverter;

import java.time.LocalDateTime;

@Data
public class WebsiteLinkExcel {

    @ExcelProperty(value = "链接ID", index = 0)
    private String id;

    @ExcelProperty(value = "网站名称", index = 1)
    private String websiteName;

    @ExcelProperty(value = "链接", index = 2)
    private String url;

    @ExcelProperty(value = "是否外部链接", index = 3, converter = ExtLinkConverter.class)
    private Boolean extLink;

    @ExcelProperty(value = "链接类型", index = 4, converter = UrlTypeConverter.class)
    private Integer urlType;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "创建时间", index = 5)
    private LocalDateTime createdAt;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "更新时间", index = 6)
    private LocalDateTime updatedAt;
}
