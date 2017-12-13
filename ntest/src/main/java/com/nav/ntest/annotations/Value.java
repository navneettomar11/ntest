package com.nav.ntest.annotations;

public @interface Value {
    String value();
    JavaTypes type() default JavaTypes.STRING;
}
