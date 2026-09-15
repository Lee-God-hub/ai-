package com.smartproperty.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * AI文案生成请求DTO
 * 用于接收房东提交的房源信息，生成宣传文案
 *
 * @author 毕业设计项目
 */
public class ContentRequest {

    /**
     * 房源标题
     * 必填
     */
    @NotBlank(message = "房源标题不能为空")
    @Size(max = 100, message = "房源标题长度不能超过100字符")
    private String title;

    /**
     * 房源类型
     * 0-住宅，1-公寓，2-别墅，3-商铺
     * 必填
     */
    @NotNull(message = "房源类型不能为空")
    private Integer propertyType;

    /**
     * 交易类型
     * 0-出租，1-出售
     * 必填
     */
    @NotNull(message = "交易类型不能为空")
    private Integer transactionType;

    /**
     * 面积（平方米）
     * 必填，最小值0.01
     */
    @NotNull(message = "面积不能为空")
    @DecimalMin(value = "0.01", message = "面积必须大于0")
    private BigDecimal area;

    /**
     * 卧室数量
     */
    private Integer bedrooms;

    /**
     * 卫生间数量
     */
    private Integer bathrooms;

    /**
     * 价格
     * 必填，最小值0.01
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    /**
     * 价格类型
     * 0-月租，1-总价
     */
    private Integer priceType;

    /**
     * 城市
     * 必填
     */
    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市名称长度不能超过50字符")
    private String city;

    /**
     * 区域/区县
     */
    @Size(max = 50, message = "区域名称长度不能超过50字符")
    private String district;

    /**
     * 详细地址
     */
    @Size(max = 200, message = "详细地址长度不能超过200字符")
    private String address;

    /**
     * 朝向
     * 如：南北通透、朝南、朝北等
     */
    @Size(max = 50, message = "朝向描述长度不能超过50字符")
    private String orientation;

    /**
     * 楼层信息
     * 如：高层、中层、低层、1/10等
     */
    @Size(max = 50, message = "楼层信息长度不能超过50字符")
    private String floor;

    /**
     * 配套设施
     * 如：地铁、学校、商场、公园等，多个用逗号分隔
     */
    @Size(max = 500, message = "配套设施描述长度不能超过500字符")
    private String facilities;

    /**
     * 装修情况
     * 如：精装修、简装、毛坯等
     */
    @Size(max = 50, message = "装修情况描述长度不能超过50字符")
    private String decoration;

    // ===== 构造方法 =====

    public ContentRequest() {
    }

    // ===== Getter / Setter =====

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(Integer propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getDecoration() {
        return decoration;
    }

    public void setDecoration(String decoration) {
        this.decoration = decoration;
    }

    @Override
    public String toString() {
        return "ContentRequest{" +
                "title='" + title + '\'' +
                ", propertyType=" + propertyType +
                ", transactionType=" + transactionType +
                ", area=" + area +
                ", bedrooms=" + bedrooms +
                ", bathrooms=" + bathrooms +
                ", price=" + price +
                ", priceType=" + priceType +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", address='" + address + '\'' +
                ", orientation='" + orientation + '\'' +
                ", floor='" + floor + '\'' +
                ", facilities='" + facilities + '\'' +
                ", decoration='" + decoration + '\'' +
                '}';
    }
}
