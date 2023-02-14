package com.learning.utility.excel;

import com.learning.entity.BatchEntity;
import com.learning.entity.CourseEntity;
import com.learning.enums.BatchStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class BatchReader {

    public List<BatchEntity> getBatchObjects(InputStream inputStream) {

        List<BatchEntity> batchEntityList = new ArrayList<>();
        try {

            //to set the file path of Excel file
            //FileInputStream file = new FileInputStream(new File(".\\resources\\batch-data.xlsx"));

            //creating object of XSSFWorkbook to open Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            //getting sheet at which my data is present
            XSSFSheet sheet = workbook.getSheetAt(2);//starts with 0

            getBatchList(sheet, batchEntityList);//private method to get Batch list

            inputStream.close();//closing the workbook
        } catch (Exception e) {
            e.printStackTrace();
        }
        return batchEntityList;
    }

    private static void getBatchList(XSSFSheet sheet, List<BatchEntity> batchEntityList) {
        //loop iterating through rows
        for (int index = sheet.getFirstRowNum() + 1; index <= sheet.getLastRowNum(); index++) {
            //get row by passing index
            Row row = sheet.getRow(index);

            BatchEntity batchEntity = new BatchEntity();

            //loop iterating through columns
            for (int index2 = row.getFirstCellNum(); index2 < row.getLastCellNum(); index2++) {
                //get cell by passing index
                Cell cell = row.getCell(index2);
                if (index2 == 0) {
                    batchEntity.setId((long) cell.getNumericCellValue());// getting cell type for numeric value
                } else if (index2 == 1) {
                    batchEntity.setStudentCount((int) cell.getNumericCellValue());
                } else if (index2 == 2) {
                    batchEntity.setStartDate(cell.getLocalDateTimeCellValue().toLocalDate());
                } else if (index2 == 3) {
                    batchEntity.setEndDate(cell.getLocalDateTimeCellValue().toLocalDate());
                } else if (index2 == 4) {
                    batchEntity.setBatchStatus(BatchStatus.valueOf(cell.getStringCellValue()));
                } else if (index2 == 5) {
                    batchEntity.setCourseId((long) cell.getNumericCellValue());
                } else if (index2 == 6) {
                    batchEntity.setTimeSlotId((long) cell.getNumericCellValue());
                } else {
                    System.err.println("data not found");
                }
            }
            //adding objects to list
            batchEntityList.add(batchEntity);
        }
    }
}

