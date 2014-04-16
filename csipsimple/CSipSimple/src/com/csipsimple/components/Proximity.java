package com.csipsimple.components;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.csipsimple.utils.Log;

public class Proximity extends Component implements SensorEventListener {
	private static final String THIS_FILE = "PROXIMITY";
	private android.hardware.SensorManager mSensorManager;
	private android.hardware.Sensor mSensor;
	
	public Proximity(String id, String name, String type, ComponentManager cm) {
		super(id, name, type, cm);
		initialize();
	}
	
	public void initialize() {
		
		mSensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);		
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (mSensor == null) {
			setStatus(ComponentStatus.UNAVAILABLE);
			Log.d(THIS_FILE,"Proximity sensor unavailable");
		}
		else {
			setStatus(ComponentStatus.AVAILABLE);
			mSensorManager.registerListener(this, mSensor,SensorManager.SENSOR_DELAY_NORMAL);
			Log.d(THIS_FILE,"Proximity sensor initialized");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(THIS_FILE,"Proximity sensor accuracy changed");
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.d(THIS_FILE,"Proximity sensor value changed");
		if (event.values[0] == 0) {
			setStatus(ComponentStatus.ON);
		}
		else {
			setStatus(ComponentStatus.AVAILABLE);
		}
		
		if (getStatus() == ComponentStatus.ON) {
			//TODO do something
			
//			Vibrator v = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
//			v.vibrate(300);		
		}
	}
}
