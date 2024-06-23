package com.worldline.easypay.smartbank.cache;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CacheRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheRepository.class);

    private Map<String, CacheRecord> cache = new HashMap<>();

    public void put(String key, Object value) {
        cache.put(key, new CacheRecord(value));
        LOGGER.info("New heap size is now: {}/{}|{}", Runtime.getRuntime().totalMemory(), Runtime.getRuntime().maxMemory(), Runtime.getRuntime().freeMemory());
    }

    public Object get(String key) {
        var record = cache.get(key);
        if (record == null) {
            return null;
        }
        return record.entry();
    }

}
