package com.learning.mongoRepository;

import com.learning.collection.StudentBatchCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentBatchMongoRepo extends MongoRepository<StudentBatchCollection, Long> {
}
