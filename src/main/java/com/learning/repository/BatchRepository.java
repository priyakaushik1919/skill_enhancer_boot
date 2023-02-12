package com.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.entity.BatchEntity;

public interface BatchRepository extends JpaRepository<BatchEntity, Long> {

}
