package com.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.entity.TrainerEntity;

public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {

}
