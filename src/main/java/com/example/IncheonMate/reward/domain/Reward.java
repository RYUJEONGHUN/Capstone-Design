package com.example.IncheonMate.reward.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Document(collection = "reward")
public class Reward {

    @Id
    private String id;

    private String placeId;
    @Indexed
    private String naegiftId;

    private int remainStock;//한 상점에서 여러 쿠폰 발급하면 분리 필요
    @Builder.Default
    private boolean isActive = true;

    //동시성 제어
    //@Version
    //private Long version;

    @Builder.Default
    private List<Coupon> coupons = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Coupon{
        private String id;
        private String couponId;
        private LocalDateTime purchasedAt; //내기프트에서 구매 시간(지금은 우리 서비스에 등록한 시간으로 설정)
        @Builder.Default
        private String rewardCourseId = null;//어느 리워드 코스에서 지급하는 쿠폰인지
        @Builder.Default
        private boolean isDelivered = false; //사용자에게 지급되었는지
        private String deliveredUserId; //어느 사용자에게 지급되었는지
        private LocalDateTime deliveredAt; //지급 시간
        private LocalDate expiredAt; //만료일(내기프트 기본값: 구매일로부터 3개월)
    }

    public void updateRemainStock(){
        this.remainStock = (this.coupons != null) ? this.coupons.size() : 0;
    }

}
