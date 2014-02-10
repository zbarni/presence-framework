/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.message;

import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.message.MessageExt;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.ha.javax.sip.SipLoadBalancer;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;
import org.mobicents.servlet.sip.address.GenericURIImpl;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.ApplicationRoutingHeaderComposer;
import org.mobicents.servlet.sip.core.MobicentsExtendedListeningPoint;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.MobicentsSipServletMessageFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.message.OutboundProxy;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.security.AuthInfoImpl;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

public class SipFactoryImpl implements MobicentsSipFactory,  Externalizable {	

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SipFactoryImpl.class
			.getCanonicalName());
	private static final String TAG_PARAM = "tag";
	private static final String METHOD_PARAM = "method";
	private static final String MADDR_PARAM = "maddr";
	private static final String TTL_PARAM = "ttl";
	private static final String TRANSPORT_PARAM = "transport";
	private static final String LR_PARAM = "lr";

	private boolean useLoadBalancer = false;
	private boolean routeOrphanRequests = false;
	private SipLoadBalancer loadBalancerToUse = null;
	
	private static boolean initialized;
	public static AddressFactory addressFactory;
	public static HeaderFactory headerFactory;
	public static SipFactory sipFactory;
	public static MessageFactory messageFactory;
	
	private MobicentsSipServletMessageFactory mobicentsSipServletMessageFactory;

	public void initialize(String pathName, boolean prettyEncoding) {
		if (!initialized) {
			try {
				System.setProperty("gov.nist.core.STRIP_ADDR_SCOPES", "true");
				sipFactory = SipFactory.getInstance();
				sipFactory.setPathName(pathName);
				addressFactory = sipFactory.createAddressFactory();				
				headerFactory = sipFactory.createHeaderFactory();
				if(prettyEncoding) {
					((HeaderFactoryImpl)headerFactory).setPrettyEncoding(prettyEncoding);
				}
				messageFactory = sipFactory.createMessageFactory();
				initialized = true;
			} catch (PeerUnavailableException ex) {
				logger.error("Could not instantiate factories -- exitting", ex);
				throw new IllegalArgumentException("Cannot instantiate factories ", ex);
			}
		}
	}
	
	public static class NamesComparator implements Comparator<String>, Serializable {		
		private static final long serialVersionUID = 1L;

		public int compare(String o1, String o2) {
			return o1.compareToIgnoreCase(o2);
		}
	}
	
	public static final Set<String> FORBIDDEN_PARAMS = new HashSet<String>();

	static {
		FORBIDDEN_PARAMS.add(TAG_PARAM);
		FORBIDDEN_PARAMS.add(METHOD_PARAM);
		FORBIDDEN_PARAMS.add(MADDR_PARAM);
		FORBIDDEN_PARAMS.add(TTL_PARAM);
		FORBIDDEN_PARAMS.add(TRANSPORT_PARAM);
		FORBIDDEN_PARAMS.add(LR_PARAM);
	}	

	private transient SipApplicationDispatcher sipApplicationDispatcher = null;
	
	public SipFactoryImpl() {}
	/**
	 * Dafault constructor
	 * @param sipApplicationDispatcher 
	 */
	public SipFactoryImpl(SipApplicationDispatcher sipApplicationDispatcher) {		
		this.sipApplicationDispatcher = sipApplicationDispatcher;
		mobicentsSipServletMessageFactory = initMobicentsSipServletMessageFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createAddress(java.lang.String)
	 */
	public Address createAddress(String sipAddress)
			throws ServletParseException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating Address from [" + sipAddress + "]");
			}

			AddressImpl retval = new AddressImpl();
			retval.setValue(sipAddress);
			return retval;
		} catch (IllegalArgumentException e) {
			throw new ServletParseException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createAddress(javax.servlet.sip.URI)
	 */
	public Address createAddress(URI uri) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating Address fromm URI[" + uri.toString()
					+ "]");
		}
		URIImpl uriImpl = (URIImpl) uri;
		return new AddressImpl(SipFactoryImpl.addressFactory
				.createAddress(uriImpl.getURI()), null, ModifiableRule.Modifiable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createAddress(javax.servlet.sip.URI,
	 *      java.lang.String)
	 */
	public Address createAddress(URI uri, String displayName) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating Address from URI[" + uri.toString()
						+ "] with display name[" + displayName + "]");
			}

			javax.sip.address.Address address = SipFactoryImpl.addressFactory
					.createAddress(((URIImpl) uri).getURI());
			address.setDisplayName(displayName);
			return new AddressImpl(address, null, ModifiableRule.Modifiable);

		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createApplicationSession()
	 */
	public SipApplicationSession createApplicationSession() {
		throw new UnsupportedOperationException("use createApplicationSession(SipContext sipContext) instead !");
	}
	
	/**
	 * Creates an application session associated with the context
	 * @param sipContext
	 * @return
	 */
	public MobicentsSipApplicationSession createApplicationSession(SipContext sipContext) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new application session for sip context "+ sipContext.getApplicationName());
		}
		//call id not needed anymore since the sipappsessionkey is not a callid anymore but a random uuid
		SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
				sipContext.getApplicationName(), 
				null,
				null);		
		MobicentsSipApplicationSession sipApplicationSession = sipContext.getSipManager().getSipApplicationSession(
				sipApplicationSessionKey, true);
		
		if(StaticServiceHolder.sipStandardService.isHttpFollowsSip()) {
			String jvmRoute = StaticServiceHolder.sipStandardService.getJvmRoute();
			if(jvmRoute != null) {
				sipApplicationSession.setJvmRoute(jvmRoute);
			}
		}
			
		return sipApplicationSession.getFacade();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, javax.servlet.sip.Address,
	 *      javax.servlet.sip.Address)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, Address from, Address to, String handler, String originalCallId, String fromTagToUse) {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM_A[" + from + "] TO_A[" + to + "]");
		}

		validateCreation(method, sipAppSession);

		try { 
			//javadoc specifies that a copy of the address should be done hence the clone
			return createSipServletRequest(sipAppSession, method, (Address)from.clone(), (Address)to.clone(), handler, originalCallId, fromTagToUse);
		} catch (ServletParseException e) {
			logger.error("Error creating sipServletRequest", e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, javax.servlet.sip.URI, javax.servlet.sip.URI)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, URI from, URI to, String handler) {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM_URI[" + from + "] TO_URI[" + to + "]");
		}

		validateCreation(method, sipAppSession);

		//javadoc specifies that a copy of the uri should be done hence the clone
		Address toA = this.createAddress(to.clone());
		Address fromA = this.createAddress(from.clone());

		try {
			return createSipServletRequest(sipAppSession, method, fromA, toA, handler, null, null);
		} catch (ServletParseException e) {
			logger.error("Error creating sipServletRequest", e);
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, String from, String to, String handler) throws ServletParseException {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM["
							+ from + "] TO[" + to + "]");
		}

		validateCreation(method, sipAppSession);

		Address toA = this.createAddress(to);
		Address fromA = this.createAddress(from);

		return createSipServletRequest(sipAppSession, method, fromA, toA, handler, null, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipServletRequest,
	 *      boolean)
	 */
	public SipServletRequest createRequest(SipServletRequest origRequest,
			boolean sameCallId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating SipServletRequest from original request["
					+ origRequest + "] with same call id[" + sameCallId + "]");
		}
	    
		final SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
		final MobicentsSipApplicationSession originalAppSession = (MobicentsSipApplicationSession) origRequestImpl.getApplicationSession(false);		
		if (originalAppSession == null) {
			throw new IllegalStateException("original request's app session does not exists");
		}			
		
		final Request newRequest = (Request) origRequestImpl.message.clone();
		((MessageExt)newRequest).setApplicationData(null);
		//removing the via header from original request
		newRequest.removeHeader(ViaHeader.NAME);	
		
		// cater to http://code.google.com/p/sipservlets/issues/detail?id=31 to be able to set the rport in applications
		final SipApplicationDispatcher sipApplicationDispatcher = getSipApplicationDispatcher();
		final String branch = JainSipUtils.createBranch(originalAppSession.getKey().getId(),  sipApplicationDispatcher.getHashFromApplicationName(originalAppSession.getKey().getApplicationName()));
		ViaHeader viaHeader = JainSipUtils.createViaHeader(
				getSipNetworkInterfaceManager(), newRequest, branch, null);
		newRequest.addHeader(viaHeader);
		
		final FromHeader newFromHeader = (FromHeader) newRequest.getHeader(FromHeader.NAME); 
		
		//assign a new from tag
		newFromHeader.removeParameter("tag");
		//remove the to tag
		((ToHeader) newRequest.getHeader(ToHeader.NAME))
				.removeParameter("tag");
		// Remove the route header ( will point to us ).
		// commented as per issue 649
