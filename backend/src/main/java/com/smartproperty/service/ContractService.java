package com.smartproperty.service;

import com.smartproperty.entity.Contract;
import java.util.List;

/**
 * 合同管理Service接口
 */
public interface ContractService {

    /**
     * 创建合同
     */
    Contract createContract(Contract contract);

    /**
     * 获取合同详情
     */
    Contract getContract(Long id);

    /**
     * 根据预约ID获取合同
     */
    Contract getContractByAppointmentId(Long appointmentId);

    /**
     * 签署合同
     */
    void signContract(Long id, Long userId);

    /**
     * 合同备案
     */
    void recordContract(Long id, String recordNo, String recordOrg);

    /**
     * 获取用户的合同列表
     */
    List<Contract> getMyContracts(Long userId);

    /**
     * 更新合同状态
     */
    void updateStatus(Long id, Integer status);
}
