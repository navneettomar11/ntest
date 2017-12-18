package com.nav.ntest.generator;

import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static com.nav.ntest.generator.JUnitTestCreatorConstant.NEWLINE;

public class TestClass {

    private String packageName;

    private String className;

    private Collection<String> importList = new HashSet<>();

    private java.util.Map<String, TestMethod> testMethodMap = new java.util.HashMap<>();

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Collection<String> getImportList() {
        return importList;
    }

    public void setImportList(Collection<String> importList) {
        this.importList = importList;
    }

    public Map<String, TestMethod> getTestMethodMap() {
        return testMethodMap;
    }

    public void setTestMethodMap(Map<String, TestMethod> testMethodMap) {
        this.testMethodMap = testMethodMap;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder(String.format("package %s;",this.packageName));
        builder.append(NEWLINE);
        for(String importName : importList){
            builder.append(String.format("import %s;",importName));
            builder.append(NEWLINE);
        }
        builder.append(String.format("public class %sTest {",this.className));
        builder.append(NEWLINE);
        for(Map.Entry<String, TestMethod> testMethodEntry : testMethodMap.entrySet()){
            builder.append(testMethodEntry.getValue().toString());
            builder.append(NEWLINE);
        }
        builder.append("}");
        return builder.toString();
    }

}
