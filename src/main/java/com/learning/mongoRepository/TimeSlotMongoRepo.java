package com.learning.mongoRepository;

import com.learning.collection.TimeSlotCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TimeSlotMongoRepo extends MongoRepository<TimeSlotCollection, Long> {
}
