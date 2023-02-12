package com.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.entity.CourseEntity;

public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

}
