package org.kangjia.hook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

/**
 * 导入excel的钩子
 */
public interface ExcelImportHandler<T> {

    /**
     * 处理每列数据的回调，可以在这里对每一列数据进行校验，
     * 一些比如像非空、唯一等校验需要用户自己编写校验规则，并set到对应的对象属性中
     * @param sheet       sheet对象
     * @param rowNum      当前行索引
     * @param row         当前行对象
     * @param column      当前列索引
     * @param cell        单元格对象
     * @param vo          导入的承接对象
     * @param value       单元格值
     */
    public boolean eachColumnCallBack(Sheet sheet, int rowNum, Row row, int column, Cell cell, T vo, Object value);

    /**
     * 处理每行数据的回调,在这里可以set导入数据之外的字段，如创建时间等,也可在这里查看到此对象内的数据信息
     * @param sheet     sheet对象
     * @param rowNum   行序号
     * @param row      行对象
     * @param vo       导入的承接对象
     * @return
     */
    public T eachRowCallBack(Sheet sheet, int rowNum, Row row, T vo);

    /**
     * 批量保存的回调，数据缓存区放满或者数据读取完执行此方法。
     * @param cacheList   数据缓存集合
     * @return
     */
    public boolean batchSaveCallBack(List<T> cacheList);
}
