package com.learning.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.learning.constant.NumberConstant;
import com.learning.exception.DataNotFoundException;
import com.learning.utility.excel.StudentReader;
import com.learning.utility.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.learning.entity.StudentEntity;
import com.learning.model.StudentModel;
import com.learning.repository.StudentRepository;
import com.learning.service.CommonService;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StudentService implements CommonService<StudentModel, Long> {

	private final StudentRepository studentRepository;
	private final ModelMapper modelMapper;
	private final StudentReader studentReader;
	private final EmailSender emailSender;


	@Override
	public List<StudentModel> getAllRecords() {
		List<StudentEntity> studentEntityList = studentRepository.findAll();
		if (!CollectionUtils.isEmpty(studentEntityList)) {
			List<StudentModel> studentModelList = studentEntityList.stream().map(studentEntity -> {
				StudentModel studentModel = new StudentModel();
				//	BeanUtils.copyProperties(studentEntity, studentModel);
				modelMapper.map(studentEntity, studentModel);
				return studentModel;
			}).collect(Collectors.toList());
			return studentModelList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<StudentModel> getLimitedRecords(int count) {
		List<StudentEntity> studentEntityList = studentRepository.findAll();
		if (!CollectionUtils.isEmpty(studentEntityList)) {
			List<StudentModel> studentModelList = studentEntityList.stream()
					.limit(count)
					.map(studentEntity -> {
						StudentModel studentModel = new StudentModel();
						//	BeanUtils.copyProperties(studentEntity, studentModel);
						modelMapper.map(studentEntity, studentModel);
						return studentModel;
					}).collect(Collectors.toList());
			return studentModelList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<StudentModel> getSortedRecords(String sortBy) {
		List<StudentEntity> studentEntityList = studentRepository.findAll();
		if (Objects.nonNull(studentEntityList) && studentEntityList.size() > NumberConstant.ZERO) {
			Comparator<StudentEntity> comparator = findSuitableComparator(sortBy);
			List<StudentModel> studentModelList = studentEntityList.stream()
					.sorted(comparator)
					.map(studentEntity -> {
						StudentModel studentModel = modelMapper.map(studentEntity, StudentModel.class);
						return studentModel;
					})
					.collect(Collectors.toList());
			return studentModelList;
		} else {
			return Collections.emptyList();
		}
	}

		@Override
		public StudentModel saveRecord (StudentModel studentModel){
			if (Objects.nonNull(studentModel)) {
				StudentEntity studentEntity = new StudentEntity();
				//	BeanUtils.copyProperties(studentModel, studentEntity);
				modelMapper.map(studentModel, studentEntity);
				studentRepository.save(studentEntity);
			}
			return studentModel;
		}

		@Override
		public List<StudentModel> saveAll (List <StudentModel> studentModelList) {
			if (Objects.nonNull(studentModelList) && studentModelList.size() > NumberConstant.ZERO) {
				List<StudentEntity> studentEntityList = studentModelList.stream()
						.map(studentModel -> {
							StudentEntity entity = modelMapper.map(studentModel, StudentEntity.class);
							return entity;
						})
						.collect(Collectors.toList());
				studentRepository.saveAll(studentEntityList);
			}
			return studentModelList;
		}

		@Override
		public StudentModel getRecordById(Long id){
			Optional<StudentEntity> optionalEntity = studentRepository.findById(id);
			if (optionalEntity.isPresent()) {
				StudentEntity studentEntity = optionalEntity.get();
				StudentModel studentModel = modelMapper.map(studentEntity, StudentModel.class);
				return studentModel;
			}
			//throw new IllegalArgumentException("Student Entity Not Found for id:"+id);
			throw new DataNotFoundException("Student Entity Not Found for id:"+id);
		}

		@Override
		public void deleteRecordById (Long id){
			studentRepository.deleteById(id);
		}

		@Override
		public StudentModel updateRecordById (Long id, StudentModel studentModel){
			Optional<StudentEntity> optionalStudentEntity = studentRepository.findById(id);
			if (optionalStudentEntity.isPresent()) {
				StudentEntity studentEntity = optionalStudentEntity.get();
				//BeanUtils.copyProperties(studentModel, studentEntity);
				modelMapper.map(studentModel, studentEntity);
				studentRepository.save(studentEntity);
			}
			return studentModel;
		}

		public void saveExcelFile (MultipartFile file){
			if (file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
				try {
					List<StudentEntity> studentEntityList = studentReader.getStudentObjects(file.getInputStream());
					studentRepository.saveAll(studentEntityList);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		public void emailSender(){
			List<String> emails  = studentRepository.findAllEmail();
			emailSender.mailSenderThread(emails);
		}

		public void sendEmailWithAttachment(){
		List<String> emails  = studentRepository.findAllEmail();
		emailSender.sendMailWithAttachment(emails);
		}

		private Comparator<StudentEntity> findSuitableComparator (String sortBy){
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
	}

