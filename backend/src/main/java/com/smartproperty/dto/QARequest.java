package com.smartproperty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI问答请求DTO
 * 用于接收前端发送的问答请求
 *
 * @author 毕业设计项目
 */
public class QARequest {

    /**
     * 用户提出的问题
     * 必填，长度限制1-500字符
     */
    @NotBlank(message = "问题不能为空")
    @Size(min = 1, max = 500, message = "问题长度必须在1-500字符之间")
    private String question;

    /**
     * 问题类型（可选）
     * buy: 买房相关
     * rent: 租房相关
     * policy: 政策相关
     * layout: 户型相关
     * general: 通用问题
     */
    @Size(max = 50, message = "问题类型长度不能超过50字符")
    private String questionType;

    // ===== 构造方法 =====

    public QARequest() {
    }

    public QARequest(String question) {
        this.question = question;
    }

    public QARequest(String question, String questionType) {
        this.question = question;
        this.questionType = questionType;
    }

    // ===== Getter / Setter =====

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    @Override
    public String toString() {
        return "QARequest{" +
                "question='" + question + '\'' +
                ", questionType='" + questionType + '\'' +
                '}';
    }
}
