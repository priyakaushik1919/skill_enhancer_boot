package com.learning.rest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.learning.model.BatchModel;
import com.learning.model.TimeSlotModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.learning.model.StudentModel;
import com.learning.model.TrainerModel;
import com.learning.service.impl.TrainerService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/trainer")
@RequiredArgsConstructor
public class TrainerController {

	private final TrainerService trainerService;

	@GetMapping
	public List<TrainerModel> getAllRecords(){
		return trainerService.getAllRecords();
	}

	@GetMapping("get-records")
	public List<TrainerModel> getAllRecords(@RequestParam(value = "count" ,required = false , defaultValue = "0")
										  int count, @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy) {
		if (count == 0 && (Objects.isNull(sortBy) || sortBy.isBlank())) {
			return trainerService.getAllRecords();
		} else if (count > 0) {
			return trainerService.getLimitedRecords(count);
		} else {
			return trainerService.getSortedRecords(sortBy);
		}
	}
	
	@PostMapping
	public TrainerModel save(@RequestBody TrainerModel trainerModel) {
		return trainerService.saveRecord(trainerModel);
	}

	@PostMapping("/all")
	public List<TrainerModel> saveAll(@RequestBody List<TrainerModel> trainerModelList){
		return trainerService.saveAll(trainerModelList);
	}

	@PutMapping("/{id}")
	public TrainerModel updateById(@PathVariable Long id, @RequestBody TrainerModel trainerModel) {
		return trainerService.updateRecordById(id, trainerModel);
	}

	@GetMapping("/{id}")
	public TrainerModel getRecordById(@PathVariable Long id){
		return trainerService.getRecordById(id);
	}

	@DeleteMapping("/{id}")
	public void deleteRecordById(@PathVariable Long id) {
		trainerService.deleteRecordById(id);
	}

	@PostMapping("/upload")
	public String uploadExcelFile(@RequestParam ("file") MultipartFile file){
		trainerService.saveExcelFile(file);
		return "file uploaded successfully";
	}
}
