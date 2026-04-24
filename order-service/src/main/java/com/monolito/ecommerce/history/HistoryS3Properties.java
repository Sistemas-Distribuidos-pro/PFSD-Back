package com.monolito.ecommerce.history;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "history.s3")
public class HistoryS3Properties {

    private boolean enabled;
    private String bucketName;
    private String region;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
