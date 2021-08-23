package org.kangjia.importFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入操作的返回结果封装类
 * @author ren
 * @date 2021年08月19日 15:30:05
 */
public class ImportResult {

    /** 状态码 （200：全部成功，500：全部失败，300：部分成功） */
    private Type code;

    /** 发生错误的次数 */
    private Integer errorCount;

    /** 发生的错误描述记录 */
    private List<String> errorRecord;

    /** 发生错误的数据在excel文件中的行号 */
    private List<Integer> errorRow;

    /** 成功保存的数据在excel文件中的行号 */
    private List<Integer> successRow;

    /** 耗时（毫秒） */
    private Long elapsedTime;

    public Type getCode() {
        return code;
    }

    public void setCode(Type code) {
        this.code = code;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public List<String> getErrorRecord() {
        return errorRecord;
    }

    public void setErrorRecord(List<String> errorRecord) {
        this.errorRecord = errorRecord;
    }

    public List<Integer> getErrorRow() {
        return errorRow;
    }

    public void setErrorRow(List<Integer> errorRow) {
        this.errorRow = errorRow;
    }

    public void addErrorRow(Integer errorRow){
        if(this.errorRow == null){
            this.errorRow = new ArrayList<Integer>();
        }
        this.errorRow.add(errorRow);
    }

    public List<Integer> getSuccessRow() {
        return successRow == null ? new ArrayList<Integer>():successRow;
    }

    public void setSuccessRow(List<Integer> successRow) {
        this.successRow = successRow;
    }

    public void addSuccessRow(Integer successRow){
        if(this.successRow == null){
            this.successRow = new ArrayList<Integer>();
        }
        this.successRow.add(successRow);
    }

    public Long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * 状态类型
     */
    public enum Type {
        /** 全部成功 */
        SUCCESS(200),
        /** 部分成功 */
        PART_PASS(300),
        /** 全部错误 */
        ERROR(500);

        private final int value;

        Type(int value)
        {
            this.value = value;
        }

        public int value()
        {
            return this.value;
        }
    }

    @Override
    public String toString() {
        return "ImportResult{" +
                "code=" + code +
                ", errorCount=" + errorCount +
                ", errorRecord=" + errorRecord +
                ", errorRow=" + errorRow +
                ", successRow=" + successRow +
                ", elapsedTime=" + elapsedTime +
                '}';
    }
}
