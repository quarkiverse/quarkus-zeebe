package io.quarkiverse.zeebe;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Variable {
    String value() default "";
}