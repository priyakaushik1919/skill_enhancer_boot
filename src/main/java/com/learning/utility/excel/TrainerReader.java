package com.learning.utility.excel;
import com.learning.entity.StudentEntity;
import com.learning.entity.TrainerEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class TrainerReader {

    public List<TrainerEntity> getTrainerObjects(InputStream inputStream) {

        List<TrainerEntity> trainerEntityList = new ArrayList<>();
        try {

            //to set the file path of Excel file
            //FileInputStream file = new FileInputStream(new File(".\\resources\\trainerdata.xlsx"));

            //creating object of XSSFWorkbook to open Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            //getting sheet at which my data is present
            XSSFSheet sheet = workbook.getSheetAt(2);//starts with 0

            getTrainerList(sheet, trainerEntityList);//private method to get trainer list

            inputStream.close();//closing the workbook
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trainerEntityList;
    }

    private static void getTrainerList(XSSFSheet sheet, List<TrainerEntity> trainerEntityList) {
        //loop iterating through rows
        for (int index = sheet.getFirstRowNum() + 1; index <= sheet.getLastRowNum(); index++) {
            //get row by passing index
            Row row = sheet.getRow(index);

            TrainerEntity trainerEntity = new TrainerEntity();

            //loop iterating through columns
            for (int index2 = row.getFirstCellNum(); index2 < row.getLastCellNum(); index2++) {
                //get cell by passing index
                Cell cell = row.getCell(index2);
                if (index2 == 0) {
                    trainerEntity.setId((long) cell.getNumericCellValue());// getting cell type for numeric value
                } else if (index2 == 1) {
                    trainerEntity.setName(cell.getStringCellValue());// getting cell type for string value
                }  else if (index2 ==2 ) {
                    trainerEntity.setSpecialization(cell.getStringCellValue());
                } else {
                    System.err.println("data not found");
                }
            }
            //adding objects to list
            trainerEntityList.add(trainerEntity);
        }
    }
}
