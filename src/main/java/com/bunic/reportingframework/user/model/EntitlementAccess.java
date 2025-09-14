package com.bunic.reportingframework.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntitlementAccess {
    private List<String> region_level;
    private List<String> country_level;
}
