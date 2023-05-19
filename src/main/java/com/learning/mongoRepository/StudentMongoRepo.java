package com.learning.mongoRepository;

import com.learning.collection.StudentCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StudentMongoRepo extends MongoRepository<StudentCollection, Long> {

}
