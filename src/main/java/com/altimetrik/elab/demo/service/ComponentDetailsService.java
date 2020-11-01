package com.altimetrik.elab.demo.service;

import org.springframework.stereotype.Service;

import com.altimetrik.elab.demo.bean.PairedComponentDetailsBean;

/**
 * @author skondapalli
 */
@Service
public interface ComponentDetailsService {

	boolean createComponentDetails(final String applicationName);

	PairedComponentDetailsBean findAll(final String applicationName);

}
