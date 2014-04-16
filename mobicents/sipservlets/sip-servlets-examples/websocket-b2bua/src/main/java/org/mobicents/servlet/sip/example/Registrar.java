package org.mobicents.servlet.sip.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

public class Registrar {
	private static Logger logger = Logger.getLogger(Registrar.class);
	private static final long serialVersionUID = 1L;
	private static SipFactory sipFactory;
	private static HashMap<SipURI, User> users = new HashMap<SipURI, User>();
	
	public Registrar(SipFactory _sipFactory) {
		sipFactory = _sipFactory;
	}
	
	/**
	 * Adds (registers) a new user to list or a new address to an already
	 * registered URI. Each user is identified by the user part in the URI.
	 * Saved addresses have one of the following forms: 
	 * 1) sip:user@host:port
	 * 2) sip:user@host:port;gr=uniqueid
	 * @param regUri request URI in the REGISTER message, to which incoming 
	 * calls will be targeted
	 * @param address contact header as received in the REGISTER message 
	 * @return contact header as in the received register request plus
	 * the pub-gruu parameter if sip.instance found in header 
	 */
	public Address addUser(Address addr, SipURI regUri) {
		SipURI cRegUri = stripAllParameters(regUri);
		User u = users.get(cRegUri);
		if (u != null) {
			// if uri already registered (possibly
			Address newAddr = addGruuToAddress(addr);
			u.addUAinstance(newAddr);
			logger.info("User already registered : " + u.getRegistrationUri() + ""
					+ " new address : " + newAddr);
		}
		else {
			// if uri not yet registered create new user
			u = new User(cRegUri);
			Address newAddr = addGruuToAddress(addr);
			u.addUAinstance(newAddr);
			users.put(cRegUri, u);
			logger.info("Registered new address : " + newAddr + "for user : "
					+ u.getRegistrationUri());
		}
		return addPubGruu(addr);
	}
	
	public void removeUser(Address addr, SipURI regUri) {
		SipURI contactUri = (SipURI)addr.getURI();
		logger.info("Removing address of user with URI : " + stripAllParameters(regUri)
				+ " and address: " + addr);

		User u = users.get(stripAllParameters(regUri));
		if (u != null) {
			u.removeInstance(contactUri);
		}
	}
	
	public static SipURI stripAllParameters(SipURI uri) {
		SipURI newUri = (SipURI)uri.clone();
		for (Iterator<String> iter = uri.getParameterNames();iter.hasNext();) {
			newUri.removeParameter(iter.next());
		}
		return newUri;
	}
	
	public boolean isUriRegistered(SipURI uri) {
		uri = stripAllParameters(uri);
		return (users.get(uri) == null) ? false:true;
	}
	
//	private Address createGRUU(Address addr) {
//		if (addr.getParameter("+sip.instance") == null) {
//			return null;
//		}
//		Address newAddr;
//		SipURI uri = stripAllParameters((SipURI)addr.getURI());
//		try {
//			newAddr = sipFactory.createAddress(uri);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//		// extract sip instance
//		String sip_instance = addr.getParameter("+sip.instance")
//				.replace("\"<", "").replace(">\"","");
//		newAddr.setParameter("gr", sip_instance);
//		return newAddr;
//	}
	
	/**
	 * Copies the contact header from the REGISTER message into a new 
	 * address, removing the +sip.instance parameter.
	 * @param addr
	 * @return
	 */
	private Address addGruuToAddress(Address addr) {
		if (addr.getParameter("+sip.instance") == null) {
			return addr;
		}
		Address newAddr;
		try {
			newAddr = sipFactory.createAddress(addr.toString());
		} catch (ServletParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return addr;
		}
		// extract sip instance
		String sip_instance = addr.getParameter("+sip.instance")
				.replace("\"<", "").replace(">\"","");
		newAddr.setParameter("gr", sip_instance);
		newAddr.removeParameter("+sip.instance");
		return newAddr;
	}
	
	public Address getUserAddress(SipURI uri) {
		uri = stripAllParameters(uri);		
		logger.info("Getting address of user with URI : " + uri);
		User u = users.get(uri);
		if (u == null) {
			return null;
		}
		logger.info("Found address for URI : (" + uri+") : " + u.getContactAddress(uri));
		return u.getContactAddress(uri);
	}
	
	public ArrayList<UAinstance> getAllUserAddress(SipURI regUri) {
		User u = users.get(stripAllParameters(regUri));
		if (u == null) {
			return null;
		}
		logger.info("Getting list of addresses for URI : (" + stripAllParameters(regUri)+") : ");
		return u.getAllContactAddress();
	}
	
	/**
	 * Adds the pub-gruu parameter to the parameter addr. Teh returned
	 * address will be the Contact header in the 200 OK response.  
	 * @param addr
	 * @return address containing the pub-gruu if sip.instance parameter is
	 * present, otherwise same address without modification
	 */
	private Address addPubGruu(Address addr) {
		if (addr.getParameter("+sip.instance") == null) {
			return addr;
		}
		Address newAddr;
		try {
			newAddr = sipFactory.createAddress(addr.toString());
			// extract sip instance
			String sip_instance = addr.getParameter("+sip.instance")
					.replace("\"<", "").replace(">\"","");
			String sip_uri = addr.getURI().toString().replace(";ob", "");
			String pub_gruu = "\"" + sip_uri + ";gr=" + sip_instance + "\"";
			newAddr.setParameter("pub-gruu", pub_gruu);
			return newAddr;
		} catch (ServletParseException e) {
			logger.error("Error adding pub-gruu to address", e);
			return addr;
		}
	}
}
