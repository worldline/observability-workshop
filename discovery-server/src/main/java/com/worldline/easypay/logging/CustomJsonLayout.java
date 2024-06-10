package com.worldline.easypay.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import java.util.Map;


public class CustomJsonLayout extends JsonLayout {

    private static final String APPLICATION_NAME = "application";
    private static final String APPLICATION_INSTANCE = "instance";

    private String applicationName;
    private String applicationInstance;

    public CustomJsonLayout() {
        super();
    }

    @Override
    protected void addCustomDataToJsonMap(Map<String, Object> map, ILoggingEvent event) {
        add(APPLICATION_NAME, applicationName != null, applicationName, map);
        add(APPLICATION_INSTANCE, applicationInstance != null, applicationInstance, map);
    }

    public String getApplicationName(String applicationName) {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationInstance(String applicationInstance) {
        return applicationInstance;
    }

    public void setApplicationInstance(String applicationInstance) {
        this.applicationInstance = applicationInstance;
    }
}