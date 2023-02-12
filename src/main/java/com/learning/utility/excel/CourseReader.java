package com.learning.utility.excel;

import com.learning.entity.CourseEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class CourseReader {

    public List<CourseEntity> getCourseObjects(InputStream inputStream) {

        List<CourseEntity> courseEntityList = new ArrayList<>();
        try {

            //to set the file path of Excel file
            //FileInputStream file = new FileInputStream(new File(".\\resources\\course-data.xlsx"));

            //creating object of XSSFWorkbook to open Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            //getting sheet at which my data is present
            XSSFSheet sheet = workbook.getSheetAt(3);//starts with 0

            getCourseList(sheet, courseEntityList);//private method to get course list

            inputStream.close();//closing the workbook
        } catch (Exception e) {
            e.printStackTrace();
        }
        return courseEntityList;
    }

    private static void getCourseList(XSSFSheet sheet, List<CourseEntity> courseEntityList) {
        //loop iterating through rows
        for (int index = sheet.getFirstRowNum() + 1; index <= sheet.getLastRowNum(); index++) {
            //get row by passing index
            Row row = sheet.getRow(index);

            CourseEntity courseEntity = new CourseEntity();

            //loop iterating through columns
            for (int index2 = row.getFirstCellNum(); index2 < row.getLastCellNum(); index2++) {
                //get cell by passing index
                Cell cell = row.getCell(index2);
                if (index2 == 0) {
                    courseEntity.setId((long) cell.getNumericCellValue());// getting cell type for numeric value
                } else if (index2 == 1) {
                    courseEntity.setName(cell.getStringCellValue());// getting cell type for string value
                } else if (index2 == 2) {
                    courseEntity.setCurriculum(cell.getStringCellValue());
                } else if (index2 == 3) {
                    courseEntity.setDuration(cell.getStringCellValue());
                } else {
                    System.err.println("data not found");
                }
            }
            //adding objects to list
            courseEntityList.add(courseEntity);
        }
    }
}
