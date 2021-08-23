package org.kangjia.extend;

import org.apache.poi.ss.usermodel.*;

import java.util.HashMap;
import java.util.Map;

/** * 给用户提供的自定义接口

 */
public interface Customize {

    Map<String, CellStyle> styles = new HashMap<String, CellStyle>();

    /**
     * 自定义表格样式
     *
     * @param wb 工作薄对象
     * @return 样式列表
     */
    public Map<String, CellStyle> createCustomizeStyles(Workbook wb);
}
