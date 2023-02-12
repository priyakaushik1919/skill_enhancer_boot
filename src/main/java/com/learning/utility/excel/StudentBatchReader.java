package com.learning.utility.excel;

import com.learning.entity.StudentBatchEntity;
import com.learning.entity.StudentEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class StudentBatchReader {

    public List<StudentBatchEntity> getStudentBatchObjects(InputStream inputStream) {

        List<StudentBatchEntity> studentBatchEntityList = new ArrayList<>();
        try {

            //to set the file path of Excel file
            //FileInputStream file = new FileInputStream(new File(".\\resources\\studentBatch-data.xlsx"));

            //creating object of XSSFWorkbook to open Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            //getting sheet at which my data is present
            XSSFSheet sheet = workbook.getSheetAt(5);//starts with 0

            getStudentBatchList(sheet, studentBatchEntityList);//private method to get studentBatch list

            inputStream.close();//closing the workbook
        } catch (Exception e) {
            e.printStackTrace();
        }
        return studentBatchEntityList;
    }

    private static void getStudentBatchList(XSSFSheet sheet, List<StudentBatchEntity> studentBatchEntityList) {
        //loop iterating through rows
        for (int index = sheet.getFirstRowNum() + 1; index <= sheet.getLastRowNum(); index++) {
            //get row by passing index
            Row row = sheet.getRow(index);

            StudentBatchEntity studentBatchEntity = new StudentBatchEntity();

            //loop iterating through columns
            for (int index2 = row.getFirstCellNum(); index2 < row.getLastCellNum(); index2++) {
                //get cell by passing index
                Cell cell = row.getCell(index2);
                if (index2 == 0) {
                    studentBatchEntity.setId((long) cell.getNumericCellValue());// getting cell type for numeric value
                } else if (index2 == 1) {
                    studentBatchEntity.setFees(cell.getNumericCellValue());
                } else if (index2 == 2) {
                    studentBatchEntity.setStudentId((long) cell.getNumericCellValue());
                } else if (index2 == 3) {
                    studentBatchEntity.setBatchId((long) cell.getNumericCellValue());
                } else {
                    System.err.println("data not found");
                }
            }
            //adding objects to list
            studentBatchEntityList.add(studentBatchEntity);
        }
    }
}
