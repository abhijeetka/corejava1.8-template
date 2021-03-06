package com.altimetrik.elab.demo.service.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.altimetrik.elab.demo.bean.PairedComponentDetailsBean;
import com.altimetrik.elab.demo.entity.ComponentDetailsEntity;
import com.altimetrik.elab.demo.repository.ComponentDetailsRepository;
import com.altimetrik.elab.demo.service.ComponentDetailsService;

/**
 * @author skondapalli
 */
@Service
public class ComponentDetailsServiceImpl implements ComponentDetailsService {

	protected static Logger logger = LoggerFactory.getLogger(ComponentDetailsServiceImpl.class.getName());

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ComponentDetailsRepository componentDetailsRepository;

	@Override
	public PairedComponentDetailsBean findAll(final String applicationName) {
		return new PairedComponentDetailsBean(this.componentDetailsRepository.getByComponentName(applicationName),
				this.componentDetailsRepository.getByComponentNameNotIn(applicationName));
	}

	@Override
	public boolean createComponentDetails(final String applicationName) {
		if (this.componentDetailsRepository.findByComponentName(applicationName) == null) {
			this.componentDetailsRepository
					.save(new ComponentDetailsEntity(applicationName, UUID.randomUUID().toString()));
		}
		return true;
	}

	@Scheduled(cron = "${cron.component.identifier.reg-ex}")
	public void regenerateComponentIdentifier() {
		final ComponentDetailsEntity componentDetails = this.componentDetailsRepository
				.findByComponentName(this.applicationName);
		componentDetails.setComponentIdentifier(UUID.randomUUID().toString());
		this.componentDetailsRepository.save(componentDetails);
	}

}
