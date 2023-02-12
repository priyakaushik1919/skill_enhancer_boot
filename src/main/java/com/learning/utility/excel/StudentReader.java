package com.learning.utility.excel;

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
public class StudentReader {

    public List<StudentEntity> getStudentObjects(InputStream inputStream) {

        List<StudentEntity> studentEntityList = new ArrayList<>();
        try {

            //to set the file path of Excel file
            //FileInputStream file = new FileInputStream(new File(".\\resources\\student-data.xlsx"));

            //creating object of XSSFWorkbook to open Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            //getting sheet at which my data is present
            XSSFSheet sheet = workbook.getSheetAt(1);//starts with 0

            getStudentList(sheet, studentEntityList);//private method to get student list

            inputStream.close();//closing the workbook
        } catch (Exception e) {
            e.printStackTrace();
        }
        return studentEntityList;
    }

    private static void getStudentList(XSSFSheet sheet, List<StudentEntity> studentEntityList) {
        //loop iterating through rows
        for (int index = sheet.getFirstRowNum() + 1; index <= sheet.getLastRowNum(); index++) {
            //get row by passing index
            Row row = sheet.getRow(index);

            StudentEntity studentEntity = new StudentEntity();

            //loop iterating through columns
            for (int index2 = row.getFirstCellNum(); index2 < row.getLastCellNum(); index2++) {
                //get cell by passing index
                Cell cell = row.getCell(index2);
                if (index2 == 0) {
                    studentEntity.setId((long) cell.getNumericCellValue());// getting cell type for numeric value
                } else if (index2 == 1) {
                    studentEntity.setName(cell.getStringCellValue());// getting cell type for string value
                } else if (index2 == 2) {
                    studentEntity.setContactDetails((long) cell.getNumericCellValue());
                } else if (index2 == 3) {
                    studentEntity.setQualification(cell.getStringCellValue());
                } else if (index2 == 4) {
                    studentEntity.setEmail(cell.getStringCellValue());
                } else {
                    System.err.println("data not found");
                }
            }
            //adding objects to list
            studentEntityList.add(studentEntity);
        }
    }
}
