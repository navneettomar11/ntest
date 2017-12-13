package com.nav.ntest.annotations;

public @interface AssertCondition {
    AssertConditionType type() default AssertConditionType.ASSERT_EQUALS;
    String expected();
}
