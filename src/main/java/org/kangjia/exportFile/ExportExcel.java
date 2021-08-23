package org.kangjia.exportFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.kangjia.annotation.Excel;
import org.kangjia.annotation.ExcelTitle;
import org.kangjia.exceptions.ExportException;
import org.kangjia.extend.Customize;
import org.kangjia.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * 执行导出操作
 */
public class ExportExcel<T> {

    private final static Logger log = LoggerFactory.getLogger(ExportExcel.class);

    /**
     * 实体对象
     */
    private Class<T> clazz;

    /**
     * 导出数据列表
     */
    private List<T> list;

    /**
     * 工作表名称
     */
    private String sheetName;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * Excel注解列表
     */
    private List<Object[]> fields;

    /**
     * Excel sheet最大行数，如果数据量过大就生成多个sheet
     */
    private final int sheetSize = 65000;

    /**
     * 工作薄对象
     */
    private Workbook wb;

    /**
     * 工作表对象
     */
    private Sheet sheet;

    /**
     * web响应
     */
    private HttpServletResponse response;

    /**
     * 样式列表
     */
    private Map<String, CellStyle> styles;

    /**
     * 统计列表
     */
    private Map<String, Double> statistics = new HashMap<String, Double>();

    /**
     * 自定义样式接口
     */
    private Customize customize;

    public ExportExcel(Class<T> clazz, List<T> list, String sheetName, String fileName, HttpServletResponse response,
                       Customize customize){
        log.info("==> 正在初始化参数...");
        this.clazz = clazz;
        this.list = list == null ? new ArrayList<>():list;
        this.sheetName = sheetName;
        this.fileName = this.encodingFileName(fileName);
        this.response = response;
        this.wb = new SXSSFWorkbook();
        //得到所有要导出的字段
        this.createExcelField();
        this.customize = customize;
        log.info("==> 参数初始化完成...");
    }

    /** ======================================初始化部分开始====================================== **/

    /**
     * 得到所有要导出的字段（加注解的字段）
     */
    private void createExcelField(){
        log.debug("==> 正在检索加注解的字段...");
        this.fields = new ArrayList<Object[]>();
        //定义对象字段的集合
        List<Field> tempFields = new ArrayList<Field>();
        //把实体类父类的所有属性加进集合  (考虑到性能问题，这里就只检索了一层父类的属性)
        tempFields.addAll(Arrays.asList(this.clazz.getSuperclass().getDeclaredFields()));
        //把实体类的所有属性加进集合
        tempFields.addAll(Arrays.asList(this.clazz.getDeclaredFields()));
        //递归把加了注解的属性都放到集合中
        this.recursionFieldToList(tempFields,"");
        //给excel内容标题进行排序
        log.debug("==> 正在排序字段...");
        //this.fields = this.fields.stream().sorted(Comparator.comparing(fieldsObj -> ((Excel) fieldsObj[1]).sort())).collect(Collectors.toList());
        this.sortField();
    }

