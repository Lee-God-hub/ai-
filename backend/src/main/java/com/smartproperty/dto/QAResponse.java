package com.smartproperty.dto;

import java.util.List;

/**
 * AI问答响应DTO
 * 用于返回AI的回答结果给前端
 *
 * @author 毕业设计项目
 */
public class QAResponse {

    /**
     * AI返回的答案
     */
    private String answer;

    /**
     * 问题类型
     */
    private String questionType;

    /**
     * 相关问题推荐列表
     * 可以为用户提供更多相关问题的建议
     */
    private List<String> relatedQuestions;

    /**
     * 回答的置信度（0-1）
     * 可选字段，表示AI对答案的信心程度
     */
    private Double confidence;

    // ===== 构造方法 =====

    public QAResponse() {
    }

    public QAResponse(String answer) {
        this.answer = answer;
    }

    public QAResponse(String answer, String questionType) {
        this.answer = answer;
        this.questionType = questionType;
    }

    public QAResponse(String answer, String questionType, List<String> relatedQuestions) {
        this.answer = answer;
        this.questionType = questionType;
        this.relatedQuestions = relatedQuestions;
    }

    // ===== Getter / Setter =====

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

    public List<String> getRelatedQuestions() {
        return relatedQuestions;
    }

    public void setRelatedQuestions(List<String> relatedQuestions) {
        this.relatedQuestions = relatedQuestions;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "QAResponse{" +
                "answer='" + answer + '\'' +
                ", questionType='" + questionType + '\'' +
                ", relatedQuestions=" + relatedQuestions +
                ", confidence=" + confidence +
                '}';
    }
}
