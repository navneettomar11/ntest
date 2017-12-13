package com.nav.ntest.config;

public @interface AssertCondition {
    AssertConditionType type() default AssertConditionType.ASSERT_EQUALS;
    String expected();
}
