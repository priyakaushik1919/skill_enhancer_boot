package com.learning.service.impl;

import com.learning.collection.StudentCollection;
import com.learning.entity.StudentEntity;
import com.learning.exception.DataNotFoundException;
import com.learning.model.StudentModel;
import com.learning.mongoRepository.StudentMongoRepo;
import com.learning.repository.StudentRepository;
import com.learning.service.CommonService;
import com.learning.utility.email.EmailSender;
import com.learning.utility.excel.StudentReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService implements CommonService<StudentModel, Long> {

    private final StudentRepository jpaRepository;
    private final ModelMapper modelMapper;
    private final StudentReader studentReader;
    private final EmailSender emailSender;
    private final StudentMongoRepo mongoRepository;


    @Override
    public List<StudentModel> getAllRecords() {
        List<StudentCollection> studentCollectionList = mongoRepository.findAll();
        List<StudentEntity> studentEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(studentCollectionList)) {
            List<StudentModel> studentModelList = studentCollectionList.stream()
                    .map(studentCollection -> modelMapper.map(studentCollection, StudentModel.class))
                    .collect(Collectors.toList());
            return studentModelList;
        } else if (!CollectionUtils.isEmpty(studentEntityList)) {
            List<StudentModel> studentModelList = studentEntityList.stream()
                    .map(studentEntity ->
                            //	BeanUtils.copyProperties(studentEntity, studentModel);
                            modelMapper.map(studentEntity, StudentModel.class))
                    .collect(Collectors.toList());
            return studentModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<StudentModel> getLimitedRecords(int count) {
        List<StudentCollection> studentCollectionList = mongoRepository.findAll();
        List<StudentEntity> studentEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(studentCollectionList)) {
            List<StudentModel> studentModelList = studentCollectionList.stream()
                    .limit(count)
                    .map(studentCollection -> modelMapper.map(studentCollection, StudentModel.class))
                    .collect(Collectors.toList());
            return studentModelList;
        } else if (!CollectionUtils.isEmpty(studentEntityList)) {
            List<StudentModel> studentModelList = studentEntityList.stream()
                    .limit(count)
                    .map(studentEntity ->
                            //	BeanUtils.copyProperties(studentEntity, studentModel);
                            modelMapper.map(studentEntity, StudentModel.class))
                    .collect(Collectors.toList());
            return studentModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<StudentModel> getSortedRecords(String sortBy) {
        List<StudentCollection> studentCollectionList = mongoRepository.findAll();
        List<StudentEntity> studentEntityList = jpaRepository.findAll();
        if (!CollectionUtils.isEmpty(studentCollectionList)) {
            Comparator<StudentCollection> comparator = findSuitableComparatorTwo(sortBy);
            List<StudentModel> studentModelList = studentCollectionList.stream()
                    .sorted(comparator)
                    .map(studentCollection -> modelMapper.map(studentCollection, StudentModel.class))
                    .collect(Collectors.toList());
            return studentModelList;
        } else if (!CollectionUtils.isEmpty(studentEntityList)) {
            Comparator<StudentEntity> comparator = findSuitableComparator(sortBy);
            List<StudentModel> studentModelList = studentEntityList.stream()
                    .sorted(comparator)
                    .map(studentEntity -> modelMapper.map(studentEntity, StudentModel.class))
                    .collect(Collectors.toList());
            return studentModelList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public StudentModel saveRecord(StudentModel studentModel) {
        if (Objects.nonNull(studentModel)) {
            StudentEntity studentEntity = new StudentEntity();
            //	BeanUtils.copyProperties(studentModel, studentEntity);
            modelMapper.map(studentModel, studentEntity);
            jpaRepository.save(studentEntity);

            Runnable runnable = () -> {
                StudentCollection studentCollection = new StudentCollection();
                modelMapper.map(studentEntity, studentCollection);
                mongoRepository.save(studentCollection);
            };
            CompletableFuture.runAsync(runnable);
        }
        return studentModel;
    }

    @Override
    public List<StudentModel> saveAll(List<StudentModel> studentModelList) {
        if (!CollectionUtils.isEmpty(studentModelList)) {
            List<StudentEntity> studentEntityList = studentModelList.stream()
                    .map(studentModel -> modelMapper.map(studentModel, StudentEntity.class))
                    .collect(Collectors.toList());
            jpaRepository.saveAll(studentEntityList);
            Runnable runnable = () -> {
                List<StudentCollection> studentCollectionList = studentEntityList.stream()
                        .map(studentEntity -> modelMapper.map(studentEntity, StudentCollection.class))
                        .collect(Collectors.toList());
                mongoRepository.saveAll(studentCollectionList);
            };
            CompletableFuture.runAsync(runnable);
        }
        return studentModelList;
    }

    @Override
    public StudentModel getRecordById(Long id) {
        if (mongoRepository.existsById(id)) {
            StudentCollection studentCollection = mongoRepository.findById(id)
                    .orElseThrow(() ->
                            new DataNotFoundException("Record Not Found" + id));
            StudentModel studentModel = modelMapper.map(studentCollection, StudentModel.class);
            return studentModel;
        }
        StudentEntity studentEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Record Not found" + id));
        StudentModel studentModel = modelMapper.map(studentEntity, StudentModel.class);
        return studentModel;
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
    public StudentModel updateRecordById(Long id, StudentModel studentModel) {
        StudentEntity studentEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
        //BeanUtils.copyProperties(studentModel, studentEntity);
        modelMapper.map(studentModel, studentEntity);
        jpaRepository.save(studentEntity);

        Runnable runnable = () -> {
            StudentCollection studentCollection = mongoRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
            modelMapper.map(studentModel, studentCollection);
            mongoRepository.save(studentCollection);
        };
        CompletableFuture.runAsync(runnable);
        return studentModel;
    }

    public void emailSender() {
        List<String> emails = jpaRepository.findAllEmail();
        emailSender.mailSenderThread(emails);
    }

    public void sendEmailWithAttachment() {
        List<String> emails = jpaRepository.findAllEmail();
        emailSender.sendMailWithAttachment(emails);
    }

    @Override
    public void saveExcelFile(MultipartFile file) {
        if (file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            try {
                List<StudentEntity> studentEntityList = studentReader.getStudentObjects(file.getInputStream());
                jpaRepository.saveAll(studentEntityList);
                CompletableFuture.runAsync(() -> {
                    List<StudentCollection> studentCollectionList = studentEntityList.stream()
                            .map(studentEntity -> modelMapper.map(studentEntity, StudentCollection.class))
                            .collect(Collectors.toList());
                    mongoRepository.saveAll(studentCollectionList);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Comparator<StudentEntity> findSuitableComparator(String sortBy) {
        Comparator<StudentEntity> comparator;
        switch (sortBy) {
            case "name": {
                comparator = (studentOne, studentTwo) ->
                        studentOne.getName().compareTo(studentTwo.getName());
                break;
            }
            case "email": {
                comparator = (studentOne, studentTwo) ->
                        studentOne.getEmail().compareTo(studentTwo.getEmail());
                break;
            }
            default: {
                comparator = (studentOne, studentTwo) ->
                        studentOne.getId().compareTo(studentTwo.getId());
            }
        }
        return comparator;
    }

    private Comparator<StudentCollection> findSuitableComparatorTwo(String sortBy) {
        Comparator<StudentCollection> comparator;
        switch (sortBy) {
            case "name": {
                comparator = (studentOne, studentTwo) ->
                        studentOne.getName().compareTo(studentTwo.getName());
                break;
            }
            case "email": {
                comparator = (studentOne, studentTwo) ->
                        studentOne.getEmail().compareTo(studentTwo.getEmail());
                break;
            }
            default: {
                comparator = (studentOne, studentTwo) ->
                        studentOne.getId().compareTo(studentTwo.getId());
            }
        }
        return comparator;
    }
}

