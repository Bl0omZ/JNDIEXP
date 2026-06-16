package com.bloom.parser;

public enum ActionType {
    DNS_PROBE("dns-probe", "出网验证", RiskLevel.LOW),
    REMOTE_CLASS_LOAD("remote-class-load", "远程 class 加载", RiskLevel.MEDIUM),
    SERIALIZED_GADGET("serialized-gadget", "序列化 gadget", RiskLevel.HIGH),
    COMMAND("command", "命令执行", RiskLevel.HIGH),
    REVERSE_SHELL("reverse-shell", "反弹 shell", RiskLevel.CRITICAL),
    MEMSHELL("memshell", "内存马植入", RiskLevel.CRITICAL),
    UNKNOWN("unknown", "未知", RiskLevel.MEDIUM);

    private final String code;
    private final String description;
    private final RiskLevel riskLevel;

    ActionType(String code, String description, RiskLevel riskLevel) {
        this.code = code;
        this.description = description;
        this.riskLevel = riskLevel;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
}
