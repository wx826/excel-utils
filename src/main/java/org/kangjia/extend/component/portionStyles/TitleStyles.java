package org.kangjia.extend.component.portionStyles;

import org.apache.poi.ss.usermodel.*;
import org.kangjia.extend.component.CustomizeStyles;

/**
 * 自定义标题单元格样式的接口
 */
public abstract class TitleStyles extends CustomizeStyles {

    @Override
    protected abstract CellStyle createTitleStyles(Workbook wb);

    @Override
    protected CellStyle createDataStyles(Workbook wb){
        CellStyle style = wb.createCellStyle();
        //设置单元格水平对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置单元格垂直对齐方式
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置右边框
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置左边框
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置上边框
        style.setBorderTop(BorderStyle.THIN);
        //设置上边框颜色
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置下边框
        style.setBorderBottom(BorderStyle.THIN);
        //设置下边框颜色
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置字体样式
        Font dataFont = wb.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        style.setFont(dataFont);
        return style;
    }

    @Override
    protected CellStyle createTotalStyles(Workbook wb){
        CellStyle style = wb.createCellStyle();
        //设置单元格水平对齐方式
        style.setAlignment(HorizontalAlignment.RIGHT);
        //设置单元格垂直对齐方式
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置右边框
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置左边框
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置上边框
        style.setBorderTop(BorderStyle.THIN);
        //设置上边框颜色
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置下边框
        style.setBorderBottom(BorderStyle.THIN);
        //设置下边框颜色
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置字体样式
        Font totalFont = wb.createFont();
        totalFont.setFontName("Arial");
        totalFont.setFontHeightInPoints((short) 10);
        style.setFont(totalFont);
        return style;
    }
}
