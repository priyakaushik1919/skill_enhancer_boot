package com.learning.collection;

import com.learning.enums.BatchStatus;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDate;
@Data
@Document(collection = "batch")
public class BatchCollection {
    @Id
    private Long id;
    private Integer studentCount;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(value = EnumType.STRING)
    private BatchStatus batchStatus;
    private Long courseId;
    private Long timeSlotId;
}
