package com.example.IncheonMate.member.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SasangAnswerDto {
    private int questionId;
    @Min(1) @Max(4)
    private int answer;
}
/*테스트 JSON
[
  { "questionId": 1, "answer": 3 },
  { "questionId": 2, "answer": 1 },
  { "questionId": 3, "answer": 4 },
  { "questionId": 4, "answer": 2 },
  { "questionId": 5, "answer": 3 },
  { "questionId": 6, "answer": 1 },
  { "questionId": 7, "answer": 4 },
  { "questionId": 8, "answer": 2 },
  { "questionId": 9, "answer": 3 },
  { "questionId": 10, "answer": 1 },
  { "questionId": 11, "answer": 4 },
  { "questionId": 12, "answer": 2 },
  { "questionId": 13, "answer": 3 }
]
 */
