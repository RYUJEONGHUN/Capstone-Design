package com.example.IncheonMate.reward.repository;

import com.example.IncheonMate.reward.domain.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface RewardRepository extends MongoRepository<Reward, String> {

    Optional<Reward> findByNaegiftId(String naegiftId);

    boolean existsByCouponsCouponId(String couponId);

    @Query("{ 'coupons.rewardCourseId': ?0 }")
    Optional<Reward> findByRewardCourseId(String rewardCourseId);
}
