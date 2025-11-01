package com.bunic.reporting_framework.task.service;

import com.bunic.reportingframework.collection.model.Column;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.collection.model.PivotConfig;
import com.bunic.reportingframework.task.model.ReportDataResponse;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.service.PivotTableService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.*;
        import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;

        import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PivotTableServiceTest {

    @InjectMocks
    private PivotTableService pivotTableService;

    private Metadata metadata;
    private List<DBObject> sampleData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // --- Mock columns ---
        Column col1 = new Column();
        col1.setField("region");
        col1.setTitle("Region");
        col1.setType("string");
        col1.setHidden(false);

        Column col2 = new Column();
        col2.setField("pnl");
        col2.setTitle("PnL");
        col2.setType("number");
        col2.setHidden(false);
        col2.setFormat("K"); // should divide by 1000

        metadata = new Metadata();
        metadata.setColumns(List.of(col1, col2));

        // --- Sample DBObject data ---
        DBObject row1 = new BasicDBObject();
        row1.put("region", "ASIA");
        row1.put("pnl", new BigDecimal("100000"));

        DBObject row2 = new BasicDBObject();
        row2.put("region", "EMEA");
        row2.put("pnl", new BigDecimal("50000"));

        sampleData = new ArrayList<>();
        sampleData.add(row1);
        sampleData.add(row2);
    }

    @Test
    void testGetReportDataResponse_NonPivot() {
        // pivotConfig = null → should go through getNonPivotDataResponse()
        ReportDataResponse response = pivotTableService.getReportDataResponse(metadata, null, sampleData);

        assertThat(response).isNotNull();
        assertThat(response.getColumns()).hasSize(2);
        assertThat(response.getNonPivotedData()).isNotEmpty();
        assertThat(response.getGrandTotal()).isNotNull();
    }

    @Test
    void testGetReportDataResponse_WithPivotConfig() {
        // pivotConfig provided → should go through getPivotDataResponse()
        PivotConfig pivotConfig = new PivotConfig();
        pivotConfig.setRowGroup(List.of("region"));
        pivotConfig.setColumnGroup(Collections.emptyList());

        ReportDataResponse response = pivotTableService.getReportDataResponse(metadata, pivotConfig, sampleData);

        assertThat(response).isNotNull();
        assertThat(response.getColumns()).isNotEmpty();
        assertThat(response.getPivotedData()).isNotNull();
    }

    @Test
    void testGenerateExcel_CreatesFileAndWritesContent() throws Exception {
        // Given
        Map<String, Object> emailTemplateData = new HashMap<>();
        emailTemplateData.put("attachmentFileName", "TestReport");

        Task task = new Task();
        task.setPath(System.getProperty("java.io.tmpdir")); // safe temporary directory

        // When
        pivotTableService.generateExcel(emailTemplateData, sampleData, metadata, task);

        // Then
        File generatedFile = new File(System.getProperty("java.io.tmpdir"), "TestReport.xlsx");
        assertTrue(generatedFile.exists(), "Excel file should be created");

        // Optional: Verify workbook structure
        try (FileInputStream fis = new FileInputStream(generatedFile)) {
            var workbook = WorkbookFactory.create(fis);
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getSheetName()).isEqualTo("Data");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Region");
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("PnL");
        }

        // Cleanup
        Files.deleteIfExists(generatedFile.toPath());
    }
}
