package com.bloom.parser;

public enum RiskLevel {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    CRITICAL("严重");

    private final String label;

    RiskLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
