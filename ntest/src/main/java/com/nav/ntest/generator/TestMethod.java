package com.nav.ntest.generator;

import com.google.common.base.Objects;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TestMethod {

    private String methodName;

    private java.util.Map<String,java.util.Collection<String>> testCases = new java.util.HashMap<>();

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<String, Collection<String>> getTestCases() {
        return testCases;
    }

    public void setTestCases(Map<String, Collection<String>> testCases) {
        this.testCases = testCases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestMethod that = (TestMethod) o;
        return Objects.equal(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(methodName, testCases);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("public void %s() {",methodName));

        builder.append("}");
        return builder.toString();
    }
}
