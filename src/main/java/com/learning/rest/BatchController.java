package com.learning.rest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.learning.model.BatchModel;
import com.learning.model.StudentModel;
import com.learning.service.impl.BatchService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchController {
    private final BatchService batchService;

	@GetMapping
	public List<BatchModel> getAllRecords(){
		return batchService.getAllRecords();
	}

	@GetMapping("get-records")
	public List<BatchModel> getAllRecords(@RequestParam(value = "count" ,required = false , defaultValue = "0")
											  int count,@RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy) {
		if (count == 0 && (Objects.isNull(sortBy) || sortBy.isBlank())) {
			return batchService.getAllRecords();
		} else if (count > 0) {
			return batchService.getLimitedRecords(count);
		} else {
			return batchService.getSortedRecords(sortBy);
		}
	}
	@PostMapping
	public BatchModel save(@RequestBody BatchModel batchModel) {
		return batchService.saveRecord(batchModel);
	}

	@PostMapping("/all")
	public List<BatchModel> saveAll(@RequestBody List<BatchModel> batchModelList){
		return batchService.saveAll(batchModelList);
	}
	@PutMapping("/{id}")
	public BatchModel updateById(@PathVariable Long id, @RequestBody BatchModel batchModel) {
		return batchService.updateRecordById(id, batchModel);
	}

	@GetMapping("/{id}")
	public BatchModel getRecordById(@PathVariable Long id){
		return batchService.getRecordById(id);
	}

	@DeleteMapping("/{id}")
	public void deleteRecordById(@PathVariable Long id) {
		batchService.deleteRecordById(id);
	}

	@PostMapping("/upload")
	public String uploadExcelFile(@RequestParam ("file") MultipartFile file){
		batchService.saveExcelFile(file);
		return "file uploaded successfully";
	}
}
