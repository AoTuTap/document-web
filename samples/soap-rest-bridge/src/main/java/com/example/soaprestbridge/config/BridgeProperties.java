package com.example.soaprestbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bridge")
public class BridgeProperties {

    private String restBaseUrl;
    private String defaultTargetPath;
    private int connectTimeout = 3000;
    private int readTimeout = 5000;

    public String getRestBaseUrl() {
        return restBaseUrl;
    }

    public void setRestBaseUrl(String restBaseUrl) {
        this.restBaseUrl = restBaseUrl;
    }

    public String getDefaultTargetPath() {
        return defaultTargetPath;
    }

    public void setDefaultTargetPath(String defaultTargetPath) {
        this.defaultTargetPath = defaultTargetPath;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
