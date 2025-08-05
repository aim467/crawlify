package org.crawlify.common.entity;


import cn.idev.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.crawlify.common.excel.handler.ExtLinkConverter;
import org.crawlify.common.excel.handler.UrlTypeConverter;

import java.time.LocalDateTime;

@Data
@TableName(value = "website_link")
public class WebsiteLink {
    @TableId(type = IdType.AUTO)
    @ExcelProperty(value = "链接ID", index = 0)
    private Long id;
    @ExcelProperty(value = "链接", index = 1)
    private String url;
    @ExcelProperty(value = "网站ID", index = 2)
    private Integer websiteId;
    @ExcelProperty(value = "是否外部链接", index = 3, converter = ExtLinkConverter.class)
    private Boolean extLink;
    @ExcelProperty(value = "链接类型", index = 4, converter = UrlTypeConverter.class)
    private Integer urlType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "创建时间", index = 5)
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "更新时间", index = 6)
    private LocalDateTime updatedAt;
}

