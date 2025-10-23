package com.bunic.reportingframework.collection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRow {

    private List<GroupColumn> cols;
}
