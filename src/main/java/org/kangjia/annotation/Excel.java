package org.kangjia.annotation;

import java.lang.annotation.*;

/**
 * 自定义导出Excel数据注解
 * 
 * @author ren
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Excel {
    /**
     * 导出到Excel中的名字.
     */
    public String value() default "";

    /**
     * 导出到Excel中的名字.
     */
    public String name() default "";

    /**
     * 导出时在excel中排序
     */
    public int sort() default Integer.MAX_VALUE;

    /**
     * 日期格式, 如: yyyy-MM-dd
     */
    public String dateFormat() default "";

    /**
     * 内容对应代理表达式,
     * 好多情况下我们喜欢用不同的数字(key)代替不同的状态(value)，这个属性可以用来配置它们之间的对应关系
     * (如: {"0:男","1:女","2:未知"})
     */
    public String[] converSingleMatchupExp() default {};

    /**
     * 内容区间匹配表达式,仅限数字类型的属性。表达式中用“[属性值]”表示实际属性值。
     * 有的时候我们的属性值是一个数字,导出的时候我们希望可以转换成更有利于阅读或者隐藏细节的情况,
     * 这种情况就需要配置区间代理表达式了。
     * 表达式格式为：if([属性值]>数值)文本,if([属性值]>数值)文本 。多个区间判断用“,”隔开，区间判断优先级以区间顺序为准。
     * (如成绩区间: {"if([score]<60):不及格","if([score]<=80):不错","if([score]<90):良好","if([score]<=100):优秀"})
     *  亦可简写为：(如:{"([score]<60):不及格","([score]<=80):不错","([score]<90):良好","([score]<=100):优秀"})
     *  此表达式还支持简单的计算语法，
     *  如速度区间：{"([journey]/[time]<=30):低速","([journey]/[time]<=50):中速","([journey]/[time]<=80):快速","([journey]/[time]<=120):高速","([journey]/[time]>120):飞得低"}
     */
    @Deprecated
    public String[] matchSectionExp() default {};

    /**
     * 计算表达式，支持简单的加、减、乘、除和取余计算，用这个表达式可以计算简单的临时字段，
     * 无需sql查询时候计算或是遍历集合计算。
     * 仅支持单条数据计算，如计算总价："[unitPrice]*[number]"
     */
    @Deprecated
    public String simpleCalculateExp() default "";

    /**
     * 导出类型（0数字 1字符串），选择数字类型时加前后缀无效
     */
    public ColumnType cellType() default ColumnType.STRING;
    
    /**
     * 设置某列只能是固定值
     */
    public String[] combo() default {};

    /**
     * 导出时在excel中每个数据列的高度 单位为字符
     */
    public short titleHeight() default 25;

    /**
     * 导出时在excel中每个数据列的高度 单位为字符
     */
    public short height() default 20;

    /**
     * 导出时在excel中每个列的宽 单位为字符
     */
    public double width() default 16;

    /**
     * 文字前缀,如￥ 100 变成￥100,加前缀后单元格格式只能是字符串型
     */
    public String prefix() default "";

    /**
     * 文字后缀,如% 90 变成90%,加后缀后单元格格式只能是字符串型
     */
    public String suffix() default "";

    /**
     * 导出操作当值为空时,字段的默认值
     */
    public String defaultValue() default "";

    /**
     * 是否导出数据,有些情况需要保持为空,希望用户填写这一列.
     */
    public boolean isExportDate() default true;

    /**
     * 是否拥有下级，有时候我们的实体类中的字段是另一个实体类，而那个实体类的属性也加了此注解
     */
    public boolean hasChildren() default false;

    /**
     * 是否自动统计数据,在最后追加一行统计数据总和(只能是number类型才能统计，否则报错)
     */
    public boolean isStatistics() default false;

    public enum ColumnType {
        NUMERIC(0), STRING(1);

        private final int value;

        ColumnType(int value)
        {
            this.value = value;
        }

        public int value()
        {
            return this.value;
        }
    }
}