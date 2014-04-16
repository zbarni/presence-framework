package org.mobicents.servlet.sip.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;


public class User {
	private static Logger logger = Logger.getLogger(User.class);
	private SipURI cRegUri; // registered URI address, not contact
	private HashMap<SipURI,UAinstance> uaList = new HashMap<SipURI,UAinstance>();
	
	public User(SipURI uri) {
		this.cRegUri = Registrar.stripAllParameters(uri);
	}

	public SipURI getRegistrationUri() {
		return this.cRegUri;
	}
	
	/**
	 * In case of new registration, adds a new address to the list with
	 * the URI from the contact header. 
	 * @param addr
	 * @return
	 */
	public boolean addUAinstance(Address addr) {
		SipURI cContactUri = Registrar
				.stripAllParameters((SipURI)addr.getURI());
		UAinstance ua = new UAinstance(cContactUri, addr);
		uaList.put(cContactUri,ua);
		return true;
	}
	
	public Address getContactAddress(SipURI contactUri) {
		return uaList.get(Registrar.stripAllParameters(contactUri)).getContactAddress();
	}
	
	public boolean isInstanceRegistered(SipURI contactUri) {
		return uaList.containsKey(Registrar.stripAllParameters(contactUri));
	}
	
//	public Address getContactAddress(String gruu) {
//		SipURI uri = gruuMap.get(gruu);
//		if (uri != null) {
//			return contactAddr.get(uri);
//		}
//		return null;
//	}
	
	public ArrayList<UAinstance> getAllContactAddress() {
		return new ArrayList<UAinstance>(uaList.values());
	}
	
	public void removeInstance(SipURI contactUri) {
		uaList.remove(Registrar.stripAllParameters(contactUri));
	}
}
