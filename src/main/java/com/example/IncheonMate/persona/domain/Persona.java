package com.example.IncheonMate.persona.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "persona")
public class Persona {

    @Id
    private String id; //***************반드시 수동으로 ID만들어야함*****************
    //selectedPersonaId를 Member collection에 넣어야하는데, id값이 알아볼수 없는 문자열이면 확인할 수 없다

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String name; //페르소나 이름
    private String tags; //페르소나 선택화면과 AI용 말투를 위한 태그(#침착한,#활발한...)
    //private String description; //--AI 프롬프트용
    //private String catchPhrase; //--AI 프롬프트용

    private String overlayImageURL;//페르소나와 함께 사진찍기에 나올 페르소나 캐릭터 이미지 URL
    private String mapImageURL;//맵 화면 아래쪽에 들어갈 페르소나 이미지 URL
    private String selectImageURL;//페르소나 선택화면에 들어갈 페르소나 이미지 URL
    private String chatImageURL;//채팅화면에 들어갈 페르소나 아이콘 이미지 URL
}
