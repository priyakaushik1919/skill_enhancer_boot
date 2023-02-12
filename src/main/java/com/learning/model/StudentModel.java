package com.learning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentModel {

	private Long id;
	private String name;
	private Long contactDetails;
	private String qualification;
	private String email;


}
