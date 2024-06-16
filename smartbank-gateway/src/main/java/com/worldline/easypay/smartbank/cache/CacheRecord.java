package com.worldline.easypay.smartbank.cache;

public record CacheRecord(
        Object entry,
        byte[] ioBuffer) {
    public CacheRecord(Object entry) {
        this(entry, new byte[1048576]);
    }
}
