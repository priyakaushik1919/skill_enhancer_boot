package com.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.entity.StudentBatchEntity;

public interface StudentBatchRepository extends JpaRepository<StudentBatchEntity, Long> {

}
