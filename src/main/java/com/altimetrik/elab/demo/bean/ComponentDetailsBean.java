package com.altimetrik.elab.demo.bean;

/**
 * @author skondapalli
 */
public class ComponentDetailsBean {

	private String componentName;
	private String componentIdentifier;

	public ComponentDetailsBean() {
		super();
	}

	public ComponentDetailsBean(final String componentName, final String componentIdentifier) {
		super();
		this.componentName = componentName;
		this.componentIdentifier = componentIdentifier;
	}

	public String getComponentName() {
		return this.componentName;
	}

	public void setComponentName(final String componentName) {
		this.componentName = componentName;
	}

	public String getComponentIdentifier() {
		return this.componentIdentifier;
	}

	public void setComponentIdentifier(final String componentIdentifier) {
		this.componentIdentifier = componentIdentifier;
	}

	@Override
	public String toString() {
		return "ComponentDetailsBean [componentName=" + this.componentName + ", componentIdentifier="
				+ this.componentIdentifier + "]";
	}

}
