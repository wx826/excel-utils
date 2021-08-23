package org.kangjia.annotation;

import java.lang.annotation.*;

/**
 * @author ren
 * @date 2021年08月01日 11:00:08
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ExcelTitle {
    public String value();
}
