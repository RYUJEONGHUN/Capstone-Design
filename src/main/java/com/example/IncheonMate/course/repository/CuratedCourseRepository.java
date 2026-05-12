package com.example.IncheonMate.course.repository;

import com.example.IncheonMate.course.domain.CuratedCourse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CuratedCourseRepository extends MongoRepository<CuratedCourse, String> {

    List<CuratedCourse> findByIsVisibleTrue();
}
