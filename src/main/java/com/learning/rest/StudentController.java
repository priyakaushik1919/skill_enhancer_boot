package com.learning.rest;

import com.learning.model.BatchModel;
import com.learning.model.StudentModel;
import com.learning.service.impl.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

	private final StudentService studentService;

	@GetMapping
	public List<StudentModel> getAllRecords(){
		return studentService.getAllRecords();
	}

	@GetMapping("/{id}")
	public StudentModel getRecordById(@PathVariable Long id) {
		return studentService.getRecordById(id);
	}

	@GetMapping("get-records")
	public List<StudentModel> getAllRecords(@RequestParam(value = "count", required = false, defaultValue = "0")
											int count, @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy) {
		if (count == 0 && (Objects.isNull(sortBy) || sortBy.isBlank())) {
			return studentService.getAllRecords();
		} else if (count > 0) {
			return studentService.getLimitedRecords(count);
		} else {
			return studentService.getSortedRecords(sortBy);
		}
	}

	@PostMapping
	public StudentModel save(@RequestBody StudentModel studentModel) {
		return studentService.saveRecord(studentModel);
	}

	@PostMapping("/all")
	public List<StudentModel> saveAll(@RequestBody List<StudentModel> studentModelList) {
		return studentService.saveAll(studentModelList);
	}

	@PutMapping("/{id}")
	public StudentModel updateById(@PathVariable Long id, @RequestBody StudentModel studentModel) {
		return studentService.updateRecordById(id, studentModel);
	}

	@DeleteMapping("/{id}")
	public void deleteRecordById(@PathVariable Long id) {
		studentService.deleteRecordById(id);
	}

	@PostMapping("/upload")
	public String uploadExcelFile(@RequestParam("file") MultipartFile file) {
		studentService.saveExcelFile(file);
		return "file uploaded successfully";
	}

	@PostMapping("/email")
	public void emailSender() {
		studentService.emailSender();
	}

	@PostMapping("/email/attachment")
	public void sendEmailWithAttachment() {
		studentService.sendEmailWithAttachment();
	}

}
