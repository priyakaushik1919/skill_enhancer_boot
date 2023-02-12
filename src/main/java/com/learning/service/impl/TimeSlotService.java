package com.learning.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.learning.constant.NumberConstant;
import com.learning.entity.StudentEntity;
import com.learning.utility.excel.TimeSlotReader;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.learning.entity.TimeSlotEntity;
import com.learning.model.TimeSlotModel;
import com.learning.repository.TimeSlotRepository;
import com.learning.service.CommonService;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TimeSlotService implements CommonService<TimeSlotModel, Long> {
    private final TimeSlotRepository timeSlotRepository;
    private final ModelMapper modelMapper;

    private final TimeSlotReader timeSlotReader;

    @Override
    public List<TimeSlotModel> getAllRecords() {
        List<TimeSlotEntity> timeSlotEntityList = timeSlotRepository.findAll();
        if (!CollectionUtils.isEmpty(timeSlotEntityList)) {
            List<TimeSlotModel> timeSlotModelList = timeSlotEntityList.stream()
                    .map(timeSlotEntity -> {
                        TimeSlotModel timeSlotModel = new TimeSlotModel();
                        //	BeanUtils.copyProperties(timeSlotEntity, timeSlotModel);
                        modelMapper.map(timeSlotEntity, timeSlotModel);
                        return timeSlotModel;
                    }).collect(Collectors.toList());
            return timeSlotModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TimeSlotModel> getLimitedRecords(int count) {
        List<TimeSlotEntity> timeSlotEntityList = timeSlotRepository.findAll();
        if (!CollectionUtils.isEmpty(timeSlotEntityList)) {
            List<TimeSlotModel> timeSlotModelList = timeSlotEntityList.stream()
                    .limit(count)
                    .map(timeSlotEntity -> {
                        TimeSlotModel timeSlotModel = new TimeSlotModel();
                        //	BeanUtils.copyProperties(timeSlotEntity, timeSlotModel);
                        modelMapper.map(timeSlotEntity, timeSlotModel);
                        return timeSlotModel;
                    }).collect(Collectors.toList());
            return timeSlotModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TimeSlotModel> getSortedRecords(String sortBy) {
        List<TimeSlotEntity> timeSlotEntityList = timeSlotRepository.findAll();
        if (Objects.nonNull(timeSlotEntityList) && timeSlotEntityList.size() > NumberConstant.ZERO) {
            Comparator<TimeSlotEntity> comparator = findSuitableComparator(sortBy);
            List<TimeSlotModel> timeSlotModelList = timeSlotEntityList.stream().sorted(comparator)
                    .map(timeSlotEntity -> {
                        TimeSlotModel timeSlotModel = modelMapper.map(timeSlotEntity, TimeSlotModel.class);
                        return timeSlotModel;
                    }).collect(Collectors.toList());
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
            timeSlotRepository.save(timeSlotEntity);
        }
        return timeSlotModel;
    }

    @Override
    public List<TimeSlotModel> saveAll(List<TimeSlotModel> timeSlotModelList) {
        if (Objects.nonNull(timeSlotModelList) && timeSlotModelList.size() > NumberConstant.ZERO) {
            List<TimeSlotEntity> timeSlotEntityList = timeSlotModelList.stream().map(timeSlotModel -> {
                TimeSlotEntity entity = modelMapper.map(timeSlotModel, TimeSlotEntity.class);
                return entity;
            }).collect(Collectors.toList());
            timeSlotRepository.saveAll(timeSlotEntityList);
        }
        return timeSlotModelList;
    }

    @Override
    public TimeSlotModel getRecordById(Long id) {
        Optional<TimeSlotEntity> optionalEntity = timeSlotRepository.findById(id);
        if (optionalEntity.isPresent()) {
            TimeSlotEntity timeSlotEntity = optionalEntity.get();
            TimeSlotModel timeSlotModel = modelMapper.map(timeSlotEntity, TimeSlotModel.class);
            return timeSlotModel;
        }
        throw new IllegalArgumentException("TimeSlot Entity Not Found for id:" + id);
    }

    @Override
    public void deleteRecordById(Long id) {
        timeSlotRepository.deleteById(id);
    }

    @Override
    public TimeSlotModel updateRecordById(Long id, TimeSlotModel timeSlotModel) {
        Optional<TimeSlotEntity> optionalTimeSlotEntity = timeSlotRepository.findById(id);
        if (optionalTimeSlotEntity.isPresent()) {
            TimeSlotEntity timeSlotEntity = optionalTimeSlotEntity.get();
            //	BeanUtils.copyProperties(timeSlotModel, timeSlotEntity);
            modelMapper.map(timeSlotModel, timeSlotEntity);
            timeSlotRepository.save(timeSlotEntity);
        }
        return timeSlotModel;
    }

    public void saveExcelFile(MultipartFile file) {
        if (file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            try {
                List<TimeSlotEntity> timeSlotEntityList = timeSlotReader.getTimeSlotObjects(file.getInputStream());
                timeSlotRepository.saveAll(timeSlotEntityList);
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
}
