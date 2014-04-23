package com.csipsimple.components;

import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.utils.Log;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Accelerometer extends AbstractComponent implements SensorInterface, SensorEventListener {
	private static final String THIS_FILE = "ACCELEROMETER";
	private android.hardware.SensorManager mSensorManager;
	private android.hardware.Sensor mSensor;

	public Accelerometer(String id, String name, String type,Components descriptor) {
		super(id, name, type, descriptor);
		initialize();
	}

	public void updateComponent() {
		Log.d(THIS_FILE, "Updating accelerometer");
	}

	/**
	 * En/Dis-able the Accelerometer.
	 *
	 * @param doEnable
	 *            <code>true</code> for enable.<br>
	 *            <code>false</code> for disable.
	 * @throws UnsupportedOperationException
	 */
	//	public void setEnableAccelerometer(boolean doEnable)
	//			throws UnsupportedOperationException {
	//		if (!accelerometerAvailable)
	//			throw new UnsupportedOperationException(
	//					"Accelerometer is not available.");
	//
	//		/* If should be enabled and not already is: */
	//		if (doEnable && !this.isEnabled) {
	//			Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER);
	//			this.isEnabled = true;
	//		} else /* If should be disabled and not already is: */
	//			if (!doEnable && this.isEnabled) {
	//				Sensors.disableSensor(Sensors.SENSOR_ACCELEROMETER);
	//				this.isEnabled = false;
	//			}
	//	}	

	public void onSensorChanged(SensorEvent event) {
//		TextView tvX= (TextView)findViewById(R.id.x_axis);
//		TextView tvY= (TextView)findViewById(R.id.y_axis);
//		TextView tvZ= (TextView)findViewById(R.id.z_axis);
//		ImageView iv = (ImageView)findViewById(R.id.image);
//		float x = event.values[0];
//		float y = event.values[1];
//		float z = event.values[2];
//		if (!mInitialized) {
//			mLastX = x;
//			mLastY = y;
//			mLastZ = z;
//			tvX.setText("0.0");
//			tvY.setText("0.0");
//			tvZ.setText("0.0");
//			mInitialized = true;
//		} else {
//			float deltaX = Math.abs(mLastX - x);
//			float deltaY = Math.abs(mLastY - y);
//			float deltaZ = Math.abs(mLastZ - z);
//			if (deltaX < NOISE) deltaX = (float)0.0;
//			if (deltaY < NOISE) deltaY = (float)0.0;
//			if (deltaZ < NOISE) deltaZ = (float)0.0;
//			mLastX = x;
//			mLastY = y;
//			mLastZ = z;
//			tvX.setText(Float.toString(deltaX));
//			tvY.setText(Float.toString(deltaY));
//			tvZ.setText(Float.toString(deltaZ));
//			iv.setVisibility(View.VISIBLE);
//			if (deltaX > deltaY) {
//				iv.setImageResource(R.drawable.horizontal);
//			} else if (deltaY > deltaX) {
//				iv.setImageResource(R.drawable.vertical);
//			} else {
//				iv.setVisibility(View.INVISIBLE);
//			}
//		}
	}

	public void initialize() {
		mSensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);		
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		if (mSensor == null) {
			this.setStatus(ComponentStatus.UNAVAILABLE);
			Log.d(THIS_FILE,"Accelerometer unavailable");
		} 
		else {
			this.setStatus(ComponentStatus.AVAILABLE);
			mSensorManager.registerListener(this, mSensor,SensorManager.SENSOR_DELAY_NORMAL);
			Log.d(THIS_FILE,"Accelerometer initialized");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
}
