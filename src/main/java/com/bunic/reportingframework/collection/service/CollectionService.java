package com.bunic.reportingframework.collection.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.bunic.reportingframework.collection.controller.CollectionController;
import com.bunic.reportingframework.collection.dao.CollectionDao;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.bunic.reportingframework.collection.model.Metadata;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CollectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionService.class);

    @Autowired
    CollectionDao collectionDao;

    @PostConstruct
    private void init() throws Exception {
        LOGGER.info("Configuring Metadata and Views on Application Startup");
        configureMetadataAndViews();
    }

	public void configureMetadataAndViews() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ClassPathResource resource = new ClassPathResource("collections.initialize/ReportingFrameworkMetadata.json");
		InputStream inputStream = resource.getInputStream();
		Metadata[] metadataArray = mapper.readValue(inputStream, Metadata[].class);
		List<Metadata> metadataList = Arrays.asList(metadataArray);
        collectionDao.SaveAllMetadata(metadataList);
	}
}
