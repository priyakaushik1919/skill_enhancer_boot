package com.learning.service.impl;

import com.learning.collection.StudentBatchCollection;
import com.learning.entity.StudentBatchEntity;
import com.learning.exception.DataNotFoundException;
import com.learning.model.StudentBatchModel;
import com.learning.mongoRepository.StudentBatchMongoRepo;
import com.learning.repository.StudentBatchRepository;
import com.learning.service.CommonService;
import com.learning.utility.excel.StudentBatchReader;
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
public class StudentBatchService implements CommonService<StudentBatchModel, Long> {
	private final StudentBatchRepository jpaRepository;
	private final ModelMapper modelMapper;
	private final StudentBatchReader studentBatchReader;
	private final StudentBatchMongoRepo mongoRepository;

	@Override
	public List<StudentBatchModel> getAllRecords() {
		List<StudentBatchCollection> studentBatchCollectionList = mongoRepository.findAll();
		List<StudentBatchEntity> studentBatchEntityList = jpaRepository.findAll();
		if (!CollectionUtils.isEmpty(studentBatchCollectionList)) {
			List<StudentBatchModel> studentBatchModelList = studentBatchCollectionList.stream()
					.map(studentBatchCollection -> modelMapper.map(studentBatchCollection, StudentBatchModel.class))
					.collect(Collectors.toList());
			return studentBatchModelList;
		} else if (!CollectionUtils.isEmpty(studentBatchEntityList)) {
			List<StudentBatchModel> studentBatchModelList = studentBatchEntityList.stream()
					.map(studentBatchEntity ->
							//	BeanUtils.copyProperties(studentEntity, studentModel);
							modelMapper.map(studentBatchEntity, StudentBatchModel.class))
					.collect(Collectors.toList());
			return studentBatchModelList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<StudentBatchModel> getLimitedRecords(int count) {
		List<StudentBatchCollection> studentBatchCollectionList = mongoRepository.findAll();
		List<StudentBatchEntity> studentBatchEntityList = jpaRepository.findAll();
		if (!CollectionUtils.isEmpty(studentBatchCollectionList)) {
			List<StudentBatchModel> studentBatchModelList = studentBatchCollectionList.stream()
					.limit(count)
					.map(studentBatchCollection -> modelMapper.map(studentBatchCollection, StudentBatchModel.class))
					.collect(Collectors.toList());
			return studentBatchModelList;
		} else if (!CollectionUtils.isEmpty(studentBatchEntityList)) {
			List<StudentBatchModel> studentBatchModelList = studentBatchEntityList.stream()
					.limit(count)
					.map(studentBatchEntity ->
							//	BeanUtils.copyProperties(studentBatchEntity, studentBatchModel);
							modelMapper.map(studentBatchEntity, StudentBatchModel.class))
					.collect(Collectors.toList());
			return studentBatchModelList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<StudentBatchModel> getSortedRecords(String sortBy) {
		List<StudentBatchCollection> studentBatchCollectionList = mongoRepository.findAll();
		List<StudentBatchEntity> studentBatchEntityList = jpaRepository.findAll();
		if (!CollectionUtils.isEmpty(studentBatchCollectionList)) {
			Comparator<StudentBatchCollection> comparator = findSuitableComparatorTwo(sortBy);
			List<StudentBatchModel> studentBatchModelList = studentBatchCollectionList.stream()
					.sorted(comparator)
					.map(studentBatchCollection -> modelMapper.map(studentBatchCollection, StudentBatchModel.class))
					.collect(Collectors.toList());
			return studentBatchModelList;
		} else if (!CollectionUtils.isEmpty(studentBatchEntityList)) {
			Comparator<StudentBatchEntity> comparator = findSuitableComparator(sortBy);
			List<StudentBatchModel> studentBatchModelList = studentBatchEntityList.stream()
					.sorted(comparator)
					.map(studentBatchEntity -> modelMapper.map(studentBatchEntity, StudentBatchModel.class))
					.collect(Collectors.toList());
			return studentBatchModelList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public StudentBatchModel saveRecord(StudentBatchModel studentBatchModel) {
		if (Objects.nonNull(studentBatchModel)) {
			StudentBatchEntity studentBatchEntity = new StudentBatchEntity();
			//	BeanUtils.copyProperties(studentBatchModel, studentBatchEntity);
			modelMapper.map(studentBatchModel, studentBatchEntity);
			jpaRepository.save(studentBatchEntity);

			Runnable runnable = () -> {
				StudentBatchCollection studentBatchCollection = new StudentBatchCollection();
				modelMapper.map(studentBatchEntity, studentBatchCollection);
				mongoRepository.save(studentBatchCollection);
			};
			CompletableFuture.runAsync(runnable);
		}
		return studentBatchModel;
	}
	@Override
	public List<StudentBatchModel> saveAll(List<StudentBatchModel> studentBatchModelList) {
		if (!CollectionUtils.isEmpty(studentBatchModelList)) {
			List<StudentBatchEntity> studentBatchEntityList = studentBatchModelList.stream()
					.map(studentBatchModel -> modelMapper.map(studentBatchModel, StudentBatchEntity.class))
					.collect(Collectors.toList());
			jpaRepository.saveAll(studentBatchEntityList);
			Runnable runnable = () -> {
				List<StudentBatchCollection> studentBatchCollectionList = studentBatchEntityList.stream()
						.map(studentBatchEntity -> modelMapper.map(studentBatchEntity, StudentBatchCollection.class))
						.collect(Collectors.toList());
				mongoRepository.saveAll(studentBatchCollectionList);
			};
			CompletableFuture.runAsync(runnable);
		}
		return studentBatchModelList;
	}
	@Override
	public StudentBatchModel getRecordById(Long id) {
		if (mongoRepository.existsById(id)) {
			StudentBatchCollection studentBatchCollection = mongoRepository.findById(id)
					.orElseThrow(() ->
							new DataNotFoundException("Record Not Found" + id));
			StudentBatchModel studentBatchModel = modelMapper.map(studentBatchCollection, StudentBatchModel.class);
			return studentBatchModel;
		}
		StudentBatchEntity studentBatchEntity = jpaRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("Record Not found" + id));
		StudentBatchModel studentBatchModel = modelMapper.map(studentBatchEntity, StudentBatchModel.class);
		return studentBatchModel;
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
	public StudentBatchModel updateRecordById(Long id, StudentBatchModel studentBatchModel) {
		StudentBatchEntity studentBatchEntity = jpaRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
		//BeanUtils.copyProperties(studentBatchModel, studentBatchEntity);
		modelMapper.map(studentBatchModel, studentBatchEntity);
		jpaRepository.save(studentBatchEntity);

		Runnable runnable = () -> {
			StudentBatchCollection studentBatchCollection = mongoRepository.findById(id)
					.orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
			modelMapper.map(studentBatchModel, studentBatchCollection);
			mongoRepository.save(studentBatchCollection);
		};
		CompletableFuture.runAsync(runnable);
		return studentBatchModel;
	}

	public void saveExcelFile(MultipartFile file) {
		if (file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			try {
				List<StudentBatchEntity> studentBatchEntityList = studentBatchReader.getStudentBatchObjects(file.getInputStream());
				jpaRepository.saveAll(studentBatchEntityList);
				CompletableFuture.runAsync(() -> {
					List<StudentBatchCollection> studentBatchCollectionList = studentBatchEntityList.stream()
							.map(studentBatchEntity -> modelMapper.map(studentBatchEntity, StudentBatchCollection.class))
							.collect(Collectors.toList());
					mongoRepository.saveAll(studentBatchCollectionList);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Comparator<StudentBatchEntity> findSuitableComparator(String sortBy) {
		Comparator<StudentBatchEntity> comparator;
		switch (sortBy) {
			case "fees": {
				comparator = (studentBatchOne, studentBatchTwo) ->
						studentBatchOne.getFees().compareTo(studentBatchTwo.getFees());
				break;
			}
			case "batchId": {
				comparator = (studentBatchOne, studentBatchTwo) ->
						studentBatchOne.getBatchId().compareTo(studentBatchTwo.getBatchId());
				break;
			}
			default: {
				comparator = (studentBatchOne, studentBatchTwo) ->
						studentBatchOne.getId().compareTo(studentBatchTwo.getId());
			}
		}
		return comparator;
	}

	private Comparator<StudentBatchCollection> findSuitableComparatorTwo(String sortBy) {
		Comparator<StudentBatchCollection> comparator;
		switch (sortBy) {
			case "fees": {
				comparator = (studentBatchOne, studentBatchTwo) ->
						studentBatchOne.getFees().compareTo(studentBatchTwo.getFees());
				break;
			}
			case "batchId": {
				comparator = (studentBatchOne, studentBatchTwo) ->
						studentBatchOne.getBatchId().compareTo(studentBatchTwo.getBatchId());
				break;
			}
			default: {
				comparator = (studentBatchOne, studentBatchTwo) ->
						studentBatchOne.getId().compareTo(studentBatchTwo.getId());
			}
		}
		return comparator;
	}
}
