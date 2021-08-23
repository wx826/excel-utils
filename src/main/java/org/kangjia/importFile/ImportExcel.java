package org.kangjia.importFile;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.kangjia.ExcelUtils;
import org.kangjia.exceptions.ImportException;
import org.kangjia.hook.ExcelImportHandler;
import org.kangjia.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 执行导入操作
 */
public class ImportExcel<T> {

    private final static Logger log = LoggerFactory.getLogger(ImportExcel.class);

    /**
     * 统计发生错误的次数
     */
    private Integer errorCount;

    /**
     * 发生的错误记录
     */
    private List<String> errorRecord;

    /**
     * 实体对象
     */
    private Class<T> clazz;

    /**
     * 文件中标题的行数
     */
    private Integer titleRowNum;

    /**
     * 导入数据的缓存大小
     */
    private Integer cacheSize;

    /**
     * 导入数据的缓存集合
     */
    private List<T> cacheList;

    /**
     * 工作薄对象
     */
    private Workbook wb;

    /**
     * 工作表对象
     */
    private Sheet sheet;

    /**
     * 数据导入的钩子
     */
    private ExcelImportHandler<T> handler;

    public ImportExcel(Class<T> clazz,InputStream is,String sheetName, Integer titleRowNum, Integer cacheSize, ExcelImportHandler handler) throws IOException, InvalidFormatException {
        init(clazz, is, sheetName, titleRowNum, cacheSize, handler);
    }

    /** ======================================初始化部分开始====================================== **/

    private void init(Class<T> clazz, InputStream is, String sheetName, Integer titleRowNum, Integer cacheSize, ExcelImportHandler handler) throws IOException, InvalidFormatException {
        log.info("==> 正在初始化参数...");
        this.errorCount = 0;
        this.errorRecord = new ArrayList<>();
        this.clazz = clazz;
        this.titleRowNum = titleRowNum == null ? 1:titleRowNum;
        this.wb = WorkbookFactory.create(is);
        if(StringUtils.isNotEmpty(sheetName)){
            this.sheet = this.wb.getSheet(sheetName);
        }else{
            this.sheet = this.wb.getSheetAt(0);
        }
        this.cacheSize = cacheSize == null ? 100:cacheSize;
        this.cacheList = new ArrayList<T>();
        this.handler = handler;
        log.info("==> 参数初始化完成...");
    }

    /** ======================================初始化部分结束====================================== **/


    /** ======================================导入入口====================================== **/

    /**
     * 执行导入操作
     */
    public ImportResult execute() throws ImportException{
        long startTime = System.currentTimeMillis();
        log.info("==> 开始执行导入操作...");
        ImportResult result = new ImportResult();
        result.setCode(ImportResult.Type.ERROR);
        if(this.sheet == null){
            throw new ImportException("无法获取sheet对象。");
        }

        //获取总行数
        int rowNum = this.sheet.getPhysicalNumberOfRows();
        for (int i = this.titleRowNum; i < rowNum; i++) {
            boolean flag = true;
            T vo = null;
            try {
                vo = clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new ImportException("目标对象不可创建，请确保您承接数据的类型不是接口或不是其他不可创建的类型。");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new ImportException("创建目标对象实例发生错误，没有找到可访问的无参构造。");
            }
            //获取列数
            Row row = this.sheet.getRow(i);
            int columns = row.getPhysicalNumberOfCells();
            try {
                for (int j = 0; j < columns; j++) {
                    Cell cell = row.getCell(j);
                    //获取单元格的原始值
                    Object value = ExcelUtils.getExcelDateByIndex(this.sheet,i, j, cell.getCellTypeEnum());
                    if(!handler.eachColumnCallBack(this.sheet, i, row, j, cell, vo, value)){
                        throw new ImportException("在用户自定义校验阶段未能通过。");
                    }
                }
                vo = handler.eachRowCallBack(this.sheet, i,row,vo);
            } catch (Exception e) {
                e.printStackTrace();
                this.errorCount++;
                this.errorRecord.add("导入第"+(i+1)+"行数据时出错了，错误原因:"+e.getMessage());
                result.addErrorRow(i+1);
                log.error("导入第{}行数据时出错了，错误原因:{}",(i+1),e.getMessage());
                flag = false;
            }
            if(flag){
                this.cacheList.add(vo);
                result.addSuccessRow(i+1);
            }
            if(this.cacheSize.intValue() == this.cacheList.size() || rowNum == i + 1){
                if(handler.batchSaveCallBack(this.cacheList)){
                    this.cacheList.clear();
                }
            }
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime-startTime;
        log.info("==> 导出操作执行完成,总耗时{}毫秒...",elapsedTime);
        result.setElapsedTime(elapsedTime);
        result.setErrorCount(this.errorCount);
        result.setErrorRecord(this.errorRecord);
        if(!result.getSuccessRow().isEmpty()){
            if(result.getSuccessRow().size() == rowNum - this.titleRowNum){
                result.setCode(ImportResult.Type.SUCCESS);
            }else{
                result.setCode(ImportResult.Type.PART_PASS);
            }
        }
        return result;
    }
}
