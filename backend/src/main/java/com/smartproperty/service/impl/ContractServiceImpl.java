package com.smartproperty.service.impl;

import com.smartproperty.entity.Contract;
import com.smartproperty.mapper.ContractMapper;
import com.smartproperty.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 合同管理Service实现类
 */
@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractMapper contractMapper;

    @Override
    @Transactional
    public Contract createContract(Contract contract) {
        // 生成合同编号
        if (contract.getContractNo() == null || contract.getContractNo().isEmpty()) {
            contract.setContractNo(generateContractNo(contract.getContractType()));
        }
        
        contract.setStatus(0); // 草稿
        contract.setCreateTime(LocalDateTime.now());
        contractMapper.insert(contract);
        return contract;
    }

    @Override
    public Contract getContract(Long id) {
        return contractMapper.selectById(id);
    }

    @Override
    public Contract getContractByAppointmentId(Long appointmentId) {
        return contractMapper.selectByAppointmentId(appointmentId);
    }

    @Override
    @Transactional
    public void signContract(Long id, Long userId) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        if (contract.getStatus() != 1) {
            throw new RuntimeException("合同状态不允许签署");
        }

        contract.setStatus(2); // 已签署
        contract.setSignDate(LocalDate.now());
        contractMapper.updateById(contract);
    }

    @Override
    @Transactional
    public void recordContract(Long id, String recordNo, String recordOrg) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }

        contract.setStatus(3); // 已备案
        contract.setRecordNo(recordNo);
        contract.setRecordDate(LocalDate.now());
        contract.setRecordOrg(recordOrg);
        contractMapper.updateById(contract);
    }

    @Override
    public List<Contract> getMyContracts(Long userId) {
        return contractMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        contract.setStatus(status);
        contractMapper.updateById(contract);
    }

    /**
     * 生成合同编号
     */
    private String generateContractNo(Integer contractType) {
        String prefix;
        switch (contractType) {
            case 0: prefix = "RC"; break; // Rent Contract
            case 1: prefix = "PC"; break; // Purchase Contract
            case 2: prefix = "IC"; break; // Intention Contract
            default: prefix = "CT"; break;
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + timestamp + (int)(Math.random() * 1000);
    }
}
