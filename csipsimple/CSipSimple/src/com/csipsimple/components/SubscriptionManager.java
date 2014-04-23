package com.csipsimple.components;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.csipsimple.service.ComponentService;

public class SubscriptionManager {
	public static final String THIS_FILE = "SUB MGR";
	private ComponentService mComponentService;
	private Context mContext;

	private HashMap<String, ArrayList<String>> buddySubscriptions = 
			new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> incomingSubscriptions = 
			new HashMap<String, ArrayList<String>>();

	public SubscriptionManager(ComponentService cService) {
		this.mComponentService = cService;
//		this.mContext = (Context) cService;
	}

	//	private boolean 

	public boolean isSubscriptionToComponent(String uri, String component) {
		ArrayList<String> al = buddySubscriptions.get(uri);
		if (al != null) {
			for (String c : al) {
				if (c.equals(component)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * If uri is not in Buddy List, SipService will automatically add it.
	 *  
	 * @param uri
	 * @param component
	 */
	public void createSubscription(String uri, String component) {
		int buddyId = -1;
		if (isSubscriptionToComponent(uri, component)) {
			return;
		}
		if (!buddySubscriptions.containsKey(uri)) {
			buddySubscriptions.put(uri, new ArrayList<String>());
//			try {
////				buddyId = mComponentService.getSipService().addBuddyForOutgoingSubscription(uri);
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		// add component to subscription list
		buddySubscriptions.get(uri).add(component);

		if (buddyId != -1) {
			Log.d(THIS_FILE, "@zbuddy Buddy id is " + Integer.toString(buddyId) + " and we're subscribing");
			// send subscription
//			try {
////				mComponentService.getSipService().createSubscription(buddyId, component);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
		}
	}

	public void cancelSubscription(String uri, String component) {

	}
	
	public void onIncomingSubscription(String componentId) {
		
	}

	public void cleanUp() {
		//TODO implement
	}
}
