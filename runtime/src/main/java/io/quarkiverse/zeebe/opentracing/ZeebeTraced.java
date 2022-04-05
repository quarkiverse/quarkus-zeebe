package io.quarkiverse.zeebe.opentracing;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

@Documented
@InterceptorBinding
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface ZeebeTraced {
}