package com.csipsimple.components;

import android.media.AudioManager;

import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.utils.Log;

public class Loudspeaker extends AbstractComponent {
	private static final String THIS_FILE = "HEADSET";

	private AudioManager audioManager;

	public Loudspeaker(String id, String name, String type, Components descriptor) {
		super(id, name, type, descriptor);
		initialize();
	}

	public void updateComponent() {
//		this.setStatus(this.mHeadsetConnected ? ComponentStatus.AVAILABLE : ComponentStatus.UNAVAILABLE); 
//		Log.d(THIS_FILE, "@headset updating headset");
//		this.getComponentManager().updateComponentModel(this);
	}
	
	public void initialize() {
		audioManager = (AudioManager)getContext().getSystemService(getContext().AUDIO_SERVICE);
		if (audioManager == null) {
			setStatus(ComponentStatus.UNAVAILABLE);
			Log.d(THIS_FILE,"Loudspeaker could not be initialized. Audiomanager null");
		}
		else {
			setStatus(ComponentStatus.AVAILABLE);
			Log.d(THIS_FILE,"Loudspeaker initialized");
		}
	}
}
