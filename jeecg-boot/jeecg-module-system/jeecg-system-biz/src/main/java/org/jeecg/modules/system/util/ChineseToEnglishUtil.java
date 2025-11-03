package org.jeecg.modules.system.util;

import cn.hutool.core.util.StrUtil;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.suggest.Suggester;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description: 中文转英文工具类
 * @Author: jeecg-boot
 * @Date: 2025-06-26
 * @Version: V1.0
 */
public class ChineseToEnglishUtil {

    /**
     * 中文字符正则表达式
     */
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]+");

    /**
     * 中文转英文（驼峰命名）
     * @param chinese 中文名称
     * @return 驼峰命名的英文
     */
    public static String chineseToCamelCase(String chinese) {
        if (StrUtil.isBlank(chinese)) {
            return StrUtil.EMPTY;
        }

        // 使用HanLP进行中文分词
        List<Term> terms = HanLP.segment(chinese);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i).word;
            if (CHINESE_PATTERN.matcher(term).find()) {
                // 如果是中文，转换为拼音
                String pinyin = HanLP.convertToPinyinString(term, "", false);
                if (i == 0) {
                    // 第一个单词首字母小写
                    sb.append(StrUtil.lowerFirst(pinyin));
                } else {
                    // 后续单词首字母大写
                    sb.append(StrUtil.upperFirst(pinyin));
                }
            } else {
                // 如果不是中文，直接保留
                if (i == 0) {
                    sb.append(StrUtil.lowerFirst(term));
                } else {
                    sb.append(StrUtil.upperFirst(term));
                }
            }
        }

        return sb.toString();
    }

    /**
     * 中文转英文（下划线命名）
     * @param chinese 中文名称
     * @return 下划线命名的英文
     */
    public static String chineseToUnderlineCase(String chinese) {
        if (StrUtil.isBlank(chinese)) {
            return StrUtil.EMPTY;
        }

        // 使用HanLP进行中文分词
        List<Term> terms = HanLP.segment(chinese);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i).word;
            if (CHINESE_PATTERN.matcher(term).find()) {
                // 如果是中文，转换为拼音
                String pinyin = HanLP.convertToPinyinString(term, "", false);
                if (i == 0) {
                    // 第一个单词全小写
                    sb.append(StrUtil.lowerCase(pinyin));
                } else {
                    // 后续单词全小写，前面加下划线
                    sb.append("_").append(StrUtil.lowerCase(pinyin));
                }
            } else {
                // 如果不是中文，直接保留
                if (i == 0) {
                    sb.append(StrUtil.lowerCase(term));
                } else {
                    sb.append("_").append(StrUtil.lowerCase(term));
                }
            }
        }

        return sb.toString();
    }

    /**
     * 中文转英文（数据库表名）
     * @param chinese 中文名称
     * @return 数据库表名
     */
    public static String chineseToDbTableName(String chinese) {
        if (StrUtil.isBlank(chinese)) {
            return StrUtil.EMPTY;
        }

        // 使用下划线命名，并全小写
        String tableName = chineseToUnderlineCase(chinese);
        return StrUtil.lowerCase(tableName);
    }

    /**
     * 中文转英文（数据库字段名）
     * @param chinese 中文名称
     * @return 数据库字段名
     */
    public static String chineseToDbColumnName(String chinese) {
        if (StrUtil.isBlank(chinese)) {
            return StrUtil.EMPTY;
        }

        // 使用下划线命名，并全小写
        String columnName = chineseToUnderlineCase(chinese);
        return StrUtil.lowerCase(columnName);
    }

    /**
     * 中文转英文（Java字段名）
     * @param chinese 中文名称
     * @return Java字段名
     */
    public static String chineseToJavaFieldName(String chinese) {
        if (StrUtil.isBlank(chinese)) {
            return StrUtil.EMPTY;
        }

        // 使用驼峰命名
        return chineseToCamelCase(chinese);
    }

    /**
     * 中文转英文（Java类名）
     * @param chinese 中文名称
     * @return Java类名
     */
    public static String chineseToJavaClassName(String chinese) {
        if (StrUtil.isBlank(chinese)) {
            return StrUtil.EMPTY;
        }

        // 使用驼峰命名，首字母大写
        String camelCase = chineseToCamelCase(chinese);
        return StrUtil.upperFirst(camelCase);
    }
}