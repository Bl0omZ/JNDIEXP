package com.bloom.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class RouteParserTest {

    private final RouteParser parser = new RouteParser();

    @Test
    public void testBasicCommand() {
        ParsedRoute route = parser.parse("basic/open -a calculator");
        assertTrue(route.isValid());
        assertEquals(RouteType.BASIC, route.getRouteType());
        assertEquals(ActionType.COMMAND, route.getActionType());
        assertEquals("open -a calculator", route.getCommand());
    }

    @Test
    public void testBasicBase64() {
        ParsedRoute route = parser.parse("basic/base64/b3BlbiAtYSBjYWxjdWxhdG9y");
        assertTrue(route.isValid());
        assertEquals(RouteType.BASIC, route.getRouteType());
        assertEquals(ActionType.COMMAND, route.getActionType());
        assertEquals("open -a calculator", route.getCommand());
    }

    @Test
    public void testBasicReverseShell() {
        ParsedRoute route = parser.parse("basic/reverseshell/127.0.0.1/4444");
        assertTrue(route.isValid());
        assertEquals(RouteType.BASIC, route.getRouteType());
        assertEquals(ActionType.REVERSE_SHELL, route.getActionType());
        assertTrue(route.getCommand().contains("127.0.0.1"));
        assertTrue(route.getCommand().contains("4444"));
    }

    @Test
    public void testBasicReverseShell2() {
        ParsedRoute route = parser.parse("basic/reverseshell2/127.0.0.1/4444");
        assertTrue(route.isValid());
        assertEquals(RouteType.BASIC, route.getRouteType());
        assertEquals(ActionType.REVERSE_SHELL, route.getActionType());
    }

    @Test
    public void testBasicMemshell() {
        ParsedRoute route = parser.parse("basic/memshell/TomcatFilterMemShellFromThread");
        assertTrue(route.isValid());
        assertEquals(RouteType.BASIC, route.getRouteType());
        assertEquals(ActionType.MEMSHELL, route.getActionType());
        assertEquals("CLASS:TomcatFilterMemShellFromThread", route.getCommand());
    }

    @Test
    public void testBasicMemshellFile() {
        ParsedRoute route = parser.parse("basic/memshell/FILE:data/exploit.class");
        assertTrue(route.isValid());
        assertEquals(RouteType.BASIC, route.getRouteType());
        assertEquals(ActionType.MEMSHELL, route.getActionType());
        assertEquals("FILE:data/exploit.class", route.getCommand());
    }

    @Test
    public void testURLDNS() {
        ParsedRoute route = parser.parse("URLDNS/dnslog.cn");
        assertTrue(route.isValid());
        assertEquals(RouteType.GADGET, route.getRouteType());
        assertEquals(ActionType.DNS_PROBE, route.getActionType());
        assertEquals("dnslog.cn", route.getCommand());
    }

    @Test
    public void testGadgetBase64() {
        ParsedRoute route = parser.parse("EL/base64/b3BlbiAtYSBjYWxjdWxhdG9y");
        assertTrue(route.isValid());
        assertEquals(RouteType.GADGET, route.getRouteType());
        assertEquals(ActionType.COMMAND, route.getActionType());
        assertEquals("open -a calculator", route.getCommand());
    }

    @Test
    public void testGadgetReverseShell() {
        ParsedRoute route = parser.parse("EL/reverseshell/127.0.0.1/4444");
        assertTrue(route.isValid());
        assertEquals(RouteType.GADGET, route.getRouteType());
        assertEquals(ActionType.REVERSE_SHELL, route.getActionType());
    }

    @Test
    public void testGadgetMemshell() {
        ParsedRoute route = parser.parse("EL/memshell/tomcatmemshell1");
        assertTrue(route.isValid());
        assertEquals(RouteType.GADGET, route.getRouteType());
        assertEquals(ActionType.MEMSHELL, route.getActionType());
        assertEquals("CLASS:tomcatmemshell1", route.getCommand());
    }

    @Test
    public void testFuzz() {
        ParsedRoute route = parser.parse("fuzz/EL/dnslog.cn");
        assertTrue(route.isValid());
        assertEquals(RouteType.FUZZ, route.getRouteType());
        assertEquals(ActionType.DNS_PROBE, route.getActionType());
        assertEquals("EL", route.getPayloadType());
        assertEquals("dnslog.cn", route.getCommand());
    }

    @Test
    public void testFuzzByDNS() {
        ParsedRoute route = parser.parse("fuzzbyDNS/dnslog.cn");
        assertTrue(route.isValid());
        assertEquals(RouteType.FUZZ_BY_DNS, route.getRouteType());
        assertEquals(ActionType.DNS_PROBE, route.getActionType());
        assertEquals("dnslog.cn", route.getCommand());
    }

    @Test
    public void testNullInput() {
        ParsedRoute route = parser.parse(null);
        assertFalse(route.isValid());
        assertFalse(route.getValidationErrors().isEmpty());
    }

    @Test
    public void testEmptyInput() {
        ParsedRoute route = parser.parse("");
        assertFalse(route.isValid());
        assertFalse(route.getValidationErrors().isEmpty());
    }

    @Test
    public void testInvalidFormat() {
        ParsedRoute route = parser.parse("invalid");
        assertFalse(route.isValid());
        assertFalse(route.getValidationErrors().isEmpty());
    }

    @Test
    public void testCommonsCollectionsMemshell() {
        ParsedRoute route = parser.parse("CommonsCollections1/memshell/TomcatFilterMemShellFromThread");
        assertTrue(route.isValid());
        assertEquals(RouteType.GADGET, route.getRouteType());
        assertEquals(ActionType.MEMSHELL, route.getActionType());
    }

    @Test
    public void testRiskLevels() {
        assertEquals(RiskLevel.LOW, ActionType.DNS_PROBE.getRiskLevel());
        assertEquals(RiskLevel.HIGH, ActionType.COMMAND.getRiskLevel());
        assertEquals(RiskLevel.CRITICAL, ActionType.REVERSE_SHELL.getRiskLevel());
        assertEquals(RiskLevel.CRITICAL, ActionType.MEMSHELL.getRiskLevel());
    }

    // 非法 IP 的 reverse shell 必须在发送前失败（spec: 参数校验）。
    @Test
    public void testReverseShellInvalidIp() {
        ParsedRoute route = parser.parse("basic/reverseshell/999.999.999.999/4444");
        assertFalse(route.isValid());
        assertFalse(route.getValidationErrors().isEmpty());
    }

    // 非法端口必须失败。
    @Test
    public void testReverseShellInvalidPort() {
        ParsedRoute route = parser.parse("basic/reverseshell/127.0.0.1/notaport");
        assertFalse(route.isValid());
        assertFalse(route.getValidationErrors().isEmpty());
    }

    // 超出范围的端口必须失败。
    @Test
    public void testReverseShellPortOutOfRange() {
        ParsedRoute route = parser.parse("basic/reverseshell/127.0.0.1/70000");
        assertFalse(route.isValid());
    }

    // reverse shell 缺少 ip/port 段必须失败，而不是静默返回空命令。
    @Test
    public void testReverseShellMissingParams() {
        ParsedRoute route = parser.parse("basic/reverseshell/127.0.0.1");
        assertFalse(route.isValid());
    }

    // gadget reverse shell 同样校验。
    @Test
    public void testGadgetReverseShellInvalidIp() {
        ParsedRoute route = parser.parse("EL/reverseshell/notanip/4444");
        assertFalse(route.isValid());
    }

    // 无显式 method 的 gadget 探测（如 EL/域名）不应把域名塞进 method 字段。
    @Test
    public void testGadgetNoMethodMethodEmpty() {
        ParsedRoute route = parser.parse("EL/dnslog.cn");
        assertTrue(route.isValid());
        assertEquals(RouteType.GADGET, route.getRouteType());
        assertEquals("", route.getMethod());
        assertEquals("dnslog.cn", route.getCommand());
    }

    // basic 命令模式：命令不应被当成 method。
    @Test
    public void testBasicCommandMethodEmpty() {
        ParsedRoute route = parser.parse("basic/open -a calculator");
        assertTrue(route.isValid());
        assertEquals("", route.getMethod());
        assertEquals("open -a calculator", route.getCommand());
        assertEquals(ActionType.COMMAND, route.getActionType());
    }

    // #9: basic 路由未知 memshell 名必须在解析期失败（dry-run 可发现，不再目标端静默 404）。
    @Test
    public void testBasicMemshellUnknownNameInvalid() {
        ParsedRoute route = parser.parse("basic/memshell/NoSuchShell");
        assertFalse(route.isValid());
        assertTrue(route.getValidationErrors().toString().contains("无该内存马类型"));
    }

    // #9: gadget 路由的 memshell 名是自由文本，不受 basic 存在性校验影响（回归保护）。
    @Test
    public void testGadgetMemshellFreeFormNameStillValid() {
        ParsedRoute route = parser.parse("EL/memshell/tomcatmemshell1");
        assertTrue(route.isValid());
    }

    // 合法 reverse shell 仍然正常工作（回归保护）。
    @Test
    public void testReverseShellValidStillWorks() {
        ParsedRoute route = parser.parse("basic/reverseshell/127.0.0.1/4444");
        assertTrue(route.isValid());
        assertEquals("reverseshell", route.getMethod());
        assertEquals(ActionType.REVERSE_SHELL, route.getActionType());
    }
}
