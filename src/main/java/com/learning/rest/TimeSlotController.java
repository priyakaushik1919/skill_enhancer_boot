package com.learning.rest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.learning.model.BatchModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.learning.model.StudentModel;
import com.learning.model.TimeSlotModel;
import com.learning.service.impl.TimeSlotService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/timeSlot")
@RequiredArgsConstructor
public class TimeSlotController {

	private final TimeSlotService timeSlotService;

	@GetMapping
	public List<TimeSlotModel> getAllRecords(){
		return timeSlotService.getAllRecords();
	}

	@GetMapping("get-records")
	public List<TimeSlotModel> getAllRecords(@RequestParam(value = "count" ,required = false , defaultValue = "0")
										  int count,@RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy) {
		if (count == 0 && (Objects.isNull(sortBy) || sortBy.isBlank())) {
			return timeSlotService.getAllRecords();
		} else if (count > 0) {
			return timeSlotService.getLimitedRecords(count);
		} else {
			return timeSlotService.getSortedRecords(sortBy);
		}
	}
	
	@PostMapping
	public TimeSlotModel save(@RequestBody TimeSlotModel timeSlotModel) {
		return timeSlotService.saveRecord(timeSlotModel);
	}

	@PostMapping("/all")
	public List<TimeSlotModel> saveAll(@RequestBody List<TimeSlotModel> timeSlotModelList){
		return timeSlotService.saveAll(timeSlotModelList);
	}
	@PutMapping("/{id}")
	public TimeSlotModel updateById(@PathVariable Long id, @RequestBody TimeSlotModel timeSlotModel) {
		return timeSlotService.updateRecordById(id, timeSlotModel);
	}

	@GetMapping("/{id}")
	public TimeSlotModel getRecordById(@PathVariable Long id){
		return timeSlotService.getRecordById(id);
	}

	@DeleteMapping("/{id}")
	public void deleteRecordById(@PathVariable Long id) {
		timeSlotService.deleteRecordById(id);
	}

	@PostMapping("/upload")
	public String uploadExcelFile(@RequestParam ("file") MultipartFile file){
		timeSlotService.saveExcelFile(file);
		return "file uploaded successfully";
	}
}
