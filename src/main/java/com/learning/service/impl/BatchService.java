package com.learning.service.impl;

import com.learning.collection.BatchCollection;
import com.learning.collection.StudentCollection;
import com.learning.entity.BatchEntity;
import com.learning.entity.StudentEntity;
import com.learning.exception.DataNotFoundException;
import com.learning.model.BatchModel;
import com.learning.model.StudentModel;
import com.learning.mongoRepository.BatchMongoRepo;
import com.learning.repository.BatchRepository;
import com.learning.service.CommonService;
import com.learning.utility.excel.BatchReader;
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
public class BatchService implements CommonService<BatchModel, Long> {
    private final BatchRepository jpaRepository;
    private final ModelMapper modelMapper;
    private final BatchReader batchReader;
    private final BatchMongoRepo mongoRepository;

    @Override
    public List<BatchModel> getAllRecords() {
        List<BatchCollection> batchCollectionList = mongoRepository.findAll();
        List<BatchEntity> batchEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(batchCollectionList)) {
            List<BatchModel> batchModelList = batchCollectionList.stream()
                    .map(batchCollection -> modelMapper.map(batchCollection, BatchModel.class))
                    .collect(Collectors.toList());
            return batchModelList;
        } else if (!CollectionUtils.isEmpty(batchEntityList)) {
            List<BatchModel> batchModelList = batchEntityList.stream()
                    .map(batchEntity ->
                            //	BeanUtils.copyProperties(batchEntity, batchModel);
                            modelMapper.map(batchEntity, BatchModel.class))
                    .collect(Collectors.toList());
            return batchModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<BatchModel> getLimitedRecords(int count) {
        List<BatchCollection> batchCollectionList = mongoRepository.findAll();
        List<BatchEntity> batchEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(batchCollectionList)) {
            List<BatchModel> batchModelList = batchCollectionList.stream()
                    .limit(count)
                    .map(batchCollection -> modelMapper.map(batchCollection, BatchModel.class))
                    .collect(Collectors.toList());
            return batchModelList;
        } else if (!CollectionUtils.isEmpty(batchEntityList)) {
            List<BatchModel> batchModelList = batchEntityList.stream()
                    .limit(count)
                    .map(batchEntity ->
                            //	BeanUtils.copyProperties(batchEntity, batchModel);
                            modelMapper.map(batchEntity, BatchModel.class))
                    .collect(Collectors.toList());
            return batchModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<BatchModel> getSortedRecords(String sortBy) {
        List<BatchCollection> batchCollectionList = mongoRepository.findAll();
        List<BatchEntity> batchEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(batchCollectionList)) {
            Comparator<BatchCollection> comparator = findSuitableComparatorTwo(sortBy);
            List<BatchModel> batchModelList = batchCollectionList.stream()
                    .sorted(comparator)
                    .map(batchCollection -> modelMapper.map(batchCollection, BatchModel.class))
                    .collect(Collectors.toList());
            return batchModelList;
        } else if (!CollectionUtils.isEmpty(batchEntityList)) {
            Comparator<BatchEntity> comparator = findSuitableComparator(sortBy);
            List<BatchModel> batchModelList = batchEntityList.stream()
                    .sorted(comparator)
                    .map(batchEntity -> modelMapper.map(batchEntity, BatchModel.class))
                    .collect(Collectors.toList());
            return batchModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public BatchModel saveRecord(BatchModel batchModel) {
        if (Objects.nonNull(batchModel)) {
            BatchEntity batchEntity = new BatchEntity();
            //	BeanUtils.copyProperties(batchModel, batchEntity);
            modelMapper.map(batchModel, batchEntity);
            jpaRepository.save(batchEntity);

            Runnable runnable = () -> {
                BatchCollection batchCollection = new BatchCollection();
                modelMapper.map(batchEntity, batchCollection);
                mongoRepository.save(batchCollection);
            };
            CompletableFuture.runAsync(runnable);
        }
        return batchModel;
    }

    @Override
    public List<BatchModel> saveAll(List<BatchModel> batchModelList) {
        if (!CollectionUtils.isEmpty(batchModelList)) {
            List<BatchEntity> batchEntityList = batchModelList.stream()
                    .map(batchModel -> modelMapper.map(batchModel, BatchEntity.class))
                    .collect(Collectors.toList());
            jpaRepository.saveAll(batchEntityList);
            Runnable runnable = () -> {
                List<BatchCollection> batchCollectionList = batchEntityList.stream()
                        .map(batchEntity -> modelMapper.map(batchEntity, BatchCollection.class))
                        .collect(Collectors.toList());
                mongoRepository.saveAll(batchCollectionList);
            };
            CompletableFuture.runAsync(runnable);
        }
        return batchModelList;
    }

    @Override
    public BatchModel getRecordById(Long id) {
        if (mongoRepository.existsById(id)) {
            BatchCollection batchCollection = mongoRepository.findById(id)
                    .orElseThrow(() ->
                            new DataNotFoundException("Record Not Found" + id));
            BatchModel batchModel = modelMapper.map(batchCollection, BatchModel.class);
            return batchModel;
        }
        BatchEntity batchEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Record Not found" + id));
        BatchModel batchModel = modelMapper.map(batchEntity, BatchModel.class);
        return batchModel;
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
    public BatchModel updateRecordById(Long id, BatchModel batchModel) {
        BatchEntity batchEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
        //BeanUtils.copyProperties(batchModel, batchEntity);
        modelMapper.map(batchModel, batchEntity);
        jpaRepository.save(batchEntity);

        Runnable runnable = () -> {
            BatchCollection batchCollection = mongoRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
            modelMapper.map(batchModel, batchCollection);
            mongoRepository.save(batchCollection);
        };
        CompletableFuture.runAsync(runnable);
        return batchModel;
    }

    public void saveExcelFile(MultipartFile file) {
        if (file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            try {
                List<BatchEntity> batchEntityList = batchReader.getBatchObjects(file.getInputStream());
                jpaRepository.saveAll(batchEntityList);
                CompletableFuture.runAsync(() -> {
                    List<BatchCollection> batchCollectionList = batchEntityList.stream()
                            .map(batchEntity -> modelMapper.map(batchEntity, BatchCollection.class))
                            .collect(Collectors.toList());
                    mongoRepository.saveAll(batchCollectionList);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Comparator<BatchEntity> findSuitableComparator(String sortBy) {
        Comparator<BatchEntity> comparator;
        switch (sortBy) {
            case "studentCount": {
                comparator = (batchOne, batchTwo) -> batchOne.getStudentCount().compareTo(batchTwo.getStudentCount());
                break;
            }
            case "timeSlotId": {
                comparator = (batchOne, batchTwo) -> batchOne.getTimeSlotId().compareTo(batchTwo.getTimeSlotId());
                break;
            }
            default: {
                comparator = (batchOne, batchTwo) -> batchOne.getId().compareTo(batchTwo.getId());
            }
        }
        return comparator;
    }

    private Comparator<BatchCollection> findSuitableComparatorTwo(String sortBy) {
        Comparator<BatchCollection> comparator;
        switch (sortBy) {
            case "studentCount": {
                comparator = (batchOne, batchTwo) -> batchOne.getStudentCount().compareTo(batchTwo.getStudentCount());
                break;
            }
            case "timeSlotId": {
                comparator = (batchOne, batchTwo) -> batchOne.getTimeSlotId().compareTo(batchTwo.getTimeSlotId());
                break;
            }
            default: {
                comparator = (batchOne, batchTwo) -> batchOne.getId().compareTo(batchTwo.getId());
            }
        }
        return comparator;
    }
}
