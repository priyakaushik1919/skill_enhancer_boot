package com.learning.collection;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@Document(collection = "student")
public class StudentCollection {
    @Id
    private Long id;
    private String name;
    private Long contactDetails;
    private String qualification;
    private String email;
}
