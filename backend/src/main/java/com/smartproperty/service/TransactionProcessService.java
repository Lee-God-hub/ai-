package com.smartproperty.service;

import com.smartproperty.entity.TransactionProcessLog;
import java.util.List;
import java.util.Map;

/**
 * 交易流程管理Service接口
 */
public interface TransactionProcessService {

    /**
     * 获取流程详情（包含阶段列表和进度）
     */
    Map<String, Object> getProcessDetail(Long appointmentId);

    /**
     * 推进到下一阶段
     */
    void nextStage(Long appointmentId, Long operatorId, String operatorName, String remark);

    /**
     * 获取流程日志列表
     */
    List<TransactionProcessLog> getProcessLogs(Long appointmentId);

    /**
     * 取消交易
     */
    void cancelTransaction(Long appointmentId, Long operatorId, String operatorName, String reason);

    /**
     * 初始化流程（创建第一条日志）
     */
    void initProcess(Long appointmentId, Integer transactionType);
}
