package com.learning.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name= "studentBatch")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentBatchEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Double fees;
	private Long studentId;
	private Long batchId;

}
