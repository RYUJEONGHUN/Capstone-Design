package com.example.IncheonMate.chat.dto;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.GuestChatSession;
import com.example.IncheonMate.chat.domain.type.AuthorType;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import com.example.IncheonMate.place.domain.Place;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.poi.ss.formula.functions.T;

import javax.swing.plaf.basic.BasicTreeUI;
import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    public record CurrentDto(
            String sessionId, //세션 ID
            String title, //제목
            List<MessageDto> messages,
            Integer remainingChatCount //게스트 남은 채팅 횟수(null이면 정회원,not null이면 게스트): 프론트엔드는 이걸로 정회원,게스트 분리
    ) {
        public static ChatResponse.CurrentDto of(String sessionId, String title, List<MessageDto> messages, Integer remainingChatCount) {
            return new ChatResponse.CurrentDto(sessionId, title, messages, remainingChatCount);
        }

        public static ChatResponse.CurrentDto fromMember(ChatSession chatSession) {
            List<MessageDto> messageDtoList = chatSession.getMessages().stream()
                    .map(ChatResponse.MessageDto::fromMember)
                    .toList();
            return new ChatResponse.CurrentDto(chatSession.getId(), chatSession.getTitle().toString(), messageDtoList, null);
        }
    }

    @Schema(name = "ResponseMessageDto")
    public record MessageDto(
            String messageId,
            LocalDateTime messagedAt,
            AuthorType authorType,
            String content
    ) {
        public static ChatResponse.MessageDto fromMember(ChatSession.Message message) {
            return new ChatResponse.MessageDto(message.getId(), message.getMessagedAt(), message.getAuthorType(), message.getContent());
        }

        public static ChatResponse.MessageDto fromGuest(GuestChatSession.Message message) {
            return new ChatResponse.MessageDto(message.getId(), message.getMessagedAt(), message.getAuthorType(), message.getContent());
        }
    }

    public record Generation(
            MessageDto user,
            MessageDto ai,
            boolean isCourse,
            TravelCourseDto travelCourse
    ) {
        public static ChatResponse.Generation fromGuest(GuestChatSession.Message userMessage, GuestChatSession.Message aiChatMessage) {
            return new Generation(
                    MessageDto.fromGuest(userMessage),
                    MessageDto.fromGuest(aiChatMessage),
                    false,
                    null
            );
        }

        public static ChatResponse.Generation fromUser(ChatSession.Message userMessage, ChatSession.Message aiMessage) {
            return new Generation(
                    MessageDto.fromMember(userMessage),
                    MessageDto.fromMember(aiMessage),
                    false,
                    null
            );
        }

        public static ChatResponse.Generation fromUserWithTravelCourse(ChatSession.Message userMessage, ChatSession.Message aiMessage, TravelCourseDto travelCourseDto) {
            return new Generation(
                    MessageDto.fromMember(userMessage),
                    MessageDto.fromMember(aiMessage),
                    true,
                    travelCourseDto
            );
        }
    }

    public record CourseSpotDto(
            int spotOrder, //여행 코스 순서
            String name, // 장소명
            String address, //주소
            String thumbnailUrl, //사진
            CoursePlaceCategory coursePlaceCategory, //카테고리(
            String kakaoId, //카카오 ID || 카카오 URL ????
            String naegiftUrl, //내기프트 URL
            String expertComment,
            Double x,
            Double y
    ) {
        public static CourseSpotDto of(int spotOrder, Place place) {
            return new CourseSpotDto(
                    spotOrder,
                    place.getName(),
                    place.getAddress(),
                    place.getThumbnailUrl(),
                    place.getCoursePlaceCategory(),
                    place.getKakaoId(), //id로할지 url로 할지
                    "https://shopuser-qa.naegift.com/" + place.getNaegiftId() + "?channel_no=1", //https://shopuser-qa.naegift.com/xxxxx?channel_no=1
                    place.getExpertComment(),
                    place.getX(),
                    place.getY()
            );
        }
    }

    public record TravelCourseDto(
            String title,
            List<CourseSpotDto> courseSpots
    ) {
        public static TravelCourseDto of(String title, List<CourseSpotDto> courseSpots){
            return new TravelCourseDto(title, courseSpots);
        }
    }

    public record TravelCourseIdDto(
            String travelCourseId
    ){}

}
