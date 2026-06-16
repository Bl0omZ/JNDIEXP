package com.bloom.parser;

public enum RouteType {
    BASIC("basic", "远程 class 引用"),
    GADGET("gadget", "序列化 gadget"),
    FUZZ("fuzz", "Fuzz 探测"),
    FUZZ_BY_DNS("fuzzbyDNS", "DNS 探测"),
    MEMSHELL("memshell", "内存马");

    private final String prefix;
    private final String description;

    RouteType(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDescription() {
        return description;
    }

    public static RouteType fromPrefix(String prefix) {
        if (prefix == null) return null;
        String lower = prefix.toLowerCase();
        for (RouteType type : values()) {
            if (type.prefix.equalsIgnoreCase(lower)) {
                return type;
            }
        }
        return null;
    }
}
