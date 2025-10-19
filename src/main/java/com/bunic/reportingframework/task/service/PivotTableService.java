package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.collection.model.Column;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.collection.model.PivotConfig;
import com.bunic.reportingframework.task.model.*;
import com.mongodb.DBObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.bunic.reportingframework.common.constant.Constant.*;

@Service
public class PivotTableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PivotTableService.class);

    public ReportDataResponse getReportDataResponse(Metadata metadata, Task task, PivotConfig pivotConfig, List<DBObject> data){
        if(pivotConfig != null){
            return getPivotDataResponse(data, pivotConfig, metadata);
        } else {
            return getNonPivotDataResponse(data, metadata);
        }
    }

    private ReportDataResponse getPivotDataResponse(List<DBObject> data, PivotConfig pivotConfig, Metadata metadata){
        var reportDataResponse = new ReportDataResponse();

        var columns = metadata.getColumns().stream().filter(column -> !column.isHidden()).toList();
        reportDataResponse.setColumns(columns);

        var pivotTableContext = new PivotTableContext(data, columns, pivotConfig);
        var pivotData = applyPivoting(pivotTableContext);
        reportDataResponse.setPivotedData(pivotData);
        return reportDataResponse;
    }

    private List<PivotData> applyPivoting(PivotTableContext context) {
        var pivotDataList = new ArrayList<PivotData>();
        var rowGroup = context.getRowGroup();
        for (int i = rowGroup.size(); i > 0; i--) {
            var groupingBy = context.getRowGroup().subList(0, i);
            var groupByCount = context.getDbObjects().stream().sorted(sort(context, groupingBy)).collect(Collectors.groupingBy(dbObject -> groupingBy.stream()
                    .map(groupBy -> (String) dbObject.get(groupBy)).collect(Collectors.joining("~")), LinkedHashMap::new, Collectors.counting()));

            groupByCount.keySet().stream().forEach(key-> {
                var pivotedData = new PivotData();
                pivotedData.setId(key);
                pivotedData.setLevel(groupingBy.size());
                var groupByRow = groupingBy(context.getDbObjects(), context, groupingBy);

                if(context.isColumnGrouping()){

                } else if (context.isPivotDisableRowGrouping()) {

                } else {
                    var rowGroupValues = getRowGroupValues(context, groupingBy);
                    pivotedData.setRowGroupValues(rowGroupValues.get(key));
                    var calculatedBreachDetails = calculateRowGroupingBreachStatus(rowGroupValues.get(key), context);
                    pivotedData.setRowGroupedBreachDetails(calculatedBreachDetails);
                    pivotedData.setBreachDetailsByLevel(BREACH_STRING_FORMAT_DEFAULT);
                }
                pivotDataList.add(pivotedData);
            });

        }
        return pivotDataList;
    }

    private Map<String, String> calculateRowGroupingBreachStatus(Map<String, BigDecimal> rowGroupValues, PivotTableContext context) {
        var breachDetails = new HashMap<String, String>();
        context.getColumns().stream().filter(column -> column.getNumberFormat() != null
                        && column.getNumberFormat().getRule() != null)
                .forEach(column -> {
                    var numberFormat = column.getNumberFormat();
                    var numberFormatRule = numberFormat.getRule();
                    if (numberFormatRule.equalsIgnoreCase(BREACH_TYPE_BREACH_STYLING)) {
                        var breachStatus = getRowGroupingBreachStylingBreachStatus(rowGroupValues, column);
                        breachDetails.put(column.getField(), breachStatus);
                    } else if (column.getType().equalsIgnoreCase(TYPE_NUMBER) && numberFormatRule.equalsIgnoreCase(BREACH_TYPE_POSITIVE_NEGATIVE)) {
                        var breachStatus = getRowGroupingPositiveNegativeBreachStatus(rowGroupValues, column);
                        breachDetails.put(column.getField(), breachStatus);
                    }
                });
        return breachDetails;

    }

    private Map<String, Map<String, BigDecimal>> getRowGroupValues(PivotTableContext context, List<String> groupingBy) {
        return groupingBy(context.getDbObjects(), context, groupingBy).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x-> getNumberValuesToUpdate(x.getValue(), context), (x,y) -> y, LinkedHashMap::new));
    }

    private Map<String, BigDecimal> getNumberValuesToUpdate(List<DBObject> dbObjects, PivotTableContext context) {
        var fieldByNumbers = getColumnByType(context.getColumns(), TYPE_NUMBER).stream().collect(Collectors.toMap(Column::getField, column -> dbObjects.stream().map(dbObject -> dbObject.get(column.getField()) != null ? new BigDecimal(String.valueOf(dbObject.get(column.getField()))) : new BigDecimal("0")).reduce(BigDecimal.ZERO, BigDecimal::add), (x,y) -> y, LinkedHashMap::new));
        return getFormmattedNumber(fieldByNumbers, context);
    }

    private Map<String, BigDecimal> getFormmattedNumber(Map<String, BigDecimal> fieldByNumbers, PivotTableContext context){
        return fieldByNumbers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x-> formatNumber(context, x.getKey(), x.getValue()), (x,y) -> y, LinkedHashMap::new));
    }

    private BigDecimal formatNumber(PivotTableContext context, String field, BigDecimal value){
        var column = getColumnByFieldName(context.getColumns(), field);
        if(column == null || StringUtil.isBlank(column.getFormat()) ||  field != null){
            return value;
        }
        return getFormattedValue(column.getFormat(), value);
    }

    private BigDecimal getFormattedValue(String format, BigDecimal value) {
        return switch (format) {
            case null -> value;
            case "K" -> value.divide(BigDecimal.valueOf(1000)).setScale(0, RoundingMode.HALF_UP);
            case "MM" -> value.divide(BigDecimal.valueOf(1000000)).setScale(0, RoundingMode.HALF_UP);
            default -> value.divide(BigDecimal.valueOf(1)).setScale(0, RoundingMode.HALF_UP);
        };
    }

    private List<Column> getColumnByType(List<Column> columns, String type){
        return columns.stream().filter(column -> type.equalsIgnoreCase(column.getType())).toList();
    }

    private Column getColumnByFieldName(List<Column> columns, String field){
        return columns.stream().filter(column -> field.equalsIgnoreCase(column.getField())).findFirst().orElse(null);
    }

    private Map<String, List<DBObject>> groupingBy(List<DBObject> dbObjects, PivotTableContext context, List<String> groupingBy){
        return dbObjects.stream().sorted(sort(context, groupingBy))
                .collect(Collectors.groupingBy(dbObject -> groupingBy.stream()
                .map(groupBy -> (String) dbObject.get(groupBy)).collect(Collectors.joining("~")), LinkedHashMap::new, Collectors.toList()));

    }

    private Comparator<DBObject> sort(PivotTableContext context, List<String> groupingBy) {
        if(groupingBy.isEmpty()){
            return (dbObject1, dbObject2) -> 0;
        }
        Comparator<DBObject> comparator = Comparator.nullsFirst(sortBy(context, groupingBy.get(0)));
        int i = 1;
        while (i < groupingBy.size()) {
            comparator = comparator.thenComparing(sortBy(context, groupingBy.get(i)));
            i++;
        }
        return comparator;
    }

    private Comparator<DBObject> sortBy(PivotTableContext context, String sortField){
        return (dbObject1, dbObject2) -> StringUtils.compareIgnoreCase((String)dbObject1.get(sortField), (String)dbObject2.get(sortField));
    }

    private ReportDataResponse getNonPivotDataResponse(List<DBObject> data, Metadata metadata){
        var reportDataResponse = new ReportDataResponse();
        var columns = metadata.getColumns().stream().filter(column -> !column.isHidden()).toList();
        reportDataResponse.setColumns(columns);

        var formattedData = getFormattedData(data, metadata);

        var NonPivotData = getNonPivotData(formattedData, metadata);
        reportDataResponse.setNonPivotedData(NonPivotData);

        var grandTotal = new GrandTotal();
        reportDataResponse.setGrandTotal(grandTotal);
        return reportDataResponse;
    }

    private List<DBObject> getFormattedData(List<DBObject> data, Metadata metadata) {
        metadata.getColumns().stream().filter(column -> column != null && column.getType().equalsIgnoreCase(TYPE_NUMBER))
                .forEach(column -> data.stream().forEach(dbObject -> {
                    if (dbObject.containsField(column.getField()) && dbObject.get(column.getField()) != null) {
                        var value = new BigDecimal(dbObject.get(column.getField()).toString());
                        var formattedValue = getFormattedValue(column.getFormat(), value);
                        dbObject.put(column.getField(), formattedValue);
                    }
                }));
        return data;
    }

    private List<NonPivotData> getNonPivotData(List<DBObject> data, Metadata metadata) {
        var nonPivotDataList = new ArrayList<NonPivotData>();
        for (var dbObject : data) {
            var nonPivotData = new NonPivotData();
            nonPivotData.setValues(dbObject);
            nonPivotData.setBreachDetails(calculateBreachDetails(dbObject, metadata));
            nonPivotDataList.add(nonPivotData);
        }
        return nonPivotDataList;
    }

    private Map<String, String> calculateBreachDetails(DBObject dbObject, Metadata metadata) {
        var breachDetails = new HashMap<String, String>();
        metadata.getColumns().stream().filter(column -> column.getNumberFormat() != null
                        && column.getNumberFormat().getRule() != null)
                .forEach(column -> {
                    var numberFormat = column.getNumberFormat();
                    var numberFormatRule = numberFormat.getRule();
                    if (numberFormatRule.equalsIgnoreCase(BREACH_TYPE_BREACH_STYLING)) {
                        var breachStatus = getBreachStylingBreachStatus(dbObject, column);
                        breachDetails.put(column.getField(), breachStatus);
                    } else if (column.getType().equalsIgnoreCase(TYPE_NUMBER) && numberFormatRule.equalsIgnoreCase(BREACH_TYPE_POSITIVE_NEGATIVE)) {
                        var breachStatus = getPositiveNegativeBreachStatus(dbObject, column);
                        breachDetails.put(column.getField(), breachStatus);
                    }
                });
        return breachDetails;
    }

    private String getPositiveNegativeBreachStatus(DBObject dbObject, Column column) {
        if (!dbObject.containsField(column.getField()) || dbObject.get(column.getField()) == null) {
            return BREACH_NUMBER_FORMAT_DEFAULT;
        }
        var value = new BigDecimal(dbObject.get(column.getField()).toString());
        return value.compareTo(BigDecimal.ZERO) >= 0 ? BREACH_NUMBER_FORMAT_GREEN : BREACH_NUMBER_FORMAT_RED;
    }
    private String getRowGroupingPositiveNegativeBreachStatus(Map<String, BigDecimal> rowGroupValue, Column column) {
        if (!rowGroupValue.containsKey(column.getField()) || rowGroupValue.get(column.getField()) == null) {
            return BREACH_NUMBER_FORMAT_DEFAULT;
        }
        var value = new BigDecimal(rowGroupValue.get(column.getField()).toString());
        return value.compareTo(BigDecimal.ZERO) >= 0 ? BREACH_NUMBER_FORMAT_GREEN : BREACH_NUMBER_FORMAT_RED;
    }

    private String getBreachStylingBreachStatus(DBObject dbObject, Column column) {
        if (!dbObject.containsField(column.getField()) || dbObject.get(column.getField()) == null) {
            return BREACH_NUMBER_FORMAT_DEFAULT;
        }
        return BREACH_NUMBER_FORMAT_DEFAULT;
    }

    private String getRowGroupingBreachStylingBreachStatus(Map<String, BigDecimal> rowGroupValue, Column column) {
        if (!rowGroupValue.containsKey(column.getField()) || rowGroupValue.get(column.getField()) == null) {
            return BREACH_NUMBER_FORMAT_DEFAULT;
        }
        return BREACH_NUMBER_FORMAT_DEFAULT;
    }

    public void generateExcel(List<DBObject> data, Metadata metadata, Task task) throws Exception {
        var path = task.getPath();
        System.out.println("path: " + path);
        String excelFilePath = String.format("%s%s", path,"output.xlsx");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < metadata.getColumns().size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(metadata.getColumns().get(i).getTitle());
        }

        // Populate data rows
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            DBObject dbObject = data.get(rowIndex);

            for (int colIndex = 0; colIndex < metadata.getColumns().size(); colIndex++) {
                Column column = metadata.getColumns().get(colIndex);
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
        for (int i = 0; i < metadata.getColumns().size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
            workbook.write(fos);
        }

        workbook.close();
    }

}
