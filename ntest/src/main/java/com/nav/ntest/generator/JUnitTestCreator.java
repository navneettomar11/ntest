package com.nav.ntest.generator;


//import com.google.googlejavaformat.java.FormatterException;
import com.nav.ntest.annotations.AssertConditionType;
import com.nav.ntest.annotations.JTestCase;
import com.nav.ntest.annotations.JTestCases;
import org.apache.maven.Maven;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.google.googlejavaformat.java.Formatter;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Thread.*;


@Mojo(name = "generate")
public class JUnitTestCreator extends AbstractMojo {

    private static final String ASSERT_STATIC_IMPORT = "import static org.junit.Assert";
    private static final String ALL_ASSERT_STATIC_IMPORT = "import static org.junit.Assert.*";

    @Parameter
    private String packageName;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Execute Source Directory : "+packageName);
        try {
            List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
            URL[] runtimeUrls = new URL[runtimeClasspathElements.size()];
            for (int i = 0; i < runtimeClasspathElements.size(); i++) {
                String element = (String) runtimeClasspathElements.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            try(URLClassLoader newLoader = new URLClassLoader(runtimeUrls, currentThread().getContextClassLoader())){
                generateJUNitTestClass(packageName, null, newLoader);
            } catch (IOException e) {
                getLog().error(e);
            }
        } catch (DependencyResolutionRequiredException | MalformedURLException  e) {
            getLog().error(e);
        }
    }

    /**
     * Generating JUnit Test class for given package and create Test class in given test directory path.
     * @param packageName
     * @param testDirPath Give absolute test diretory path
     */
    public void generateJUNitTestClass(String packageName, String testDirPath, URLClassLoader urlClassLoader) {
        getLog().info("Generating JUnit class Start...");
        try{
            java.util.Collection<Class> classes = getAllJavaFileForGivenPackage(packageName, urlClassLoader);
            if(classes.isEmpty()){
                getLog().error("No Java Classes Found");
                return;
            }
            for(Class c : classes){
                TestClass testClass= generateJUnitTestFileContent(c);
                getLog().info(testClass.toString());
            }
        }catch(IOException ioex){
            getLog().error("Error occurred", ioex);
        }
        getLog().info("Generating JUNit class End...");
    }

    /**
     * Getting all java class from given packge
     * @param packageDirectoryName
     * @return
     */
    private java.util.Collection<Class> getAllJavaFileForGivenPackage(String packageDirectoryName, URLClassLoader urlClassLoader) throws IOException{
        getLog().info("Getting all java files from given package start...");
        java.util.Collection<Class> classes = new ArrayList<>();
         for(String sourceDirectoryName : project.getCompileSourceRoots()){
            File packageDirectory = new File(sourceDirectoryName, packageDirectoryName.replace('.','/'));
             try {
                 classes.addAll(findClasses(packageDirectory, packageDirectoryName, urlClassLoader));
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             }
             getLog().info("Package Directory "+packageDirectory);
        }
        getLog().info("Getting all java files from given package end...");
        return  classes;
    }


    /**
     * Generating content of Unit Test class for given class
     * @param c
     * @return
     */
    private TestClass generateJUnitTestFileContent(Class c){
        getLog().info("Generating Test class content start...");
        TestClass testClass = new TestClass();
        testClass.setPackageName(c.getPackage().getName());
        testClass.setClassName(c.getSimpleName());
        Method[] methods = c.getMethods();
        java.util.Map<String, TestMethod> testMethods = new java.util.HashMap<>();
        for(Method method: methods){
            JTestCases jTestCases = method.getAnnotation(JTestCases.class);
            if(jTestCases == null || jTestCases.cases().length == 0){
                continue;
            }
            TestMethod testMethod = new TestMethod();
            String methodName = method.getName();
            getLog().info("Method Name : "+methodName);
            int methodCount = isTestMethodExist(methodName,testMethods.keySet());
            if(methodCount > 0){
                methodName+=methodCount;
            }
            getLog().info("Method Name - Method Count : "+methodName+" - "+ methodCount);
            testMethod.setMethodName(methodName);
            for(JTestCase testCase : jTestCases.cases()){
                String expected = wrapToObject(method.getReturnType(),testCase.expected());
                testMethod.getTestCases().put(expected, getTestMethodArugments(testCase, method));
            }
            getLog().info(testMethod.toString());
            testMethods.put(methodName,testMethod);
        }
        testClass.setTestMethodMap(testMethods);
        getLog().info("Generating Test class content end...");
        return testClass;

    }

