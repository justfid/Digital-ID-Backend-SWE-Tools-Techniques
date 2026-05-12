package com.digitalid.consumption;

public class VerificationResponse {

    private final boolean valid;
    private final String reason;

    private VerificationResponse(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public static VerificationResponse valid(String reason) {
        return new VerificationResponse(true, reason);
    }

    public static VerificationResponse invalid(String reason) {
        return new VerificationResponse(false, reason);
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }
}