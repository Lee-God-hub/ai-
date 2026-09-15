package com.smartproperty.enums;

/**
 * 买房流程阶段枚举
 */
public enum PurchaseStage {
    VIEWING("viewing", "预约看房"),
    VIEWING_CONFIRMED("viewing_confirmed", "看房确认"),
    INTENTION("intention", "意向谈价"),
    SIGN_INTENTION("sign_intention", "签订意向书"),
    PAY_DEPOSIT("pay_deposit", "支付定金"),
    SIGN_CONTRACT("sign_contract", "签订购房合同"),
    PAY_DOWN_PAYMENT("pay_down_payment", "支付首付款"),
    ONLINE_SIGN("online_sign", "网签备案"),
    LOAN_APPROVAL("loan_approval", "贷款审批"),
    TRANSFER("transfer", "产权过户"),
    HANDOVER("handover", "交房验收"),
    COMPLETED("completed", "已完成");

    private final String code;
    private final String name;

    PurchaseStage(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static PurchaseStage fromCode(String code) {
        for (PurchaseStage stage : values()) {
            if (stage.code.equals(code)) {
                return stage;
            }
        }
        return null;
    }
}
