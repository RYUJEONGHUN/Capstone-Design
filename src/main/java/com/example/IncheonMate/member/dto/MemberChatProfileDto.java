package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.Gender;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.domain.type.PersonaType;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Map;

public class MemberChatProfileDto {


    public record ProfileResponse(
            @JsonProperty("session_id") String identifier,
            String role,
            String lang,
            String nickname,
            LocalDate birthDate,
            Gender gender,
            MbtiType mbtiType,
            SasangType sasangType,
            PersonaType personaType,
            @JsonProperty("isGuest") boolean isGuest
    ) {
        public static ProfileResponse fromMember(Member member) {
            return new ProfileResponse(
                    member.getEmail(),
                    member.getRole(),
                    member.getLang(),
                    member.getNickname(),
                    member.getBirthDate(),
                    member.getGender(),
                    member.getMbti(),
                    member.getSasang(),
                    member.getSelectedPersona(),
                    false);
        }

        //identifier,role,lang,nickname,persona만 있음
        public static ProfileResponse fromGuest(String guestId, Map<Object, Object> guestProfile){
            Object personaObj = guestProfile.get("persona");
            PersonaType personaType = (personaObj != null) ? PersonaType.valueOf(personaObj.toString()) : null;

            Object langObj = guestProfile.get("lang");
            String lang = (langObj != null) ? langObj.toString() : "kor";

            String nickname = "게스트" + (guestId.length() >= 4 ? guestId.substring(0, 4) : guestId);

            return new ProfileResponse(
                    guestId,
                    "ROLE_GUEST",
                    lang,
                    nickname,
                    null, null, null, null,
                    personaType,
                    true
            );
        }
    }


}
