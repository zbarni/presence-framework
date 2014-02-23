package com.csipsimple.components;

import com.csipsimple.utils.Log;

import android.content.Context;

public class Component {
	private static final String THIS_FILE = "COMPONENT";
	
	public enum ComponentStatus {
		UNAVAILABLE,
		AVAILABLE,
		ON,
		OFF;
	}
	
	protected String mId;
	protected String mName;
	protected String mProfile;
	
	protected Context mContext;	
	
	protected ComponentStatus status;
	
	// only read sensor data if this is true
	protected boolean readSensor = false;
	
	protected android.hardware.SensorManager mSensorManager;
	protected android.hardware.Sensor mSensor;
	
	public Component(Context context, String id, String name) {
		Log.d(THIS_FILE,"Component constructor");
		this.mId = id;
		this.mName = name;
		this.mContext = context;
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

}