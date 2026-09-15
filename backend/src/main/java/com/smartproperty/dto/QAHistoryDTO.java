package com.smartproperty.dto;

import java.time.LocalDateTime;

/**
 * AI问答历史DTO
 * 用于返回用户的问答历史记录
 *
 * @author 毕业设计项目
 */
public class QAHistoryDTO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 用户ID
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
     */
    private String questionType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ===== 构造方法 =====

    public QAHistoryDTO() {
    }

    public QAHistoryDTO(Long id, Long userId, String question, String answer, String questionType, LocalDateTime createTime) {
        this.id = id;
        this.userId = userId;
        this.question = question;
        this.answer = answer;
        this.questionType = questionType;
        this.createTime = createTime;
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
        return "QAHistoryDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", questionType='" + questionType + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
