package com.csipsimple.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.csipsimple.components.AbstractComponent.ComponentStatus;
import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.service.ComponentService;
import com.csipsimple.utils.Log;

@SuppressLint("NewApi")
public class ComponentManager {
	public static final String THIS_FILE = "COMP MGR";

	// Service connection
	private Context mContext = null;
	private static ComponentManager mInstance = null;
	private ComponentService mComponentService = null;

	// Sensors
	public Proximity proximity;
	public LocationTracker location;
	public Headset headset;
	public Accelerometer accelerometer;

	// Actuators	
	public Vibrator vibrator;
	public Loudspeaker loudspeaker; 

	public Context getContext() {
		return this.mContext;
	}
	
	/**
	 * Returns an instance of ComponentManager class.
	 * @return class instance
	 */
	public static ComponentManager getInstance() {
		if (mInstance == null) {
			mInstance = new ComponentManager();
			return mInstance;
		} 
		else {
			return mInstance;
		}
	}
	
	// empty constructor
	public ComponentManager() {	}
	
	/**
	 * This must be called the first time this class is created, otherwise 
	 * it won't have access to PublicationManager and SubscriptionManager
	 * classes and their functions. 
	 * @param service
	 */
	public void initManager (ComponentService service) {
		this.mContext = (Context) service.getSipService();
		this.mComponentService = service;
		initComponents();
	}

	/**
	 * 
	 */
	public void initComponents() {
		proximity = new Proximity(ComponentProfile.ID_PROXIMITY,
				"Proximity", 
				ComponentProfile.SENSOR,
				Components.PROXIMITY);
		
		headset = new Headset(ComponentProfile.ID_HEADSET_WIRED,
				"Wired headset", 
				ComponentProfile.SENSOR,
				Components.HEADSET);
		
		location = new LocationTracker(ComponentProfile.ID_GPS_RECEIVER,
				"GPS receiver",
				ComponentProfile.SENSOR, 
				Components.GPS_RECEIVER);
		
		accelerometer = new Accelerometer(ComponentProfile.ID_ACCELEROMETER,
				"Accelerometer",
				ComponentProfile.SENSOR, 
				Components.ACCELEROMETER);

		vibrator = new Vibrator(ComponentProfile.ID_VIBRATOR,
				"Vibrator", 
				ComponentProfile.ACTUATOR,
				Components.VIBRATOR);
		
		Log.d(THIS_FILE,"@XML Components have been initialized!");
		
//		loudspeaker = new Loudspeaker("built.in.loudspeaker","Built in loudspeaker", ComponentProfile.ACTUATOR, this);
	}
	
	public void onComponentChange(AbstractComponent component) {
		mComponentService.getPublicationManager().updateComponentModel(component);
	}

	public AbstractComponent getAbstractComponent(Components c) {
		switch (c) {
		case ACCELEROMETER:
			return accelerometer;
		case GPS_RECEIVER:
			return location;
		case HEADSET:
			return headset.getStatus() == ComponentStatus.UNAVAILABLE ? null : headset;
		case VIBRATOR:
			return vibrator;
		case PROXIMITY:
			return proximity;
		case LOUDSPEAKER:
			return loudspeaker;
		
		default:
			return null;
		}
	}

	/**
	 * Gets device manufacturer and model number.
	 * @return string
	 */
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	} 
}
