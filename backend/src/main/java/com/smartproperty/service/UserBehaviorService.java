package com.smartproperty.service;

import com.smartproperty.common.AIConstants;
import com.smartproperty.entity.UserBehaviorLog;
import com.smartproperty.mapper.UserBehaviorLogMapper;
import com.smartproperty.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户行为日志服务类
 * 记录用户在平台上的各种行为，为AI推荐算法提供数据支持
 * 
 * 支持的行为类型：
 * - view: 浏览房源
 * - favorite: 收藏房源
 * - unfavorite: 取消收藏
 * - appointment: 预约看房
 * 
 * @author 毕业设计项目
 */
@Service
public class UserBehaviorService {

    private static final Logger log = LoggerFactory.getLogger(UserBehaviorService.class);

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    /**
     * 记录用户浏览房源行为
     * 异步执行，不影响主流程性能
     *
     * @param propertyId 房源ID
     */
    @Async
    public void recordView(Long propertyId) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                log.debug("未登录用户浏览房源，不记录行为日志");
                return;
            }

            UserBehaviorLog log = new UserBehaviorLog();
            log.setUserId(userId);
            log.setPropertyId(propertyId);
            log.setBehaviorType(AIConstants.BEHAVIOR_TYPE_VIEW);
            log.setCreateTime(LocalDateTime.now());

            behaviorLogMapper.insert(log);
            this.log.debug("记录用户 {} 浏览房源 {} 的行为", userId, propertyId);
        } catch (Exception e) {
            log.error("记录浏览行为失败", e);
        }
    }

    /**
     * 记录用户收藏房源行为
     * 异步执行，不影响主流程性能
     *
     * @param propertyId 房源ID
     */
    @Async
    public void recordFavorite(Long propertyId) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return;
            }

            UserBehaviorLog log = new UserBehaviorLog();
            log.setUserId(userId);
            log.setPropertyId(propertyId);
            log.setBehaviorType(AIConstants.BEHAVIOR_TYPE_FAVORITE);
            log.setCreateTime(LocalDateTime.now());

            behaviorLogMapper.insert(log);
            this.log.info("记录用户 {} 收藏房源 {} 的行为", userId, propertyId);
        } catch (Exception e) {
            log.error("记录收藏行为失败", e);
        }
    }

    /**
     * 记录用户取消收藏行为
     * 异步执行，不影响主流程性能
     *
     * @param propertyId 房源ID
     */
    @Async
    public void recordUnfavorite(Long propertyId) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return;
            }

            UserBehaviorLog log = new UserBehaviorLog();
            log.setUserId(userId);
            log.setPropertyId(propertyId);
            log.setBehaviorType(AIConstants.BEHAVIOR_TYPE_UNFAVORITE);
            log.setCreateTime(LocalDateTime.now());

            behaviorLogMapper.insert(log);
            this.log.info("记录用户 {} 取消收藏房源 {} 的行为", userId, propertyId);
        } catch (Exception e) {
            log.error("记录取消收藏行为失败", e);
        }
    }

    /**
     * 记录用户预约看房行为
     * 异步执行，不影响主流程性能
     *
     * @param propertyId 房源ID
     */
    @Async
    public void recordAppointment(Long propertyId) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return;
            }

            UserBehaviorLog log = new UserBehaviorLog();
            log.setUserId(userId);
            log.setPropertyId(propertyId);
            log.setBehaviorType(AIConstants.BEHAVIOR_TYPE_APPOINTMENT);
            log.setCreateTime(LocalDateTime.now());

            behaviorLogMapper.insert(log);
            this.log.info("记录用户 {} 预约房源 {} 的行为", userId, propertyId);
        } catch (Exception e) {
            log.error("记录预约行为失败", e);
        }
    }

    /**
     * 记录通用用户行为
     * 
     * @param userId 用户ID
     * @param propertyId 房源ID
     * @param behaviorType 行为类型
     */
    public void recordBehavior(Long userId, Long propertyId, String behaviorType) {
        try {
            UserBehaviorLog log = new UserBehaviorLog();
            log.setUserId(userId);
            log.setPropertyId(propertyId);
            log.setBehaviorType(behaviorType);
            log.setCreateTime(LocalDateTime.now());

            behaviorLogMapper.insert(log);
            this.log.info("记录用户 {} 对房源 {} 的行为: {}", userId, propertyId, behaviorType);
        } catch (Exception e) {
            log.error("记录用户行为失败", e);
        }
    }
}
