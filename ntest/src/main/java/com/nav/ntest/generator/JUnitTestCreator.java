package com.nav.ntest.generator;


import com.google.googlejavaformat.java.FormatterException;
import com.nav.ntest.annotations.AssertConditionType;
import com.nav.ntest.annotations.JTestCase;
import com.nav.ntest.annotations.JTestCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.googlejavaformat.java.Formatter;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class JUnitTestCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JUnitTestCreator.class);
    private static final String NEW_LINE = "\r\n";
    private static final String ASSERT_STATIC_IMPORT = "import static org.junit.Assert";
    private static final String ALL_ASSERT_STATIC_IMPORT = "import static org.junit.Assert.*";

    private JUnitTestCreator(){
        throw new AssertionError();
    }

    /**
     * Generating JUnit Test class for given package and create Test class in given test directory path.
     * @param packageName
     * @param testDirPath Give absolute test diretory path
     */
    public static void generateJUNitTestClass(String packageName, String testDirPath) {
        LOGGER.info("Generating JUnit class Start...");
        try{
            java.util.Collection<Class> classes = getAllJavaFileForGivenPackage(packageName);
            if(classes.isEmpty()){
                LOGGER.error("No Java Classes Found");
                return;
            }
            for(Class c : classes){
                String testFileContent = generateJUnitTestFileContent(c);
                LOGGER.info(testFileContent);
            }
        }catch(IOException ioex){
            LOGGER.error("Error occurred", ioex);
        }
        LOGGER.info("Generating JUNit class End...");
    }

    /**
     * Generating content of Unit Test class for given class
     * @param c
     * @return
     */
    private static String generateJUnitTestFileContent(Class c){
        LOGGER.info("Generating Test class content start...");
        StringBuilder testFileContent = new StringBuilder(getTestClassFileFromTemplate(c.getPackage().getName(), c.getSimpleName()));
        Method[] methods = c.getMethods();
        Map<String,Integer> methodList = new HashMap<>();
        for(Method method: methods){
            JTestCases jTestCases = method.getAnnotation(JTestCases.class);
            if(jTestCases != null){

            }
        }
        LOGGER.info("Generating Test class content end...");
        return testFileContent.toString();
    }
    /**
     * Getting all java class from given packge
     * @param packageName
     * @return
     */
    private static java.util.Collection<Class> getAllJavaFileForGivenPackage(String packageName) throws IOException{
        LOGGER.info("Getting all java files from given package start...");
        java.util.Collection<Class> classes = new ArrayList<>();
        String packageDirectory = packageName.replace('.','/');
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packageDirectory);
        Collection<File> dirs = new ArrayList();
        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        for (File directory : dirs) {
            try{
                classes.addAll(findClasses(directory, packageName));
            }catch (ClassNotFoundException clnex){
                LOGGER.error("Class Not found :", clnex);
            }
        }
        LOGGER.info("Getting all java files from given package end...");
        return  classes;
    }

    private static String getTestClassFileFromTemplate(String packageName, String className){
        //if(TEMPLATE_FILE_CONTENT == null){
            StringBuilder temp = new StringBuilder();
            InputStream templateInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("javaClassTemplate.txt");
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(templateInputStream))){
                String line = null;
                while( (line = reader.readLine())!=null){
                 temp.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TEMPLATE_FILE_CONTENT = temp.toString();
        //}
        String templateContent = temp.toString();
        templateContent = templateContent.replaceAll("\\{\\{packageName\\}\\}", packageName);
        templateContent = templateContent.replaceAll("\\{\\{className\\}\\}", className);
        return templateContent;
    }
    /**
     * Create JUnit Test Case
     * @param packageName
     * @throws IOException
     */
    public static void createJUnitTestClass2(String packageName, String testDirPath) throws IOException {
        ArrayList<Class> classes = new ArrayList();
        for(Class c : classes){
            StringBuilder testFileContent = new StringBuilder();
            testFileContent.append("package "+c.getPackage().getName()+";"+NEW_LINE);
            testFileContent.append("import org.junit.Test;"+NEW_LINE);
            testFileContent.append("import static org.junit.Assert.assertEquals;"+NEW_LINE);
            testFileContent.append("public class "+c.getSimpleName()+"Test {"+NEW_LINE);
            Method[] methods = c.getMethods();
            Map<String,Integer> methodList = new HashMap<>();
            for(Method method: methods){
               JTestCases jTestCases = method.getAnnotation(JTestCases.class);
               if(jTestCases != null){
                   testFileContent.append(writeJTestCase(jTestCases,method,c,methodList));
               }
           }
            testFileContent.append("}");
            System.out.println(testFileContent);
            String formattedSource = null;
            try {
                formattedSource = new Formatter().formatSource(testFileContent.toString());
                writePhysicalTestFile(formattedSource, testDirPath, c);
            } catch (FormatterException e) {
                LOGGER.error("Format Exception ", e);
            }
            LOGGER.info("Formatted Source Code : {0}",formattedSource);
        }
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
        testFileContent.append("@Test"+NEW_LINE);
        testFileContent.append("public void "+methodName+"(){"+NEW_LINE);
        int testCaseIndex = 0;
        if(!Modifier.isStatic(method.getModifiers())){
            testFileContent.append(c.getName()+" obj = new "+c.getName()+"();"+NEW_LINE);
        }
        for(JTestCase testCase : testCases.cases()){
            String[] arguments = testCase.arguments();
            Class[] paramTypesClasses =method.getParameterTypes();
            //if(arguments.length != paramTypesClasses.length){
            //    throw new RuntimeException("Parameters are not  matched");
            //}
            int paramTypeClassIndex =0;
            StringJoiner params = new StringJoiner(",");
            if(arguments==null || arguments.length == 0){
                params.add("null");
            }else {
                for(String args : arguments){
                    params.add(wrapToObject(paramTypesClasses[paramTypeClassIndex++],args));
                }
            }

            if(Modifier.isStatic(method.getModifiers())){
                testFileContent.append(method.getReturnType().getName()+" result"+testCaseIndex+" = "+c.getName()+"."+method.getName()+"("+params.toString()+");"+NEW_LINE);
            }else{
                //testFileContent.append(c.getName()+" obj = new "+c.getName()+"();"+NEW_LINE);
                testFileContent.append(method.getReturnType().getName()+" result"+testCaseIndex+" = obj."+method.getName()+"("+params.toString()+");"+NEW_LINE);
            }

            if(AssertConditionType.ASSERT_EQUALS.equals(testCase.assertType())){
                testFileContent.append("assertEquals("+wrapToObject(method.getReturnType(),testCase.expected())+", result"+testCaseIndex+");"+NEW_LINE);
            }
            testCaseIndex++;
        }
        testFileContent.append("}"+ NEW_LINE);
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
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
