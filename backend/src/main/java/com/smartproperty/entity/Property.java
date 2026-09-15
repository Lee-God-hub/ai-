package com.smartproperty.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房源实体类
 *
 * @author 毕业设计项目
 */
public class Property {

    /** 房源ID */
    private Long id;

    /** 房东ID */
    private Long landlordId;

    /** 房源标题 */
    private String title;

    /** 房源描述 */
    private String description;

    /** 价格 */
    private BigDecimal price;

    /** 价格类型：0-月租，1-总价 */
    private Integer priceType;

    /** 面积 */
    private BigDecimal area;

    /** 卧室数量 */
    private Integer bedrooms;

    /** 卫生间数量 */
    private Integer bathrooms;

    /** 详细地址 */
    private String address;

    /** 城市 */
    private String city;

    /** 区域/区县 */
    private String district;

    /** 房源类型：0-住宅，1-公寓，2-别墅 */
    private Integer propertyType;

    /** 交易类型：0-出租，1-出售 */
    private Integer transactionType;

    /** 房源图片URL，多个用逗号分隔 */
    private String images;

    /** 状态：0-待审核，1-审核通过，2-审核驳回，3-已上架，4-已下架 */
    private Integer status;

    /** 浏览次数 */
    private Integer viewCount;

    /** 驳回原因 */
    private String rejectReason;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 房东信息（关联查询用，非数据库字段） */
    @TableField(exist = false)
    private User landlord;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLandlordId() { return landlordId; }
    public void setLandlordId(Long landlordId) { this.landlordId = landlordId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getPriceType() { return priceType; }
    public void setPriceType(Integer priceType) { this.priceType = priceType; }

    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }

    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }

    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public Integer getPropertyType() { return propertyType; }
    public void setPropertyType(Integer propertyType) { this.propertyType = propertyType; }

    public Integer getTransactionType() { return transactionType; }
    public void setTransactionType(Integer transactionType) { this.transactionType = transactionType; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public User getLandlord() { return landlord; }
    public void setLandlord(User landlord) { this.landlord = landlord; }
}
