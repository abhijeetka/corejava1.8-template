package com.altimetrik.elab.demo.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author skondapalli
 */
@RunWith(SpringRunner.class)
public class DefaultControllerTests {

	private Gson gson;

	private MockMvc mockMvc;

	@InjectMocks
	private DefaultController defaultController;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(this.defaultController).build();
		gson = new GsonBuilder().serializeNulls().create();
	}

	private String mockDefaultResponse() {
		return "Hello, Welcome to Engineering Lab! Start editing to see some magic happen :)";
	}

	@Test
	public void getDefault_Success() throws Exception {
		final MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		Assert.assertEquals(this.mockDefaultResponse(), response.getContentAsString());
	}

}
