package com.bloom.server;

import com.bloom.parser.ParsedRoute;
import com.bloom.parser.RouteParser;
import com.bloom.session.VerificationSession;
import com.bloom.util.config;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.InitialContext;
import java.io.File;
import java.net.ServerSocket;
import java.util.List;

/**
 * 实证 hsqldb_initSql 二阶段回连的 DN 编码回环：
 * 目标端 JNDI lookup -> 本工具 LDAP 收到 baseDN -> RouteParser 提取命令。
 *
 * 关键问题：JNDI 的 LDAP URL 解析是否对 DN 做百分号解码？
 * 这决定 hsqldb_initSql 里命令该用哪种编码写入 ldap://.../basic/<cmd>。
 *
 * 安全约束：测试 JVM 强制 trustURLCodebase=false，JNDI 不会真正加载
 * 远端二阶段类；并用标记文件断言命令未被执行。
 */
public class JndiDnEncodingTest {

    private static int ldapPort;
    private static int httpPort;

    @BeforeClass
    public static void startServers() throws Exception {
        // 禁止测试 JVM 远程类加载，确保二阶段类不会被加载执行
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "false");
        ldapPort = freePort();
        httpPort = freePort();
        config.ip = "127.0.0.1";
        config.ldapPort = ldapPort;
        config.httpPort = httpPort;
        HttpServerStart.start();
        LdapServer.lanuchLDAPServer();
        Thread.sleep(300);
    }

    /**
     * 给定一种编码方式，发起 lookup 并返回服务端实际收到、且被 RouteParser 提取出的命令。
     */
    private String roundTrip(String encodedSegment) throws Exception {
        List<VerificationSession.VerificationEntry> entries = LdapServer.getSession().getEntries();
        int before = entries.size();
        String url = "ldap://127.0.0.1:" + ldapPort + "/basic/" + encodedSegment;
        try {
            new InitialContext().lookup(url);
        } catch (Exception expected) {
            // trustURLCodebase=false 时解析 Reference 会抛异常，属预期；只关心服务端收到的 baseDN
        }
        Thread.sleep(200);
        List<VerificationSession.VerificationEntry> after = LdapServer.getSession().getEntries();
        Assert.assertTrue("服务端应收到 lookup 请求: " + url, after.size() > before);
        String receivedBaseDN = after.get(after.size() - 1).getBaseDN();
        System.out.println("[enc] url段=" + encodedSegment + " | 收到baseDN=" + receivedBaseDN);
        ParsedRoute route = new RouteParser().parse(receivedBaseDN);
        System.out.println("[enc] 提取命令=" + route.getCommand() + " | errors=" + route.getValidationErrors());
        return route.getCommand();
    }

    @Test
    public void currentHsqldbEncodingRoundTrips() throws Exception {
        // 与 JDBCRce.hsqldb_initSql 当前实现一致的编码：URLEncoder + 空格转 %20
        String originalCmd = "touch /tmp/hsqldb-enc-probe";
        String encoded = java.net.URLEncoder.encode(originalCmd).replace("+", "%20");
        String extracted = roundTrip(encoded);
        Assert.assertEquals("当前 hsqldb 编码应回环出原命令", originalCmd, extracted);
        // 命令未被执行的安全断言
        Assert.assertFalse("命令不得在测试 JVM 执行", new File("/tmp/hsqldb-enc-probe").exists());
    }

    @Test
    public void spaceOnlyEncodingRoundTrips() throws Exception {
        // 备选方案：只把空格替换为 %20，斜杠保持字面量
        String originalCmd = "touch /tmp/hsqldb-enc-probe2";
        String encoded = originalCmd.replace(" ", "%20");
        String extracted = roundTrip(encoded);
        System.out.println("[enc] 仅空格编码提取结果=" + extracted);
        Assert.assertFalse("命令不得在测试 JVM 执行", new File("/tmp/hsqldb-enc-probe2").exists());
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
