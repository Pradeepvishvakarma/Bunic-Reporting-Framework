package com.bunic.reportingframework.collection.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bunic.reportingframework.collection.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/bunic")
public class CollectionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionController.class);

	@Autowired
	CollectionService collectionService;

	@GetMapping("/hello")
	public String helloWorld() {
		return "Welcome to hello World";
	}

	@PostMapping("/collections/configure")
	@ResponseBody
	@Operation(summary = "configure reports metadata")
	public String configureMetadataAndViews() {
		try {
			collectionService.configureMetadataAndViews();
			return "metadata configured successfully";
		} catch (Exception e) {
			LOGGER.error("Error while configuration metadata");
			return "Error while configuration metadata";
		}

	}
}
