package com.csipsimple.components;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;

import com.csipsimple.utils.Log;

public class Component {
	private static final String THIS_FILE = "CMP BASE";
	public static final String SENSOR = "sensor";
	public static final String ACTUATOR = "actuator";
	
	private static ComponentManager componentManager;
	private String mComponentType;
	private Element mComponentElement;
	
	public enum ComponentStatus {
		UNAVAILABLE,
		AVAILABLE, // component is there but is NOT active
		ON // component is there and it IS active 
	}
	
	protected String mId;
	protected String mName;
	protected String mProfile = "http://google.com";
	
	protected ComponentStatus status;
	// only read sensor data if this is true
	protected boolean readSensor = false;
	
	protected ComponentManager getComponentManager() {
		return componentManager;
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmProfile() {
		return mProfile;
	}

	public void setProfile(String mProfile) {
		this.mProfile = mProfile;
	}
	
	public Component(String id, String name, String type, ComponentManager cm) {
		Log.d(THIS_FILE,"Component constructor");
		this.mId = id;
		this.mName = name;
		this.mComponentType = type;
		this.mComponentElement = null;
		Component.componentManager = cm;
	}
	
	public ComponentStatus getStatus() {
		return status;
	}

	public void setStatus(ComponentStatus status) {
		this.status = status;
	}

	public Context getContext() {
		return componentManager.getContext();
	}

	public Element getComponentElement(Document document) {
		if (this.mComponentElement == null) {
			this.mComponentElement = document.createElement((this.mComponentType == ACTUATOR) ? ACTUATOR : SENSOR);
			this.mComponentElement.setAttribute("id", this.mId);
			Element name = document.createElement("name");
			Element profile = document.createElement("profile");
			name.setTextContent(this.mName);
			profile.setTextContent("http://temporary-profile-link");
			this.mComponentElement.appendChild(name);
			this.mComponentElement.appendChild(profile);
		}
		return (this.getStatus() == ComponentStatus.AVAILABLE || this.getStatus() == ComponentStatus.ON)  ? this.mComponentElement : null;
	}
	
	public String getType() {
		return this.mComponentType;
	}
}