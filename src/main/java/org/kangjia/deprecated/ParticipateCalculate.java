package org.kangjia.deprecated;

import java.lang.annotation.*;

/**
 * 参与计算的的列要加的注解
 * @author ren
 * @date 2021年08月18日 09:19:23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Deprecated
public @interface ParticipateCalculate {
}
