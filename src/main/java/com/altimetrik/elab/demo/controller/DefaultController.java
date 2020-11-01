package com.altimetrik.elab.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author skondapalli
 */
@RestController
@RequestMapping(value = "/")
@CrossOrigin
public class DefaultController {

	@GetMapping
	public ResponseEntity<String> getDefault() {
		return new ResponseEntity<String>("Hello, Welcome to Engineering Lab! Start editing to see some magic happen :)", HttpStatus.OK);
	}

}
