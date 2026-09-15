package com.smartproperty.dto;

public class ContentGenerationResponse {
    private String shortVersion;
    private String detailVersion;
    private Long generationId;
    private Long generationTime;

    public ContentGenerationResponse() {
    }

    public ContentGenerationResponse(String shortVersion, String detailVersion) {
        this.shortVersion = shortVersion;
        this.detailVersion = detailVersion;
    }

    public ContentGenerationResponse(String shortVersion, String detailVersion, Long generationId) {
        this.shortVersion = shortVersion;
        this.detailVersion = detailVersion;
        this.generationId = generationId;
    }

    public String getShortVersion() {
        return shortVersion;
    }

    public void setShortVersion(String shortVersion) {
        this.shortVersion = shortVersion;
    }

    public String getDetailVersion() {
        return detailVersion;
    }

    public void setDetailVersion(String detailVersion) {
        this.detailVersion = detailVersion;
    }

    public Long getGenerationId() {
        return generationId;
    }

    public void setGenerationId(Long generationId) {
        this.generationId = generationId;
    }

    public Long getGenerationTime() {
        return generationTime;
    }

    public void setGenerationTime(Long generationTime) {
        this.generationTime = generationTime;
    }

    @Override
    public String toString() {
        return "ContentGenerationResponse{" +
                "shortVersion='" + shortVersion + '\'' +
                ", detailVersion='" + detailVersion + '\'' +
                ", generationId=" + generationId +
                ", generationTime=" + generationTime +
                '}';
    }
}