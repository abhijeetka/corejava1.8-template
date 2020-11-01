package com.altimetrik.elab.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altimetrik.elab.demo.bean.PairedComponentDetailsBean;
import com.altimetrik.elab.demo.service.ComponentDetailsService;

/**
 * @author skondapalli
 */
@RestController
@RequestMapping(value = "/service")
public class ServiceController {

	protected static Logger logger = LoggerFactory.getLogger(ServiceController.class.getName());

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ComponentDetailsService componentDetailsService;

	@GetMapping
	public PairedComponentDetailsBean findAll() {
		return this.componentDetailsService.findAll(this.applicationName);
	}

}
