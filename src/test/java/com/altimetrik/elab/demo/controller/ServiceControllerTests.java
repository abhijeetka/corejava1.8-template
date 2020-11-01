package com.altimetrik.elab.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.altimetrik.elab.demo.bean.ComponentDetailsBean;
import com.altimetrik.elab.demo.bean.PairedComponentDetailsBean;
import com.altimetrik.elab.demo.service.ComponentDetailsService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author skondapalli
 */
@RunWith(SpringRunner.class)
public class ServiceControllerTests {

	private Gson gson;

	private MockMvc mockMvc;

	@Mock
	private ComponentDetailsService componentDetailsService;

	@InjectMocks
	private ServiceController serviceController;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(this.serviceController).build();
		gson = new GsonBuilder().serializeNulls().create();
	}

	private PairedComponentDetailsBean mockPairedComponentDetails() {
		final List<ComponentDetailsBean> componentDetails = new ArrayList<ComponentDetailsBean>(1);
		final PairedComponentDetailsBean pairedComponentDetails = new PairedComponentDetailsBean("componentName", "componentIdentifier");
		pairedComponentDetails.setPairedComponentDetails(componentDetails);
		return pairedComponentDetails;
	}

	@Test
	public void findAll_Success() throws Exception {
		Mockito.when(this.componentDetailsService.findAll(Mockito.any())).thenReturn(this.mockPairedComponentDetails());
		final MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/service").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		JSONAssert.assertEquals(this.gson.toJson(this.mockPairedComponentDetails()), response.getContentAsString(), true);
	}

}
