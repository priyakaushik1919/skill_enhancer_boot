package com.learning.collection;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.time.LocalTime;

@Data
@Document(collection = "timeSlot")
public class TimeSlotCollection {
    @Id
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long trainerId;
}
