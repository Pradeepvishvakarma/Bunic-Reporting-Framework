package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.collection.model.Column;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerService.class);

    public void createExcel() throws Exception {
        var data = getData();
        var columns = getColumns();
        generateExcel(data, columns);
        LOGGER.info("Excel created");

    }

    private List<DBObject> getData(){
        var data = new ArrayList<DBObject>();
        var dbObject1 = new BasicDBObject();
        dbObject1.put("region","ASIA");
        dbObject1.put("country","INDIA");
        dbObject1.put("totalPnl",500);
        dbObject1.put("irdl",1656776);
        dbObject1.put("fxdl",656);
        var dbObject2 = new BasicDBObject();
        dbObject2.put("region","ASIA");
        dbObject2.put("country","INDIA");
        dbObject2.put("totalPnl",400);
        dbObject2.put("irdl",556443);
        dbObject2.put("fxdl",9956);
//        var dbObject3 = new BasicDBObject();
//        dbObject3.put("totalPnl",500);
//        var dbObject4 = new BasicDBObject();
//        dbObject4.put("irdl",656);
//        var dbObject5 = new BasicDBObject();
//        dbObject5.put("fxdl",-90145);
        data.add(dbObject1);
        data.add(dbObject2);
//        data.add(dbObject3);
//        data.add(dbObject4);
//        data.add(dbObject5);
        return data;
    }

    private List<Column> getColumns(){
        var columns = new ArrayList<Column>();
        var column1 = new Column();
        column1.setField("region");
        column1.setTitle("Region");
        var column2 = new Column();
        column2.setField("country");
        column2.setTitle("Country");
        var column3 = new Column();
        column3.setField("totalPnl");
        column3.setTitle("Total Pnl");
        column3.setFormat("K");
        var column4 = new Column();
        column4.setField("irdl");
        column4.setTitle("IRDL");
        column4.setFormat("MM");
        var column5 = new Column();
        column5.setField("fxdl");
        column5.setTitle("FXDL");

        columns.add(column1);
        columns.add(column2);
        columns.add(column3);
        columns.add(column4);
        columns.add(column5);
        return columns;
    }

    public void generateExcel(List<DBObject> data, List<Column> columns) throws Exception {
        String filePath = "C:\\Workspace\\Development\\Repositories\\reporting-framework\\output.xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i).getTitle());
        }

        // Populate data rows
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            DBObject dbObject = data.get(rowIndex);

            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                Column column = columns.get(colIndex);
                Object value = dbObject.get(column.getField());
                Cell cell = row.createCell(colIndex);

                if (value instanceof Number) {
                    double numVal = ((Number) value).doubleValue();
                    String format = column.getFormat();

                    // Apply formatting if specified
                    if ("K".equalsIgnoreCase(format)) {
                        numVal = numVal / 1_000;
                    } else if ("MM".equalsIgnoreCase(format)) {
                        numVal = numVal / 1_000_000;
                    }

                    cell.setCellValue(numVal);
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                } else {
                    cell.setBlank();
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < columns.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }

        workbook.close();
    }
}
