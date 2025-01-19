package dev.gideonwhite1029.horizon.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemovedConfig {

    String name();

    String[] category();

    boolean transform() default false;

    Class<? extends ConfigConvert<?>> convert() default ConfigVerify.BooleanConfigVerify.class;
}