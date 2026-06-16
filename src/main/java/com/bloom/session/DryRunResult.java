package com.bloom.session;

import com.bloom.parser.ActionType;
import com.bloom.parser.ParsedRoute;
import com.bloom.parser.RiskLevel;
import com.bloom.parser.RouteType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DryRunResult {
    private final List<DryRunEntry> entries = new ArrayList<>();
    private boolean allValid = true;

    public void addEntry(ParsedRoute route) {
        DryRunEntry entry = new DryRunEntry(
                route.getRouteType(),
                route.getPayloadType(),
                route.getMethod(),
                route.getActionType(),
                route.getRawBaseDN(),
                route.isValid(),
                route.getValidationErrors()
        );
        entries.add(entry);
        if (!route.isValid()) {
            allValid = false;
        }
    }

    public List<DryRunEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isAllValid() {
        return allValid;
    }

    public void printReport() {
        System.out.println("=== DRY RUN REPORT ===");
        System.out.println("Total entries: " + entries.size());
        System.out.println("Valid: " + (allValid ? "ALL" : "SOME FAILED"));
        System.out.println("---");

        for (DryRunEntry entry : entries) {
            String status = entry.isValid() ? "OK" : "FAIL";
            System.out.println(String.format("[%s] %s | %s | %s | action=%s | risk=%s",
                    status,
                    entry.getRouteType() != null ? entry.getRouteType().getPrefix() : "null",
                    entry.getPayloadType(),
                    entry.getMethod(),
                    entry.getActionType().getCode(),
                    entry.getActionType().getRiskLevel().getLabel()));
            if (!entry.isValid()) {
                for (String error : entry.getErrors()) {
                    System.out.println("  ERROR: " + error);
                }
            }
        }
        System.out.println("======================");
    }

    public static class DryRunEntry {
        private final RouteType routeType;
        private final String payloadType;
        private final String method;
        private final ActionType actionType;
        private final String rawBaseDN;
        private final boolean valid;
        private final List<String> errors;

        public DryRunEntry(RouteType routeType, String payloadType, String method,
                           ActionType actionType, String rawBaseDN, boolean valid,
                           List<String> errors) {
            this.routeType = routeType;
            this.payloadType = payloadType;
            this.method = method;
            this.actionType = actionType;
            this.rawBaseDN = rawBaseDN;
            this.valid = valid;
            this.errors = Collections.unmodifiableList(errors);
        }

        public RouteType getRouteType() { return routeType; }
        public String getPayloadType() { return payloadType; }
        public String getMethod() { return method; }
        public ActionType getActionType() { return actionType; }
        public String getRawBaseDN() { return rawBaseDN; }
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
    }
}
