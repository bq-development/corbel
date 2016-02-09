package io.corbel.notifications.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Alberto J. Rubio
 */
@Target(ElementType.FIELD) @Retention(RetentionPolicy.RUNTIME)
public @interface Template {}
