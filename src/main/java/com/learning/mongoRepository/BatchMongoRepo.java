package com.learning.mongoRepository;

import com.learning.collection.BatchCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface BatchMongoRepo extends MongoRepository<BatchCollection, Long> {
}
