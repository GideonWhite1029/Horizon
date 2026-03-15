package dev.gideonwhite1029.horizon.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalConfig {
    String name();

    String[] category();

    boolean lock() default false;

    Class<? extends ConfigVerify<?>> verify() default ConfigVerify.BooleanConfigVerify.class;

    String[] comment() default {};
}
