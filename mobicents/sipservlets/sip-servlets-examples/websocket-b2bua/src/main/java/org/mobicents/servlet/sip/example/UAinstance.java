package org.mobicents.servlet.sip.example;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipURI;

public class UAinstance {
	private SipURI cContactUri;
	private Address contactAddress;
	public SipURI getContactUri() {
		return cContactUri;
	}

	public void setContactUri(SipURI contactUri) {
		this.cContactUri = contactUri;
	}

	public Address getContactAddress() {
		return contactAddress;
	}

	public void setContactAddress(Address contactAddress) {
		this.contactAddress = contactAddress;
	}

	public String getGruu() {
		return gruu;
	}

	public void setGruu(String gruu) {
		this.gruu = gruu;
	}

	private String gruu;
	
	public UAinstance(SipURI curi, Address addr) {
		this.cContactUri = curi;
		this.contactAddress = addr;
		this.gruu = contactAddress.getParameter("gr");
	}
	
	public String getGruuParam() {
		return this.gruu;
	}
	
	
}
