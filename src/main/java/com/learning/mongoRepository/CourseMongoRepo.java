package com.learning.mongoRepository;

import com.learning.collection.CourseCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface CourseMongoRepo extends MongoRepository<CourseCollection, Long>{
}
