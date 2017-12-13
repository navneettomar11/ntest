package com.nav.ntest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JTestCase {
    String[] arguments() default {};
    AssertConditionType assertType();
    String expected();
}


