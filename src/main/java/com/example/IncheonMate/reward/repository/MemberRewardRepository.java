package com.example.IncheonMate.reward.repository;

import com.example.IncheonMate.reward.domain.MemberReward;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRewardRepository extends MongoRepository<MemberReward, String> {

    List<MemberReward> findAllByMemberId(String identifier);

    Optional<MemberReward> findByMemberIdAndRewardCourseId(String memberId, String rewardCourseId);
}
