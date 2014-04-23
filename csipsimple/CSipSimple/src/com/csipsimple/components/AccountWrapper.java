package com.csipsimple.components;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;

import com.csipsimple.api.SipProfile;
import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.service.SipService;
import com.csipsimple.utils.Log;

public class AccountWrapper {
	public static final String THIS_FILE = "AccountWrapper";
	
	private SipProfile profile;
	private Set<Components> components = new HashSet<Components>();

	private Document document;

	public void setDocument(Document document) {
		this.document = document;
	}

	public AccountWrapper(SipProfile profile) {
		this.profile = profile;
	}

	public Document getDocument() {
		return this.document;
	}

	public SipProfile getProfile() {
		return this.profile; 
	}

	public Set<Components> getComponents() {
		return components;
	}

	public void setComponents(Set<Components> components) {
		this.components = components;
	}

	public void loadComponents(SipService srv) {
		Log.d(THIS_FILE, "@XML Loading components..., service is  " + Boolean.toString(srv == null));
		for (Components c : Components.values()) {
			AbstractComponent ac = ComponentManager.getInstance().getAbstractComponent(c);
			Log.d(THIS_FILE, "@XML iterating...");
			if (ac != null) {
				this.components.add(c);
				srv.addComponent(this.profile.id, ac.getId());
				Log.d(THIS_FILE,"@XML Adding component " + ac.getId() + 
						" for account URI " + this.profile.getUriString()
						+ "and ID : "+ Long.toString(this.profile.id));
			}
		}
//		Log.d(THIS_FILE, "@XML There are " + Integer.toString((int)srv.getComponentCount((int)this.profile.id)));
//		srv.getComponentCount((int)this.profile.id);
	}
}
