package com.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.entity.TimeSlotEntity;

public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, Long> {

}
