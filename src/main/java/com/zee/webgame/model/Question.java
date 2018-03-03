package com.zee.webgame.model;

import java.io.Serializable;

/**
 * Created by zhouzehui on 18/3/3.
 */
public class Question implements Serializable {
    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    private String questionId;

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    private String questionName;

    public String getFistAnswer() {
        return fistAnswer;
    }

    public void setFistAnswer(String fistAnswer) {
        this.fistAnswer = fistAnswer;
    }

    private String fistAnswer;

    public String getSecondAnswer() {
        return secondAnswer;
    }

    public void setSecondAnswer(String secondAnswer) {
        this.secondAnswer = secondAnswer;
    }

    public String getThirdAnswer() {
        return thirdAnswer;
    }

    public void setThirdAnswer(String thirdAnswer) {
        this.thirdAnswer = thirdAnswer;
    }

    public String getFourthAnswer() {
        return fourthAnswer;
    }

    public void setFourthAnswer(String fourthAnswer) {
        this.fourthAnswer = fourthAnswer;
    }

    private String secondAnswer;

    private String thirdAnswer;

    private String fourthAnswer;

    public String getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(String rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    private String rightAnswer;
}
