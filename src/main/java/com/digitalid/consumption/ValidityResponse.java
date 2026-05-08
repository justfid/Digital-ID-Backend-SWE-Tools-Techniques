package com.digitalid.consumption;

public class ValidityResponse {

    private final boolean valid;
    private final String reason;

    public ValidityResponse(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }
}