    private java.util.Collection<String> getTestMethodArugments(JTestCase testCase, Method method){
        getLog().info("Generate Test Method Arguments start...");
        java.util.List<String> argumentList = new java.util.ArrayList<>();
        String[] arguments = testCase.arguments();
        Class[] paramTypesClasses =method.getParameterTypes();
        getLog().info("Param Type Class "+paramTypesClasses);
        if(arguments==null || arguments.length == 0){
            argumentList.add("null");
        }else {
            int paramTypeClassIndex = 0;
            for(String args : arguments){
                argumentList.add(wrapToObject(paramTypesClasses[paramTypeClassIndex++],args));
            }
        }
        getLog().info("Generate Test Method Arguments end..."+argumentList);
        return argumentList;
    }

    private static void writePhysicalTestFile(String fileContent, String testDirthPath, Class c) throws IOException {
        File testDirectory = new File(testDirthPath);
        if(!testDirectory.exists() || !testDirectory.isDirectory()){
            throw new RuntimeException("");
        }
        String packageName = c.getPackage().getName();
        String testClassDirectoryPath = packageName.replace('.','/');
        File testClassDirectory = new File(testDirectory,testClassDirectoryPath);
        if(!testClassDirectory.exists() && !testClassDirectory.mkdirs()){
            throw new RuntimeException("");
        }
        Files.write(Paths.get(testClassDirectory.getAbsolutePath()+File.separatorChar+c.getSimpleName()+"Test.java"), fileContent.getBytes());
    }
    private static String writeJTestCase(JTestCases testCases, Method method, Class c,Map<String,Integer> methodList){
        StringBuilder testFileContent = new StringBuilder();
        String methodName = method.getName()+"Test";
        if(!methodList.containsKey(methodName)){
            methodList.put(methodName,0);
        }else{
            Integer count = methodList.get(methodName);
            count++;
            methodName = String.format(methodName+"%d",count);
        }
        //testFileContent.append("@Test"+NEW_LINE);
        // testFileContent.append("public void "+methodName+"(){"+NEW_LINE);
        int testCaseIndex = 0;
        if(!Modifier.isStatic(method.getModifiers())){
            //testFileContent.append(c.getName()+" obj = new "+c.getName()+"();"+NEW_LINE);
        }
        for(JTestCase testCase : testCases.cases()){
            String[] arguments = testCase.arguments();
            Class[] paramTypesClasses =method.getParameterTypes();
            //if(arguments.length != paramTypesClasses.length){
            //    throw new RuntimeException("Parameters are not  matched");
            //}
            int paramTypeClassIndex =0;
            StringBuilder params = new StringBuilder(",");
            if(arguments==null || arguments.length == 0){
                params.append("null");
            }else {
                for(String args : arguments){
                    params.append(wrapToObject(paramTypesClasses[paramTypeClassIndex++],args));
                }
            }

            if(Modifier.isStatic(method.getModifiers())){
                //testFileContent.append(method.getReturnType().getName()+" result"+testCaseIndex+" = "+c.getName()+"."+method.getName()+"("+params.toString()+");"+NEW_LINE);
            }else{
                //testFileContent.append(c.getName()+" obj = new "+c.getName()+"();"+NEW_LINE);
               // testFileContent.append(method.getReturnType().getName()+" result"+testCaseIndex+" = obj."+method.getName()+"("+params.toString()+");"+NEW_LINE);
            }

            if(AssertConditionType.ASSERT_EQUALS.equals(testCase.assertType())){
              //  testFileContent.append("assertEquals("+wrapToObject(method.getReturnType(),testCase.expected())+", result"+testCaseIndex+");"+NEW_LINE);
            }
            testCaseIndex++;
        }
        // testFileContent.append("}"+ NEW_LINE);
        return testFileContent.toString();
    }

    private static String wrapToObject(Class c, String value){
        switch (c.getSimpleName()){
            case "Long" : return "new Long("+value+")";
            case "Double" : return "new Double("+value+")";
            case "Float" : return "new Float("+value+")";
            case "Integer" : return "new Integer("+value+")";
            case "String" : return "\""+value+"\"";
            default: return value;
        }
    }
    private List<Class> findClasses(File directory, String packageName, URLClassLoader urlClassLoader) throws ClassNotFoundException, IOException {
        List<Class> classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                getLog().info("Directory "+file.getName());
                classes.addAll(findClasses(file, packageName + "." + file.getName(), urlClassLoader));
            } else if (file.getName().endsWith(".java")) {
                getLog().info("Java "+file.getName());
                classes.add(urlClassLoader.loadClass(packageName + '.' + file.getName().substring(0, file.getName().length() - 5)));
            }
        }
        return classes;
    }

    private int isTestMethodExist(String givenMethodName, Set<String> methodNameList){
        int methodCount = 0;
        getLog().info("Method List "+methodNameList+" Given Method Name "+givenMethodName);
        for(String methodName : methodNameList){
            if(methodName.startsWith(givenMethodName)){
                methodCount++;
            }
        }
        return methodCount;
    }


}
