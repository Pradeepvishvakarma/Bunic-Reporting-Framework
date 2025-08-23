package com.bunic.reportingframework.collection.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.bunic.reportingframework.collection.model.Metadata;
//import com.bunic.reportingframework.collection.repository.CollectionRepo;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CollectionService {

//	@Autowired
//	CollectionRepo collectionRepo;
//
//	@Autowired
//	private JdbcTemplate jdbcTemplate;

	public void configureMetadataAndViews() throws Exception {
		// Delete existing table if it exists
//		jdbcTemplate.execute("DROP TABLE IF EXISTS REPORTINGFRAMEWORKMETADATA");
//		// Create table
//		jdbcTemplate.execute(
//				"CREATE TABLE REPORTINGFRAMEWORKMETADATA (id INT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(255), name VARCHAR(255), description VARCHAR(255))");
		// Read JSON file
		ObjectMapper mapper = new ObjectMapper();
		ClassPathResource resource = new ClassPathResource("collections.initialize/ReportingFrameworkMetadata.json");
		InputStream inputStream = resource.getInputStream();
		Metadata[] metadataArray = mapper.readValue(inputStream, Metadata[].class);
		// Store data in the database
		List<Metadata> metadataList = Arrays.asList(metadataArray);
//		collectionRepo.saveAll(metadataList);
	}
}
