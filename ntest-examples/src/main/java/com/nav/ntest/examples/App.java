package com.nav.ntest.examples;

import com.nav.ntest.generator.JUnitTestCreator;

import java.io.IOException;

public class App 
{
    public static void main( String[] args ) throws IOException {
        JUnitTestCreator.generateJUNitTestClass("com.nav.ntest_examples.examples","/Users/navneet/Projects/ntest-project/ntest-examples/src/test/java/");
    }
}
