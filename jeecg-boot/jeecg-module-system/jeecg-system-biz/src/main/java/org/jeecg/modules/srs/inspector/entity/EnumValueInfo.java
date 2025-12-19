package org.jeecg.modules.srs.inspector.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 枚举值信息，存储枚举常量的所有属性值
 */
@Data
public class EnumValueInfo {
    /**
     * 枚举全限定名
     */
    private String enumFullName;

    /**
     * 枚举值名称（如 SUCCESS, FAIL）
     */
    private String valueName;

    /**
     * 枚举值的属性映射
     * key: 属性名（如 name, code）
     * value: 属性值
     */
    private Map<String, String> properties = new LinkedHashMap<>();

    /**
     * 构造函数参数列表（按顺序）
     */
    private List<String> constructorArgs = new ArrayList<>();

    public EnumValueInfo(String enumFullName, String valueName) {
        this.enumFullName = enumFullName;
        this.valueName = valueName;
    }

    /**
     * 获取指定属性值
     */
    public String getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * 设置属性值
     */
    public void setProperty(String propertyName, String value) {
        properties.put(propertyName, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(enumFullName).append(".").append(valueName);
        if (!properties.isEmpty()) {
            sb.append(" {");
            properties.forEach((k, v) -> sb.append(k).append("=").append(v).append(", "));
            sb.setLength(sb.length() - 2); // 移除最后的 ", "
            sb.append("}");
        }
        return sb.toString();
    }
}
