package com.nav.ntest.examples;

import com.nav.ntest.annotations.*;

public class MathUtils {

    public MathUtils(){

    }
    @JTestCases(cases = {@JTestCase(arguments = {"10","20"},assertType = AssertConditionType.ASSERT_EQUALS, expected = "30")})
    public Integer add(Integer a, Integer b){
        return a+b;
    }

    @JTestCases(cases ={@JTestCase(arguments = {"10","20"},assertType = AssertConditionType.ASSERT_EQUALS, expected = "30")})
    public Long add(Long a, Long b){
        return a+b;
    }

    @JTestCases(cases ={
            @JTestCase(arguments = {"10","20"},assertType = AssertConditionType.ASSERT_EQUALS, expected = "-10"),
            @JTestCase(arguments = {"-10","30"}, assertType = AssertConditionType.ASSERT_EQUALS, expected = "-40")})
    public static Integer subtract(Integer a, Integer b){
        return a - b;
    }

    @JTestCases(cases ={
            @JTestCase(arguments = {"10","20"},assertType = AssertConditionType.ASSERT_EQUALS, expected = "200")})
    public Double multiply(Double a, Double b){
        return a*b;
    }

    @JTestCases(cases ={
            @JTestCase(arguments = {"10","20"},assertType = AssertConditionType.ASSERT_EQUALS, expected = ".5")})
    public Double divide(Double a, Double b){
        return a/b;
    }

}
