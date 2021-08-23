package org.kangjia.deprecated;

/**
 * 导出excel的钩子
 */
public interface ExcelExportHandler {

    /**
     * 创建Sheet之前
     */
    public void beforeSheetCreate();

    /**
     * 创建Sheet之后
     */
    public void afterSheetCreate();

    /**
     * 创建标题之前
     */
    public void beforeTitleCreate();

    /**
     * 创建标题之后
     */
    public void afterTitleCreate();

    /**
     * 创建内容之前
     */
    public void beforeDataCreate();

    /**
     * 创建内容之后
     */
    public void afterDataCreate();
}
