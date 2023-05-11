package com.learning.rest;

import java.util.List;
import java.util.Objects;

import com.learning.model.BatchModel;
import com.learning.model.StudentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.learning.model.StudentBatchModel;
import com.learning.service.impl.StudentBatchService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/studentBatch")
@RequiredArgsConstructor
public class StudentBatchController {

    private final StudentBatchService studentBatchService;

    @GetMapping
    public List<StudentBatchModel> getAllStudent() {
        return studentBatchService.getAllRecords();
    }

    @GetMapping("get-records")
    public List<StudentBatchModel> getAllRecords(@RequestParam(value = "count", required = false, defaultValue = "0")
                                                 int count, @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy) {
        if (count == 0 && (Objects.isNull(sortBy) || sortBy.isBlank())) {
            return studentBatchService.getAllRecords();
        } else if (count > 0) {
            return studentBatchService.getLimitedRecords(count);
        } else {
            return studentBatchService.getSortedRecords(sortBy);
        }
    }

    @PostMapping
    public StudentBatchModel save(@RequestBody StudentBatchModel studentBatchModel) {
        return studentBatchService.saveRecord(studentBatchModel);
    }

    @PostMapping("/all")
    public List<StudentBatchModel> saveAll(@RequestBody List<StudentBatchModel> studentBatchModelList) {
        return studentBatchService.saveAll(studentBatchModelList);
    }

    @PutMapping("/{id}")
    public StudentBatchModel updateById(@PathVariable Long id, @RequestBody StudentBatchModel studentBatchModel) {
        return studentBatchService.updateRecordById(id, studentBatchModel);
    }

    @GetMapping("/{id}")
    public StudentBatchModel getRecordById(@PathVariable Long id) {
        return studentBatchService.getRecordById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRecordById(@PathVariable Long id) {
        studentBatchService.deleteRecordById(id);
    }

    @PostMapping("/upload")
    public String uploadExcelFile(@RequestParam("file") MultipartFile file) {
        studentBatchService.saveExcelFile(file);
        return "file uploaded successfully";
    }
}
