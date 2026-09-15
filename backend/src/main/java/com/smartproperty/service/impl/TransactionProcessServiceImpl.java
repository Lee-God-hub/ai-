package com.smartproperty.service.impl;

import com.smartproperty.entity.Appointment;
import com.smartproperty.entity.TransactionProcessLog;
import com.smartproperty.enums.PurchaseStage;
import com.smartproperty.enums.RentStage;
import com.smartproperty.mapper.AppointmentMapper;
import com.smartproperty.mapper.TransactionProcessLogMapper;
import com.smartproperty.service.TransactionProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 交易流程管理Service实现类
 */
@Service
public class TransactionProcessServiceImpl implements TransactionProcessService {

    @Autowired
    private TransactionProcessLogMapper processLogMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Map<String, Object> getProcessDetail(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        List<TransactionProcessLog> logs = processLogMapper.selectByAppointmentId(appointmentId);
        
        // 获取所有阶段
        List<Map<String, Object>> stages = new ArrayList<>();
        if (appointment.getTransactionType() == 0) {
            // 租房流程
            for (RentStage stage : RentStage.values()) {
                Map<String, Object> stageMap = new HashMap<>();
                stageMap.put("code", stage.getCode());
                stageMap.put("name", stage.getName());
                stageMap.put("status", getStageStatus(logs, stage.getCode()));
                stages.add(stageMap);
            }
        } else {
            // 买房流程
            for (PurchaseStage stage : PurchaseStage.values()) {
                Map<String, Object> stageMap = new HashMap<>();
                stageMap.put("code", stage.getCode());
                stageMap.put("name", stage.getName());
                stageMap.put("status", getStageStatus(logs, stage.getCode()));
                stages.add(stageMap);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("appointment", appointment);
        result.put("stages", stages);
        result.put("logs", logs);
        result.put("currentStage", appointment.getCurrentStage());

        return result;
    }

    private Integer getStageStatus(List<TransactionProcessLog> logs, String stageCode) {
        for (TransactionProcessLog log : logs) {
            if (log.getStageCode().equals(stageCode)) {
                return log.getStatus();
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void nextStage(Long appointmentId, Long operatorId, String operatorName, String remark) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        String currentStage = appointment.getCurrentStage();
        String nextStageCode = getNextStageCode(currentStage, appointment.getTransactionType());
        
        if (nextStageCode == null) {
            throw new RuntimeException("已经是最后阶段");
        }

        // 更新当前阶段为已完成
        TransactionProcessLog currentLog = processLogMapper.selectByAppointmentAndStage(appointmentId, currentStage);
        if (currentLog != null) {
            currentLog.setStatus(1); // 已完成
            processLogMapper.updateById(currentLog);
        }

        // 创建下一阶段日志
        TransactionProcessLog newLog = new TransactionProcessLog();
        newLog.setAppointmentId(appointmentId);
        newLog.setStageCode(nextStageCode);
        newLog.setStageName(getStageName(nextStageCode, appointment.getTransactionType()));
        newLog.setStatus(0); // 进行中
        newLog.setOperatorId(operatorId);
        newLog.setOperatorName(operatorName);
        newLog.setRemark(remark);
        newLog.setCreateTime(LocalDateTime.now());
        processLogMapper.insert(newLog);

        // 更新预约表
        appointment.setCurrentStage(nextStageCode);
        appointment.setStageStatus(0); // 进行中
        appointmentMapper.updateById(appointment);
    }

    @Override
    public List<TransactionProcessLog> getProcessLogs(Long appointmentId) {
        return processLogMapper.selectByAppointmentId(appointmentId);
    }

    @Override
    @Transactional
    public void cancelTransaction(Long appointmentId, Long operatorId, String operatorName, String reason) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 创建取消日志
        TransactionProcessLog log = new TransactionProcessLog();
        log.setAppointmentId(appointmentId);
        log.setStageCode("cancelled");
        log.setStageName("交易已取消");
        log.setStatus(3); // 已取消
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(reason);
        log.setCreateTime(LocalDateTime.now());
        processLogMapper.insert(log);

        // 更新预约状态
        appointment.setStatus(3); // 已取消
        appointmentMapper.updateById(appointment);
    }

    @Override
    @Transactional
    public void initProcess(Long appointmentId, Integer transactionType) {
        // 创建初始阶段日志
        TransactionProcessLog log = new TransactionProcessLog();
        log.setAppointmentId(appointmentId);
        log.setStageCode("viewing");
        log.setStageName("预约看房");
        log.setStatus(0); // 进行中
        log.setCreateTime(LocalDateTime.now());
        processLogMapper.insert(log);

        // 更新预约信息
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        appointment.setTransactionType(transactionType);
        appointment.setCurrentStage("viewing");
        appointment.setStageStatus(0);
        appointmentMapper.updateById(appointment);
    }

    private String getNextStageCode(String currentStage, Integer transactionType) {
        if (transactionType == 0) {
            // 租房流程
            RentStage current = RentStage.fromCode(currentStage);
            if (current == null || current == RentStage.COMPLETED) {
                return null;
            }
            RentStage[] stages = RentStage.values();
            for (int i = 0; i < stages.length - 1; i++) {
                if (stages[i] == current) {
                    return stages[i + 1].getCode();
                }
            }
        } else {
            // 买房流程
            PurchaseStage current = PurchaseStage.fromCode(currentStage);
            if (current == null || current == PurchaseStage.COMPLETED) {
                return null;
            }
            PurchaseStage[] stages = PurchaseStage.values();
            for (int i = 0; i < stages.length - 1; i++) {
                if (stages[i] == current) {
                    return stages[i + 1].getCode();
                }
            }
        }
        return null;
    }

    private String getStageName(String stageCode, Integer transactionType) {
        if (transactionType == 0) {
            RentStage stage = RentStage.fromCode(stageCode);
            return stage != null ? stage.getName() : stageCode;
        } else {
            PurchaseStage stage = PurchaseStage.fromCode(stageCode);
            return stage != null ? stage.getName() : stageCode;
        }
    }
}
