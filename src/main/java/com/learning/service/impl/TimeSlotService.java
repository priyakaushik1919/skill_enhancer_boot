package com.learning.service.impl;

import com.learning.collection.TimeSlotCollection;
import com.learning.entity.TimeSlotEntity;
import com.learning.exception.DataNotFoundException;
import com.learning.model.TimeSlotModel;
import com.learning.mongoRepository.TimeSlotMongoRepo;
import com.learning.repository.TimeSlotRepository;
import com.learning.service.CommonService;
import com.learning.utility.excel.TimeSlotReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService implements CommonService<TimeSlotModel, Long> {
    private final TimeSlotRepository jpaRepository;
    private final ModelMapper modelMapper;
    private final TimeSlotReader timeSlotReader;
    private final TimeSlotMongoRepo mongoRepository;

    @Override
    public List<TimeSlotModel> getAllRecords() {
        List<TimeSlotCollection> timeSlotCollectionList = mongoRepository.findAll();
        List<TimeSlotEntity> timeSlotEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(timeSlotCollectionList)) {
            List<TimeSlotModel> timeSlotModelList = timeSlotCollectionList.stream()
                    .map(timeSlotCollection -> modelMapper.map(timeSlotCollection, TimeSlotModel.class))
                    .collect(Collectors.toList());
            return timeSlotModelList;
        } else if (!CollectionUtils.isEmpty(timeSlotEntityList)) {
            List<TimeSlotModel> timeSlotModelList = timeSlotEntityList.stream()
                    .map(timeSlotEntity ->
                            //	BeanUtils.copyProperties(timeSlotEntity, timeSlotModel);
                            modelMapper.map(timeSlotEntity, TimeSlotModel.class))
                    .collect(Collectors.toList());
            return timeSlotModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TimeSlotModel> getLimitedRecords(int count) {
        List<TimeSlotCollection> timeSlotCollectionList = mongoRepository.findAll();
        List<TimeSlotEntity> timeSlotEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(timeSlotCollectionList)) {
            List<TimeSlotModel> timeSlotModelList = timeSlotCollectionList.stream()
                    .limit(count)
                    .map(timeSlotCollection -> modelMapper.map(timeSlotCollection, TimeSlotModel.class))
                    .collect(Collectors.toList());
            return timeSlotModelList;
        } else if (!CollectionUtils.isEmpty(timeSlotEntityList)) {
            List<TimeSlotModel> timeSlotModelList = timeSlotEntityList.stream()
                    .limit(count)
                    .map(timeSlotEntity ->
                            //	BeanUtils.copyProperties(timeSlotEntity, timeSlotModel);
                            modelMapper.map(timeSlotEntity, TimeSlotModel.class))
                    .collect(Collectors.toList());
            return timeSlotModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TimeSlotModel> getSortedRecords(String sortBy) {
        List<TimeSlotCollection> timeSlotCollectionList = mongoRepository.findAll();
        List<TimeSlotEntity> timeSlotEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(timeSlotCollectionList)) {
            Comparator<TimeSlotCollection> comparator = findSuitableComparatorTwo(sortBy);
            List<TimeSlotModel> timeSlotModelList = timeSlotCollectionList.stream()
                    .sorted(comparator)
                    .map(timeSlotCollection -> modelMapper.map(timeSlotCollection, TimeSlotModel.class))
                    .collect(Collectors.toList());
            return timeSlotModelList;
        } else if (!CollectionUtils.isEmpty(timeSlotEntityList)) {
            Comparator<TimeSlotEntity> comparator = findSuitableComparator(sortBy);
            List<TimeSlotModel> timeSlotModelList = timeSlotEntityList.stream()
                    .sorted(comparator)
                    .map(timeSlotEntity -> modelMapper.map(timeSlotEntity, TimeSlotModel.class))
                    .collect(Collectors.toList());
            return timeSlotModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public TimeSlotModel saveRecord(TimeSlotModel timeSlotModel) {
        if (Objects.nonNull(timeSlotModel)) {
            TimeSlotEntity timeSlotEntity = new TimeSlotEntity();
            //	BeanUtils.copyProperties(timeSlotModel, timeSlotEntity);
            modelMapper.map(timeSlotModel, timeSlotEntity);
            jpaRepository.save(timeSlotEntity);

            Runnable runnable = () -> {
                TimeSlotCollection timeSlotCollection = new TimeSlotCollection();
                modelMapper.map(timeSlotEntity, timeSlotCollection);
                mongoRepository.save(timeSlotCollection);
            };
            CompletableFuture.runAsync(runnable);
        }
        return timeSlotModel;
    }

    @Override
    public List<TimeSlotModel> saveAll(List<TimeSlotModel> timeSlotModelList) {
        if (!CollectionUtils.isEmpty(timeSlotModelList)) {
            List<TimeSlotEntity> timeSlotEntityList = timeSlotModelList.stream()
                    .map(timeSlotModel -> modelMapper.map(timeSlotModel, TimeSlotEntity.class))
                    .collect(Collectors.toList());
            jpaRepository.saveAll(timeSlotEntityList);
            Runnable runnable = () -> {
                List<TimeSlotCollection> timeSlotCollectionList = timeSlotEntityList.stream()
                        .map(timeSlotEntity -> modelMapper.map(timeSlotEntity, TimeSlotCollection.class))
                        .collect(Collectors.toList());
                mongoRepository.saveAll(timeSlotCollectionList);
            };
            CompletableFuture.runAsync(runnable);
        }
        return timeSlotModelList;
    }

    @Override
    public TimeSlotModel getRecordById(Long id) {
        if (mongoRepository.existsById(id)) {
            TimeSlotCollection timeSlotCollection = mongoRepository.findById(id)
                    .orElseThrow(() ->
                            new DataNotFoundException("Record Not Found" + id));
            TimeSlotModel timeSlotModel = modelMapper.map(timeSlotCollection, TimeSlotModel.class);
            return timeSlotModel;
        }
        TimeSlotEntity timeSlotEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Record Not found" + id));
        TimeSlotModel timeSlotModel = modelMapper.map(timeSlotEntity, TimeSlotModel.class);
        return timeSlotModel;
    }

    @Override
    public void deleteRecordById(Long id) {
        if (mongoRepository.existsById(id)) {
            log.info("[deleteRecordById] from Mongo: {}", id);
            mongoRepository.deleteById(id);
        }
        if (jpaRepository.existsById(id)) {
            CompletableFuture.runAsync(()-> {
                log.info("[deleteRecordById] from MySql: {}", id);
                jpaRepository.deleteById(id);
            });
        }
    }

    @Override
    public TimeSlotModel updateRecordById(Long id, TimeSlotModel timeSlotModel) {
        TimeSlotEntity timeSlotEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
        //BeanUtils.copyProperties(timeSlotModel, timeSlotEntity);
        modelMapper.map(timeSlotModel, timeSlotEntity);
        jpaRepository.save(timeSlotEntity);

        Runnable runnable = () -> {
            TimeSlotCollection timeSlotCollection = mongoRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
            modelMapper.map(timeSlotModel, timeSlotCollection);
            mongoRepository.save(timeSlotCollection);
        };
        CompletableFuture.runAsync(runnable);
        return timeSlotModel;
    }

    @Override
    public void saveExcelFile(MultipartFile file) {
        if (file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            try {
                List<TimeSlotEntity> timeSlotEntityList = timeSlotReader.getTimeSlotObjects(file.getInputStream());
                jpaRepository.saveAll(timeSlotEntityList);
                CompletableFuture.runAsync(() -> {
                    List<TimeSlotCollection> timeSlotCollectionList = timeSlotEntityList.stream()
                            .map(timeSlotEntity -> modelMapper.map(timeSlotEntity, TimeSlotCollection.class))
                            .collect(Collectors.toList());
                    mongoRepository.saveAll(timeSlotCollectionList);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Comparator<TimeSlotEntity> findSuitableComparator(String sortBy) {
        Comparator<TimeSlotEntity> comparator;
        switch (sortBy) {
            case "startTime": {
                comparator = (timeSlotOne, timeSlotTwo) -> timeSlotOne.getStartTime().compareTo(timeSlotTwo.getStartTime());
                break;
            }
            case "endTime": {
                comparator = (timeSlotOne, timeSlotTwo) -> timeSlotOne.getEndTime().compareTo(timeSlotTwo.getEndTime());
                break;
            }
            default: {
                comparator = (timeSlotOne, timeSlotTwo) -> timeSlotOne.getId().compareTo(timeSlotTwo.getId());
            }
        }
        return comparator;
    }

    private Comparator<TimeSlotCollection> findSuitableComparatorTwo(String sortBy) {
        Comparator<TimeSlotCollection> comparator;
        switch (sortBy) {
            case "startTime": {
                comparator = (timeSlotOne, timeSlotTwo) -> timeSlotOne.getStartTime().compareTo(timeSlotTwo.getStartTime());
                break;
            }
            case "endTime": {
                comparator = (timeSlotOne, timeSlotTwo) -> timeSlotOne.getEndTime().compareTo(timeSlotTwo.getEndTime());
                break;
            }
            default: {
                comparator = (timeSlotOne, timeSlotTwo) -> timeSlotOne.getId().compareTo(timeSlotTwo.getId());
            }
        }
        return comparator;
    }
}
