package com.bunic.reportingframework.collection.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.bunic.reportingframework.collection.dao.CollectionDao;
import com.bunic.reportingframework.exception.BunicException;
import com.bunic.reportingframework.exception.BunicInvalidRequestException;
import com.bunic.reportingframework.exception.BunicUnauthorizedException;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
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

    @Autowired
    UserService userService;

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

    public Metadata getMetadataByCode(User user, String reportCode) throws BunicException {
        var metadataList = getMetadataList();
        var metadata = metadataList.stream().filter(entry -> reportCode.equalsIgnoreCase(entry.getCode())).findFirst().orElse(null);
        if(metadata == null){
            throw new BunicInvalidRequestException("Metadata not found for report: " + reportCode);
        }
        hasAccess(user, metadata);
        LOGGER.info("report: {} - metadata: {}", reportCode, metadata);
        return metadata;
    }

    public Metadata getMetadataByCode(String reportCode) throws BunicException {
        var metadataList = getMetadataList();
        var metadata = metadataList.stream().filter(entry -> reportCode.equalsIgnoreCase(entry.getCode())).findFirst().orElse(null);
        if(metadata == null){
            throw new BunicInvalidRequestException("Metadata not found for report: " + reportCode);
        }
        LOGGER.info("report: {} - metadata: {}", reportCode, metadata);
        return metadata;
    }

    public List<Metadata> getMetadataList(){
        return collectionDao.getAllMetadata();
    }

    public void hasAccess(User user, Metadata metadata) throws BunicUnauthorizedException {
        if(user == null){
            throw new BunicUnauthorizedException("request is not authorised");
        }
        var specifiedUsers = metadata.getUsers();
        if (specifiedUsers != null && !specifiedUsers.isEmpty() && !specifiedUsers.contains(user.getUserId())) {
            throw new BunicUnauthorizedException("User is not authorized to access this metadata");
        }
    }
}
