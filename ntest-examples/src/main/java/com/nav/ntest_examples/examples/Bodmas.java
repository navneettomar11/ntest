package com.nav.ntest_examples.examples;

import com.nav.ntest.config.AssertConditionType;
import com.nav.ntest.config.JTestCase;
import com.nav.ntest.config.JTestCases;

public class Bodmas {

    private static final String BRACKET_START = "(";
    private static final String BRACKET_END = ")";
    private static final String ADDITION = "+";
    private static final String SUBTRACTION = "-";
    private static final String MULTIPLY = "*";
    private static final String DIVISION = "/";
    private static final String POWER = "^";

    private static final String[] OPERATORS_ONE = {POWER, DIVISION, MULTIPLY};
    private static final String[] OPERATORS_TWO = {ADDITION, SUBTRACTION};
    private static  final String[] OPERATORS_THREE = {SUBTRACTION, ADDITION};

    @JTestCases(cases = {
            @JTestCase(arguments = {"3/3*(2+3)+(5^2)"},assertType = AssertConditionType.ASSERT_EQUALS, expected = "30"),
            @JTestCase(arguments = {"6+3*7-5"},assertType = AssertConditionType.ASSERT_EQUALS, expected = "22"),
            @JTestCase(arguments = {"25-48/6+12*2"},assertType = AssertConditionType.ASSERT_EQUALS, expected = "41"),
            @JTestCase(arguments = {"25"},assertType = AssertConditionType.ASSERT_EQUALS, expected = "25"),
            @JTestCase(arguments = {},assertType = AssertConditionType.ASSERT_EQUALS, expected = "-1")
    })
    public int calculatation(String expression) {
        if(expression == null){
            return -1;
        }
        System.out.println("Expression : "+expression);
        expression = solveBracket(expression);
        java.util.List splitExprList = splitExpressionIntoList(expression);
        Integer result = calauateExpression(splitExprList);
        System.out.println("Expression Result: "+result);
        return result;
    }

    public Bodmas(){

    }

    private String solveBracket(String expression) {
        while(expression.contains("(") && expression.contains(")")){
            int firstIndex = expression.indexOf(BRACKET_START);
            int nextIndex = expression.indexOf(BRACKET_END);
            String subExpression = expression.substring(firstIndex, nextIndex+1);
            java.util.List splitExprList = splitExpressionIntoList(subExpression.substring(1, subExpression.length()-1));
            Integer result = calauateExpression(splitExprList);
            expression = expression.replace(subExpression,Integer.toString(result));
        }
        return expression;
    }

    private Integer calauateExpression(java.util.List<String> splitExprList){
        for(String op : OPERATORS_ONE){
            splitExprList = calauateSplitExpression(splitExprList,op);
        }
        if(splitExprList.size() > 1) {
            int subtractionIndex = splitExprList.indexOf(SUBTRACTION);
            int additionIndex = splitExprList.indexOf(ADDITION);
            Double subtractionOperand = 0d, additionOperand = 0d;
            if(subtractionIndex > -1){
                subtractionOperand = Double.parseDouble(splitExprList.get(subtractionIndex - 1));
            }
            if(additionIndex > -1){
                additionOperand = Double.parseDouble(splitExprList.get(additionIndex - 1));
            }
            String[] operators = OPERATORS_TWO;
            if (subtractionOperand > additionOperand){
                operators = OPERATORS_THREE;
            }
            for(String op : operators){
                splitExprList = calauateSplitExpression(splitExprList,op);
            }
        }
        return Integer.parseInt(splitExprList.get(0));
    }

    private Double calculateExpressionValue(Double first, Double second, String operator) {
        switch(operator){
            case DIVISION : return first / second;
            case MULTIPLY : return first * second;
            case ADDITION : return first + second;
            case POWER : return Math.pow(first,second);
            case SUBTRACTION :
            default: return first - second;
        }
    }

    private java.util.List<String> calauateSplitExpression(java.util.List<String> splitExprList, String operator){
        while(splitExprList.contains(operator)){
            int powerCharacterIndex = splitExprList.indexOf(operator);
            Double firstOperand = Double.parseDouble(splitExprList.get(powerCharacterIndex - 1));
            Double secondOperand = Double.parseDouble(splitExprList.get(powerCharacterIndex + 1));
            Double result = calculateExpressionValue(firstOperand, secondOperand, operator);
            java.util.List tmpSplitExprList = new java.util.ArrayList<>();
            for(int i=0;i<splitExprList.size(); i++){
                if(i == powerCharacterIndex - 1){
                    tmpSplitExprList.add(Integer.toString(result.intValue()));
                }
                if(i == powerCharacterIndex || i == powerCharacterIndex - 1 || i == powerCharacterIndex + 1){
                    continue;
                }
                tmpSplitExprList.add(splitExprList.get(i));
            }
            splitExprList = tmpSplitExprList;
        }
        return splitExprList;
    }

    private java.util.List<String> splitExpressionIntoList(String expression){
        String tmp="";
        java.util.List<String> splitExpressionList = new java.util.ArrayList();
        for(char z : expression.toCharArray()){
            if(Character.isDigit(z)){
                tmp+=z;
            }else {
                splitExpressionList.add(tmp);
                splitExpressionList.add(Character.toString(z));
                tmp = "";

            }
        }
        if(!"".equalsIgnoreCase(tmp)) {
            splitExpressionList.add(tmp);
        }
        return splitExpressionList;
    }
}