//		newRequest.removeHeader(RouteHeader.NAME);
		
		// Remove the record route headers. This is a new call leg.
		newRequest.removeHeader(RecordRouteHeader.NAME);
		
		//For non-REGISTER requests, the Contact header field is not copied 
		//but is populated by the container as usual
		if(!Request.REGISTER.equalsIgnoreCase(origRequest.getMethod())) {
			try {
				//For non-REGISTER requests, the Contact header field is not copied
				//but is populated by the container as usual
				if(!Request.REGISTER.equalsIgnoreCase(origRequest.getMethod())) {
					newRequest.removeHeader(ContactHeader.NAME);
			
					//Adding default contact header for specific methods only
					if(JainSipUtils.CONTACT_HEADER_METHODS.contains(newRequest.getMethod())) {
						String fromName = null;
						String displayName = origRequest.getFrom().getDisplayName();
						if(origRequest.getAddressHeader(ContactHeader.NAME).getURI() instanceof SipURI) {
							fromName = ((SipURI)origRequest.getFrom().getURI()).getUser();
						}
						// Create the contact name address.
						ContactHeader contactHeader = null;
						// if a sip load balancer is present in front of the server, the contact header is the one from the sip lb
						// so that the subsequent requests can be failed over
						if(useLoadBalancer) {
							javax.sip.address.SipURI sipURI = addressFactory.createSipURI(fromName, loadBalancerToUse.getAddress().getHostAddress());
							sipURI.setHost(loadBalancerToUse.getAddress().getHostAddress());
							sipURI.setPort(loadBalancerToUse.getSipPort());
							sipURI.setTransportParam(JainSipUtils.findTransport(newRequest));
							javax.sip.address.Address contactAddress = addressFactory.createAddress(sipURI);
							if(displayName != null && displayName.length() > 0) {
								contactAddress.setDisplayName(displayName);
							}
							contactHeader = headerFactory.createContactHeader(contactAddress);
						} else {
							contactHeader = JainSipUtils.createContactHeader(getSipNetworkInterfaceManager(), newRequest, displayName, fromName, null);
						}
			
						if(contactHeader != null) {
							newRequest.addHeader(contactHeader);
						}
					}
				}
			} catch (Exception ex) {
				logger.warn("Unable to create Contact Header. It will be added later on send.", ex);
			}
		}		
		try {
			if(!sameCallId) {
				//Creating new call id
				final Iterator<MobicentsExtendedListeningPoint> listeningPointsIterator = getSipNetworkInterfaceManager().getExtendedListeningPoints();				
				if(!listeningPointsIterator.hasNext()) {				
					throw new IllegalStateException("There is no SIP connectors available to create the request");
				}
				final MobicentsExtendedListeningPoint extendedListeningPoint = listeningPointsIterator.next();
				final CallIdHeader callIdHeader = SipFactoryImpl.headerFactory.createCallIdHeader(extendedListeningPoint.getSipProvider().getNewCallId().getCallId());
				newRequest.setHeader(callIdHeader);
				if(logger.isDebugEnabled()) {
					logger.debug("not reusing same call id, new call id is " + callIdHeader);
				}
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("reusing same call id = " + origRequestImpl.getCallId());
				}
			}
									
			newFromHeader.setTag(ApplicationRoutingHeaderComposer.getHash(getSipApplicationDispatcher(), originalAppSession.getKey().getApplicationName(), originalAppSession.getKey().getId()));
			
			final MobicentsSipSessionKey key = SessionManagerUtil.getSipSessionKey(originalAppSession.getKey().getId(), originalAppSession.getKey().getApplicationName(), newRequest, false);
			final MobicentsSipSession session = originalAppSession.getSipContext().getSipManager().getSipSession(key, true, this, originalAppSession);			
			final MobicentsSipSession originalSession = origRequestImpl.getSipSession();
			if(originalSession != null) {
				session.setHandler(originalSession.getHandler());
			} else if(originalAppSession.getCurrentRequestHandler() != null) {
				session.setHandler(originalAppSession.getCurrentRequestHandler());
			}
			
			final SipServletRequestImpl newSipServletRequest = (SipServletRequestImpl) mobicentsSipServletMessageFactory.createSipServletRequest(
					newRequest,
					session, 
					null, 
					null, 
					JainSipUtils.DIALOG_CREATING_METHODS.contains(newRequest.getMethod()));			
			//JSR 289 Section 15.1.6
			newSipServletRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, origRequest);
			
			if(logger.isDebugEnabled()) {
				logger.debug("newSipServletRequest = " + newSipServletRequest);
			}	
			
			return newSipServletRequest;
		} catch (Exception ex) {
			logger.error("Unexpected exception ", ex);
			throw new IllegalArgumentException(
					"Illegal arg ecnountered while creatigng b2bua", ex);
		}			
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createSipURI(java.lang.String,
	 *      java.lang.String)
	 */
	public SipURI createSipURI(String user, String host) {		
		if (logger.isDebugEnabled()) {
			logger.debug("Creating SipURI from USER[" + user + "] HOST[" + host
					+ "]");
		}		
		// Fix for http://code.google.com/p/sipservlets/issues/detail?id=145
		if(user != null && user.trim().isEmpty()) {
			user = null;
		}
		try {
			return new SipURIImpl(SipFactoryImpl.addressFactory.createSipURI(
					user, host), ModifiableRule.Modifiable);
		} catch (ParseException e) {
			logger.error("couldn't parse the SipURI from USER[" + user
					+ "] HOST[" + host + "]", e);
			throw new IllegalArgumentException("Could not create SIP URI user = " + user + " host = " + host);
		}
	}

	public URI createURI(String uri) throws ServletParseException {
//		if(!checkScheme(uri)) {
//			// testCreateProxyBranches101 needs this to be IllegalArgumentExcpetion, but the test is wrong
//			throw new ServletParseException("The uri " + uri + " is not valid");
//		}
		try {
			javax.sip.address.URI jainUri = SipFactoryImpl.addressFactory
					.createURI(uri);
			if (jainUri instanceof javax.sip.address.SipURI) {
				return new SipURIImpl(
						(javax.sip.address.SipURI) jainUri, ModifiableRule.Modifiable);
			} else if (jainUri instanceof javax.sip.address.TelURL) {
				return new TelURLImpl(
						(javax.sip.address.TelURL) jainUri);
			} else {
				return new GenericURIImpl(jainUri);
			}
		} catch (ParseException ex) {
			throw new ServletParseException("Bad param " + uri, ex);
		}
	}

	// ------------ HELPER METHODS
	// -------------------- createRequest
	/**
	 * Does basic check for illegal methods, wrong state, if it finds, it throws
	 * exception
	 * 
	 */
	private static void validateCreation(String method, SipApplicationSession app) {

		if (method.equals(Request.ACK)) {
			throw new IllegalArgumentException(
					"Wrong method to create request with[" + Request.ACK + "]!");
		}
		if (method.equals(Request.PRACK)) {
			throw new IllegalArgumentException(
					"Wrong method to create request with[" + Request.PRACK + "]!");
		}
		if (method.equals(Request.CANCEL)) {
			throw new IllegalArgumentException(
					"Wrong method to create request with[" + Request.CANCEL
							+ "]!");
		}
		if (!((MobicentsSipApplicationSession)app).isValidInternal()) {
			throw new IllegalArgumentException(
					"Cant associate request with invalidaded sip session application!");
		}

	}

	/**
	 * This method actually does create javax.sip.message.Request, dialog(if
	 * method is INVITE or SUBSCRIBE), ctx and wraps this in new sipsession
	 * 
	 * @param sipAppSession
	 * @param method
	 * @param from
	 * @param to
	 * @param originalCallId 
	 * @return
	 */
	private SipServletRequest createSipServletRequest(
			SipApplicationSession sipAppSession, String method, Address from,
			Address to, String handler, String originalCallId, String fromTagToUse) throws ServletParseException {
		
		MobicentsSipApplicationSession mobicentsSipApplicationSession = (MobicentsSipApplicationSession) sipAppSession;
		
		// the request object with method, request URI, and From, To, Call-ID,
		// CSeq, Route headers filled in.
		Request requestToWrap = null;

		ContactHeader contactHeader = null;
		ToHeader toHeader = null;
		FromHeader fromHeader = null;
		CSeqHeader cseqHeader = null;
		CallIdHeader callIdHeader = null;
		MaxForwardsHeader maxForwardsHeader = null;		

		// FIXME: Is this nough?
		// We need address from which this will be sent, also this one will be
		// default for contact and via
		String transport = ListeningPoint.UDP;

		// LETS CREATE OUR HEADERS			
		javax.sip.address.Address fromAddress = null;
		try {
			// Issue 676 : Any component of the from and to URIs not allowed in the context of
			// SIP From and To headers are removed from the copies [refer Table 1, Section
			// 19.1.1, RFC3261]
			for(String param : FORBIDDEN_PARAMS) {
				from.getURI().removeParameter(param);	
			}
			
			// Issue 676 : from tags not removed so removing the tag
			from.removeParameter(TAG_PARAM);
			
			fromAddress = SipFactoryImpl.addressFactory
					.createAddress(((URIImpl)from.getURI()).getURI());
			fromAddress.setDisplayName(from.getDisplayName());		
			
			fromHeader = SipFactoryImpl.headerFactory.createFromHeader(fromAddress, null);			
		} catch (Exception pe) {
			throw new ServletParseException("Impossoible to parse the given From " + from.toString(), pe);
		}
		javax.sip.address.Address toAddress = null; 
		try{
			// Issue 676 : Any component of the from and to URIs not allowed in the context of
			// SIP From and To headers are removed from the copies [refer Table 1, Section
			// 19.1.1, RFC3261]
			for(String param : FORBIDDEN_PARAMS) {
				to.getURI().removeParameter(param);	
			}
			// Issue 676 : to tags not removed so removing the tag
			to.removeParameter(TAG_PARAM);
			
			toAddress = SipFactoryImpl.addressFactory
				.createAddress(((URIImpl)to.getURI()).getURI());
			
			toAddress.setDisplayName(to.getDisplayName());

			toHeader = SipFactoryImpl.headerFactory.createToHeader(toAddress, null);										
		} catch (Exception pe) {
			throw new ServletParseException("Impossoible to parse the given To " + to.toString(), pe);
		}
		try {
			cseqHeader = SipFactoryImpl.headerFactory.createCSeqHeader(1L, method);
			// Fix provided by Hauke D. Issue 411
			MobicentsSipApplicationSessionKey sipApplicationSessionKey = mobicentsSipApplicationSession.getKey();
//			if(sipApplicationSessionKey.isAppGeneratedKey()) {
			if(originalCallId == null) {
				final Iterator<MobicentsExtendedListeningPoint> listeningPointsIterator = getSipNetworkInterfaceManager().getExtendedListeningPoints();				
				if(listeningPointsIterator.hasNext()) {
					callIdHeader = sipApplicationDispatcher.getSipFactory().getHeaderFactory().createCallIdHeader(
							listeningPointsIterator.next().getSipProvider().getNewCallId().getCallId());
				} else {
					throw new IllegalStateException("There is no SIP connectors available to create the request");
				}
			} else {
				callIdHeader = SipFactoryImpl.headerFactory.createCallIdHeader(originalCallId);
			}
//			} else {
//				callIdHeader = SipFactoryImpl.headerFactory.createCallIdHeader(
//						sipApplicationSessionKey.getId());
//			}
			maxForwardsHeader = SipFactoryImpl.headerFactory
					.createMaxForwardsHeader(JainSipUtils.MAX_FORWARD_HEADER_VALUE);
			URIImpl requestURI = (URIImpl)to.getURI().clone();

			// copying address params into headers.
			// commented out because of Issue 1105
//			Iterator<String> keys = to.getParameterNames();
//
//			while (keys.hasNext()) {
//				String key = keys.next();				
//				toHeader.setParameter(key, to.getParameter(key));
//			}
//
//			keys = from.getParameterNames();
//
//			while (keys.hasNext()) {
//				String key = keys.next();				
//				fromHeader.setParameter(key, from.getParameter(key));
//			}
			//Issue 112 by folsson : no via header to add will be added when the request will be sent out
			List<Header> viaHeaders = new ArrayList<Header>();
						 			
			requestToWrap = SipFactoryImpl.messageFactory.createRequest(
					requestURI.getURI(), 
					method, 
					callIdHeader, 
					cseqHeader, 
					fromHeader, 
					toHeader, 
					viaHeaders, 
					maxForwardsHeader);

			//Adding default contact header for specific methods only
			if(JainSipUtils.CONTACT_HEADER_METHODS.contains(method)) {				
				String fromName = null;
				String displayName = fromHeader.getAddress().getDisplayName();
				if(fromHeader.getAddress().getURI() instanceof javax.sip.address.SipURI) {
					fromName = ((javax.sip.address.SipURI)fromHeader.getAddress().getURI()).getUser();
				}										
				// Create the contact name address.
				contactHeader = null;
				// if a sip load balancer is present in front of the server, the contact header is the one from the sip lb
				// so that the subsequent requests can be failed over
				if(useLoadBalancer) {
					javax.sip.address.SipURI sipURI = SipFactoryImpl.addressFactory.createSipURI(fromName, loadBalancerToUse.getAddress().getHostAddress());
					sipURI.setHost(loadBalancerToUse.getAddress().getHostAddress());
					sipURI.setPort(loadBalancerToUse.getSipPort());			
					sipURI.setTransportParam(transport);
					javax.sip.address.Address contactAddress = SipFactoryImpl.addressFactory.createAddress(sipURI);
					if(displayName != null && displayName.length() > 0) {
						contactAddress.setDisplayName(displayName);
					}
					contactHeader = SipFactoryImpl.headerFactory.createContactHeader(contactAddress);													
				} else {
					contactHeader = JainSipUtils.createContactHeader(getSipNetworkInterfaceManager(), requestToWrap, displayName, fromName, null);
				}
			}
			// Add all headers		
			if(contactHeader != null) {
				requestToWrap.addHeader(contactHeader);
			}
						
			if(fromTagToUse == null) {
				fromHeader.setTag(ApplicationRoutingHeaderComposer.getHash(sipApplicationDispatcher, sipAppSession.getApplicationName(), sipApplicationSessionKey.getId()));
			} else {
				fromHeader.setTag(fromTagToUse);
			}
			
			MobicentsSipSessionKey key = SessionManagerUtil.getSipSessionKey(
					mobicentsSipApplicationSession.getKey().getId(), mobicentsSipApplicationSession.getKey().getApplicationName(), requestToWrap, false);
			MobicentsSipSession session = mobicentsSipApplicationSession.getSipContext().getSipManager().
				getSipSession(key, true, this, mobicentsSipApplicationSession);
			session.setHandler(handler);
			session.setLocalParty(new AddressImpl(fromAddress, null, ModifiableRule.NotModifiable));
			session.setRemoteParty(new AddressImpl(toAddress, null, ModifiableRule.NotModifiable));
			
			// cater to http://code.google.com/p/sipservlets/issues/detail?id=31 to be able to set the rport in applications
			final SipApplicationDispatcher sipApplicationDispatcher = getSipApplicationDispatcher();
			final String branch = JainSipUtils.createBranch(sipApplicationSessionKey.getId(),  sipApplicationDispatcher.getHashFromApplicationName(sipApplicationSessionKey.getApplicationName()));
			ViaHeader viaHeader = JainSipUtils.createViaHeader(
    				getSipNetworkInterfaceManager(), requestToWrap, branch, session.getOutboundInterface());
			requestToWrap.addHeader(viaHeader);
			
			SipServletRequest retVal = (SipServletRequestImpl) mobicentsSipServletMessageFactory.createSipServletRequest(
					requestToWrap, session, null, null,
					JainSipUtils.DIALOG_CREATING_METHODS.contains(method));						
			
			return retVal;
		} catch (Exception e) {
			throw new IllegalStateException("Error creating sipServletRequest", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Parameterable createParameterable(String value) throws ServletParseException {
		try {			 
			Header header = SipFactoryImpl.headerFactory.createHeader(ContactHeader.NAME, value);
			return SipServletMessageImpl.createParameterable(header, SipServletMessageImpl.getFullHeaderName(header.getName()), true);
		} catch (ParseException e) {
			try {
				Header header = SipFactoryImpl.headerFactory.createHeader(ContentTypeHeader.NAME, value);
				return SipServletMessageImpl.createParameterable(header, SipServletMessageImpl.getFullHeaderName(header.getName()), true);
			} catch (ParseException pe) {
				// Contribution from Nishihara, Naoki from Japan for Issue http://code.google.com/p/mobicents/issues/detail?id=1856
				// Cannot create a parameterable header for Session-Expires
				try {
					Header header = SipFactoryImpl.headerFactory.createHeader(ContentDispositionHeader.NAME, value);
					return SipServletMessageImpl.createParameterable(header, SipServletMessageImpl.getFullHeaderName(header.getName()), true);
				} catch (ParseException pe2) {
					throw new ServletParseException("Impossible to parse the following parameterable "+ value , pe2);
				}
			}
		} 		
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public SipApplicationRouterInfo getNextInterestedApplication(SipServletRequestImpl sipServletRequestImpl) {
		return sipApplicationDispatcher.getNextInterestedApplication(sipServletRequestImpl);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createApplicationSessionByAppName(java.lang.String)
	 */
	public SipApplicationSession createApplicationSessionByAppName(
			String sipAppName) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new application session for application name " + sipAppName);
		}
		SipContext sipContext = sipApplicationDispatcher.findSipApplication(sipAppName);
		if(sipContext == null) {
			throw new IllegalArgumentException("The specified application "+sipAppName+" is not currently deployed");
		}		
		MobicentsSipApplicationSession sipApplicationSession = createApplicationSession(sipContext);
		// make sure to acquire this app session and add it to the set of app sessions we monitor in the context of the application
		// to release them all when we exit application code
		sipContext.enterSipApp(sipApplicationSession, null, true);
		
		return sipApplicationSession;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createApplicationSessionByKey(java.lang.String)
	 */
	public SipApplicationSession createApplicationSessionByKey(
			String sipApplicationKey) {
		// should not be called directly, should be called through the facade object only
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createAuthInfo()
	 */
	public AuthInfo createAuthInfo() {
		return new AuthInfoImpl();
	}

	/**
	 * @return the sipApplicationDispatcher
	 */
	public SipApplicationDispatcher getSipApplicationDispatcher() {
		if(sipApplicationDispatcher == null) {
			sipApplicationDispatcher = StaticServiceHolder.sipStandardService.getSipApplicationDispatcher();
		}
		return sipApplicationDispatcher;
	}

	/**
	 * @param sipApplicationDispatcher the sipApplicationDispatcher to set
	 */
	public void setSipApplicationDispatcher(
			SipApplicationDispatcher sipApplicationDispatcher) {
		this.sipApplicationDispatcher = sipApplicationDispatcher;
	}
	
	/**
	 * Retrieve the manager for the sip network interfaces
	 * @return the manager for the sip network interfaces
	 */
	public SipNetworkInterfaceManager getSipNetworkInterfaceManager() {
		return sipApplicationDispatcher.getSipNetworkInterfaceManager();
	}

	/**
	 * @return the loadBalancerToUse
	 */
	public SipLoadBalancer getLoadBalancerToUse() {
		return loadBalancerToUse;
	}

	/**
	 * @param loadBalancerToUse the loadBalancerToUse to set
	 */
	public void setLoadBalancerToUse(SipLoadBalancer loadBalancerToUse) {		
		if(loadBalancerToUse == null) {
			useLoadBalancer = false;
		} else {
			useLoadBalancer = true;
		}
		this.loadBalancerToUse = loadBalancerToUse;
		if(logger.isInfoEnabled()) {
			logger.info("Load Balancer to Use " + loadBalancerToUse);
		}
	}

	/**
	 * @return the useLoadBalancer
	 */
	public boolean isUseLoadBalancer() {
		return useLoadBalancer;
	}

	/**
	 * 
	 * @param request
	 * @throws ParseException
	 */
	public void addLoadBalancerRouteHeader(Request request) {
		try {
			String transport = JainSipUtils.findTransport(request);
			String host = null;
			int port = -1; 
			OutboundProxy proxy = StaticServiceHolder.sipStandardService.getOutboundProxy();
			if(proxy == null) {
				if(transport.equalsIgnoreCase("ws")){
					//This is a WebSocket request through LB, no need to add Route header.
					return;
				} else {
					host = loadBalancerToUse.getAddress().getHostAddress();
					port = loadBalancerToUse.getSipPort();
				}
			} else {				
				host = proxy.getHost();
				port = proxy.getPort();				
			}
			javax.sip.address.SipURI sipUri = SipFactoryImpl.addressFactory.createSipURI(null, host);
			sipUri.setPort(port);
			sipUri.setLrParam();
			sipUri.setTransportParam(transport);
			MobicentsExtendedListeningPoint listeningPoint = 
				getSipNetworkInterfaceManager().findMatchingListeningPoint(transport, false);
			sipUri.setParameter(MessageDispatcher.ROUTE_PARAM_NODE_HOST, 
					listeningPoint.getHost(JainSipUtils.findUsePublicAddress(getSipNetworkInterfaceManager(), request, listeningPoint)));
			sipUri.setParameter(MessageDispatcher.ROUTE_PARAM_NODE_PORT, 
					"" + listeningPoint.getPort());
			javax.sip.address.Address routeAddress = 
				SipFactoryImpl.addressFactory.createAddress(sipUri);
			RouteHeader routeHeader = 
				SipFactoryImpl.headerFactory.createRouteHeader(routeAddress);			
			request.addFirst(routeHeader);
			if(Request.REGISTER.equalsIgnoreCase(request.getMethod())) {
				PathHeader pathHeader = 
						((HeaderFactoryExt)SipFactoryImpl.headerFactory).createPathHeader(routeAddress);
				request.addFirst(pathHeader);
			}
		} catch (ParseException e) {
			//this should never happen
			throw new IllegalArgumentException("Impossible to set the Load Balancer Route Header !", e);
		} catch (SipException e) {
			//this should never happen
			throw new IllegalArgumentException("Impossible to set the Load Balancer Route Header !", e);
		}
	}
	
	public void addIpLoadBalancerRouteHeader(Request request, String lbhost, int lbport) {
		try {
			String host = null;
			int port = -1; 
			OutboundProxy proxy = StaticServiceHolder.sipStandardService.getOutboundProxy();
			if(proxy == null) {
				host = lbhost;
				port = lbport;
			} else {
				host = proxy.getHost();
				port = proxy.getPort();	
			}
			javax.sip.address.SipURI sipUri = SipFactoryImpl.addressFactory.createSipURI(null, host);
			sipUri.setPort(port);
			sipUri.setLrParam();
			String transport = JainSipUtils.findTransport(request);
			sipUri.setTransportParam(transport);
			MobicentsExtendedListeningPoint listeningPoint = 
				getSipNetworkInterfaceManager().findMatchingListeningPoint(transport, false);
			sipUri.setParameter(MessageDispatcher.ROUTE_PARAM_NODE_HOST, 
					listeningPoint.getHost(JainSipUtils.findUsePublicAddress(getSipNetworkInterfaceManager(), request, listeningPoint)));
			sipUri.setParameter(MessageDispatcher.ROUTE_PARAM_NODE_PORT, 
					"" + listeningPoint.getPort());
			javax.sip.address.Address routeAddress = 
				SipFactoryImpl.addressFactory.createAddress(sipUri);
			RouteHeader routeHeader = 
				SipFactoryImpl.headerFactory.createRouteHeader(routeAddress);
			request.addFirst(routeHeader);			
		} catch (ParseException e) {
			//this should never happen
			throw new IllegalArgumentException("Impossible to set the Load Balancer Route Header !", e);
		} catch (SipException e) {
			//this should never happen
			throw new IllegalArgumentException("Impossible to set the Load Balancer Route Header !", e);
		}
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		useLoadBalancer = in.readBoolean();
		if(useLoadBalancer) {
			loadBalancerToUse = (SipLoadBalancer) in.readObject();
		}
		if(mobicentsSipServletMessageFactory == null) {
			mobicentsSipServletMessageFactory = initMobicentsSipServletMessageFactory();
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(useLoadBalancer);
		if(useLoadBalancer) {
			out.writeObject(loadBalancerToUse);
		}
	}
	
	public boolean isRouteOrphanRequests() {		
		return routeOrphanRequests;
	}
	
	public void setRouteOrphanRequests(boolean routeOrphanRequets) {
		this.routeOrphanRequests = routeOrphanRequets;
	}
	@Override
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, Address from, Address to) {
		throw new UnsupportedOperationException("Use the one createRequest(SipApplicationSession appSession, String method, Address from, Address to, String handler) method instead");
	}
	@Override
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, String from, String to) throws ServletParseException {
		throw new UnsupportedOperationException("Use the one createRequest(SipApplicationSession appSession, String method, String from, String to, String handler) method instead");
	}
	@Override
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, URI from, URI to) {
		throw new UnsupportedOperationException("Use the one createRequest(SipApplicationSession appSession, String method, URI from, URI to, String handler) method instead");
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactoryImpl#getAddressFactory()
	 */
	public AddressFactory getAddressFactory() {		
		return addressFactory;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactoryImpl#getHeaderFactory()
	 */
	public HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactoryImpl#getMessageFactory()
	 */
	public MessageFactory getMessageFactory() {
		return messageFactory;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactoryImpl#getSipFactory()
	 */
	public SipFactory getJainSipFactory() {
		return sipFactory;
	}

	@Override
	public MobicentsSipServletMessageFactory getMobicentsSipServletMessageFactory() {
		return mobicentsSipServletMessageFactory;
	}
	
	protected MobicentsSipServletMessageFactory initMobicentsSipServletMessageFactory() {
		MobicentsSipServletMessageFactory factory = null;
		try {
			factory = (MobicentsSipServletMessageFactory)
				Class.forName(StaticServiceHolder.sipStandardService.getMobicentsSipServletMessageFactoryClassName()).newInstance();
			factory.setMobicentsSipFactory(this);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Impossible to load the MobicentsSipServletMessageFactory ",e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Impossible to load the MobicentsSipServletMessageFactory ",e);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Impossible to load the MobicentsSipServletMessageFactory ",e);
		}	
		return factory;
	}
}
