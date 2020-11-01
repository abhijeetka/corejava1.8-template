package com.altimetrik.elab.demo.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.junit4.SpringRunner;

import com.altimetrik.elab.demo.bean.ComponentDetailsBean;
import com.altimetrik.elab.demo.bean.PairedComponentDetailsBean;
import com.altimetrik.elab.demo.entity.ComponentDetailsEntity;
import com.altimetrik.elab.demo.repository.ComponentDetailsRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author skondapalli
 */
@RunWith(SpringRunner.class)
public class ComponentDetailsServiceImplTests {

	private Gson gson;

	@Mock
	private ComponentDetailsRepository componentDetailsRepository;

	@InjectMocks
	private ComponentDetailsServiceImpl componentDetailsService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		gson = new GsonBuilder().serializeNulls().create();
	}

	private ComponentDetailsBean mockComponentDetails() {
		final ComponentDetailsBean componentDetails = new ComponentDetailsBean("componentName", "componentIdentifier");
		return componentDetails;
	}

	private List<ComponentDetailsBean> mockPairedComponentDetails() {
		final ComponentDetailsBean pairedComponentDetails = new ComponentDetailsBean("pairedComponentName", "pairedComponentIdentifier");
		return Arrays.asList(pairedComponentDetails);
	}

	private PairedComponentDetailsBean mockPairedComponentDetailsResponse() {
		final List<ComponentDetailsBean> componentDetails = new ArrayList<ComponentDetailsBean>(1);
		componentDetails.add(new ComponentDetailsBean("pairedComponentName", "pairedComponentIdentifier"));
		final PairedComponentDetailsBean pairedComponentDetails = new PairedComponentDetailsBean("componentName", "componentIdentifier");
		pairedComponentDetails.setPairedComponentDetails(componentDetails);
		return pairedComponentDetails;
	}

	@Test
	public void findAll_Success() throws Exception {
		Mockito.when(this.componentDetailsRepository.getByComponentName(Mockito.any())).thenReturn(this.mockComponentDetails());
		Mockito.when(this.componentDetailsRepository.getByComponentNameNotIn(Mockito.any())).thenReturn(this.mockPairedComponentDetails());
		JSONAssert.assertEquals(this.gson.toJson(this.mockPairedComponentDetailsResponse()), this.gson.toJson(this.componentDetailsService.findAll(Mockito.any())), true);
	}

	@Test
	public void createComponentDetails_Success() throws Exception {
		Mockito.when(this.componentDetailsRepository.findByComponentName(Mockito.any())).thenReturn(null);
		Mockito.when(this.componentDetailsRepository.save(Mockito.any())).thenReturn(new ComponentDetailsEntity());
		Assert.assertEquals(true, this.componentDetailsService.createComponentDetails(Mockito.any()));
	}

	@Test
	public void createExistingComponentDetails_Success() throws Exception {
		Mockito.when(this.componentDetailsRepository.findByComponentName(Mockito.any())).thenReturn(new ComponentDetailsEntity());
		Mockito.when(this.componentDetailsRepository.save(Mockito.any())).thenReturn(new ComponentDetailsEntity());
		Assert.assertEquals(true, this.componentDetailsService.createComponentDetails(Mockito.any()));
	}

	@Test
	public void regenerateComponentIdentifier_Success() throws Exception {
		Mockito.when(this.componentDetailsRepository.findByComponentName(Mockito.any())).thenReturn(new ComponentDetailsEntity());
		Mockito.when(this.componentDetailsRepository.save(Mockito.any())).thenReturn(new ComponentDetailsEntity());
		this.componentDetailsService.regenerateComponentIdentifier();
		Assert.assertTrue(true);
	}

}
