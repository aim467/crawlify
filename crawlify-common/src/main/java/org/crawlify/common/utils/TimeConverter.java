package org.crawlify.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeConverter {
    private final ZoneId defaultZone;
    private final ZoneId targetZone;

    public TimeConverter(ZoneId defaultZone, ZoneId targetZone) {
        this.defaultZone = defaultZone != null ? defaultZone : ZoneId.systemDefault();
        this.targetZone = targetZone != null ? targetZone : ZoneId.systemDefault();
    }

    public TimeConverter() {
        this(null, null);
    }

    /**
     * 转换时间输入为 yyyy-MM-dd HH:mm:ss 格式
     */
    public String convert(Object input) {
        if (input == null) {
            throw new IllegalArgumentException("时间输入不能为空");
        }

        ZonedDateTime zdt = null;
        String str = input.toString().trim();

        try {
            // Unix 时间戳
            if (str.matches("^\\d{9,10}$")) { // 秒
                long ts = Long.parseLong(str);
                zdt = Instant.ofEpochSecond(ts).atZone(ZoneOffset.UTC);
            } else if (str.matches("^\\d{12,13}$")) { // 毫秒
                long ts = Long.parseLong(str);
                zdt = Instant.ofEpochMilli(ts).atZone(ZoneOffset.UTC);
            } else if (str.matches("^\\d{15,16}$")) { // 微秒
                long ts = Long.parseLong(str);
                zdt = Instant.ofEpochMilli(ts / 1000).atZone(ZoneOffset.UTC);
            }

            // ISO 8601 基础格式
            else if (str.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$")) {
                LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                zdt = ldt.atZone(defaultZone);
            }
            // ISO 8601 带毫秒
            else if (str.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+$")) {
                LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
                zdt = ldt.atZone(defaultZone);
            }
            // ISO 8601 带时区
            else if (str.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}([+\\-]\\d{2}:\\d{2}|Z)$")) {
                if (str.endsWith("Z")) {
                    str = str.replace("Z", "+00:00");
                }
                OffsetDateTime odt = OffsetDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
                zdt = odt.toZonedDateTime();
            }

            // RFC 1123
            else if (Pattern.matches("^[A-Za-z]{3}, .* GMT$", str)) {
                ZonedDateTime parsed = ZonedDateTime.parse(str, DateTimeFormatter.RFC_1123_DATE_TIME);
                zdt = parsed.withZoneSameInstant(ZoneOffset.UTC);
            }

            // MySQL DATETIME
            else if (str.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {
                LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                zdt = ldt.atZone(defaultZone);
            }

            // 中文日期（宽松）：只要包含“年/月/日”就走这里
            else if (str.contains("年") && str.contains("月") && str.contains("日")) {
                zdt = parseChinese(str).atZone(defaultZone);
            }

            // 美式格式 MM/dd/yyyy hh:mm:ss a
            else if (str.matches("^\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} [AP]M$")) {
                LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("M/d/yyyy h:m:s a", Locale.ENGLISH));
                zdt = ldt.atZone(defaultZone);
            }

            // 欧式格式 dd/MM/yyyy HH:mm:ss
            else if (str.matches("^\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}$")) {
                LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("d/M/yyyy H:m:s"));
                zdt = ldt.atZone(defaultZone);
            }

            // 紧凑格式 yyyyMMddTHHmmss
            else if (str.matches("^\\d{8}T\\d{6}$")) {
                LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
                zdt = ldt.atZone(defaultZone);
            }

            // Excel 序列号
            else if (str.matches("^\\d+\\.\\d+$")) {
                double serial = Double.parseDouble(str);
                // Excel 基准 (1900-01-01 = 1，但 Excel bug 认为 1900 是闰年，因此基准用 1899-12-30)
                LocalDateTime base = LocalDateTime.of(1899, 12, 30, 0, 0);
                LocalDateTime ldt = base.plusSeconds((long) (serial * 86400));
                zdt = ldt.atZone(ZoneOffset.UTC);
            }

            // 通用解析 (模糊格式)
            else {
                try {
                    zdt = ZonedDateTime.parse(str);
                } catch (DateTimeParseException e1) {
                    try {
                        LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("d.M.yyyy H:m:s"));
                        zdt = ldt.atZone(defaultZone);
                    } catch (DateTimeParseException e2) {
                        throw new IllegalArgumentException("无法识别的时间格式: " + str);
                    }
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("无法识别的时间格式: " + str, e);
        }

        // 转换为目标时区
        ZonedDateTime converted = zdt.withZoneSameInstant(targetZone);
        return converted.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    public static String getSystemTimezone() {
        return ZoneId.systemDefault().toString();
    }

    // ----------------- 中文解析核心 -----------------
    // 兼容：全角数字/冒号、可无空格、可用“时分秒/点”、可带上午/下午、秒可省略
    private static LocalDateTime parseChinese(String raw) {
        String s = toAsciiDigits(raw)
                .replace('\u00A0', ' ')      // 不换行空格
                .replace("：", ":")          // 全角冒号
                .trim();

        boolean hasAM = s.contains("上午") || s.toLowerCase(Locale.ROOT).contains("am");
        boolean hasPM = s.contains("下午") || s.toLowerCase(Locale.ROOT).contains("pm");

        // 去掉 AM/PM 标记文本
        s = s.replace("上午", "").replace("下午", "")
                .replaceAll("(?i)\\bAM\\b|\\bPM\\b", "")
                .trim();

        // 1) 格式一：yyyy年MM月dd日 HH:mm[:ss]
        Pattern p1 = Pattern.compile("^\\s*(\\d{4})年\\s*(\\d{1,2})月\\s*(\\d{1,2})日\\s*(?:([0-9]{1,2})(?::([0-9]{1,2})(?::([0-9]{1,2}))?)?)?\\s*$");
        Matcher m1 = p1.matcher(s);
        if (m1.matches()) {
            int y = Integer.parseInt(m1.group(1));
            int M = Integer.parseInt(m1.group(2));
            int d = Integer.parseInt(m1.group(3));
            int h = parseOrDefault(m1.group(4), 0);
            int min = parseOrDefault(m1.group(5), 0);
            int sec = parseOrDefault(m1.group(6), 0);
            h = applyAmPm(h, hasAM, hasPM);
            return LocalDateTime.of(y, M, d, h, min, sec);
        }

        // 2) 格式二：yyyy年MM月dd日 HH时mm分[ss秒] / HH点mm分[ss秒]
        Pattern p2 = Pattern.compile("^\\s*(\\d{4})年\\s*(\\d{1,2})月\\s*(\\d{1,2})日\\s*(?:([0-9]{1,2})\\s*[时点]\\s*([0-9]{1,2})\\s*分(?:\\s*([0-9]{1,2})\\s*秒?)?)?\\s*$");
        Matcher m2 = p2.matcher(s);
        if (m2.matches()) {
            int y = Integer.parseInt(m2.group(1));
            int M = Integer.parseInt(m2.group(2));
            int d = Integer.parseInt(m2.group(3));
            int h = parseOrDefault(m2.group(4), 0);
            int min = parseOrDefault(m2.group(5), 0);
            int sec = parseOrDefault(m2.group(6), 0);
            h = applyAmPm(h, hasAM, hasPM);
            return LocalDateTime.of(y, M, d, h, min, sec);
        }

        throw new IllegalArgumentException("无法识别的中文时间格式: " + raw);
    }

    private static int parseOrDefault(String s, int def) {
        return (s == null || s.isEmpty()) ? def : Integer.parseInt(s);
    }

    private static int applyAmPm(int hour, boolean am, boolean pm) {
        if (pm && hour < 12) return hour + 12;
        if (am && hour == 12) return 0;
        return hour;
    }

    private static String toAsciiDigits(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // 全角数字 U+FF10..FF19
            if (c >= '０' && c <= '９') {
                sb.append((char) (c - '０' + '0'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String normalizeSpaces(String s) {
        return s == null ? null : s.replace('\u00A0', ' ').trim().replaceAll("\\s+", " ");
    }


    // ======================
    // 使用示例
    // ======================
    public static void main(String[] args) {
        TimeConverter converter = new TimeConverter();

        System.out.println("==================================");
        System.out.println("全能时间格式转换工具演示");
        System.out.println("系统时区: " + TimeConverter.getSystemTimezone());
        System.out.println("==================================");

        Object[][] testCases = {
                {"秒级时间戳", 1718438400},
                {"毫秒级时间戳", 1718438400000L},
                {"微秒级时间戳", 1718438400000000L},
                {"ISO基础格式", "2024-06-15T12:30:45"},
                {"ISO带毫秒", "2024-06-15T12:30:45.123"},
                {"ISO带时区", "2024-06-15T12:30:45+08:00"},
                {"ISO UTC格式", "2024-06-15T04:30:45Z"},
                {"RFC 1123", "Sat, 15 Jun 2024 12:30:45 GMT"},
                {"MySQL DATETIME", "2024-06-15 12:30:45"},
                {"中文格式", "2024年06月15日 12:30:45"},
                {"美式格式", "06/15/2024 12:30:45 PM"},
                {"欧式格式", "15/06/2024 12:30:45"},
                {"紧凑ISO", "20240615T123045"},
                {"Excel序列号", "45456.521"},
                {"模糊格式", "15.06.2024 12:30:45"}
        };

        for (Object[] testCase : testCases) {
            try {
                String result = converter.convert(testCase[1]);
                System.out.printf("%-10s | 输入: %-25s | 输出: %s%n", testCase[0], testCase[1], result);
            } catch (Exception e) {
                System.out.printf("%-10s | 输入: %-25s | 错误: %s%n", testCase[0], testCase[1], e.getMessage());
            }
        }

        System.out.println("\n==================================");
        System.out.println("自定义时区演示 (转换为UTC)");
        System.out.println("==================================");

        TimeConverter utcConverter = new TimeConverter(null, ZoneOffset.UTC);
        String r1 = utcConverter.convert("2024-06-15 12:30:45");
        String r2 = utcConverter.convert("2024-06-15T20:30:45+08:00");

        System.out.println("本地时间转UTC  | 输入: 2024-06-15 12:30:45      | 输出(UTC): " + r1);
        System.out.println("带时区时间转UTC | 输入: 2024-06-15T20:30:45+08:00 | 输出(UTC): " + r2);


        String[] zhCases = {
                "2024年06月15日 12:30:45",
                "2024年6月5日 8:3:5",
                "2024年06月15日12:30:45",
                "2024年6月5日 8时3分5秒",
                "2024年6月5日 上午 8:03:05",
                "2024年6月5日 下午 8:03:05",
                "２０２４年０６月１５日 １２：３０：４５" // 全角
        };

        for (String c : zhCases) {
            try {
                System.out.println(c + " -> " + converter.convert(c));
            } catch (Exception e) {
                System.out.println(c + " -> ERROR: " + e.getMessage());
            }
        }
    }
}

