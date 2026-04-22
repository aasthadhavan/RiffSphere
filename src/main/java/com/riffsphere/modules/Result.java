package com.riffsphere.modules;

import com.riffsphere.models.Song;
import java.util.*;

/**
 * Generic Result<T> wrapper returned by operations that can fail.
 * Demonstrates: Generics, Immutability, Functional Error Handling.
 */
public final class Result<T> {

    private final T       value;
    private final String  message;
    private final boolean success;

    private Result(T value, String message, boolean success) {
        this.value   = value;
        this.message = message;
        this.success = success;
    }

    public static <T> Result<T> ok(T value)              { return new Result<>(value, "OK", true); }
    public static <T> Result<T> ok(T value, String msg)  { return new Result<>(value, msg, true);  }
    public static <T> Result<T> fail(String message)     { return new Result<>(null,  message, false); }

    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    public T       getValue()  { return value; }
    public String  getMessage(){ return message; }

    public T getOrElse(T fallback) { return success ? value : fallback; }

    @Override
    public String toString() {
        return success ? "Result.ok(" + value + ")" : "Result.fail(" + message + ")";
    }
}
