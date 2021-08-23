package org.kangjia.extend.component;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.kangjia.extend.Customize;

import java.util.Map;

/**
 * 自定义单元格样式的接口
 */
public abstract class CustomizeStyles implements Customize {

    /**
     * 自定义标题样式
     * @param wb
     * @return
     */
    protected abstract CellStyle createTitleStyles(Workbook wb);

    /**
     * 自定义数据样式
     * @param wb
     * @return
     */
    protected abstract CellStyle createDataStyles(Workbook wb);

    /**
     * 自定义统计行样式
     * @param wb
     * @return
     */
    protected abstract CellStyle createTotalStyles(Workbook wb);

    @Override
    public Map<String, CellStyle> createCustomizeStyles(Workbook wb){
        styles.put("data", createDataStyles(wb));
        styles.put("header", createTitleStyles(wb));
        styles.put("total", createTotalStyles(wb));
        return styles;
    }
}
