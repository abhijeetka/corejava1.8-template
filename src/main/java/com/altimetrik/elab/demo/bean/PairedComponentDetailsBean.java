package com.altimetrik.elab.demo.bean;

import java.util.List;

/**
 * @author skondapalli
 */
public class PairedComponentDetailsBean extends ComponentDetailsBean {

	private List<ComponentDetailsBean> pairedComponentDetails;

	public PairedComponentDetailsBean() {
		super();
	}

	public PairedComponentDetailsBean(final String componentName, final String componentIdentifier) {
		super(componentName, componentIdentifier);
	}

	public PairedComponentDetailsBean(final String componentName, final String componentIdentifier,
			final List<ComponentDetailsBean> pairedComponentDetails) {
		super(componentName, componentIdentifier);
		this.pairedComponentDetails = pairedComponentDetails;
	}

	public PairedComponentDetailsBean(final ComponentDetailsBean componentDetails,
			final List<ComponentDetailsBean> pairedComponentDetails) {
		super(componentDetails.getComponentName(), componentDetails.getComponentIdentifier());
		this.pairedComponentDetails = pairedComponentDetails;
	}

	public List<ComponentDetailsBean> getPairedComponentDetails() {
		return this.pairedComponentDetails;
	}

	public void setPairedComponentDetails(List<ComponentDetailsBean> pairedComponentDetails) {
		this.pairedComponentDetails = pairedComponentDetails;
	}

}
