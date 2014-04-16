package com.csipsimple.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import com.csipsimple.utils.Log;

public class Headset extends Component {
	private static final String THIS_FILE = "HEADSET";
	private static final String BLUETOOTH = "BLUETOOTH";
	private static final String WIRED = "WIRED";

	private boolean mHeadsetConnected;
	private boolean mHeadsetMicrophone;
	private String mType;
	private String mHeadsetName;
	private AudioManager audioManager;
	private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.d(THIS_FILE + " RECEIVER", "Receiver.onReceive action: " + intent.getAction());
			if (!intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				return;
			} 
			else {
				mHeadsetConnected = (intent.getIntExtra("state", 0) == 1);
				mHeadsetMicrophone= (intent.getIntExtra("microphone", 0) == 1);
				mHeadsetName = intent.getStringExtra("name");
				Log.d(THIS_FILE,"@headset name : " + mHeadsetName);
				Log.d(THIS_FILE,"@headset connected : " + mHeadsetConnected);
				updateComponent();
			}
		};	
	};

	public Headset(String id, String name, String type, ComponentManager cm) {
		super(id, name, type, cm);
		initialize();
	}

	public void updateComponent() {
		this.setStatus(this.mHeadsetConnected ? ComponentStatus.AVAILABLE : ComponentStatus.UNAVAILABLE); 
		Log.d(THIS_FILE, "@headset updating headset");
		this.getComponentManager().updateComponentModel(this);
	}
	
	public void initialize() {
		this.audioManager = (AudioManager)getContext().getSystemService(getContext().AUDIO_SERVICE);

		IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		getContext().registerReceiver(headsetReceiver, receiverFilter);
		Log.d(THIS_FILE,"Headset initialized");
	}
}
