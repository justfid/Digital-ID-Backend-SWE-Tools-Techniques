package com.digitalid.consumption;

public class LicenceVerificationResponse {

    private final boolean valid;
    private final String reason;

    public LicenceVerificationResponse(boolean valid, String reason) {
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
