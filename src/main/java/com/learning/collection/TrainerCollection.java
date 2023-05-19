package com.learning.collection;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@Document(collection = "trainer")
public class TrainerCollection {
    @Id
    private Long id;
    private String name;
    private String specialization;
}
