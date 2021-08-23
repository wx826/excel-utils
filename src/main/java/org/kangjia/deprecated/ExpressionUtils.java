package org.kangjia.deprecated;

import org.kangjia.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义表达式解析
 * @author ren
 * @date 2021年08月17日 18:24:19
 */
public class ExpressionUtils {

    private static int leftBracket;    //左括号个数
    private static int rightBracket;   //右括号个数
    private static int startL;         //左括号的位置
    private static int startR;         //右括号的位置
    private static String leftNumber = "0";
    private static String rightNumber = "0";
    private static int[] sym = new int[4];
    private static Vector<String> list = new Vector<String>();//用来存放从字符串解析出来的字符

    /**
     * 分析公式计算结果
     * @param formula   公式
     * @param type      公式类型，0区间型，1计算型
     * @param o         数据对象
     * @return
     * @throws Exception
     */
    public static String analyseValueByFormula(String formula, Byte type, Object o) throws Exception {
        String newformula = variableAssignment(formula, o);
        String value = null;
        if(type.byteValue() == 0){//区间型
            value = analysisSectionFormula(formula,newformula);
        }else if(type.byteValue() == 1){//计算型
            value = analysisCalculateFormula(formula,newformula);
        }
        return value;
    }

    /**
     * 公式变量赋值
     * @param formula
     * @param o
     * @return
     */
    public static String variableAssignment(String formula, Object o){
        //定义集合，用于存放字符串中的变量值
        List<String> variables = new ArrayList<String>();
        //用正则找出公式中的变量
        Matcher m = getMatcher("\\[([a-zA-Z0-9]+)\\]", formula.trim());

        while(m.find()) {
            //得到所有匹配到的字符串
            String temp = formula.trim().substring(m.start(),m.end());
            if(!variables.contains(temp)){
                variables.add(temp);
            }
        }
        //把变量值赋值
        for(String v:variables){
            String clip = v.trim().substring(1,v.length()-1).trim();
            Class<? extends Object> clazz = o.getClass();
            String methodName = "get"+clip.substring(0,1).toUpperCase()+clip.substring(1);
            String realValue = null;
            try {
                Method method = clazz.getMethod(methodName, null);
                realValue = method.invoke(o).toString();
                if(realValue.indexOf("E")>0){
                    BigDecimal bd3 = new BigDecimal(realValue);
                    realValue=bd3.setScale(6, BigDecimal.ROUND_HALF_UP).toString();
                }
                //System.out.println("realValue-->"+realValue);
                if(StringUtils.isEmpty(realValue)){
                    return "参数"+v+"为空，无法计算";
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            formula = formula.replace(v, realValue);
        }
        return formula.trim();
    }

    /**
     * 解析区间型公式
     * @param originalFormula
     * @param newFormula
     * @return
     * @throws Exception
     */
    public static String analysisSectionFormula(String originalFormula,String newFormula) throws Exception {
        String[] tempArray = newFormula.split("\\,");
        String result = null;
        for(String temp:tempArray){
            //解析每条判断语句是否满足，条件成立直接跳出循环
            String regex = "\\(((\\-|\\+)?\\d+(\\.\\d+)?)+[<=>]{1,2}(\\-|\\+)?\\d+(\\.\\d+)?\\)";
            Matcher matcher = getMatcher(regex,temp);
            if(matcher.find()){
                String condition = temp.substring(matcher.start(), matcher.end());
                try {
                    if(conditionPass(originalFormula,condition)){
                        result = temp.split(regex)[1];break;
                    }
                } catch (Exception e) {
                    throw new Exception("在公式"+originalFormula+"中,"+e.getMessage());
                }
            }else{
                throw new Exception("在公式“"+originalFormula+"”中没有检索到条件语句或条件语句错误，请检查您的公式是否正确");
            }
        }
        return result;
    }

    /**
     * 区间型公式验证条件是否成立
     * @param condition
     * @return
     * @throws Exception
     */
    private static boolean conditionPass(String formula,String condition) throws Exception {
        condition = condition.trim().substring(1,condition.length()-1);
        Matcher matcher = getMatcher(">=|==|<=|>|<", condition);
        if(matcher.find()){
            String relation = condition.substring(matcher.start(),matcher.end());
            String[] values = condition.split(relation);
            BigDecimal leftValue = null;
            Matcher m = getMatcher("\\+|\\-|\\*|\\/", values[0].trim());
            if(m.find()){
                //如果左边为表达式，就计算出它的值
                leftValue = new BigDecimal(analysisCalculateFormula(formula,values[0]));
            }else{
                leftValue = new BigDecimal(values[0].trim());
            }
            BigDecimal rightValue = new BigDecimal(values[1].trim());
            int i = leftValue.compareTo(rightValue);
            if(">".equals(relation)){
                if(i > 0){return true;}
            }else if(">=".equals(relation)){
                if(i >= 0){return true;}
            }else if("==".equals(relation)){
                if(i == 0){return true;}
            }else if("<".equals(relation)){
                if(i < 0){return true;}
            }else if("<=".equals(relation)){
                if(i <= 0){return true;}
            }else{
                throw new Exception("关系运算符解析错误，请检查您的公式是否正确");
            }
        }else{
            throw new Exception("没有检索到关系运算符，请检查您的公式是否正确");
        }
        return false;
    }

    /**
     * 解析计算型公式
     * @param originalFormula
     * @param newFormula
     * @return
     */
    public static String analysisCalculateFormula(String originalFormula, String newFormula) throws Exception {
        //System.out.println("newFormula--"+newFormula);
        newFormula = "(" + replaceSubtration(newFormula) + ")";
        String formulaStr = "";
        BigDecimal calculateValue = BigDecimal.ZERO;
        try {
            if(checkValid(newFormula)){
                for (int i = 0; i < leftBracket; i++) {
                    int iStart = newFormula.lastIndexOf("(") + 1;
                    //获得最里层括号里的内容
                    formulaStr = newFormula.substring(iStart, iStart+newFormula.substring(iStart).indexOf(")")).trim();
                    symbolParse(formulaStr);
                    calculateValue = new BigDecimal(calculate());
                    iStart=newFormula.lastIndexOf("(");
                    int iEnd=newFormula.substring(iStart).indexOf(")")+1;
                    newFormula = newFormula.substring(0,iStart).trim() +
                            calculateValue +
                            newFormula.substring(iStart+iEnd, newFormula.length()).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("在公式"+originalFormula+"中,"+e.getMessage());
        }
        return calculateValue.setScale(6,BigDecimal.ROUND_HALF_UP).toString();
    }

    /*
     * 为了使公式中支持负数，使用“`”表示减号，使用“-”表示负号，把所有减号换成“‘”
     */
    private static String replaceSubtration(String vstr){
        String tmp="";
        String result="";
        int startS = vstr.indexOf("-");
        if (startS !=-1) {
            if (startS > 0) {
                tmp = vstr.substring(startS - 1, startS);
                if (!"+".equals(tmp) && !"-".equals(tmp) && !"*".equals(tmp) &&!"/".equals(tmp) &&
                        !"(".equals(tmp)){
                    result = result + vstr.substring(0, startS) + "`";
                }
                else
                    result = result + vstr.substring(0, startS + 1);
            }
            else
                result = result + vstr.substring(0, startS + 1);
            vstr = vstr.substring(startS + 1);
        }
        while (startS != -1) {
            startS = vstr.indexOf("-");
            if (startS > 0) {
                tmp = vstr.substring(startS - 1, startS);
                if (!"+".equals(tmp) && !"-".equals(tmp) && !"*".equals(tmp) &&!"/".equals(tmp) &&
                        !"(".equals(tmp))
                    result = result + vstr.substring(0, startS) + "`";
                else
                    result = result + vstr.substring(0, startS + 1);
            }
            else
                result = result + vstr.substring(0, startS + 1);
            vstr = vstr.substring(startS + 1);
        }
        result+=vstr;
        return result;
    }

    /*
     *判断输入的字符串是否合法
     */
    public static boolean checkValid(String formula) throws Exception {
        return (compareToLR(formula)&&checkFormula(formula));
    }

    /*
     *对比左右括号个数
     */
    private static boolean compareToLR(String formula) throws Exception {
        int lb = getLeftBracket(formula);
        int rb = getRightBracket(formula);
        if (lb > rb) {
            throw new Exception("左括弧的个数多于右括弧，请检查您的公式是否正确！");
        } else if(lb < rb){
            throw new Exception("左括弧的个数少于右括弧，请检查您的公式是否正确！");
        }
        return true;
    }

    /*
     * 获得左括号数
     */
    private static int getLeftBracket(String calRule) {
        leftBracket = 0;
        startL = calRule.indexOf("(");
        if (startL != -1) {
            calRule = calRule.substring(startL + 1, calRule.length());
        }
        while (startL != -1) {
            leftBracket++;
            startL = calRule.indexOf("(");
            calRule = calRule.substring(startL + 1, calRule.length());
        }
        return leftBracket;
    }

    /*
     * 获得右括号数
     */
    private static int getRightBracket(String calRule) {
        rightBracket = 0;
        startR = calRule.indexOf(")");
        if (startR != -1) {
            calRule = calRule.substring(startR + 1, calRule.length());
        }
        while (startR != -1) {
            rightBracket++;
            startR = calRule.indexOf(")");
            calRule = calRule.substring(startR + 1, calRule.length());
        }
        return rightBracket;
    }

    /*
     *检查公式中是否存在非法字符如(+、-)等
     */
    private static boolean checkFormula(String formula) throws Exception {
        boolean isOk=true;
        String[] bracket =new String[2];
        String[] sign=new String[4];
        bracket[0]="(";
        bracket[1]=")";
        sign[0]="+";
        sign[1]="`";
        sign[2]="*";
        sign[3]="/";
        String vstr="";
        for(int i=0;i<bracket.length;i++){
            for(int j=0;j<sign.length;j++){
                if (i==0)
                    vstr=bracket[i]+sign[j];
                else
                    vstr=sign[j]+bracket[i];
                if (formula.indexOf(vstr)>0){
                    throw new Exception("存在非法字符"+vstr);
                }
            }
        }
        for(int i=0;i<sign.length;i++){
            for(int j=0;j<sign.length;j++){
                vstr=sign[i]+sign[j];
                if (formula.indexOf(vstr)>0){
                    throw new Exception("存在非法字符"+vstr);
                }
            }
        }
        if (formula.indexOf("()")>0){
            throw new Exception("存在非法字符()");
        }
        return isOk;
    }

    /**
     * 抽取最终括号内数据到List
     * @param str
     */
    private static void symbolParse(String str) {
        list.clear();
        int count = 0;
        for (int i = 0; i < 4; i++) {
            compareMin(str);
            while (sym[i] != -1) {
                String insStr = str.substring(0, sym[i]).trim();
                list.add(insStr);
                insStr = str.substring(sym[i], sym[i] + 1).trim();
                list.add(insStr);

                str = str.substring(sym[i] + 1, str.length()).trim();
                compareMin(str);
                count++;
            }
        }
        if (sym[0] == -1 && sym[1] == -1 && sym[2] == -1 & sym[3] == -1) {
            list.add(str);
        }
    }

    /**
     * 循环比较赋SubString起始值
     * @param str
     */
    private static void compareMin(String str) {
        int sps = str.indexOf("`");//减法subtration
        sym[0] = sps;
        int spa = str.indexOf("+");//加法addition
        sym[1] = spa;
        int spd = str.indexOf("/");//除法division
        sym[2] = spd;
        int spm = str.indexOf("*");//乘法multiplication
        sym[3] = spm;
        for (int i = 1; i < sym.length; i++) {
            for (int j = 0; j < sym.length - i; j++)
                if (sym[j] > sym[j + 1]) {
                    int temp = sym[j];
                    sym[j] = sym[j + 1];
                    sym[j + 1] = temp;
                }
        }
    }

    /*
     * 计算
     */
    private static String calculate() throws Exception {
        try {
            //处理除法
            int spd = list.indexOf("/");
            while (spd != -1) {
                leftNumber = list.get(spd - 1).toString();
                rightNumber = list.get(spd + 1).toString();
                list.remove(spd - 1);
                list.remove(spd - 1);
                list.remove(spd - 1);
                double ln = Double.valueOf(leftNumber).doubleValue();
                double rn = Double.valueOf(rightNumber).doubleValue();
                if(rn == 0){
                    list.add(spd - 1, new BigDecimal(0.00).toPlainString());
                    spd = list.indexOf("/");
                    continue;
                }
                double answer = ln / rn;
                list.add(spd - 1, new BigDecimal(answer).toPlainString());
                spd = list.indexOf("/");
            }

            //处理乘法
            int spm = list.indexOf("*");
            while (spm != -1) {
                leftNumber = list.get(spm - 1).toString();
                rightNumber = list.get(spm + 1).toString();
                list.remove(spm - 1);
                list.remove(spm - 1);
                list.remove(spm - 1);
                double ln = Double.valueOf(leftNumber).doubleValue();
                double rn = Double.valueOf(rightNumber).doubleValue();
                double answer = ln * rn;
                list.add(spm - 1, String.valueOf(answer));
                spm = list.indexOf("*");
            }

            //处理减法
            int sps = list.indexOf("`");
            while (sps != -1) {
                leftNumber = list.get(sps - 1).toString();
                rightNumber = list.get(sps + 1).toString();
                list.remove(sps - 1);
                list.remove(sps - 1);
                list.remove(sps - 1);
                double ln = Double.valueOf(leftNumber).doubleValue();
                double rn = Double.valueOf(rightNumber).doubleValue();
                double answer = ln - rn;
                list.add(sps - 1, String.valueOf(answer));
                sps = list.indexOf("`");
            }

            //处理加法
            int spa = list.indexOf("+");
            while (spa != -1) {
                leftNumber = list.get(spa - 1).toString();
                rightNumber = list.get(spa + 1).toString();
                list.remove(spa - 1);
                list.remove(spa - 1);
                list.remove(spa - 1);
                double ln = Double.valueOf(leftNumber).doubleValue();
                double rn = Double.valueOf(rightNumber).doubleValue();
                double answer = ln + rn;
                list.add(spa - 1, String.valueOf(answer));
                spa = list.indexOf("+");
            }
            if (list.size() != 0) {
                String result = list.get(0);
                if (result == null || result.length() == 0) result = "0";
                return list.get(0);
            }else{
                throw new Exception("进行数据计算时发生未知错误,请检查公式中存在可计算的元素。");
            }
        } catch (Exception e) {
            throw new Exception("进行数据计算时发生未知错误,请检查公式中存在可计算的元素。");
        }
    }



    /**
     * 封装正则对象
     * @param regex
     * @param text
     * @return
     */
    private static Matcher getMatcher(String regex, String text){
        Pattern p = Pattern.compile(regex);
        return p.matcher(text);
    }

    public static void main(String[] args) throws Exception {

    }
}
