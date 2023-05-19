package com.learning.collection;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@Document(collection = "course")
public class CourseCollection {
    @Id
    private Long id;
    private String name;
    private String curriculum;
    private String duration;
}
