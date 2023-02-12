package com.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.entity.StudentEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    @Query(value = "select name from student", nativeQuery = true)
    List<String> findAllName();

    @Query(value = "select contact_details from student", nativeQuery = true)
    List<Long> findAllContact();

    @Query(value = "select s.email from StudentEntity s")
    List<String> findAllEmail();

    @Query(value = "select s.contactDetails from StudentEntity s")
    List<Long> findAllContact2();

}
