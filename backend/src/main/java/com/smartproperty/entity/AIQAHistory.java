package com.smartproperty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * AI问答历史记录实体类
 * 用于存储用户与AI问答机器人的对话历史
 *
 * @author 毕业设计项目
 */
@TableName("ai_qa_history")
public class AIQAHistory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联user表
     */
    private Long userId;

    /**
     * 用户提出的问题
     */
    private String question;

    /**
     * AI返回的答案
     */
    private String answer;

    /**
     * 问题类型
     * buy: 买房相关
     * rent: 租房相关
     * policy: 政策相关
     * layout: 户型相关
     * general: 通用问题
     */
    private String questionType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ===== 构造方法 =====

    public AIQAHistory() {
    }

    public AIQAHistory(Long userId, String question, String answer, String questionType) {
        this.userId = userId;
        this.question = question;
        this.answer = answer;
        this.questionType = questionType;
    }

    // ===== Getter / Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AIQAHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", questionType='" + questionType + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
