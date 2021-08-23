package org.kangjia;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.kangjia.exportFile.ExportExcel;
import org.kangjia.extend.Customize;
import org.kangjia.hook.ExcelImportHandler;
import org.kangjia.importFile.ImportExcel;
import org.kangjia.importFile.ImportResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExcelUtils{

    /**
     * 根据行和列的索引获取单元格的值
     * @param sheet           sheet对象
     * @param rowNum          行索引
     * @param columnNum       列索引
     * @param cellTypeEnum    单元格数据类型
     * @return
     */
    public static Object getExcelDateByIndex(Sheet sheet, int rowNum, int columnNum, CellType cellTypeEnum){
        Object result = "";
        Row row = sheet.getRow(rowNum);
        Cell cell = row.getCell(columnNum);
        if(CellType.NUMERIC == cellTypeEnum){
            result = cell.getNumericCellValue();
        }else if(CellType.STRING == cellTypeEnum){
            result = cell.getStringCellValue();
        }else if(CellType.FORMULA == cellTypeEnum){
            result = cell.getCellFormula();
        }else if(CellType.BLANK == cellTypeEnum){
            result = "";
        }else if(CellType.BOOLEAN == cellTypeEnum){
            result = cell.getBooleanCellValue();
        }else{
            result = null;
        }
        return result;
    }

    /**
     * 根据某一列值为“******”的这一行，来获取该行第x列的值
     * @param sheet           sheet对象
     * @param cellValue       当前单元格的值
     * @param currentColumn   当前单元格列的索引
     * @param targetColumn    目标单元格列的索引
     * @return
     */
    public static String getCellByCaseName(Sheet sheet,String cellValue,int currentColumn,int targetColumn){
        String operateSteps="";
        //获取行数
        int rows = sheet.getPhysicalNumberOfRows();
        for(int i=0;i<rows;i++){
            Row row = sheet.getRow(i);
            String cell = row.getCell(currentColumn).toString();
            if(cell.equals(cellValue)){
                operateSteps = row.getCell(targetColumn).toString();
                break;
            }
        }
        return operateSteps;
    }

    /**
     * 导出excel
     * @param clazz                 要导出的实体类的类对象
     * @param list                  要导出的数据
     * @param sheetName             sheetName
     * @param response              响应
     * @return
     */
    public static <T> void exportExcel(Class<T> clazz, List<T> list, String sheetName, HttpServletResponse response){
        exportExcel(clazz,list,sheetName,null,response);
    }

    /**
     * 导出excel
     * @param clazz                 要导出的实体类的类对象
     * @param list                  要导出的数据
     * @param sheetName             sheetName
     * @param fileName              文件名称
     * @param response              响应
     * @return
     */
    public static <T> void exportExcel(Class<T> clazz, List<T> list, String sheetName, String fileName, HttpServletResponse response){
        exportExcel(clazz,list,sheetName,fileName,response,null);
    }

    /**
     * 导出excel
     * @param clazz                 要导出的实体类的类对象
     * @param list                  要导出的数据
     * @param sheetName             sheetName
     * @param fileName              文件名称
     * @param response              响应
     * @param customize             自定义样式
     * @return
     */
    public static <T> void exportExcel(Class<T> clazz, List<T> list, String sheetName, String fileName, HttpServletResponse response, Customize customize){
        new ExportExcel<T>(clazz,list,sheetName,fileName,response,customize).execute();
    }

    /**
     * 导入excel数据到项目
     * @param is           excel文件输入流
     * @param clazz        数据承载的类对象
     * @param titleRowNum  文件中标题的行数
     * @param handler      数据导入的钩子
     * @return
     */
    public static <T> ImportResult importExcel(InputStream is, Class<T> clazz, Integer titleRowNum, ExcelImportHandler handler) throws IOException, InvalidFormatException {
        return importExcel(is,null, clazz,titleRowNum, null, handler);
    }

    /**
     * 导入excel数据到项目
     * @param is           excel文件输入流
     * @param clazz        数据承载的类对象
     * @param titleRowNum  文件中标题的行数
     * @param cacheSize    集合缓存大小
     * @param handler      数据导入的钩子
     * @return
     */
    public static <T> ImportResult importExcel(InputStream is, Class<T> clazz, Integer titleRowNum, Integer cacheSize, ExcelImportHandler handler) throws IOException, InvalidFormatException {
        return importExcel(is,null, clazz, titleRowNum, cacheSize, handler);
    }

    /**
     * 导入excel数据到项目
     * @param is           excel文件输入流
     * @param sheetName    sheetName
     * @param clazz        数据承载的类对象
     * @param titleRowNum  文件中标题的行数
     * @param handler      数据导入的钩子
     * @return
     */
    public static <T> ImportResult importExcel(InputStream is, String sheetName, Class<T> clazz, Integer titleRowNum, ExcelImportHandler handler) throws IOException, InvalidFormatException {
        return importExcel(is,sheetName, clazz, titleRowNum, null, handler);
    }

    /**
     * 导入excel数据到项目
     * @param is           excel文件输入流
     * @param sheetName    sheetName
     * @param clazz        数据承载的类对象
     * @param titleRowNum  文件内容中标题行数
     * @param cacheSize    集合缓存大小
     * @param handler      数据导入的钩子
     * @return
     */
    public static <T> ImportResult importExcel(InputStream is, String sheetName, Class<T> clazz, Integer titleRowNum, Integer cacheSize, ExcelImportHandler handler) throws IOException, InvalidFormatException {
        return new ImportExcel<T>(clazz, is, sheetName, titleRowNum, cacheSize, handler).execute();
    }
}
