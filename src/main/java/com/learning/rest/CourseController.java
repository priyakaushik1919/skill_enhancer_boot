package com.learning.rest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.learning.model.BatchModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.learning.model.CourseModel;
import com.learning.model.StudentModel;
import com.learning.service.impl.CourseService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService courseService;

	@GetMapping
	public List<CourseModel> getAllRecords(){
		return courseService.getAllRecords();
	}

	@GetMapping("get-records")
	public List<CourseModel> getAllRecords(@RequestParam(value = "count" ,required = false , defaultValue = "0")
										  int count,@RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy) {
		if (count == 0 && (Objects.isNull(sortBy) || sortBy.isBlank())) {
			return courseService.getAllRecords();
		} else if (count > 0) {
			return courseService.getLimitedRecords(count);
		} else {
			return courseService.getSortedRecords(sortBy);
		}
	}
	
	@PostMapping
	public CourseModel save(@RequestBody CourseModel courseModel) {
		return courseService.saveRecord(courseModel);
	}

	@PostMapping("/all")
	public List<CourseModel> saveAll(@RequestBody List<CourseModel> courseModelList){
		return courseService.saveAll(courseModelList);
	}
	@PutMapping("/{id}")
	public CourseModel updateById(@PathVariable Long id, @RequestBody CourseModel courseModel) {
		return courseService.updateRecordById(id, courseModel);
	}

	@GetMapping("/{id}")
	public CourseModel getRecordById(@PathVariable Long id){
		return courseService.getRecordById(id);
	}

	@DeleteMapping("/{id}")
	public void deleteRecordById(@PathVariable Long id) {
		courseService.deleteRecordById(id);
	}
	@PostMapping("/upload")
	public String uploadExcelFile(@RequestParam ("file") MultipartFile file){
		courseService.saveExcelFile(file);
		return "file uploaded successfully";
	}
}
