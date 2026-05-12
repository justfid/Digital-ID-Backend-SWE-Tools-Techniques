package com.digitalid.model;

public final class Result<T> {

    private final T value;
    private final String failureReason;
    private final boolean success;

    private Result(T value, String failureReason, boolean success) {
        this.value = value;
        this.failureReason = failureReason;
        this.success = success;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }

    public static <T> Result<T> failure(String reason) {
        return new Result<>(null, reason, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getValue() {
        if (!success) {
            throw new IllegalStateException("Result is a failure — call getFailureReason() instead");
        }
        return value;
    }

    public String getFailureReason() {
        if (success) {
            throw new IllegalStateException("Result is a success — call getValue() instead");
        }
        return failureReason;
    }
}