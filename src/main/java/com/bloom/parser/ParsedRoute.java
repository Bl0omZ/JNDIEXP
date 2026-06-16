package com.bloom.parser;

import java.util.Collections;
import java.util.List;

public class ParsedRoute {
    private final RouteType routeType;
    private final String payloadType;
    private final String method;
    private final String command;
    private final ActionType actionType;
    private final String rawBaseDN;
    private final List<String> validationErrors;
    private final boolean valid;

    private ParsedRoute(Builder builder) {
        this.routeType = builder.routeType;
        this.payloadType = builder.payloadType;
        this.method = builder.method;
        this.command = builder.command;
        this.actionType = builder.actionType;
        this.rawBaseDN = builder.rawBaseDN;
        this.validationErrors = Collections.unmodifiableList(builder.validationErrors);
        this.valid = builder.validationErrors.isEmpty();
    }

    public RouteType getRouteType() { return routeType; }
    public String getPayloadType() { return payloadType; }
    public String getMethod() { return method; }
    public String getCommand() { return command; }
    public ActionType getActionType() { return actionType; }
    public String getRawBaseDN() { return rawBaseDN; }
    public List<String> getValidationErrors() { return validationErrors; }
    public boolean isValid() { return valid; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RouteType routeType;
        private String payloadType;
        private String method;
        private String command;
        private ActionType actionType = ActionType.UNKNOWN;
        private String rawBaseDN;
        private List<String> validationErrors = new java.util.ArrayList<>();

        public Builder routeType(RouteType routeType) { this.routeType = routeType; return this; }
        public Builder payloadType(String payloadType) { this.payloadType = payloadType; return this; }
        public Builder method(String method) { this.method = method; return this; }
        public Builder command(String command) { this.command = command; return this; }
        public Builder actionType(ActionType actionType) { this.actionType = actionType; return this; }
        public Builder rawBaseDN(String rawBaseDN) { this.rawBaseDN = rawBaseDN; return this; }
        public Builder addError(String error) { this.validationErrors.add(error); return this; }

        public ParsedRoute build() {
            return new ParsedRoute(this);
        }
    }

    @Override
    public String toString() {
        return String.format("ParsedRoute{type=%s, payload=%s, method=%s, action=%s, valid=%s}",
                routeType, payloadType, method, actionType, valid);
    }
}
