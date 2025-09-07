package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.collection.model.PivotConfig;
import com.bunic.reportingframework.task.model.NonPivotData;
import com.bunic.reportingframework.task.model.PivotData;
import com.bunic.reportingframework.task.model.ReportDataResponse;
import com.bunic.reportingframework.task.model.Task;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PivotTableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PivotTableService.class);

    public ReportDataResponse getReportDataResponse(Metadata metadata, Task task, PivotConfig pivotConfig, List<DBObject> data){
        if(pivotConfig != null){
            return getPivotData(data, pivotConfig, metadata);
        } else {
            return getNonPivotData(data, metadata);
        }
    }

    private ReportDataResponse getPivotData(List<DBObject> data, PivotConfig pivotConfig, Metadata metadata){
        var reportDataResponse = new ReportDataResponse();
        var pivotDataList = new ArrayList<PivotData>();
        var pivotedData = new PivotData();

        pivotDataList.add(pivotedData);
        reportDataResponse.setPivotedData(pivotDataList);
        return reportDataResponse;
    }

    private ReportDataResponse getNonPivotData(List<DBObject> data, Metadata metadata){
        var reportDataResponse = new ReportDataResponse();
        var nonPivotData = new ArrayList<NonPivotData>();
        var NonPivotData = getNonPivotData(data);
        reportDataResponse.setNonPivotedData(NonPivotData);
        return reportDataResponse;
    }

    private List<NonPivotData> getNonPivotData(List<DBObject> data){
        var nonPivotDataList = new ArrayList<NonPivotData>();
            var nonPivotData = new NonPivotData();
            for( var dbObject : data){
                nonPivotData.setValue(dbObject);
                nonPivotDataList.add(nonPivotData);
            }
        return nonPivotDataList;
    }
}