    /**
     * 递归把加了注解的属性都放到集合中
     * @param tempFields      所有的属性集合
     * @param parentTitles    父标题,多层之间用^隔开
     */
    private void recursionFieldToList(List<Field> tempFields, String parentTitles){
        //遍历所有的属性
        for(Field field:tempFields){
            //判断属性有没有加Excel注解
            if (field.isAnnotationPresent(Excel.class)) {
                //得到该属性上的注解对象
                Excel attr = field.getAnnotation(Excel.class);
                //把加了注解的属性放到fields集合里
                if(attr.hasChildren()){
                    //如果属性有子级就继续扫描子级的属性
                    String typeName = field.getGenericType().getTypeName();
                    try {
                        //获取到子级的类对象
                        Class<?> aClass = Class.forName(typeName);
                        //获取子级的所有属性
                        Field[] declaredFields = aClass.getDeclaredFields();
                        //把加了注解的属性都放到集合中
                        recursionFieldToList(Arrays.asList(declaredFields), parentTitles+"^"+this.getTitleText(attr,field));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    this.fields.add(new Object[] {field, attr,
                            StringUtils.isNotEmpty(parentTitles)?
                                    parentTitles.substring(1)+"^"+this.getTitleText(attr,field):
                                    this.getTitleText(attr,field)});
                }
            }
        }
    }

    /**
     * 排序
     */
    private void sortField() {
        Collections.sort(this.fields, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] prior, Object[] after) {
                Excel priorAttr = (Excel)prior[1];
                Excel afterAttr = (Excel)after[1];
                return priorAttr.sort() - afterAttr.sort();
            }
        });
    }

    /**
     * 编码文件名
     * @param fileName      文件名
     * @return fileName
     */
    private String encodingFileName(String fileName) {
        if(StringUtils.isNotEmpty(fileName)){
            if(fileName.lastIndexOf(".") != -1){
                String suffix = fileName.substring(fileName.lastIndexOf("."));
                if(!".xlsx".equals(suffix.toLowerCase()) && !".xls".equals(suffix.toLowerCase())){
                    fileName = fileName + ".xlsx";
                }
            }else{
                fileName = fileName + ".xlsx";
            }
        }else{
            fileName = this.sheetName + ".xlsx";
        }
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 创建sheet,数据量大就创建多个sheet
     * @param sheetNo
     * @param index
     */
    private void createSheet(double sheetNo, int index){
        log.debug("==> 开始创建sheet...");
        //在workbook中添加一个sheet,对应Excel文件中的sheet
        this.sheet = this.wb.createSheet();
        //创建表格样式
        log.debug("开始定义表格样式...");
        this.styles = this.createStyles();
        log.debug("定义表格样式完成...");
        // 设置工作表的名称
        log.debug("开始设置工作表的名称...");
        if (sheetNo == 0) {
            this.wb.setSheetName(index, this.sheetName);
        } else {
            this.wb.setSheetName(index, this.sheetName + index);
        }
        log.debug("设置工作表名称完成...");
        log.debug("==> sheet创建完成...");
    }

    /** ======================================初始化部分结束====================================== **/


    /** ======================================工具方法部分开始====================================== **/

    /**
     * 创建表格样式
     *
     * @return 样式列表
     */
    private Map<String, CellStyle> createStyles() {
        if(this.customize == null){
            return new Customize() {
                @Override
                public Map<String, CellStyle> createCustomizeStyles(Workbook wb) {
                    Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
                    CellStyle dataStyle = wb.createCellStyle();
                    //设置单元格水平对齐方式
                    dataStyle.setAlignment(HorizontalAlignment.CENTER);
                    //设置单元格垂直对齐方式
                    dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    //设置右边框
                    dataStyle.setBorderRight(BorderStyle.THIN);
                    //设置右边框颜色
                    dataStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                    //设置左边框
                    dataStyle.setBorderLeft(BorderStyle.THIN);
                    //设置左边框颜色
                    dataStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                    //设置上边框
                    dataStyle.setBorderTop(BorderStyle.THIN);
                    //设置上边框颜色
                    dataStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                    //设置下边框
                    dataStyle.setBorderBottom(BorderStyle.THIN);
                    //设置下边框颜色
                    dataStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                    //设置字体样式
                    Font dataFont = wb.createFont();
                    dataFont.setFontName("Arial");
                    dataFont.setFontHeightInPoints((short) 10);
                    dataStyle.setFont(dataFont);
                    styles.put("data", dataStyle);

                    CellStyle headerStyle = wb.createCellStyle();
                    //设置单元格水平对齐方式
                    headerStyle.setAlignment(HorizontalAlignment.CENTER);
                    //设置单元格垂直对齐方式
                    headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    //设置右边框
                    headerStyle.setBorderRight(BorderStyle.MEDIUM);
                    //设置右边框颜色
                    headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    //设置左边框
                    headerStyle.setBorderLeft(BorderStyle.MEDIUM);
                    //设置左边框颜色
                    headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    //设置上边框
                    headerStyle.setBorderTop(BorderStyle.MEDIUM);
                    //设置上边框颜色
                    headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
                    //设置下边框
                    headerStyle.setBorderBottom(BorderStyle.MEDIUM);
                    //设置下边框颜色
                    headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                    //设置前景颜色
                    headerStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
                    //设置前景的风格样式
                    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    //设置字体样式
                    Font headerFont = wb.createFont();
                    headerFont.setFontName("Arial");
                    headerFont.setFontHeightInPoints((short) 10);
                    headerFont.setBold(true);
                    headerFont.setColor(IndexedColors.BLACK.getIndex());
                    headerStyle.setFont(headerFont);
                    styles.put("header", headerStyle);

                    CellStyle totalStyle = wb.createCellStyle();
                    //设置单元格水平对齐方式
                    totalStyle.setAlignment(HorizontalAlignment.RIGHT);
                    //设置单元格垂直对齐方式
                    totalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    //设置右边框
                    totalStyle.setBorderRight(BorderStyle.THIN);
                    //设置右边框颜色
                    totalStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                    //设置左边框
                    totalStyle.setBorderLeft(BorderStyle.THIN);
                    //设置左边框颜色
                    totalStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                    //设置上边框
                    totalStyle.setBorderTop(BorderStyle.THIN);
                    //设置上边框颜色
                    totalStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                    //设置下边框
                    totalStyle.setBorderBottom(BorderStyle.THIN);
                    //设置下边框颜色
                    totalStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                    //设置字体样式
                    Font totalFont = wb.createFont();
                    totalFont.setFontName("Arial");
                    totalFont.setFontHeightInPoints((short) 10);
                    totalStyle.setFont(totalFont);
                    styles.put("total", totalStyle);
                    return styles;
                }
            }.createCustomizeStyles(this.wb);
        }else{
            return this.customize.createCustomizeStyles(this.wb);
        }
    }

    /**
     * 得到标题文本
     * @param attr    属性上的注解对象
     * @param field   属性对象
     * @return
     */
    private String getTitleText(Excel attr,Field field){
        if (StringUtils.isNotEmpty(attr.name())) {
            return attr.name();
        }else if(StringUtils.isNotEmpty(attr.value())){
            return attr.value();
        }else{
            return field.getName();
        }
    }

    /**
     *  得到某类中加了注解并可进入的子属性
     * @param clazz
     * @return
     */
    private List<Field> getEnterableField(Class<?> clazz){
        List<Field> fieldList = new ArrayList<Field>();
        Field[] fields = clazz.getDeclaredFields();
        for(Field field:fields){
            if (field.isAnnotationPresent(Excel.class)) {
                Excel attr = field.getAnnotation(Excel.class);
                if(attr.hasChildren()){
                    fieldList.add(field);
                }
            }
        }
        return fieldList;
    }

    /**
     * 获取属性值
     * @param field
     * @param vo
     * @return
     */
    private Object getFieldValue(Field field, Object vo)throws ExportException{
        Class<? extends Object> clazz = vo.getClass();
        String fieldName = field.getName();
        String methodName = "get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
        Object invoke = null;
        try {
            Method method = clazz.getMethod(methodName, null);
            invoke = method.invoke(vo);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new ExportException("没有找到属性“"+field.getName()+"”的get方法。");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ExportException("属性“"+field.getName()+"”的get方法无法调用，请检查该方法的访问修饰符。");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new ExportException("调用属性“"+field.getName()+"”的get方法时出错，错误原因："+e.getMessage());
        }
        return invoke;
    }

    /** ======================================工具方法部分结束====================================== **/


    /** ======================================标题部分开始====================================== **/

    /**
     * 创建文件内容的标题
     */
    private int createDataTitle(){
        //定义标题行数，默认1行
        int titleRowNum = 1;
        //如果类上加了注解就创建一行总标题
        String totalTitle = null;
        if(this.clazz.isAnnotationPresent(ExcelTitle.class)){
            ExcelTitle clazzAnnotation = this.clazz.getAnnotation(ExcelTitle.class);
            totalTitle = clazzAnnotation.value();
        }
        List<String> titles = new ArrayList<>();
        List<String> normTitles = new ArrayList<>();
        Map<String,Integer> tempMap = new HashMap<>();
        for(Object[] obj:this.fields){
            String title = (String) obj[2];

            String[] temp = title.split("\\^");
            //取最大长度值
            if(temp.length > titleRowNum){
                titleRowNum = temp.length;
            }
            normTitles.add(title);
            for(String t:temp){
                titles.add(t);
            }
        }

        if(totalTitle != null){
            titleRowNum++;
            for(Object[] o:fields){
                o[2] = totalTitle+"^"+o[2];
            }
            tempMap.put(totalTitle,this.fields.size());
        }

        //如果只有一行标题，不需要分析合并的事
        if(titleRowNum == 1){
            Row row = this.sheet.createRow(0);
            int count = 0;
            for(String title:normTitles){
                Excel attr = (Excel)this.fields.get(count)[1];
                //设置单元格固定值
                if(attr.combo().length>0){
                    this.setCellFixedValue(this.sheet,attr.combo(),
                            titleRowNum,this.sheetSize-1,count,count);
                }
                // 设置列宽
                sheet.setColumnWidth(count, (int) ((attr.width() + 0.72) * 256));
                // 设置行高
                row.setHeight((short) (attr.titleHeight() * 20));
                Cell cell = row.createCell(count++);
                cell.setCellStyle(this.styles.get("header"));
                cell.setCellValue(title);
            }
            return titleRowNum;
        }

        //统计每个标题出现的次数(便于横向合并)
        for(String title:titles){
            if(tempMap.containsKey(title)){
                int count = tempMap.get(title);
                tempMap.put(title,++count);
            }else{
                tempMap.put(title,1);
            }
        }

        //循环创建每行标题
        List<Integer> lastRowWantVerticalMergerList = new ArrayList<Integer>();
        for(int i=0;i<titleRowNum;i++){
            Row row = this.sheet.createRow(i);
            for(int j = 0; j < this.fields.size();){
                Object[] obj = this.fields.get(j);
                Excel attr = (Excel) obj[1];
                String title = (String) obj[2];

                // 设置行高
                row.setHeight((short) (attr.titleHeight() * 20));
                // 设置列宽
                sheet.setColumnWidth(j, (int) ((attr.width() + 0.72) * 256));

                String[] temp = title.split("\\^");
                int titleLength = temp.length;

                //如果循环到的此列没有被之前行纵向合并就创建此列
                if(!lastRowWantVerticalMergerList.contains(j)){
                    Cell cell = row.createCell(j);

                    //设置单元格固定值
                    if(attr.combo().length>0){
                        this.setCellFixedValue(this.sheet,attr.combo(),
                                titleRowNum,this.sheetSize-1,j,j);
                    }

                    CellRangeAddress region = null;
                    if(titleLength == 1){
                        region = new CellRangeAddress(i, titleRowNum-1, j, j);
                        lastRowWantVerticalMergerList.add(j++);
                        cell.setCellValue(title);
                    }else{
                        //创建哪一行就取哪个下标的标题
                        String currentTitle = temp[i];
                        if(titleLength - 1 > i){  //如果当前行不是数组末尾下标就横向合并
                            region = new CellRangeAddress(i, i, j, j+tempMap.get(currentTitle)-1);
                            for (int c = j+1;c<j+tempMap.get(currentTitle);c++){
                                Cell nullCell = row.createCell(c);
                                nullCell.setCellStyle(this.styles.get("header"));
                            }
                            j += tempMap.get(currentTitle);
                        }else if(titleLength - 1 == i && titleLength != titleRowNum){  //如果当前行在数组末尾就纵向合并到标题最后一行
                            region = new CellRangeAddress(i, titleRowNum-1, j, j);
                            lastRowWantVerticalMergerList.add(j++);
                        }else{ j++; }
                        cell.setCellValue(currentTitle);
                    }
                    if(region != null){
                        try {
                            this.sheet.addMergedRegion(region);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                            throw new ExportException("创建复杂标题出错了，子级内的属性顺序必须相邻。");
                        }
                    }
                    cell.setCellStyle(this.styles.get("header"));
                }else{
                    Cell cell = row.createCell(j++);
                    cell.setCellStyle(this.styles.get("header"));
                }
            }
        }
        return titleRowNum;
    }

    /**
     * 设置单元格的固定值
     * @param sheet          sheet对象
     * @param textList       下拉框显示的文本数组
     * @param firstRow       开始行
     * @param endRow         结束行
     * @param firstCol       开始列
     * @param endCol         结束列
     */
    private void setCellFixedValue(Sheet sheet, String[] textList, int firstRow, int endRow, int firstCol, int endCol){
        DataValidationHelper helper = sheet.getDataValidationHelper();
        // 加载下拉列表内容
        DataValidationConstraint constraint = helper.createExplicitListConstraint(textList);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        // 处理Excel兼容性问题
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }
        sheet.addValidationData(dataValidation);
    }

    /** ======================================标题部分结束====================================== **/


    /** ======================================数据部分开始====================================== **/

    /**
     * 填充excel数据
     * @param index        序号（第几份sheet）
     * @param titleLineNum 标题行数
     */
    private void fillExcelData(int index, int titleLineNum)throws ExportException{
    	int startNo = index * this.sheetSize;
        int endNo = Math.min(startNo + this.sheetSize, this.list.size());
        for (int i = startNo; i < endNo; i++){
            Row row = this.sheet.createRow(titleLineNum++);
            // 得到导出对象.
            T vo = (T) this.list.get(i);

            int column = 0;
            //遍历属性列表
            for (Object[] os : this.fields) {
                Field field = (Field) os[0];
                Excel excel = (Excel) os[1];
                // 设置实体类私有属性可访问
                field.setAccessible(true);
                //创建数据单元格，如果当前的属性属于T对象，或者T对象里面的对象属性
                if(field.getDeclaringClass().equals(vo.getClass())){
                    this.createDataCell(excel, vo, field, row, column++);
                }else{
                    //递归进入找到当前属性所属的对象
                    Object o = this.recursionGetFieldType(field, vo);
                    this.createDataCell(excel, o, field, row, column++);
                }
            }
        }
    }

    /**
     * 递归找到当前属性所属的对象
     * @param field    当前列所代表的属性
     * @param o        可能存在的对象
     * @return
     */
    private Object recursionGetFieldType(Field field, Object o) throws ExportException{
        Class<? extends Object> clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if(Arrays.asList(fields).contains(field)){
            return o;
        }
        List<Field> enterableFieldList = this.getEnterableField(clazz);
        for(Field childField:enterableFieldList){
            String fieldName = childField.getName();
            String methodName = "get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
            try {
                Method method = clazz.getMethod(methodName, null);
                Object obj = method.invoke(o);
                return recursionGetFieldType(field, obj);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new ExportException("没有找到属性“"+field.getName()+"”的get方法。");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new ExportException("属性“"+field.getName()+"”的get方法无法调用，请检查该方法的访问修饰符。");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new ExportException("调用属性“"+field.getName()+"”的get方法时出错，错误原因："+e.getMessage());
            }
        }
        throw new ExportException("没有找到属性“"+field.getName()+"”的承载对象，或承载对象不存在。");
    }

    /**
     * 创建数据单元格
     * @param attr          每个属性上的注解对象
     * @param vo            导出对象
     * @param field         属性对象
     * @param row           行对象
     * @param column        列数（第几列）
     * @return
     */
    private Cell createDataCell(Excel attr, Object vo, Field field, Row row, int column)throws ExportException{
        // 设置行高
        row.setHeight((short) (attr.height() * 20));
        // 创建cell
        Cell cell = row.createCell(column);
        //设置单元格样式
        cell.setCellStyle(this.styles.get("data"));
        // 根据Excel中设置情况决定是否导出数据,有些情况需要保持为空,希望用户填写这一列.
        if (attr.isExportDate()) {
            // 获取到属性值
            Object value = this.getFieldValue(field, vo);
            // 设置单元格信息
            this.setCellVo(value, attr, cell);

            // 合计统计信息
            this.addStatisticsData(this.getTitleText(attr,field), Convert.toStr(value), attr);
        }
        return cell;
    }

    /**
     * 设置单元格信息
     *
     * @param value   单元格值
     * @param attr    注解相关
     * @param cell    单元格对象
     */
    private void setCellVo(Object value, Excel attr, Cell cell)throws ExportException{
        // 获取注解日期格式化属性
        String dateFormat = attr.dateFormat();
        // 获取注解内容对应代理表达式
        String[] converSingleMatchupExp = attr.converSingleMatchupExp();
        // 获取注解内容区间代理表达式
        String[] matchSectionExp = attr.matchSectionExp();
        // 获取注解内容计算表达式
        String simpleCalculateExp = attr.simpleCalculateExp();
        // 获取注解前缀属性
        String prefix = attr.prefix();
        // 获取注解后缀属性
        String suffix = attr.suffix();
        // 获取注解默认值属性
        String defaultValue = attr.defaultValue();

        //先判断数据是否为空
        other:if(StringUtils.isNotNull(value)){
            String strValue = Convert.toStr(value);
            if (StringUtils.isNotEmpty(dateFormat)) {//如果转换时间格式
                if("Date".equals(value.getClass().getSimpleName())){
                    cell.setCellValue(prefix + DateUtils.dateToStr((Date)value, dateFormat) + suffix);
                    break other;
                }else{
                    throw new ExportException("只有Date类型的数据才可以使用注解的dateFormat属性。");
                }
            } else if (StringUtils.isNotEmpty(converSingleMatchupExp)) {//如果转换内容对应代理表达式
                strValue = this.converSingleMatchupByExp(strValue, converSingleMatchupExp);
            } else if (StringUtils.isNotEmpty(matchSectionExp)) {//如果转换内容区间匹配表达式
                strValue = this.matchSectionByExp(matchSectionExp);
            } else if (StringUtils.isNotEmpty(simpleCalculateExp)) {//如果转换计算表达式
                strValue = this.simpleCalculateByExp(strValue, matchSectionExp);
            }
            if(Excel.ColumnType.STRING == attr.cellType()){
                cell.setCellValue(prefix + strValue + suffix);
            }else if(Excel.ColumnType.NUMERIC == attr.cellType()){
                Double dvalue = Convert.toDouble(value);
                if(dvalue == null){
                    throw new ExportException("转换数据类型时发生错误，注解属性选择NUMERIC类型，但字段值无法转为double类型。");
                }
                cell.setCellValue(dvalue);
            }
        }else{
            if(StringUtils.isNotEmpty(defaultValue)){
                if(Excel.ColumnType.STRING == attr.cellType()){
                    cell.setCellValue(prefix + defaultValue + suffix);
                }else if(Excel.ColumnType.NUMERIC == attr.cellType()){
                    Double dvalue = Convert.toDouble(defaultValue);
                    if(dvalue == null){
                        throw new ExportException("转换数据类型时发生错误，注解属性选择NUMERIC类型，但字段值为空，默认值无法转为double类型。");
                    }
                    cell.setCellValue(dvalue);
                }
            }
        }
    }

    /** ======================================数据部分结束====================================== **/


    /** ======================================统计部分开始====================================== **/

    /**
     * 创建统计行
     */
    private void addStatisticsRow() {
        //判断如果有统计数据
        if (this.statistics.size() > 0) {
            log.debug("==> 开始填充统计数据...");
            //在最后创建一行
            Row row = this.sheet.createRow(this.sheet.getLastRowNum() + 1);
            // 设置行高
            row.setHeight((short) (((Excel)this.fields.get(0)[1]).height() * 20));
            //获取到统计数据的键
            Set<Map.Entry<String, Double>> entries = this.statistics.entrySet();
            //创建列,合并成一行
            Cell cell = row.createCell(0);
            StringBuffer sb = new StringBuffer("合计：");
            //遍历统计数据
            Iterator<Map.Entry<String, Double>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Double> entry = iterator.next();
                sb.append("     ");
                sb.append(entry.getKey()+"："+entry.getValue()+",");
            }
            cell.setCellValue(sb.substring(0,sb.length()-1));
            CellRangeAddress region = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, this.fields.size()-1);
            this.sheet.addMergedRegion(region);
            //设置单元格样式
            cell.setCellStyle(this.styles.get("total"));
            for(int i = 1; i < this.fields.size();i++){
                Cell nullCell = row.createCell(i);
                nullCell.setCellStyle(this.styles.get("total"));
            }
            //最后清空集合
            this.statistics.clear();
            log.debug("==> 统计数据填充完成...");
        }
    }

    /**
     * 合计统计信息
     * @param columnName  列名
     * @param text        要统计的数据
     * @param attr        注解对象
     */
    private void addStatisticsData(String columnName, String text, Excel attr) {
        //如果注解有设置统计属性
        if (attr.isStatistics()) {
            Double temp = 0D;
            if (!this.statistics.containsKey(columnName)) {
                this.statistics.put(columnName, temp);
            }
            temp = Double.valueOf(text);
            this.statistics.put(columnName, this.statistics.get(columnName) + temp);
        }
    }

    /** ======================================统计部分结束====================================== **/


    /** ======================================解析表达式部分开始====================================== **/

    /**
     * 解析内容对应代理表达式 如{"0:男","1:女","2:未知"}
     *
     * @param propertyValue 参数值
     * @param converterExp  注解表达式
     * @return 解析后值
     */
    private String converSingleMatchupByExp(String propertyValue, String[] converterExp) throws ExportException {
        String result = null;
        for (String item : converterExp) {
            String[] itemArray = item.split(":");
            if(itemArray.length < 2){
                throw new ExportException("字段" + propertyValue + "的注解表达式错误，无法正确解析！");
            }
            if (propertyValue.trim().equals(itemArray[0].trim())) {
                result = itemArray[1];
            }
        }
        return result;
    }

    /**
     * 解析内容区间代理表达式 如{"([score]<60):不及格","([score]<=80):不错","([score]<90):良好","([score]<=100):优秀"}
     * @param converterExp  注解表达式
     * @return 解析后值
     */
    private String matchSectionByExp(String[] converterExp)throws ExportException{
        return "";
    }

    /**
     * 解析计算表达式，如：[unitPrice]*[number]
     * @param propertyValue  参数值
     * @param converterExp   注解表达式
     * @return
     */
    private String simpleCalculateByExp(String propertyValue, String[] converterExp)throws ExportException{
        BigDecimal bigDecimal = BigDecimal.ZERO;
        return String.valueOf(bigDecimal.doubleValue());
    }

    /** ======================================解析表达式部分结束====================================== **/


    /** ======================================导出入口====================================== **/

    /**
     * 执行导出操作
     */
    public void execute()throws ExportException{
        long startTime = System.currentTimeMillis();
        log.info("==> 开始执行导出操作...");
        //定义输出流
        OutputStream out = null;
        try {
            // 取出一共有多少个sheet.
            double sheetNo = Math.ceil(this.list.size() / this.sheetSize);
            for (int i = 0; i <= sheetNo; i++){

                //创建sheet,数据量大就创建多个sheet
                this.createSheet(sheetNo, i);

                //创建文件内容的标题
                log.debug("==> 开始创建文件内容的标题...");
                int titleLineNum = this.createDataTitle();
                log.debug("==> 文件内容的标题创建完成...");

                //填充excel数据
                log.debug("==> 开始填充excel数据...");
                this.fillExcelData(i,titleLineNum);
                log.debug("==> 填充excel数据完成...");

                //创建右下角统计行
                this.addStatisticsRow();
            }
            //创建文件名
            this.response.setHeader("Content-disposition", "attachment; filename="+this.fileName);
            //响应到客户端
            out = this.response.getOutputStream();

            log.debug("==> 正在写入到响应流中...");
            wb.write(out);
            out.flush();
            long endTime = System.currentTimeMillis();
            log.info("==> 导出操作执行完成,总耗时{}毫秒...",endTime-startTime);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (this.wb != null) {
                try {
                    this.wb.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
