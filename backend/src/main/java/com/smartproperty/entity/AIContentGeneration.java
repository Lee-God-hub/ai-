package com.smartproperty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * AI文案生成记录实体类
 * 用于存储房东使用AI生成房源文案的记录
 *
 * @author 毕业设计项目
 */
@TableName("ai_content_generation")
public class AIContentGeneration {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 房东ID，关联user表
     */
    private Long landlordId;

    /**
     * 关联的房源ID
     * 如果文案已应用到房源则记录房源ID，否则为NULL
     */
    private Long propertyId;

    /**
     * 输入的房源信息（JSON格式）
     * 包含户型、面积、价格、位置、配套设施等信息
     */
    private String inputData;

    /**
     * 生成的简短版文案（100-200字）
     * 适合用于房源列表展示
     */
    private String shortContent;

    /**
     * 生成的详细版文案（300-500字）
     * 适合用于房源详情页展示
     */
    private String detailContent;

    /**
     * 是否已应用到房源
     * 0: 未应用
     * 1: 已应用
     */
    private Integer isApplied;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ===== 构造方法 =====

    public AIContentGeneration() {
    }

    public AIContentGeneration(Long landlordId, String inputData, String shortContent, String detailContent) {
        this.landlordId = landlordId;
        this.inputData = inputData;
        this.shortContent = shortContent;
        this.detailContent = detailContent;
        this.isApplied = 0;
    }

    // ===== Getter / Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(Long landlordId) {
        this.landlordId = landlordId;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getShortContent() {
        return shortContent;
    }

    public void setShortContent(String shortContent) {
        this.shortContent = shortContent;
    }

    public String getDetailContent() {
        return detailContent;
    }

    public void setDetailContent(String detailContent) {
        this.detailContent = detailContent;
    }

    public Integer getIsApplied() {
        return isApplied;
    }

    public void setIsApplied(Integer isApplied) {
        this.isApplied = isApplied;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AIContentGeneration{" +
                "id=" + id +
                ", landlordId=" + landlordId +
                ", propertyId=" + propertyId +
                ", inputData='" + inputData + '\'' +
                ", shortContent='" + shortContent + '\'' +
                ", detailContent='" + detailContent + '\'' +
                ", isApplied=" + isApplied +
                ", createTime=" + createTime +
                '}';
    }
}
