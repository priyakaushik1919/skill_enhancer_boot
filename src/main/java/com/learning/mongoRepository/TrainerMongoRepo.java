package com.learning.mongoRepository;

import com.learning.collection.TrainerCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrainerMongoRepo extends MongoRepository<TrainerCollection, Long> {
}
