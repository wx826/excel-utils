package org.kangjia.utils;

import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 时间工具类
 * 
 * @author ren
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils{
	
	/** 年 */
	public final static int YEAR = 1;
	
	/** 月 */
	public final static int MONTH = 2;
	
	/** 日 */
	public final static int DAY = 3;
	
	/** 时 */
	public final static int HOUR = 4;
	
	/** 分 */
	public final static int MINUTE = 5;
	
	/** 秒 */
	public final static int SECOND = 6;
	
	/** 毫秒 */
	public final static int MILLISECOND = 7;
	
	/** 星期 */
	public final static int WEEK = 8;
	
	/** 24小时制 */
	public final static String HH = "HH";

	/** 12小时制 */
	public final static String hh = "hh";

    /**
     * 获取当前日期,返回字符串型，默认转换格式为yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getDate(){
        return getDate("yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * 获取当前日期,返回字符串型
     * @param format  自定义转换格式
     * @return
     */
    public static String getDate(final String format){
        return dateToStr(new Date(), format);
    }

    /**
     * date类型转String类型 ,默认格式为“yyyy-MM-dd HH:mm:ss”
     * @param date      要转换的日期
     * @return
     */
    public static String dateToStr(final Date date){
        return dateToStr(date,"yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * date类型转String类型
     * @param date      要转换的日期
     * @param format    自定义转换格式
     * @return
     */
    public static String dateToStr(final Date date, final String format){
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * String类型转Date类型   (日期字符串必须为“yyyy-MM-dd HH:mm:ss”)
     * @param dateStr     日期字符串
     * @return
     */
    public static Date strToDate(final String dateStr){
        return strToDate(dateStr,"yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * String类型转Date类型
     * @param dateStr     日期字符串
     * @param format      日期字符串格式
     * @return
     */
    public static Date strToDate(final String dateStr, final String format){
        try{
            return new SimpleDateFormat(format).parse(dateStr);
        }catch (ParseException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * String类型转String类型 (一种时间格式字符串转成另一种时间格式字符串)
     * @param dateStr       日期字符串
     * @param oldFormat     旧日期字符串格式
     * @param newFormat     新日期字符串格式
     * @return  返回新日期字符串格式
     */
    public static String strToStr(final String dateStr, final String oldFormat, final String newFormat){
    	return dateToStr(strToDate(dateStr, oldFormat), newFormat);
    }
    
    /**
     * 时间戳转时间
     * @param timestamp
     * @return
     */
    public static Date timestampToDate(String timestamp){
        Long dateLong = Long.parseLong(timestamp);
        Date date = new Date(dateLong);
        return date;
    }
    
    /**
     * 拼接日期与时间
     * @param date         日期格式化字符串
     * @param dateFormat   日期格式
     * @param time         时间格式化字符串
     * @param timeFormat   时间格式
     * @return
     */
    public static Date jointDateAndTime(String date, String dateFormat, String time, String timeFormat){		
        return jointDateAndTime(strToDate(date, dateFormat),dateToStr(strToDate(time, timeFormat), "HH:mm:ss"));
    }
    
    /**
     * 拼接日期与时间
     * @param date         日期
     * @param time         时间格式化字符串
     * @param timeFormat   时间格式
     * @return
     */
    public static Date jointDateAndTime(Date date, String time, String timeFormat){
		return jointDateAndTime(date,dateToStr(strToDate(time, timeFormat),"HH:mm:ss"));
    }
    
    /**
     * 拼接日期与时间
     * @param date  日期
     * @param time  时间格式化字符串  格式必须为HH:mm:ss
     * @return
     */
    public static Date jointDateAndTime(Date date,String time){       
	    String dateStr = dateToStr(date,"yyyy-MM-dd");
	    dateStr = dateStr + " " + time;
	    return strToDate(dateStr,"yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * 获取服务器启动时间
     * @return
     */
    public static Date getServerStartDate(){
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }
    
    /**
     * 比较两个时间大小
     * @param date1     日期字符串1
     * @param format1   日期字符串1格式
     * @param date2     日期字符串2
     * @param format2   日期字符串2格式
     * @return int  大于1    小于-1    等于0
     */
    public static int compareDate(String date1, String format1, String date2, String format2){		
		return compareDate(strToDate(date1, format1),strToDate(date2, format2));
    }
    
    /**
     * 比较两个时间大小
     * @param date1   日期字符串1
     * @param date2   日期字符串2
     * @param format  日期字符串格式
     * @return int  大于1    小于-1    等于0
     */
    public static int compareDate(String date1, String date2, String format){		
		return compareDate(strToDate(date1, format),strToDate(date2, format));
    }
    
    /**
     * 比较两个时间大小
     * @param date1  日期1
     * @param date2  日期2
     * @return int  大于1    小于-1    等于0
     */
    public static int compareDate(Date date1, Date date2){
        long time1 = date1.getTime();
        long time2 = date2.getTime();
        if(time1 > time2){
            return 1;
        }else if(time1 < time2){
            return -1;
        }else{
            return 0;
        }
    }
    
    /**
     * 计算两个时间差
     * @param startDate   开始时间
     * @param startFormat 开始时间格式
     * @param endDate     结束时间
     * @param endFormat   结束时间格式
     * @return  默认返回相差的毫秒值
     */
    public static String computeDatePoor(String startDate, String startFormat, String endDate, String endFormat) {
		return computeDatePoor(startDate, startFormat, endDate, endFormat, MILLISECOND);
    }
    
    /**
     * 计算两个时间差
     * @param startDate   开始时间
     * @param startFormat 开始时间格式
     * @param endDate     结束时间
     * @param endFormat   结束时间格式
     * @param unit        计算出的差值的单位
     * @return
     */
    public static String computeDatePoor(String startDate, String startFormat, String endDate, String endFormat, int unit) {   	
    	Date start = strToDate(startDate, startFormat);
		Date end = strToDate(endDate, endFormat);
    	try {
			return computeDatePoor(start,end,unit);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
    
    /**
     * 计算两个时间差
     * @param startDate   开始时间
     * @param endDate     结束时间
     * @param unit        计算出的差值的单位
     * @return
     * @throws Exception 
     */
    public static String computeDatePoor(Date startDate, Date endDate, int unit) throws Exception{
    	int i = compareDate(startDate,endDate);
    	long time1 = 0;
    	long time2 = 0;
    	if(i > 0) {
    		time1 = startDate.getTime();
    		time2 = endDate.getTime();
    	}else if(i < 0) {
    		time1 = endDate.getTime();
    		time2 = startDate.getTime();
    	}else {
    		return "0";
    	}
    	long res = time1 - time2;
    	switch (unit) {
			case DAY:return String.valueOf((res/(1000 * 24 * 60 * 60)));
			case HOUR:return String.valueOf((res/(1000 * 60 * 60)));
			case MINUTE:return String.valueOf((res/(1000 * 60)));
			case SECOND:return String.valueOf((res/1000));
			case MILLISECOND:return String.valueOf(res);
			default:throw new Exception("时间差单位不可以自定义哦！");
		}
    }

    /**
     * 计算两个时间差
     * @param startDate   开始时间
     * @param endDate     结束时间
     * @return   返回可阅读格式，如：2天5小时23分钟
     */
    public static String computeDatePoor(Date startDate, Date endDate){
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 得到某时间之前的时间
     * @param dateStr     时间字符串
     * @param dateFormat  时间格式
     * @param gap         差值
     * @param unit        单位
     * @return
     */
    public static Date getBeforeDate(String dateStr, String dateFormat, int gap, int unit){
		return getBeforeDate(strToDate(dateStr, dateFormat),gap,unit);
    }
    
    /**
     * 得到某时间之前的时间
     * @param date     时间
     * @param gap      差值
     * @param unit     单位
     * @return
     */
    public static Date getBeforeDate(Date date, int gap, int unit){
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	switch (unit) {
	    	case YEAR:c.add(Calendar.YEAR,-gap);break;
	    	case MONTH:c.add(Calendar.MONTH,-gap);break;
			case DAY:c.add(Calendar.DAY_OF_MONTH,-gap);break;
			case HOUR:c.add(Calendar.HOUR_OF_DAY,-gap);break;
			case MINUTE:c.add(Calendar.MINUTE,-gap);break;
			case SECOND:c.add(Calendar.SECOND,-gap);break;
			case MILLISECOND:c.add(Calendar.MILLISECOND,-gap);break;
			case WEEK:c.add(Calendar.DAY_OF_WEEK,-gap);break;
			default:try {
				throw new Exception("时间差单位不可以自定义哦！");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return c.getTime();
    }
    
    /**
     * 得到某时间之后的时间
     * @param dateStr     时间字符串
     * @param dateFormat  时间格式
     * @param gap         差值
     * @param unit        单位
     * @return
     */
    public static Date getAfterDate(String dateStr, String dateFormat, int gap, int unit){
		return getAfterDate(strToDate(dateStr, dateFormat),gap,unit);
		
    }
    
    /**
     * 得到某时间之后的时间
     * @param date  时间
     * @param gap   差值
     * @param unit  单位
     * @return
     */
    public static Date getAfterDate(Date date, int gap, int unit){
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	switch (unit) {
	    	case YEAR:c.add(Calendar.YEAR,gap);break;
	    	case MONTH:c.add(Calendar.MONTH,gap);break;
			case DAY:c.add(Calendar.DAY_OF_MONTH,gap);break;
			case HOUR:c.add(Calendar.HOUR_OF_DAY,gap);break;
			case MINUTE:c.add(Calendar.MINUTE,gap);break;
			case SECOND:c.add(Calendar.SECOND,gap);break;
			case MILLISECOND:c.add(Calendar.MILLISECOND,gap);break;
			case WEEK:c.add(Calendar.DAY_OF_WEEK,gap);break;
			default:try {
				throw new Exception("时间差单位不可以自定义哦！");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return c.getTime();
    }

    /**
     * 获取某日期的年
     * @param date    日期
     * @return int
     */
    public static int getYear(Date date) {
        return Integer.valueOf(dateToStr(date,"yyyy"));
    }

    /**
     * 获取某日期的月
     * @param date   日期
     * @return int
     */
    public static int getMonth(Date date) {
        return Integer.valueOf(dateToStr(date,"MM"));
    }

    /**
     * 获取某日期的日
     * @param date   日期
     * @return string
     */
    public static int getDay(Date date) {
        return Integer.valueOf(dateToStr(date,"dd"));
    }
    
    /**
     * 获取某日期在当前年是第几天
     * @param date   日期
     * @return string
     */
    public static int getDayForYear(Date date) {
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
        return c.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取某日期是星期几
     * @param date   日期
     * @return string
     */
    public static int getDayForWeek(Date date) {
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	int week = c.get(Calendar.DAY_OF_WEEK) - 1;
        return week == 0 ? 7:week;
    }

    /**
     * 获取某日期的时，默认24小时制
     * @param date   日期
     * @return int
     */
    public static int getHour(Date date) {
        return getHour(date, HH);
    }
    
    /**
     * 获取某日期的时，指定24小时制还是12小时制
     * @param date     日期
     * @param format   24小时制：“HH”,12小时制：“hh”
     * @return int 
     */
    public static int getHour(Date date,String format) {
    	if(HH.equals(format)) {
    		return Integer.valueOf(dateToStr(date, HH));
    	}else if(hh.equals(format)) {
    		return Integer.valueOf(dateToStr(date, hh));
    	}else {
    		return -1;
    	}
    }

    /**
     * 获取某日期的分钟
     * @param date   日期
     * @return int
     */
    public static int getMinute(Date date) {
        return Integer.valueOf(dateToStr(date, "mm"));
    }
    
    /**
     * 获取某日期在当天是第几分钟
     * @param date   日期
     * @return int
     */
    public static int getMinuteForDay(Date date) {
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        return ((hour * 60) + min);
    }
    
    /**
     * 获取某日期的秒
     * @param date   日期
     * @return int
     */
    public static int getSecond(Date date) {
        return Integer.valueOf(dateToStr(date, "ss"));
    }
    
    /**
     * 获取某日期的秒数 (从1970年开始)
     * @param date   日期
     * @return int
     */
    public static long getSecondFor1970(Date date) {
        return (date.getTime()/1000);
    }
    
    /**
     * 获取某日期的分钟数 (从1970年开始)
     * @param date   日期
     * @return int
     */
    public static long getMinuteFor1970(Date date) {
        return (date.getTime()/(1000 * 60));
    }
    
    /**
     * 获取某日期的小时数 (从1970年开始)
     * @param date   日期
     * @return int
     */
    public static long getHourFor1970(Date date) {
        return (date.getTime()/(1000 * 60 * 60));
    }

    /**
     * 判断字符串日期是否匹配指定的格式化日期
     * @param strDate        日期字符串
     * @param formatter      日期格式
     * @return
     */
    public static boolean isValidDate(String strDate, String formatter) {
        SimpleDateFormat sdf = null;
        ParsePosition pos = new ParsePosition(0);

        if (StringUtils.isEmpty(strDate) || StringUtils.isEmpty(formatter)) {
            return false;
        }
        try {
            sdf = new SimpleDateFormat(formatter);
            sdf.setLenient(false);
            Date date = sdf.parse(strDate, pos);
            if (date == null) {
                return false;
            } else {
                if (pos.getIndex() > sdf.format(date).length()) {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取某年有几天
     * @param year  年份
     * @return
     */
    public static int getYearHasSeveralDay(int year) {
    	int days = 0; //某年(year)的天数
    	if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0){//闰年的判断规则
    	   days = 366;
    	}else{
    	   days = 365;
    	}
    	return days;
    }
    
    /**
     * 获取某年某月有几天
     * @param year   年份
     * @param month  月份
     * @return
     */
    public static int getMonthHasSeveralDay(int year, int month) {
    	Calendar c = Calendar.getInstance();
    	c.set(Calendar.YEAR, year);
    	c.set(Calendar.MONTH,month - 1);//Calendar对象默认一月为0           
    	int day=c.getActualMaximum(Calendar.DAY_OF_MONTH);
    	return day;
    }
    
    /**
     * 获取某年某月有几天
     * @param date   日期
     * @return
     */
    public static int getMonthHasSeveralDay(Date date) {
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 获取某年某月的第一天
     * @param year    年
     * @param month   月
     * @return
     */
    public static Date getFirstDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,year);
        cal.set(Calendar.MONTH, month - 1);
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最小天数
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        return cal.getTime();
    }
    
    /**
     * 获取某年某月的第一天
     * @param date   日期
     * @return
     */
    public static Date getFirstDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最小天数
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        return cal.getTime();
    }

    /**
     * 获取某年某月的最后一天
     * @param year     年
     * @param month    月
     * @return
     */
    public static Date getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,year);
        cal.set(Calendar.MONTH, month - 1);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        return cal.getTime();
    }

    /**
     * 获取某年某月的最后一天
     * @param date     日期
     * @return
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        return cal.getTime();
    }

    /**
     * 获取某天的起始时间
     * @param date
     * @return
     */
    public static Date getFirstOfDay(Date date){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }

    /**
     * 获取某天的结束时间
     * @param date
     * @return
     */
    public static Date getLastOfDay(Date date){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        return calendar.getTime();
    }

    /**
     * 获取第二天的日期
     * @param date
     * @return
     */
    public static Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, +1);//+1今天的时间加一天
        date = calendar.getTime();
        return date;
    }

    /**
     * 获得本月第一天0点时间
     * @return
     */
    public static Date getTimesMonthmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    /**
     * 获得下月第一天0点时间
     * @return
     */
    public static Date getTimesMonthnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 24);
        return cal.getTime();
    }
}
