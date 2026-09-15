package com.smartproperty.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 推荐点击记录请求DTO
 * 用于记录用户点击推荐房源的行为
 *
 * @author 毕业设计项目
 */
public class RecommendClickRequest {

    /**
     * 房源ID
     * 必填
     */
    @NotNull(message = "房源ID不能为空")
    private Long propertyId;

    /**
     * 推荐来源
     * 如：recommendation（智能推荐）、hot（热门推荐）等
     */
    private String source;

    // ===== 构造方法 =====

    public RecommendClickRequest() {
    }

    public RecommendClickRequest(Long propertyId) {
        this.propertyId = propertyId;
    }

    public RecommendClickRequest(Long propertyId, String source) {
        this.propertyId = propertyId;
        this.source = source;
    }

    // ===== Getter / Setter =====

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "RecommendClickRequest{" +
                "propertyId=" + propertyId +
                ", source='" + source + '\'' +
                '}';
    }
}
