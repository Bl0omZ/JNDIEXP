package ysoserial.payloads.util;

import com.bloom.util.JavassistClassLoader;
import javassist.ClassPool;
import javassist.CtClass;

public class ClassUtil {
    public static Class genClass(String clazzName) throws Exception {
        if(clazzName.startsWith("java.")){
            return loadClass(clazzName);
        }
        // 隔离 ClassPool：全局 getDefault() 中的 CtClass 可能被其他链路生成冻结，
        // 同名 makeClass 会报 frozen class，导致探针静默丢失
        ClassPool classPool = new ClassPool();
        classPool.appendSystemPath();
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
