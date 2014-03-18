package com.csipsimple.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Vibrator;

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
			Log.d(THIS_FILE + " RECEIVER", "Receiver.onReceive action: " + intent.getAction());
			if (!intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				return;
			} 
			else {
				mHeadsetConnected = (intent.getIntExtra("state", 0) == 1);
				mHeadsetMicrophone= (intent.getIntExtra("microphone", 0) == 1);
				mHeadsetName = intent.getStringExtra("name");
			}
		};	
	};

	public Headset(Context context, String id, String name, String type) {
		super(context, id, name, type);
		initialize();
	}

	public void initialize() {
		Log.d(THIS_FILE,"Headset initialized");
		this.audioManager = (AudioManager)getContext().getSystemService(getContext().AUDIO_SERVICE);

		IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		getContext().registerReceiver(headsetReceiver, receiverFilter);
	}
}
