package com.zlj.annotation;

import org.springframework.transaction.annotation.Propagation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {
    Propagation propagation() default Propagation.REQUIRED;
}
