package ysoserial.payloads.util;

import com.bloom.util.JavassistClassLoader;
import javassist.ClassPool;
import javassist.CtClass;

public class ClassUtil {
    public static Class genClass(String clazzName) throws Exception {
        if(clazzName.startsWith("java.")){
            return loadClass(clazzName);
        }
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.makeClass(clazzName);
        ctClass.getClassFile().setVersionToJava5();
        Class clazz = ctClass.toClass(new JavassistClassLoader());
        ctClass.defrost();
        return clazz;
    }

    public static Class loadClass(String clazzName) throws Exception{
        try{
            return Class.forName(clazzName);
        }catch (ClassNotFoundException e){
            return Thread.currentThread().getContextClassLoader().loadClass(clazzName);
        }
    }
}
