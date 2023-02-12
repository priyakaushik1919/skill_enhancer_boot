package com.learning.utility.excel;

import com.learning.entity.CourseEntity;
import com.learning.entity.TimeSlotEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class TimeSlotReader {

    public List<TimeSlotEntity> getTimeSlotObjects(InputStream inputStream) {

        List<TimeSlotEntity> timeSlotEntityList = new ArrayList<>();
        try {

            //to set the file path of Excel file
            //FileInputStream file = new FileInputStream(new File(".\\resources\\timeSlot-data.xlsx"));

            //creating object of XSSFWorkbook to open Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            //getting sheet at which my data is present
            XSSFSheet sheet = workbook.getSheetAt(4);//starts with 0

            getTimeSlotList(sheet, timeSlotEntityList);//private method to get timeSlot list

            inputStream.close();//closing the workbook
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeSlotEntityList;
    }

    private static void getTimeSlotList(XSSFSheet sheet, List<TimeSlotEntity> timeSlotEntityList) {
        //loop iterating through rows
        for (int index = sheet.getFirstRowNum() + 1; index <= sheet.getLastRowNum(); index++) {
            //get row by passing index
            Row row = sheet.getRow(index);

            TimeSlotEntity timeSlotEntity = new TimeSlotEntity();

            //loop iterating through columns
            for (int index2 = row.getFirstCellNum(); index2 < row.getLastCellNum(); index2++) {
                //get cell by passing index
                Cell cell = row.getCell(index2);
                if (index2 == 0) {
                    timeSlotEntity.setId((long) cell.getNumericCellValue());// getting cell type for numeric value
                } else if (index2 == 1) {
                    timeSlotEntity.setStartTime(cell.getLocalDateTimeCellValue().toLocalTime());
                } else if (index2 == 2) {
                    timeSlotEntity.setEndTime(cell.getLocalDateTimeCellValue().toLocalTime());
                } else if (index2 == 3) {
                    timeSlotEntity.setTrainerId((long) cell.getNumericCellValue());
                } else {
                    System.err.println("data not found");
                }
            }
            //adding objects to list
            timeSlotEntityList.add(timeSlotEntity);
        }
    }
}
