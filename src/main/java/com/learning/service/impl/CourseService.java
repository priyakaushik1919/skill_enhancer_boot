package com.learning.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.learning.collection.CourseCollection;
import com.learning.collection.StudentCollection;
import com.learning.exception.DataNotFoundException;
import com.learning.model.StudentModel;
import com.learning.mongoRepository.CourseMongoRepo;
import com.learning.utility.excel.CourseReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.learning.entity.CourseEntity;
import com.learning.entity.StudentEntity;
import com.learning.model.CourseModel;
import com.learning.repository.CourseRepository;
import com.learning.service.CommonService;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService implements CommonService<CourseModel, Long>{
	private final CourseRepository jpaRepository;
	private final ModelMapper modelMapper;
	private final CourseReader courseReader;
	private final CourseMongoRepo mongoRepository;
	@Override
	public List<CourseModel> getAllRecords() {
		List<CourseCollection> courseCollectionList = mongoRepository.findAll();
		List<CourseEntity> courseEntityList = jpaRepository.findAll();
		if (!CollectionUtils.isEmpty(courseCollectionList)) {
			List<CourseModel> courseModelList = courseCollectionList.stream()
					.map(courseCollection -> modelMapper.map(courseCollection, CourseModel.class))
					.collect(Collectors.toList());
			return courseModelList;
		} else if (!CollectionUtils.isEmpty(courseEntityList)) {
			List<CourseModel> courseModelList = courseEntityList.stream()
					.map(courseEntity ->
							//	BeanUtils.copyProperties(courseEntity, courseModel);
							modelMapper.map(courseEntity, CourseModel.class))
					.collect(Collectors.toList());
			return courseModelList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<CourseModel> getLimitedRecords(int count) {
		List<CourseCollection> courseCollectionList = mongoRepository.findAll();
		List<CourseEntity> courseEntityList = jpaRepository.findAll();
		if (!CollectionUtils.isEmpty(courseCollectionList)) {
			List<CourseModel> courseModelList = courseCollectionList.stream()
					.limit(count)
					.map(courseCollection -> modelMapper.map(courseCollection, CourseModel.class))
					.collect(Collectors.toList());
			return courseModelList;
		} else if (!CollectionUtils.isEmpty(courseEntityList)) {
			List<CourseModel> courseModelList = courseEntityList.stream()
					.limit(count)
					.map(courseEntity ->
							//	BeanUtils.copyProperties(courseEntity, courseModel);
							modelMapper.map(courseEntity, CourseModel.class))
					.collect(Collectors.toList());
			return courseModelList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<CourseModel> getSortedRecords(String sortBy) {
		List<CourseCollection> courseCollectionList = mongoRepository.findAll();
		List<CourseEntity> courseEntityList = jpaRepository.findAll();
		if (!CollectionUtils.isEmpty(courseCollectionList)) {
			Comparator<CourseCollection> comparator = findSuitableComparatorTwo(sortBy);
			List<CourseModel> courseModelList = courseCollectionList.stream()
					.sorted(comparator)
					.map(courseCollection -> modelMapper.map(courseCollection, CourseModel.class))
					.collect(Collectors.toList());
			return courseModelList;
		} else if (!CollectionUtils.isEmpty(courseEntityList)) {
			Comparator<CourseEntity> comparator = findSuitableComparator(sortBy);
			List<CourseModel> courseModelList = courseEntityList.stream()
					.sorted(comparator)
					.map(courseEntity -> modelMapper.map(courseEntity, CourseModel.class))
					.collect(Collectors.toList());
			return courseModelList;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public CourseModel saveRecord(CourseModel courseModel) {
		if (Objects.nonNull(courseModel)) {
			CourseEntity courseEntity = new CourseEntity();
			//	BeanUtils.copyProperties(courseModel, courseEntity);
			modelMapper.map(courseModel, courseEntity);
			jpaRepository.save(courseEntity);

			Runnable runnable = () -> {
				CourseCollection courseCollection = new CourseCollection();
				modelMapper.map(courseEntity, courseCollection);
				mongoRepository.save(courseCollection);
			};
			CompletableFuture.runAsync(runnable);
		}
		return courseModel;
	}
	@Override
	public List<CourseModel> saveAll(List<CourseModel> courseModelList) {
		if (!CollectionUtils.isEmpty(courseModelList)) {
			List<CourseEntity> courseEntityList = courseModelList.stream()
					.map(courseModel -> modelMapper.map(courseModel, CourseEntity.class))
					.collect(Collectors.toList());
			jpaRepository.saveAll(courseEntityList);
			Runnable runnable = () -> {
				List<CourseCollection> courseCollectionList = courseEntityList.stream()
						.map(courseEntity -> modelMapper.map(courseEntity,CourseCollection.class))
						.collect(Collectors.toList());
				mongoRepository.saveAll(courseCollectionList);
			};
			CompletableFuture.runAsync(runnable);
		}
		return courseModelList;
	}
	@Override
	public CourseModel getRecordById(Long id) {
		if (mongoRepository.existsById(id)) {
			CourseCollection courseCollection = mongoRepository.findById(id)
					.orElseThrow(() ->
							new DataNotFoundException("Record Not Found" + id));
			CourseModel courseModel = modelMapper.map(courseCollection, CourseModel.class);
			return courseModel;
		}
		CourseEntity courseEntity = jpaRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("Record Not found" + id));
		CourseModel courseModel = modelMapper.map(courseEntity, CourseModel.class);
		return courseModel;
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
	public CourseModel updateRecordById(Long id, CourseModel courseModel) {
		CourseEntity courseEntity = jpaRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
		//BeanUtils.copyProperties(studentModel, studentEntity);
		modelMapper.map(courseModel, courseEntity);
		jpaRepository.save(courseEntity);

		Runnable runnable = () -> {
			CourseCollection courseCollection = mongoRepository.findById(id)
					.orElseThrow(() -> new DataNotFoundException("Record Not Found" + id));
			modelMapper.map(courseModel, courseCollection);
			mongoRepository.save(courseCollection);
		};
		CompletableFuture.runAsync(runnable);
		return courseModel;
	}

	public void saveExcelFile (MultipartFile file){
		if (file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			try {
				List<CourseEntity> courseEntityList = courseReader.getCourseObjects(file.getInputStream());
				jpaRepository.saveAll(courseEntityList);
				CompletableFuture.runAsync(() -> {
					List<CourseCollection> courseCollectionList = courseEntityList.stream()
							.map(courseEntity -> modelMapper.map(courseEntity, CourseCollection.class))
							.collect(Collectors.toList());
					mongoRepository.saveAll(courseCollectionList);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Comparator<CourseEntity> findSuitableComparator(String sortBy) {
		Comparator<CourseEntity> comparator;
		switch (sortBy) {
			case "name": {
				comparator = (courseOne, courseTwo) -> courseOne.getName().compareTo(courseTwo.getName());
				break;
			}
			case "duration": {
				comparator = (courseOne, courseTwo) -> courseOne.getDuration().compareTo(courseTwo.getDuration());
				break;
			}
			default: {
				comparator = (courseOne, courseTwo) -> courseOne.getId().compareTo(courseTwo.getId());
			}
		}
		return comparator;
	}

	private Comparator<CourseCollection> findSuitableComparatorTwo(String sortBy) {
		Comparator<CourseCollection> comparator;
		switch (sortBy) {
			case "name": {
				comparator = (courseOne, courseTwo) -> courseOne.getName().compareTo(courseTwo.getName());
				break;
			}
			case "duration": {
				comparator = (courseOne, courseTwo) -> courseOne.getDuration().compareTo(courseTwo.getDuration());
				break;
			}
			default: {
				comparator = (courseOne, courseTwo) -> courseOne.getId().compareTo(courseTwo.getId());
			}
		}
		return comparator;
	}

}
