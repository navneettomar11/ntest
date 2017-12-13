package com.nav.ntest.config;

public @interface Value {
    String value();
    JavaTypes type() default JavaTypes.STRING;
}
