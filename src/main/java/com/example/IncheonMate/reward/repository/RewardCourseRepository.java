package com.example.IncheonMate.reward.repository;

import com.example.IncheonMate.reward.domain.RewardCourse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RewardCourseRepository extends MongoRepository<RewardCourse, String> {
    List<RewardCourse> findAllByIsVisibleTrue();
}
