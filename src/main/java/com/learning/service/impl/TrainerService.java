package com.learning.service.impl;

import com.learning.collection.TrainerCollection;
import com.learning.entity.*;
import com.learning.exception.DataNotFoundException;
import com.learning.model.TrainerModel;
import com.learning.mongoRepository.TrainerMongoRepo;
import com.learning.repository.*;
import com.learning.service.CommonService;
import com.learning.utility.excel.TrainerReader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Slf4j
public class TrainerService implements CommonService<TrainerModel, Long> {
    private final TrainerRepository jpaRepository;
    private final TrainerMongoRepo mongoRepository;
    private final ModelMapper modelMapper;
    private final TrainerReader trainerReader;

    @Override
    public List<TrainerModel> getAllRecords() {
        List<TrainerEntity> trainerEntityList = jpaRepository.findAll();
        List<TrainerCollection> trainerCollectionList = mongoRepository.findAll();
        if (!CollectionUtils.isEmpty(trainerCollectionList)) {
            List<TrainerModel> trainerModelList = trainerCollectionList.stream()
                    .map(trainerCollection -> modelMapper.map(trainerCollection, TrainerModel.class))
                    .collect(Collectors.toList());
            return trainerModelList;
        } else if (!CollectionUtils.isEmpty(trainerEntityList)) {
            List<TrainerModel> trainerModelList = trainerEntityList.stream()
                    .map(trainerEntity -> modelMapper.map(trainerEntity, TrainerModel.class))
                    .collect(Collectors.toList());
            return trainerModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TrainerModel> getLimitedRecords(int count) {
        List<TrainerEntity> trainerEntityList = jpaRepository.findAll();
        List<TrainerCollection> trainerCollectionList = mongoRepository.findAll();
        if (!CollectionUtils.isEmpty(trainerCollectionList)) {
            List<TrainerModel> trainerModelList = trainerCollectionList.stream()
                    .limit(count)
                    .map(trainerCollection -> modelMapper.map(trainerCollection, TrainerModel.class))
                    .collect(Collectors.toList());
            return trainerModelList;
        } else if (!CollectionUtils.isEmpty(trainerEntityList)) {
            List<TrainerModel> trainerModelList = trainerEntityList.stream()
                    .limit(count)
                    .map(trainerEntity -> modelMapper.map(trainerEntity, TrainerModel.class))
                    .collect(Collectors.toList());
            return trainerModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TrainerModel> getSortedRecords(String sortBy) {
        List<TrainerEntity> trainerEntityList = jpaRepository.findAll();
        List<TrainerCollection> trainerCollectionList = mongoRepository.findAll();
        if (!CollectionUtils.isEmpty(trainerCollectionList)) {
            Comparator<TrainerCollection> comparator = findSuitableComparatorTwo(sortBy);
            List<TrainerModel> trainerModelList = trainerCollectionList.stream()
                    .sorted(comparator)
                    .map(trainerCollection -> modelMapper.map(trainerCollection, TrainerModel.class))
                    .collect(Collectors.toList());
            return trainerModelList;
        } else if (!CollectionUtils.isEmpty(trainerEntityList)) {
            Comparator<TrainerEntity> comparator = findSuitableComparator(sortBy);
            List<TrainerModel> trainerModelList = trainerEntityList.stream()
                    .sorted(comparator)
                    .map(trainerEntity -> modelMapper.map(trainerEntity, TrainerModel.class))
                    .collect(Collectors.toList());
            return trainerModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public TrainerModel saveRecord(TrainerModel trainerModel) {
        if (Objects.nonNull(trainerModel)) {
            TrainerEntity trainerEntity = new TrainerEntity();
            modelMapper.map(trainerModel, trainerEntity);
            jpaRepository.save(trainerEntity);

            Runnable runnable = () -> {
                TrainerCollection trainerCollection = new TrainerCollection();
                modelMapper.map(trainerEntity, trainerCollection);
                mongoRepository.save(trainerCollection);
            };
            CompletableFuture.runAsync(runnable);
        }
        return trainerModel;
    }

    @Override
    public List<TrainerModel> saveAll(List<TrainerModel> trainerModelList) {
        if (!CollectionUtils.isEmpty(trainerModelList)) {
            List<TrainerEntity> trainerEntityList = trainerModelList.stream()
                    .map(trainerModel -> modelMapper.map(trainerModel, TrainerEntity.class))
                    .collect(Collectors.toList());
            jpaRepository.saveAll(trainerEntityList);

            Runnable runnable = () -> {
                List<TrainerCollection> trainerCollectionList = trainerEntityList.stream()
                        .map(trainerEntity -> modelMapper.map(trainerEntity, TrainerCollection.class))
                        .collect(Collectors.toList());
                mongoRepository.saveAll(trainerCollectionList);
            };
        }
        return trainerModelList;
    }

    @Override
    public TrainerModel getRecordById(Long id) {
        if (mongoRepository.existsById(id)) {
            TrainerCollection trainerCollection = mongoRepository.findById(id)
                    .orElseThrow(() ->
                            new DataNotFoundException("Record Not Found" + id));
            TrainerModel trainerModel = modelMapper.map(trainerCollection, TrainerModel.class);
            return trainerModel;
        }
        TrainerEntity trainerEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
        TrainerModel trainerModel = modelMapper.map(trainerEntity, TrainerModel.class);
        return trainerModel;
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
    public TrainerModel updateRecordById(Long id, TrainerModel trainerModel) {
        TrainerEntity trainerEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
        modelMapper.map(trainerModel, trainerEntity);
        jpaRepository.save(trainerEntity);

        CompletableFuture.runAsync(() -> {
            TrainerCollection trainerCollection = mongoRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
            modelMapper.map(trainerModel, trainerCollection);
            mongoRepository.save(trainerCollection);
        });
        return trainerModel;
    }

    @Override
    public void saveExcelFile(MultipartFile file){
        if(file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            try {
                List<TrainerEntity> trainerEntityList = trainerReader.getTrainerObjects(file.getInputStream());
                jpaRepository.saveAll(trainerEntityList);
                CompletableFuture.runAsync(() -> {
                    List<TrainerCollection> trainerCollectionList = trainerEntityList.stream()
                            .map(trainerEntity -> modelMapper.map(trainerEntity, TrainerCollection.class))
                            .collect(Collectors.toList());
                    mongoRepository.saveAll(trainerCollectionList);
                });
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public Comparator<TrainerEntity> findSuitableComparator(String sortBy) {
        Comparator<TrainerEntity> comparator;
        switch (sortBy) {
            case "name": {
                comparator = (trainerOne, trainerTwo) -> trainerOne.getName().compareTo(trainerTwo.getName());
                break;
            }
            default: {
                comparator = (trainerOne, trainerTwo) -> trainerOne.getId().compareTo(trainerTwo.getId());
            }
        }
        return comparator;
    }


    public Comparator<TrainerCollection> findSuitableComparatorTwo(String sortBy) {
        Comparator<TrainerCollection> comparator;
        switch (sortBy) {
            case "name": {
                comparator = (trainerOne, trainerTwo) -> trainerOne.getName().compareTo(trainerTwo.getName());
                break;
            }
            default: {
                comparator = (trainerOne, trainerTwo) -> trainerOne.getId().compareTo(trainerTwo.getId());
            }
        }
        return comparator;
    }
}
