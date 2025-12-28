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

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String name;
    private String tags;
    //private String description; //--AI 프롬프트용
    //private String catchPhrase; //--AI 프롬프트용

    private String overlayImageURL;
    private String mapImageURL;
    private String selectImageURL;
    private String chatImageURL;
}
