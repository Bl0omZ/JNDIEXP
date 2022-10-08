package com.bloom.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Fuzz {

    /*fuzz 的refenceref类,接收到http请求，基本上可以利用成功*/
    public String EL = "javax.el.ELProcessor";
    public String BeanShell2 = "bsh.Interpreter";
    public String h2Driver="org.h2.Driver";
    public String groovyBytomcat="groovy.lang.GroovyClassLoader";
    public String tomcat_dbcp2_RCE="org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory";
    public String tomcat_dbcp1_RCE="org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory";
    public String commons_dbcp2_RCE="org.apache.commons.dbcp2.BasicDataSourceFactory";
    public String commons_dbcp1_RCE="org.apache.commons.dbcp.BasicDataSourceFactory";
    public String druidjdbc="com.alibaba.druid.pool.DruidDataSourceFactory";
    public String tomcatjdbc="org.apache.tomcat.jdbc.pool.DataSourceFactory";
    public String snakeyaml="org.yaml.snakeyaml.Yaml";
    public String XStream="com.thoughtworks.xstream.XStream";
    public String mvel="org.mvel2.sh.ShellSession";
    /*fuzz 反序列化类,接收到http请求，存在利用不成功的问题，版本或者其他依赖不满足条件*/
    public String CommonsBeanutils1 = "org.apache.commons.beanutils.BeanComparator";
    public String CommonsCollectionsK1="org.apache.commons.collections.map.LazyMap";
    public String CommonsCollectionsK2="org.apache.commons.collections4.map.LazyMap";
    public String Clojure="clojure.lang.PersistentArrayMap";
    public String Groovy="org.codehaus.groovy.runtime.ConvertedClosure";
    public String C3P0="com.mchange.v2.c3p0.PoolBackedDataSource";
    public String JSON1="net.sf.json.JSONObject";
    public String FileUpload1="org.apache.commons.fileupload.disk.DiskFileItem";
    public String hibernate="org.hibernate.engine.spi.TypedValue";
    public String MozillaRhino1="org.mozilla.javascript.MemberBox";
    public String Rome="com.sun.syndication.feed.impl.ObjectBean";

    public static final Map<String,String> Fuzzmap=new HashMap<String, String>(){
        {
            put("EL","javax.el.ELProcessor");
            put("BeanShell2","bsh.Interpreter");
            put("groovyBytomcat","groovy.lang.GroovyClassLoader");
            put("h2Driver","org.h2.Driver");
            put("tomcat_dbcp2_RCE","org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
            put("tomcat_dbcp1_RCE","org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory");
            put("commons_dbcp2_RCE","org.apache.commons.dbcp2.BasicDataSourceFactory");
            put("commons_dbcp1_RCE","org.apache.commons.dbcp.BasicDataSourceFactory");
            put("druidjdbc","com.alibaba.druid.pool.DruidDataSourceFactory");
            put("tomcatjdbc","org.apache.tomcat.jdbc.pool.DataSourceFactory");
            put("snakeyaml","org.yaml.snakeyaml.Yaml");
            put("XStream","com.thoughtworks.xstream.XStream");
            put("mvel","org.mvel2.sh.ShellSession");
            put("CommonsBeanutils1","org.apache.commons.beanutils.BeanComparator");
            put("CommonsCollectionsK1","org.apache.commons.collections.map.LazyMap");
            put("CommonsCollectionsK2","org.apache.commons.collections4.map.LazyMap");
            put("Clojure","clojure.lang.PersistentArrayMap");
            put("Groovy","org.codehaus.groovy.runtime.ConvertedClosure");
            put("C3P0","com.mchange.v2.c3p0.PoolBackedDataSource");
            put("JSON1","net.sf.json.JSONObject");
            put("FileUpload1","org.apache.commons.fileupload.disk.DiskFileItem");
            put("hibernate","org.hibernate.engine.spi.TypedValue");
            put("MozillaRhino1","org.mozilla.javascript.MemberBox");
            put("rome","com.sun.syndication.feed.impl.ObjectBean");
        }
    };


    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
//        Field[] fields = Fuzz.class.getFields();
//        Object Fuzz=Fuzz.class.newInstance();
//        Map<String,String> map=new HashMap<>();
//        for (Field x:fields) {
//            map.put(x.getName(), (String) x.get(Fuzz));//class中声明的常量传入map
//            System.out.println("(\""+x.getName()+ "\",\"" +(String) x.get(Fuzz)+"\")");
//        }

        Field[] fields = Fuzz.class.getFields();
        Object Fuzz=Fuzz.class.newInstance();
        Map<String,String> map=new HashMap<>();
        for (Field x:fields) {
            System.out.println(x.get(Fuzz));
        }
//        System.out.println(Fuzz.map);
    }
}
