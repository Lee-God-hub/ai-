package com.smartproperty.mapper;

import com.smartproperty.entity.TransactionConversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 交易流程对话Mapper接口
 */
@Mapper
public interface TransactionConversationMapper {

    /**
     * 根据预约ID查询对话列表
     */
    @Select("SELECT * FROM transaction_conversation WHERE appointment_id = #{appointmentId} ORDER BY create_time ASC")
    List<TransactionConversation> selectByAppointmentId(Long appointmentId);

    /**
     * 插入对话消息
     */
    @Insert("INSERT INTO transaction_conversation (appointment_id, sender_id, sender_name, sender_role, " +
            "message_content, message_type, is_read, create_time) " +
            "VALUES (#{appointmentId}, #{senderId}, #{senderName}, #{senderRole}, " +
            "#{messageContent}, #{messageType}, #{isRead}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TransactionConversation conversation);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE transaction_conversation SET is_read = 1 WHERE id = #{id}")
    int markAsRead(Long id);

    /**
     * 标记某个预约的所有消息为已读（排除自己发送的）
     */
    @Update("UPDATE transaction_conversation SET is_read = 1 " +
            "WHERE appointment_id = #{appointmentId} AND sender_id != #{userId} AND is_read = 0")
    int markAllAsRead(@Param("appointmentId") Long appointmentId, @Param("userId") Long userId);

    /**
     * 获取未读消息数量
     */
    @Select("SELECT COUNT(*) FROM transaction_conversation " +
            "WHERE appointment_id = #{appointmentId} AND sender_id != #{userId} AND is_read = 0")
    int getUnreadCount(@Param("appointmentId") Long appointmentId, @Param("userId") Long userId);

    /**
     * 删除消息
     */
    @Delete("DELETE FROM transaction_conversation WHERE id = #{id}")
    int deleteById(Long id);
}
