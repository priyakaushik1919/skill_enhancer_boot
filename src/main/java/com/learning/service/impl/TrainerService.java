package com.learning.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.learning.constant.NumberConstant;
import com.learning.entity.StudentEntity;
import com.learning.utility.excel.StudentReader;
import com.learning.utility.excel.TrainerReader;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.learning.entity.TrainerEntity;
import com.learning.model.TrainerModel;
import com.learning.repository.TrainerRepository;
import com.learning.service.CommonService;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TrainerService implements CommonService<TrainerModel, Long> {

    private final TrainerRepository trainerRepository;
    private final ModelMapper modelMapper;
    private final TrainerReader trainerReader;

    @Override
    public List<TrainerModel> getAllRecords() {
        List<TrainerEntity> trainerEntityList = trainerRepository.findAll();
        if (!CollectionUtils.isEmpty(trainerEntityList)) {
            List<TrainerModel> trainerModelList = trainerEntityList.stream()
                    .map(trainerEntity -> {
                        TrainerModel trainerModel = new TrainerModel();
                        //	BeanUtils.copyProperties(trainerEntity, trainerModel);
                        modelMapper.map(trainerEntity, trainerModel);
                        return trainerModel;
                    }).collect(Collectors.toList());
            return trainerModelList;
        } else {
            return Collections.emptyList();
        }
    }
    @Override
    public List<TrainerModel> getLimitedRecords(int count) {
        List<TrainerEntity> trainerEntityList = trainerRepository.findAll();
        if (!CollectionUtils.isEmpty(trainerEntityList)) {
            List<TrainerModel> trainerModelList = trainerEntityList.stream()
                    .limit(count)
                    .map(trainerEntity -> {
                        TrainerModel trainerModel = new TrainerModel();
                        //	BeanUtils.copyProperties(trainerEntity, trainerModel);
                        modelMapper.map(trainerEntity, trainerModel);
                        return trainerModel;
                    }).collect(Collectors.toList());
            return trainerModelList;
        } else {
            return Collections.emptyList();
        }
    }
    @Override
    public List<TrainerModel> getSortedRecords(String sortBy) {
        List<TrainerEntity> trainerEntityList = trainerRepository.findAll();
        if (Objects.nonNull(trainerEntityList) && trainerEntityList.size() > NumberConstant.ZERO) {
            Comparator<TrainerEntity> comparator = findSuitableComparator(sortBy);
            List<TrainerModel> trainerModelList = trainerEntityList.stream().sorted(comparator).map(trainerEntity -> {
                TrainerModel trainerModel = modelMapper.map(trainerEntity, TrainerModel.class);
                return trainerModel;
            }).collect(Collectors.toList());
            return trainerModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public TrainerModel saveRecord(TrainerModel trainerModel) {
        if (Objects.nonNull(trainerModel)) {
            TrainerEntity trainerEntity = new TrainerEntity();
            //	BeanUtils.copyProperties(trainerModel, trainerEntity);
            modelMapper.map(trainerModel, trainerEntity);
            trainerRepository.save(trainerEntity);
        }
        return trainerModel;
    }
    @Override
    public List<TrainerModel> saveAll(List<TrainerModel> trainerModelList) {
        if (Objects.nonNull(trainerModelList) && trainerModelList.size() > NumberConstant.ZERO) {
            List<TrainerEntity> trainerEntityList = trainerModelList.stream().map(trainerModel -> {
                TrainerEntity entity = modelMapper.map(trainerModel, TrainerEntity.class);
                return entity;
            }).collect(Collectors.toList());
            trainerEntityList = trainerRepository.saveAll(trainerEntityList);
        }
        return trainerModelList;
    }
    @Override
    public TrainerModel getRecordById(Long id) {
        Optional<TrainerEntity> optionalEntity = trainerRepository.findById(id);
        if (optionalEntity.isPresent()) {
            TrainerEntity trainerEntity = optionalEntity.get();
            TrainerModel trainerModel = modelMapper.map(trainerEntity, TrainerModel.class);
            return trainerModel;
        }
        throw new IllegalArgumentException("Trainer Entity Not Found for id:"+id);
    }

    @Override
    public void deleteRecordById(Long id) {
        trainerRepository.deleteById(id);
    }

    @Override
    public TrainerModel updateRecordById(Long id, TrainerModel trainerModel) {
        Optional<TrainerEntity> optionalTrainerEntity = trainerRepository.findById(id);
        if (optionalTrainerEntity.isPresent()) {
            TrainerEntity trainerEntity = optionalTrainerEntity.get();
            //	BeanUtils.copyProperties(record, trainerEntity);
            modelMapper.map(trainerModel, trainerEntity);
            trainerRepository.save(trainerEntity);
        }
        return trainerModel;
    }

    public void saveExcelFile (MultipartFile file){
        if (file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            try {
                //List<TrainerEntity> trainerEntityList = ExcelHelper.convertExcelToListOFStudent(file.getInputStream());
                List<TrainerEntity> trainerEntityList = trainerReader.getTrainerObjects(file.getInputStream());
                trainerRepository.saveAll(trainerEntityList);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    private Comparator<TrainerEntity> findSuitableComparator(String sortBy) {
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
}
