package com.bloom.session;

import com.bloom.parser.ActionType;
import com.bloom.parser.ParsedRoute;
import com.bloom.parser.RiskLevel;
import com.bloom.parser.RouteType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VerificationSession {
    private final String sessionId;
    private final Instant startTime;
    private final List<VerificationEntry> entries = new ArrayList<>();

    public VerificationSession() {
        this.sessionId = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.startTime = Instant.now();
    }

    public void recordHit(String sourceIp, ParsedRoute route, String payloadResult) {
        VerificationEntry entry = new VerificationEntry(
                Instant.now(),
                sourceIp,
                "LDAP",
                route.getPayloadType(),
                route.getMethod(),
                route.getActionType(),
                route.getRawBaseDN(),
                payloadResult != null ? "SUCCESS" : "FAILED",
                route.getValidationErrors()
        );
        entries.add(entry);
        printEntry(entry);
    }

    public List<VerificationEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    private void printEntry(VerificationEntry entry) {
        System.out.println(String.format("[SESSION %s] %s | %s | %s | %s | action=%s | risk=%s | %s",
                sessionId,
                entry.getTimestamp().toString(),
                entry.getSourceIp(),
                entry.getPayloadType(),
                entry.getMethod(),
                entry.getActionType().getCode(),
                entry.getActionType().getRiskLevel().getLabel(),
                entry.getResult()));
    }

    public static class VerificationEntry {
        private final Instant timestamp;
        private final String sourceIp;
        private final String protocol;
        private final String payloadType;
        private final String method;
        private final ActionType actionType;
        private final String baseDN;
        private final String result;
        private final List<String> errors;

        public VerificationEntry(Instant timestamp, String sourceIp, String protocol,
                                 String payloadType, String method, ActionType actionType,
                                 String baseDN, String result, List<String> errors) {
            this.timestamp = timestamp;
            this.sourceIp = sourceIp;
            this.protocol = protocol;
            this.payloadType = payloadType;
            this.method = method;
            this.actionType = actionType;
            this.baseDN = baseDN;
            this.result = result;
            this.errors = Collections.unmodifiableList(errors);
        }

        public Instant getTimestamp() { return timestamp; }
        public String getSourceIp() { return sourceIp; }
        public String getProtocol() { return protocol; }
        public String getPayloadType() { return payloadType; }
        public String getMethod() { return method; }
        public ActionType getActionType() { return actionType; }
        public String getBaseDN() { return baseDN; }
        public String getResult() { return result; }
        public List<String> getErrors() { return errors; }
    }
}
