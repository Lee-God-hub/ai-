package com.smartproperty.enums;

/**
 * 租房流程阶段枚举
 */
public enum RentStage {
    VIEWING("viewing", "预约看房"),
    VIEWING_CONFIRMED("viewing_confirmed", "看房确认"),
    INTENTION("intention", "意向谈价"),
    SIGN_CONTRACT("sign_contract", "签订合同"),
    CONTRACT_RECORD("contract_record", "合同备案"),
    HANDOVER("handover", "交付入住"),
    IN_LEASE("in_lease", "租期中"),
    RENEWAL("renewal", "续租/退房"),
    COMPLETED("completed", "已完成");

    private final String code;
    private final String name;

    RentStage(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static RentStage fromCode(String code) {
        for (RentStage stage : values()) {
            if (stage.code.equals(code)) {
                return stage;
            }
        }
        return null;
    }
}
