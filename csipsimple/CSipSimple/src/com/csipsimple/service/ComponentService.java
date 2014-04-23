/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.csipsimple.service;

import java.util.HashMap;
import java.util.Map.Entry;

import com.csipsimple.api.SipProfileState;
import com.csipsimple.components.AccountWrapper;
import com.csipsimple.components.ComponentManager;
import com.csipsimple.components.PublicationManager;
//import com.csipsimple.components.SubscriptionManager;
import com.csipsimple.utils.Log;

public class ComponentService {

	private static final String THIS_FILE = "COMP SRV";
	private SipService service;
	private static PublicationManager mPublicationManager;
//	private static SubscriptionManager mSubscriptionManager;
	private HashMap<Long,AccountWrapper> accounts = new HashMap<Long,AccountWrapper>();
	
	public void addAccount(long accId) {
		if (!this.accounts.containsKey((Long) accId)) {
			Log.d(THIS_FILE,"@XML Adding account to compmanager : " + 
					service.getAccount(accId).getUriString() + " id : " + accId);
			
			AccountWrapper acc = new AccountWrapper(service.getAccount(accId));
			this.accounts.put(accId,acc);
			acc.setDocument(mPublicationManager.createPresenceDoc(acc.getProfile()
					.getUriString().replace("<", "").replace(">", "")));
			
			Log.d(THIS_FILE,"@XML sizae after addingh " + this.accounts.size());
		}
	}
	
	public HashMap<Long,AccountWrapper> getAccounts() {
		return this.accounts;
	}
	
	public void updateAccount(SipProfileState sp) {
		if (!isAccountRegistered(sp.getAccountId())) {
			addAccount(sp.getAccountId());
		}
		this.accounts.get(Long.valueOf(sp.getAccountId())).loadComponents(service);
		
		// TODO, temporary
		this.accounts.get(Long.valueOf(sp.getAccountId())).getProfile().gruu = 
				sp.getGruu().replace("<", "").replace(">", "");
		mPublicationManager.updatePublication(accounts.get(Long.valueOf(sp.getAccountId())));
	}
	
	public boolean isAccountRegistered(long accId) {
		return accounts.containsKey((Long)accId);
	}
	
	public ComponentService (SipService srv) {
		service = srv;
		// this is very important
		ComponentManager.getInstance().initManager(this);
		mPublicationManager = new PublicationManager(this);
//		mSubscriptionManager = new SubscriptionManager(this);
		
		Log.d(THIS_FILE,"Component Service Initialized!");
	}

	public SipService getSipService() {
		return service;
	}
	
	public PublicationManager getPublicationManager() {
		return mPublicationManager;
	}
	
	
//	public SubscriptionManager getSubscriptionManager() {
//		return mSubscriptionManager;
//	}
}
