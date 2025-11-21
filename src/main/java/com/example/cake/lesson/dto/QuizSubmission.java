package com.example.cake.lesson.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizSubmission {
    private String lessonId;
    private List<Answer> answers;

    @Data
    public static class Answer {
        private String questionId;
        private List<String> selectedOptions; // Có thể nhiều đáp án (multiple choice)
    }
}

