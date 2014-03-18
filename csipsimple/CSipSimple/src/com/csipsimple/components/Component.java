package com.csipsimple.components;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;

import com.csipsimple.utils.Log;

public class Component {
	private static final String THIS_FILE = "CMP BASE";
	public static final String SENSOR = "SENSOR";
	public static final String ACTUATOR = "ACTUATOR";
	
	private String mComponentType;
	
	public enum ComponentStatus {
		UNAVAILABLE,
		AVAILABLE,
		ON,
		OFF;
	}
	
	protected String mId;
	public String getmId() {
		return mId;
	}

	public void setmId(String mId) {
		this.mId = mId;
	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmProfile() {
		return mProfile;
	}

	public void setmProfile(String mProfile) {
		this.mProfile = mProfile;
	}

	protected String mName;
	protected String mProfile = "http://google.com";
	
	protected Context mContext;	
	
	protected ComponentStatus status;
	
	// only read sensor data if this is true
	protected boolean readSensor = false;
	
	protected android.hardware.SensorManager mSensorManager;
	protected android.hardware.Sensor mSensor;
	
	public Component(Context context, String id, String name, String type) {
		Log.d(THIS_FILE,"Component constructor");
		this.mId = id;
		this.mName = name;
		this.mContext = context;
		this.mComponentType = type;
	}
	
	public ComponentStatus getStatus() {
		return status;
	}

	public void setStatus(ComponentStatus status) {
		this.status = status;
	}

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context mContext) {
		this.mContext = mContext;
	}
	
	public String getComponentXml() {
		String xml;
		
//		Document presenceDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//		Element root;
//		if (this.mComponentType == ACTUATOR) {
//			root = presenceDoc.createElement("actuator");
//		}
//		else {
//			root = presenceDoc.createElement("sensor");
//		}
//		root.setAttribute("id", this.mId);
//		Element name = presenceDoc.createElement("name");
//		
//		Element os_version = presenceDoc.createElement(XML_OS_VERSION);
//		
		xml = (this.mComponentType == ACTUATOR) ? "<actuator" : "<sensor";
		// add id attribute
		xml += " id=\"" + this.mId + "\">";
		xml += "<name>" + this.mName + "</name>";
		xml += "<profile>" + this.mProfile + "</profile>";
		xml += (this.mComponentType == ACTUATOR) ? "</actuator>" : "</sensor>";
		return xml;
	}

}