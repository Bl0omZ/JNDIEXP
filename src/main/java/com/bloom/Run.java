package com.bloom;

import com.bloom.server.HttpServerStart;
import com.bloom.util.config;

import static com.bloom.server.LdapServer.lanuchLDAPServer;

public class Run {
    public static void main(String[] args) throws Exception {
//        RmiServer rmiServer=new RmiServer();
//        rmiServer.startserver();
        config.applyCmdArgs(args);
//        LdapServer.start();
//        HTTPServer.start();
//        String http_server_ip = "127.0.0.1";
//        int ldap_port = Integer.valueOf("1389");
//        int http_server_port = Integer.valueOf("8080");
        HttpServerStart httpServerStart=new HttpServerStart();
        httpServerStart.start();
        lanuchLDAPServer();
        System.out.println("memshellKEY: ck4Gr4Qi");
    }

}
