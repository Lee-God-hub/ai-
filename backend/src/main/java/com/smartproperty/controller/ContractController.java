package com.smartproperty.controller;

import com.smartproperty.entity.Contract;
import com.smartproperty.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合同管理Controller
 */
@RestController
@RequestMapping("/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    /**
     * 创建合同
     */
    @PostMapping("/create")
    public Map<String, Object> createContract(@RequestBody Contract contract) {
        try {
            Contract created = contractService.createContract(contract);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", created);
            result.put("message", "合同创建成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 获取合同详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getContract(@PathVariable Long id) {
        try {
            Contract contract = contractService.getContract(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", contract);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 根据预约ID获取合同
     */
    @GetMapping("/by-appointment/{appointmentId}")
    public Map<String, Object> getContractByAppointment(@PathVariable Long appointmentId) {
        try {
            Contract contract = contractService.getContractByAppointmentId(appointmentId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", contract);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 签署合同
     */
    @PostMapping("/sign/{id}")
    public Map<String, Object> signContract(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            contractService.signContract(id, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "合同签署成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 合同备案
     */
    @PostMapping("/record/{id}")
    public Map<String, Object> recordContract(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String recordNo = request.get("recordNo").toString();
            String recordOrg = request.get("recordOrg").toString();
            
            contractService.recordContract(id, recordNo, recordOrg);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "合同备案成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 我的合同列表
     */
    @GetMapping("/my-list")
    public Map<String, Object> getMyContracts(@RequestParam Long userId) {
        try {
            List<Contract> contracts = contractService.getMyContracts(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", contracts);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
}
