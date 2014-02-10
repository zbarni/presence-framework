/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.servlet.sip.core.dispatchers;

import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Iterator;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipApplicationRoutingRegionType;
import javax.servlet.sip.ar.SipRouteModifier;
import javax.servlet.sip.ar.SipTargetedRequestInfo;
import javax.servlet.sip.ar.SipTargetedRequestType;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.header.ContactHeader;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.Parameters;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;
import org.mobicents.servlet.sip.address.GenericURIImpl;
import org.mobicents.servlet.sip.address.RFC2396UrlDecoder;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.DispatcherException;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.core.SipSessionRoutingType;
import org.mobicents.servlet.sip.core.descriptor.MobicentsSipServletMapping;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * This class is responsible for implementing the logic for routing an initial request
 * according to the following sections of the JSR 289 specification Section 
 * 15.4.1 Procedure for Routing an Initial Request.
 * 15.11 Session Targeting 
 * 16 Mapping Requests To Servlets 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class InitialRequestDispatcher extends RequestDispatcher {

	private static final Logger logger = Logger.getLogger(InitialRequestDispatcher.class);
	
	public InitialRequestDispatcher() {}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchMessage(SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws DispatcherException {
		final SipApplicationRouter sipApplicationRouter = sipApplicationDispatcher.getSipApplicationRouter();
		final MobicentsSipFactory sipFactoryImpl = sipApplicationDispatcher.getSipFactory();
		final SipServletRequestImpl sipServletRequest = (SipServletRequestImpl) sipServletMessage;
		if(logger.isDebugEnabled()) {
			logger.debug("Routing of Initial Request " + sipServletRequest);
		}		
		final Request request = (Request) sipServletRequest.getMessage();
		
		final RouteHeader poppedRoute = sipServletRequest.getPoppedRouteHeader();
		if(logger.isDebugEnabled()) {
			logger.debug("popped route : " + poppedRoute);
		}
		MobicentsSipSession sipSessionImpl = sipServletRequest.getSipSession();
		//set directive from popped route header if it is present			
		Serializable stateInfo = null;		
		SipApplicationRouterInfo applicationRouterInfo = null;
		//If request is received from an external SIP entity, directive is set to NEW. 
		SipApplicationRoutingDirective sipApplicationRoutingDirective = SipApplicationRoutingDirective.NEW;
		SipApplicationRoutingRegion routingRegion = null;
		if(poppedRoute != null) {
			Parameters poppedAddress = (Parameters)poppedRoute.getAddress().getURI();
			// get the state info associated with the request because it means 
			// that is has been routed back to the container			
			String directive = poppedAddress.getParameter(ROUTE_PARAM_DIRECTIVE);
			if(directive != null && directive.length() > 0) {
				//If request is received from an application, directive is set either implicitly 
				//or explicitly by the application. 
				if(logger.isDebugEnabled()) {
					logger.debug("directive before the request has been routed back to container : " + directive);
				}
				sipApplicationRoutingDirective = SipApplicationRoutingDirective.valueOf(
						SipApplicationRoutingDirective.class, directive);
				String regionLabel = poppedAddress.getParameter(ROUTE_PARAM_REGION_LABEL);
				String regionType = poppedAddress.getParameter(ROUTE_PARAM_REGION_TYPE);
				if(regionLabel != null) {
					routingRegion = new SipApplicationRoutingRegion(
							regionLabel,
							SipApplicationRoutingRegionType.valueOf(SipApplicationRoutingRegionType.class,regionType));
				}
				String previousAppName = poppedAddress.getParameter(ROUTE_PARAM_PREV_APPLICATION_NAME);
				String previousAppId = poppedAddress.getParameter(ROUTE_PARAM_PREV_APP_ID);
				if(logger.isDebugEnabled()) {
					logger.debug("application name before the request has been routed back to container : " + previousAppName);
					logger.debug("application session id before the request has been routed back to container : " + previousAppId);
				}
				SipContext sipContext = sipApplicationDispatcher.findSipApplication(previousAppName);				
				SipSessionKey sipSessionKey = SessionManagerUtil.getSipSessionKey(previousAppId, previousAppName, request, false);
				sipSessionImpl = sipContext.getSipManager().getSipSession(sipSessionKey, false, sipFactoryImpl, null);
				//If request is received from an application, and directive is CONTINUE or REVERSE, 
				//stateInfo is set to that of the original request that this request is associated with. 
				stateInfo = sipSessionImpl.getStateInfo();
				applicationRouterInfo = sipSessionImpl.getNextSipApplicationRouterInfo();
				sipSessionImpl.setNextSipApplicationRouterInfo(null);
				sipServletRequest.setSipSession(sipSessionImpl);
				if(logger.isDebugEnabled()) {
					logger.debug("state info before the request has been routed back to container : " + stateInfo);
					logger.debug("router info before the request has been routed back to container : " + applicationRouterInfo);
				}
			}
		} else if(sipSessionImpl != null) {
			stateInfo = sipSessionImpl.getStateInfo();
			sipApplicationRoutingDirective = sipServletRequest.getRoutingDirective();
			if(logger.isDebugEnabled()) {
				logger.debug("previous state info : " + stateInfo);
			}
		}				
		
		// 15.11.3 Target Session : Encode URI mechanism
		// Upon receiving an initial request for processing, a container MUST check the topmost Route header and 
		// Request-URI (in that order) to see if it contains an encoded URI. 
		// If it does, the container MUST use the encoded URI to locate the targeted SipApplicationSession object
		URI requestURI = request.getRequestURI();
		// From horacimacias : Fix for Issue 2115 MSS unable to handle GenericURI URIs
		// check the URI is an instance of Parameters. GenericURI doesn't implement Parameters
		String targetedApplicationKey;
		if(requestURI instanceof Parameters) {
			targetedApplicationKey = ((Parameters)requestURI).getParameter(MobicentsSipApplicationSession.SIP_APPLICATION_KEY_PARAM_NAME);
		} else {
			targetedApplicationKey = null;
		}
		MobicentsSipApplicationSession encodeURISipApplicationSession = retrieveTargetedApplication(targetedApplicationKey);
		SipTargetedRequestInfo targetedRequestInfo = null;
		if(encodeURISipApplicationSession != null) {
			targetedRequestInfo = new SipTargetedRequestInfo(SipTargetedRequestType.ENCODED_URI, encodeURISipApplicationSession.getApplicationName());
		} else if(poppedRoute != null) {
			Parameters poppedAddress = (Parameters)poppedRoute.getAddress().getURI();
			targetedApplicationKey = poppedAddress.getParameter(MobicentsSipApplicationSession.SIP_APPLICATION_KEY_PARAM_NAME);
			encodeURISipApplicationSession = retrieveTargetedApplication(targetedApplicationKey);
			if(encodeURISipApplicationSession != null) {
				targetedRequestInfo = new SipTargetedRequestInfo(SipTargetedRequestType.ENCODED_URI, encodeURISipApplicationSession.getApplicationName());
			}
		}	
		
		// 15.11.4 Join and Replaces Targeting Mechanism		
		SipTargetedRequestInfo joinReplacesTargetedRequestInfo = null;
		MobicentsSipSession joinReplacesCorrespondingSession = null;
		JoinHeader joinHeader = (JoinHeader)request.getHeader(JoinHeader.NAME);
		ReplacesHeader replacesHeader = (ReplacesHeader)request.getHeader(ReplacesHeader.NAME);
		if(joinHeader != null || replacesHeader != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("One Join or Replaces Header has been found : JoinHeader = " + joinHeader + ", ReplacesHeader = " + replacesHeader);
			}
			// 15.11.4.1 If a container supporting [RFC 3911] or [RFC 3891] receives an initial INVITE request with Join or Replaces header, 
			// the container must first ensure that the request passes the RFC-defined validation rules.
			if(!Request.INVITE.equals(request.getMethod())) {
				throw new DispatcherException(Response.BAD_REQUEST, "A Join or Replaces Header cannot be present in a request other than INVITE as per RFC 3911, Section 4 or RFC 3891, Section 3. Check your request " + request);
			}
			if(joinHeader != null && replacesHeader != null) {
				throw new DispatcherException(Response.BAD_REQUEST, "A Join Header and a Replaces Header cannot be present as per RFC 3911, Section 4 in the same request " + request);
			}
			Dialog joinReplacesDialog = null;
			if(joinHeader != null) {
				joinReplacesDialog = ((SipStackExt)sipProvider.getSipStack()).getJoinDialog(joinHeader);
			}
			if(replacesHeader != null) {
				joinReplacesDialog = ((SipStackExt)sipProvider.getSipStack()).getReplacesDialog(replacesHeader);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("joinReplacesDialog = " + joinReplacesDialog + ", targetedRequestInfo = " + targetedRequestInfo);
			}
			if(targetedRequestInfo != null) {
				// If no match is found, but the Request-URI in the INVITE corresponds to a conference URI, the UAS MUST ignore the Join header and continue
				// processing the INVITE as if the Join header did not exist
			}  else if(joinReplacesDialog == null && targetedRequestInfo == null) {				
				// Issue 2048 : Should comply with 15.11.4.4.If no SipSession matching the tuple is found, 
				// the container MUST pass the request to the Application Router as an untargeted request, i.e., where the SipTargetedRequestInfo argument to getNextApplication() is null.
				// so nothing to do here
				
				//Otherwise if no match is found, the UAS rejects the INVITE and returns a 481 Call/Transaction Does Not Exist response.
//				throw new DispatcherException(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "Join/Replaces Header : no match is found as per RFC 3911, Section 4 or RFC 3891, Section 3 in the request " + request);
			} else if(joinReplacesDialog != null && ((TransactionApplicationData)joinReplacesDialog.getApplicationData()) != null && !Request.INVITE.equals(((TransactionApplicationData)joinReplacesDialog.getApplicationData()).getSipServletMessage().getMethod())) {
				//Likewise, if the Join/Replaces header field matches a dialog which was not created with an INVITE, the UAS MUST reject the request with a 481 response.
				throw new DispatcherException(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "Join/Replaces header field matches a dialog which was not created with an INVITE as per RFC 3911, Section 4 or RFC 3891, Section 3 in the request " + request);
			} else if(joinReplacesDialog != null && DialogState.TERMINATED.equals(joinReplacesDialog.getState())) {
				// If the Join/Replaces header field matches a dialog which has already terminated, the UA SHOULD decline the request with a 603 Declined response.
				throw new DispatcherException(Response.DECLINE, "Join/Replaces header field matches a dialog which has already terminated as per RFC 3911, Section 4 or RFC 3891, Section 3 in the request " + request);
			}
			//TODO If the initiator of the new INVITE has authenticated successfully as equivalent to the user who is being joined, then the join is authorized
			//TODO Alternatively, the Referred-By mechanism [9] defines a mechanism that the UAS can use to verify that a join request was sent on behalf of the other participant in the matched dialog
			
			// 15.11.4.2. For a request that passes the validation step, the container MUST attempt to locate the SipSession object matching 
			// the tuple from the Join or Replaces header. In locating this SipSession, the container MUST not match a SipSession 
			// owned by an application that has acted as a proxy with respect to the candidate SipSession. 
			// It must only match a SipSession for an application that has acted as a UA. 
			// If the matching SipSession is found, the container Session Targeting must locate the corresponding SipApplicationSession 
			// object along with the application name of the application that owns the SipApplicationSession.
			joinReplacesCorrespondingSession = retrieveSipSession(joinReplacesDialog);			
			if(joinReplacesCorrespondingSession != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("the following matching sip session has been found for the Join or Replaces Header " + joinReplacesCorrespondingSession.getKey());
				}
				// 15.11.4.3. The container MUST populate a SipTargetedRequestInfo object with the type corresponding to whichever header 
				// appears in the request (e.g. JOIN or REPLACES) and with the application name that owns the identified SipApplicationSession 
				// and SipSession. 
				// The container MUST then pass the request to the Application Router’s getNextApplication() method as a targeted request i.e., 
				// along with the populated SipTargetedRequestInfo object.
				SipTargetedRequestType sipTargetedRequestType = null;
				if(joinHeader != null) {
					sipTargetedRequestType = SipTargetedRequestType.JOIN;
				} 
				if(replacesHeader != null) {
					sipTargetedRequestType = SipTargetedRequestType.REPLACES;
				}
				joinReplacesTargetedRequestInfo = new SipTargetedRequestInfo(sipTargetedRequestType, joinReplacesCorrespondingSession.getKey().getApplicationName());
			} else {
				// 15.11.4.4.If no SipSession matching the tuple is found, the container MUST pass the request to the Application Router as an untargeted request, i.e., where the SipTargetedRequestInfo argument to getNextApplication() is null.
				if(logger.isDebugEnabled()) {
					logger.debug("no matching sip session found for the Join or Replaces Header");
				}
			}
		}		
		if(joinReplacesTargetedRequestInfo != null) {
			targetedRequestInfo = joinReplacesTargetedRequestInfo;
		}
		
		// 15.4.1 Routing an Initial request Algorithm
		// 15.4.1 Procedure : point 1			
		
		//the application router shouldn't modify the request
		sipServletRequest.setReadOnly(true);
		// if the router info is null, it means that the request comes from the external
		if(applicationRouterInfo == null) {
			applicationRouterInfo =  
				sipApplicationRouter.getNextApplication(
						sipServletRequest, 
						routingRegion, 
						sipApplicationRoutingDirective,
						targetedRequestInfo,
						stateInfo);
		} else if (logger.isDebugEnabled()){
			logger.debug("application name selected by the AR before re routing this request back to the container " + applicationRouterInfo.getNextApplicationName());
		}
		
		sipServletRequest.setReadOnly(false);
		
		// 15.4.1 Procedure : point 2
		if(checkRouteModifier(applicationRouterInfo, sipServletRequest)) {
			return;
		}
		// 15.4.1 Procedure : point 3
		if(applicationRouterInfo.getNextApplicationName() == null) {
			dispatchOutsideContainer(sipServletRequest);
		} else {			
			dispatchInsideContainer(sipProvider, applicationRouterInfo, sipServletRequest, sipFactoryImpl, joinReplacesCorrespondingSession, encodeURISipApplicationSession);
		}		
	}

	/**
	 * Dispatch a request inside the container based on the information returned by the AR
	 * @param sipProvider the sip Provider on which the request has been received 
	 * @param applicationRouterInfo information returned by the AR
	 * @param sipServletRequest the request to dispatch
	 * @param sipFactoryImpl the sip factory
	 * @param joinReplacesSipSession the potential corresponding Join/Replaces SipSession previously found, can be null
	 * @throws DispatcherException a problem occured while dispatching the request
	 */
	private void dispatchInsideContainer(final SipProvider sipProvider, final SipApplicationRouterInfo applicationRouterInfo, final SipServletRequestImpl sipServletRequest, final MobicentsSipFactory sipFactoryImpl, MobicentsSipSession joinReplacesSipSession, MobicentsSipApplicationSession encodeURISipApplicationSession) throws DispatcherException {
		final String nextApplicationName = applicationRouterInfo.getNextApplicationName();
		if(logger.isDebugEnabled()) {
			logger.debug("Dispatching the request event to " + nextApplicationName);
		}
		final Request request = (Request) sipServletRequest.getMessage();
		sipServletRequest.setCurrentApplicationName(nextApplicationName);
		final SipContext sipContext = sipApplicationDispatcher.findSipApplication(nextApplicationName);
		// follow the procedures of Chapter 16 to select a servlet from the application.									
		//no matching deployed apps
		if(sipContext == null) {
			// the app returned by the Application Router returned an app
			// that is not currently deployed, sends a 500 error response as was discussed 
			// in the JSR 289 EG, (for reference thread "Questions regarding AR" initiated by Uri Segev)
			// and stops processing.
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "No matching deployed application has been found !");
		}			
		final SipManager sipManager = sipContext.getSipManager();
		
		// subscriber URI should be set before calling makeAppSessionKey method, see Issue 750
		// http://code.google.com/p/mobicents/issues/detail?id=750
		try {
			URI subscriberUri = SipFactoryImpl.addressFactory.createAddress(applicationRouterInfo.getSubscriberURI()).getURI();
			javax.servlet.sip.URI jainSipSubscriberUri = null; 
			if(subscriberUri instanceof javax.sip.address.SipURI) {
				jainSipSubscriberUri= new SipURIImpl((javax.sip.address.SipURI)subscriberUri, ModifiableRule.NotModifiable);
			} else if (subscriberUri instanceof javax.sip.address.TelURL) {
				jainSipSubscriberUri = new TelURLImpl((javax.sip.address.TelURL)subscriberUri);
			} else {
				jainSipSubscriberUri = new GenericURIImpl(subscriberUri);
			}
			sipServletRequest.setSubscriberURI(jainSipSubscriberUri);
		} catch (ParseException pe) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to parse the subscriber URI returned by the Application Router " 
					+ applicationRouterInfo.getSubscriberURI() +
					", please put one of DAR:<HeaderName> with Header containing a valid URI or an exlicit valid URI ", pe);
		} 		
		
		MobicentsSipApplicationSession sipApplicationSession = encodeURISipApplicationSession;
		if(logger.isDebugEnabled())  {
			logger.debug("the encoded URI Sip Application Session is " + sipApplicationSession);
		}
		if(joinReplacesSipSession != null && nextApplicationName.equals(joinReplacesSipSession.getKey().getApplicationName())) {
			// 15.11.4.5. If the Application Router returns an application name that matches the application name found in step 15.11.4.2, 
			// then the container must create a SipSession object and associate it with the SipApplicationSession identified in step 2. 
			final JoinHeader joinHeader = (JoinHeader)request.getHeader(JoinHeader.NAME);
			final ReplacesHeader replacesHeader = (ReplacesHeader)request.getHeader(ReplacesHeader.NAME);
			String headerName = null;
			if(joinHeader != null) {
				headerName = JoinHeader.NAME;
			} else if (replacesHeader != null) {
				headerName = ReplacesHeader.NAME;	
			}
			
			sipApplicationSession = joinReplacesSipSession.getSipApplicationSession();
			if(logger.isDebugEnabled()) {
				logger.debug("Reusing the application session from the Join/Replaces " + sipApplicationSession.getId());
			}
			SipApplicationSessionKey sipApplicationSessionKey = makeAppSessionKey(
					sipContext, sipServletRequest, nextApplicationName);
			sipContext.getSipSessionsUtil().addCorrespondingSipApplicationSession(sipApplicationSessionKey, sipApplicationSession.getKey(), headerName);
		} else if(sipApplicationSession == null) {
			// 15.11.4.6. If the Application Router returns an application name that does not match the application name found in step 15.11.4.2,
			// then the container invokes the application using its normal procedure, creating a new SipSession and SipApplicationSession.

			SipApplicationSessionKey sipApplicationSessionKey = makeAppSessionKey(
					sipContext, sipServletRequest, nextApplicationName);
			sipApplicationSession = sipManager.getSipApplicationSession(
					sipApplicationSessionKey, true);

			if(StaticServiceHolder.sipStandardService.isHttpFollowsSip()) {
				String jvmRoute = StaticServiceHolder.sipStandardService.getJvmRoute();
				if(jvmRoute != null) {
					sipApplicationSession.setJvmRoute(jvmRoute);
				}
			}
		}	
		
		//sip application session association
		final MobicentsSipApplicationSession appSession = sipApplicationSession;
		//sip session association
		final SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(appSession.getKey().getId(), nextApplicationName, request, false);
		final MobicentsSipSession sipSessionImpl = sipManager.getSipSession(sessionKey, true, sipFactoryImpl, appSession);
		sipServletRequest.setSipSession(sipSessionImpl);

		if(joinReplacesSipSession != null && nextApplicationName.equals(joinReplacesSipSession.getKey().getApplicationName())) {
			final JoinHeader joinHeader = (JoinHeader)request.getHeader(JoinHeader.NAME);
			final ReplacesHeader replacesHeader = (ReplacesHeader)request.getHeader(ReplacesHeader.NAME);
			String headerName = null;
			if(joinHeader != null) {
				headerName = JoinHeader.NAME;
			} else if (replacesHeader != null) {
				headerName = ReplacesHeader.NAME;	
			}
			// 15.11.4.5. If the Application Router returns an application name that matches the application name found in step 15.11.4.2, 
			// then the container must create a SipSession object and associate it with the SipApplicationSession identified in step 2. 
			// The association of this newly created SipSession with the one found in step 15.11.4.2 is made available to the application 
			// through the SipSessionsUtil.getCorrespondingSipSession(SipSession session, String headerName) method.			
			sipContext.getSipSessionsUtil().addCorrespondingSipSession(sipSessionImpl, joinReplacesSipSession, headerName);		
		}
		
		// set the request's stateInfo to result.getStateInfo(), region to result.getRegion(), and URI to result.getSubscriberURI().			
		sipSessionImpl.setStateInfo(applicationRouterInfo.getStateInfo());
		sipSessionImpl.setRoutingRegion(applicationRouterInfo.getRoutingRegion());
		sipServletRequest.setRoutingRegion(applicationRouterInfo.getRoutingRegion());		
		sipSessionImpl.setSipSubscriberURI(sipServletRequest.getSubscriberURI().toString());
		final long cSeq = ((CSeqHeader)request.getHeader(CSeqHeader.NAME)).getSeqNumber();
		sipSessionImpl.setCseq(cSeq);
		
		if(request.getMethod().equals(Request.INVITE)) {
			sipSessionImpl.setRequestsPending(sipSessionImpl.getRequestsPending() + 1);
			sipSessionImpl.setAckReceived(cSeq, false);			
		}
		final InitialDispatchTask dispatchTask = new InitialDispatchTask(sipServletRequest, sipProvider);
		// we enter the sip app here, thus acuiring the semaphore on the session (if concurrency control is set) before the jain sip tx semaphore is released and ensuring that
		// the tx serialization is preserved		
		sipContext.enterSipApp(sipApplicationSession, sipSessionImpl, false);
		
		// The fastest way to figure out the transport is the mandatory Via transport header 
		ViaHeader via = (ViaHeader) request.getHeader(ViaHeader.NAME);
		sipSessionImpl.setTransport(via.getTransport());
		
		handleSipOutbound(sipServletRequest);
		
		// if the flag is set we bypass the executor. This flag should be made deprecated 
		if(sipApplicationDispatcher.isBypassRequestExecutor() || ConcurrencyControlMode.Transaction.equals((sipContext.getConcurrencyControlMode()))) {
			dispatchTask.dispatchAndHandleExceptions();
		} else {
			getConcurrencyModelExecutorService(sipContext, sipServletRequest).execute(dispatchTask);
		}

	}
	
	
	/**
	 * NOTE! This is only a partial implementation and will only work
	 * when we are acting as a UAS as per RFC 5626 Section 4.3. Sending Non-REGISTER Requests.
	 * Need to add some more logic for proxy scenarios. See RFC 5626 for exact details.
	 * 
	 * The purpose of this method is to examine the request in an effort
	 * to determine if the logic of RFC 5626 should be applied. I.e., should
	 * we create and store a "flow" for this request so that subsequent requests
	 * will use the same connection as they "arrived" on.
	 * 
	 * @param request
	 */
	private void handleSipOutbound(final SipServletRequestImpl sipServletRequest) {

		// if this is not a dialog creating request, then bail out right away
		if (!JainSipUtils.DIALOG_CREATING_METHODS.contains(sipServletRequest.getMethod())) {
			return;
		}		
		
		final Request request = (Request) sipServletRequest.getMessage();
		final ContactHeader contact = (ContactHeader) request.getHeader(ContactHeader.NAME);				
				
		final URI requestUri = request.getRequestURI();
		
		//
		if ((contact != null && contact.getAddress().getURI() instanceof Parameters && ((Parameters)contact.getAddress().getURI()).getParameter(SIP_OUTBOUND_PARAM_OB) != null)) {
			final String remoteHost = sipServletRequest.getRemoteAddr();
			final int remotePort = sipServletRequest.getRemotePort();
			final String transport = sipServletRequest.getTransport();
			final MobicentsSipSession sipSessionImpl = sipServletRequest.getSipSession();

			try {							
				final javax.sip.address.SipURI flowURI = SipFactoryImpl.addressFactory.createSipURI(null, remoteHost);
				flowURI.setPort(remotePort);
				flowURI.setTransportParam(transport);

				sipSessionImpl.setFlow(flowURI);
			} catch (final ParseException e) {
				logger.warn("Unable to create new flow URI from " + remoteHost + ":" 
						+ remotePort + ";transport=" + transport + " due to a parse exception");
			} 
		} else if(sipServletRequest.getPoppedRouteHeader() != null && (requestUri instanceof javax.sip.address.SipURI && ((javax.sip.address.SipURI)requestUri).getParameter(SIP_OUTBOUND_PARAM_OB) != null)) {
			// If there is a route on the request, then we are acting as a proxy
			final javax.sip.address.SipURI poppedURI = (javax.sip.address.SipURI) sipServletRequest.getPoppedRouteHeader().getAddress().getURI();
			String flowToken = poppedURI.getUser();
			//TODO validate flow token and information and check it against session flow token
			// See Section 5.3
			return;					
		}	
	}


	/**
	 * Dispatch a request outside the container
	 * @param sipServletRequest request
	 * @throws DispatcherException problem occured when dispatching the request outside the container
	 */
	private void dispatchOutsideContainer(SipServletRequestImpl sipServletRequest) throws DispatcherException {
		final Request request = (Request) sipServletRequest.getMessage();
		if(logger.isInfoEnabled()) {
			logger.info("Dispatching the request event outside the container");
		}
		//check if the request point to another domain
		if(request.getRequestURI() instanceof TelURL) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "cannot dispatch a request with a tel url request uri outside the container ");
		}
		javax.sip.address.SipURI sipRequestUri = (javax.sip.address.SipURI)request.getRequestURI();
		
		String host = sipRequestUri.getHost();
		int port = sipRequestUri.getPort();
		String transport = JainSipUtils.findTransport(request);
		boolean isAnotherDomain = sipApplicationDispatcher.isExternal(host, port, transport);			
		ListIterator<String> routeHeaders = sipServletRequest.getHeaders(RouteHeader.NAME);				
		if(isAnotherDomain || routeHeaders.hasNext()) {
			try {
				forwardRequestStatefully(sipServletRequest, SipSessionRoutingType.PREVIOUS_SESSION, SipRouteModifier.NO_ROUTE);
			} catch (Exception e) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to forward statefully the following subsequent request " + request, e);
			}
		} else {
			// the Request-URI does not point to another domain, and there is no Route header, 
			// the container should not send the request as it will cause a loop. 
			// Instead, the container must reject the request with 404 Not Found final response with no Retry-After header.
			throw new DispatcherException(Response.NOT_FOUND, "the Request-URI does not point to another domain, and there is no Route header," + 
					"the container should not send the request as it will cause a loop. " +
					"Instead, the container must reject the request with 404 Not Found final response with no Retry-After header. You may want to check your dar configuration file to see if the request can be handled or make sure you use the correct Application Router jar.");				
		}
	}

	/**
	 * JSR 289 Section 15.4.1 Procedure : point 2
	 * Check the result.getRouteModifier() :
	 * 	If result.getRouteModifier() is ROUTE, then get the routes using result.getRoutes()
	 * 		If the first returned route is external (does not belong to this container) then 
	 * 		push all of the routes on the Route header stack of the request and send the request externally. 
	 * 		Note that the first returned route becomes the top route header of the request.
	 * 	
	 * 		If the first returned route is internal then the container MUST make it available 
	 * 		to the applications via the SipServletRequest.getPoppedRoute() method and ignore the remaining ones, if any. 
	 * 		This allows the AR modify the popped route before passing it to the application.
	 * 
	 * If result.getRouteModifier() is ROUTE_BACK then push a route back to the container followed 
	 * by the external routes obtained from result.getRoutes() and send the request externally. 
	 * Note that the container route SHOULD include AR state encoded as a route parameter in order for the AR to continue processing the application chain once the request returns back to the container.
	 * 
	 * If result.getRouteModifier() is NO_ROUTE then disregard the result.getRoutes() and proceed.
	 * 
	 * @param applicationRouterInfo the applicationRouterInfo returned by the AR
	 * @param sipServletRequest the request
	 * @return true if the request has been routed outside, false otherwise
	 * @throws DispatcherException a proble occured while checking the information returned by the AR
	 */
	private final boolean checkRouteModifier(SipApplicationRouterInfo applicationRouterInfo, SipServletRequestImpl sipServletRequest) throws DispatcherException {		
		final SipRouteModifier sipRouteModifier = applicationRouterInfo.getRouteModifier();
		if(logger.isDebugEnabled()) {
			logger.debug("the AR returned the following sip route modifier" + sipRouteModifier);
		}
		if(sipRouteModifier != null) {
			final Request request = (Request) sipServletRequest.getMessage();
			final String[] routes = applicationRouterInfo.getRoutes();
			switch(sipRouteModifier) {
				// ROUTE modifier indicates that SipApplicationRouterInfo.getRoute() returns a valid route,
				// it is up to container to decide whether it is external or internal.
				case ROUTE :					
					Address routeAddress = null; 
					RouteHeader applicationRouterInfoRouteHeader = null;
					try {
						routeAddress = SipFactoryImpl.addressFactory.createAddress(routes[0]);
						applicationRouterInfoRouteHeader = SipFactoryImpl.headerFactory.createRouteHeader(routeAddress);										
						if(sipApplicationDispatcher.isRouteExternal(applicationRouterInfoRouteHeader)) {						
							// push all of the routes on the Route header stack of the request and 
							// send the request externally
							for (int i = routes.length-1 ; i >= 0; i--) {
								routeAddress = SipFactoryImpl.addressFactory.createAddress(routes[i]);
								URI routeURI = routeAddress.getURI();
								if(routeURI.isSipURI()) {
									((javax.sip.address.SipURI)routeURI).setLrParam();
								}
								Header routeHeader = SipFactoryImpl.headerFactory.createRouteHeader(routeAddress);
								// Fix for Issue 280 provided by eelcoc
								try {
									request.addFirst(routeHeader);
								} catch (SipException e) {
									throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to add a route header to route externally the following initial request " + request, e);
								}
							}
							if(logger.isDebugEnabled()) {
								logger.debug("Routing the request externally " + sipServletRequest );
							}
							request.setRequestURI(SipFactoryImpl.addressFactory.createURI(routes[0]));
							try {
								forwardRequestStatefully(sipServletRequest, null, sipRouteModifier);
								return true;
							} catch (Exception e) {
								throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to forward statefully the following initial request " + request, e);
							}							
						} else {
							// the container MUST make the route available to the applications 
							// via the SipServletRequest.getPoppedRoute() method.							
							sipServletRequest.setPoppedRoute(applicationRouterInfoRouteHeader);
							break;
						}
					} catch (ParseException e) {
						// the AR returned an empty string route or a bad route
						// this shouldn't happen if the route modifier is ROUTE, processing is stopped
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to parse the route returned by the application router " +
								"into a compliant address" ,e);																						
					}	
				// NO_ROUTE indicates that application router is not returning any route 
				// and the SipApplicationRouterInfo.getRoute() value if any should be disregarded.	
				case NO_ROUTE :
					//nothing to do here
					//disregard the result.getRoute() and proceed. Specifically the SipServletRequest.getPoppedRoute() 
					//returns the same value as before the invocation of application router.
					break;
				// ROUTE_BACK directs the container to push its own route before 
				// pushing the external routes obtained from SipApplicationRouterInfo.getRoutes(). 
				case ROUTE_BACK :
					try {
						// Push container Route, pick up the first outbound interface
						SipURI sipURI = sipApplicationDispatcher.getOutboundInterfaces().get(0);
						sipURI.setParameter("modifier", "route_back");						
						Header routeHeader = SipFactoryImpl.headerFactory.createHeader(RouteHeader.NAME, sipURI.toString());
						// Fix for Issue 280 provided by eelcoc
						try {
							request.addFirst(routeHeader);
							// push all of the routes on the Route header stack of the request and 
							// send the request externally
							for (int i = routes.length-1 ; i >= 0; i--) {
								routeHeader = SipFactoryImpl.headerFactory.createHeader(RouteHeader.NAME, routes[i]);
								request.addFirst(routeHeader);
							}
						} catch (SipException e) {
							throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to add a route header to route externally the following initial request " + request, e);
						}
						if(logger.isDebugEnabled()) {
							logger.debug("Routing the request externally " + sipServletRequest );
						}					
						try {
							forwardRequestStatefully(sipServletRequest, null, sipRouteModifier);
							return true;
						} catch (Exception e) {
							throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to forward statefully the following subsequent request " + request, e);
						}										
					} catch (ParseException e) {
						// the AR returned an empty string route or a bad route
						// this shouldn't happen if the route modifier is ROUTE, processing is stopped
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to parse the route returned by the application router " +
								"into a compliant address" ,e);							
					}	
			}			
		}
		return false;
	}

	/**
	 * Section 15.11.3 Encode URI Mechanism
	 * The container MUST use the encoded URI to locate the targeted SipApplicationSession object. 
	 * If a valid SipApplicationSession is found, the container must determine 
	 * the name of the application that owns the SipApplicationSession object.
	 * 
	 * @param targetedApplicationKey the encoded URI parameter
	 * @return null if no corresponding SipApplicationSession could be found or 
	 * if the param is null, the SipTargetedRequestInfo completed otherwise
	 */
	private final MobicentsSipApplicationSession retrieveTargetedApplication(
			String targetedApplicationKey) {
		if( targetedApplicationKey != null && 
				targetedApplicationKey.length() > 0) {
			targetedApplicationKey = RFC2396UrlDecoder.decode(targetedApplicationKey);
			SipApplicationSessionKey targetedApplicationSessionKey;
			try {
				targetedApplicationSessionKey = SessionManagerUtil.parseSipApplicationSessionKey(targetedApplicationKey);
			} catch (ParseException e) {
				logger.error("Couldn't parse the targeted application key " + targetedApplicationKey, e);
				return null;
			}
			SipContext sipContext = sipApplicationDispatcher.findSipApplication(targetedApplicationSessionKey.getApplicationName());
			if(sipContext != null) {
				return sipContext.getSipManager().getSipApplicationSession(targetedApplicationSessionKey, false);
			}						
		}
		return null;
	}
	
	/**
	 * Try to find a matching Sip Session to a given dialog
	 * @param dialog the dialog to find the session
	 * @return the matching session, null if not session have been found
	 */
	private MobicentsSipSession retrieveSipSession(Dialog dialog) {
		
		if(dialog != null) {
			Iterator<SipContext> iterator = sipApplicationDispatcher.findSipApplications();
			while(iterator.hasNext()) {
				SipContext sipContext = iterator.next();
				SipManager sipManager = sipContext.getSipManager();
				
				Iterator<MobicentsSipSession> sipSessionsIt= sipManager.getAllSipSessions();
				while (sipSessionsIt.hasNext()) {
					MobicentsSipSession mobicentsSipSession = (MobicentsSipSession) sipSessionsIt
							.next();
					MobicentsSipSessionKey sessionKey = mobicentsSipSession.getKey();				
					if(sessionKey.getCallId().trim().equals(dialog.getCallId().getCallId())) {
						if(logger.isDebugEnabled()) {
							logger.debug("found session with the same Call Id " + sessionKey + ", to Tag " + sessionKey.getToTag());
							logger.debug("dialog localParty = " + dialog.getLocalParty().getURI() + ", localTag " + dialog.getLocalTag());
							logger.debug("dialog remoteParty = " + dialog.getRemoteParty().getURI() + ", remoteTag " + dialog.getRemoteTag());
						}
						if(sessionKey.getFromTag().equals(dialog.getLocalTag()) && sessionKey.getToTag().equals(dialog.getRemoteTag())) {
							if(mobicentsSipSession.getProxy() == null) {
								return mobicentsSipSession;	
							}
						} else if (sessionKey.getFromTag().equals(dialog.getRemoteTag()) && sessionKey.getToTag().equals(dialog.getLocalTag())){
							if(mobicentsSipSession.getProxy() == null) {
								return mobicentsSipSession;	
							}
						}
					}
				}
			}
		}
		
		return null;
	}
	
	public static class InitialDispatchTask extends DispatchTask {
		
		InitialDispatchTask(SipServletRequestImpl sipServletRequest, SipProvider sipProvider) {
			super(sipServletRequest, sipProvider);
		}
		
		public void dispatch() throws DispatcherException {
			final SipServletRequestImpl sipServletRequest = (SipServletRequestImpl)sipServletMessage;
			final MobicentsSipSession sipSessionImpl = sipServletRequest.getSipSession();
			final MobicentsSipApplicationSession appSession = sipSessionImpl.getSipApplicationSession();
			final SipContext sipContext = appSession.getSipContext();
			final Request request = (Request) sipServletRequest.getMessage();
			
			boolean batchStarted = sipContext.enterSipAppHa(true);
			try {
				sipSessionImpl.setSessionCreatingTransactionRequest(sipServletRequest);
				
				String sipSessionHandlerName = sipSessionImpl.getHandler();
				if(logger.isDebugEnabled()) {
					logger.debug("sipSessionHandlerName = " + sipSessionHandlerName);
				}
				if(sipSessionHandlerName == null || sipSessionHandlerName.length() < 1) {
					String mainServlet = appSession.getCurrentRequestHandler();
					if(logger.isDebugEnabled()) {
						logger.debug("current request Handler = " + sipSessionHandlerName);
					}
					//Fix for Issue 1200 : if we are not in a main-servlet servlet selection type, the mapping discovery should always be performed 
					if(sipContext.isMainServlet() && mainServlet != null && mainServlet.length() > 0) {
						sipSessionHandlerName = mainServlet;				
					} else {
						MobicentsSipServletMapping sipServletMapping = sipContext.findSipServletMappings(sipServletRequest);
						if(sipServletMapping == null && sipContext.getSipRubyController() == null) {
							logger.error("Sending 404 because no matching servlet found for this request ");
							sendErrorResponse(Response.NOT_FOUND, sipServletRequest, sipProvider);
							return;
						} else if(sipServletMapping != null) {							
							sipSessionHandlerName = sipServletMapping.getServletName();
							if(logger.isDebugEnabled()) {
								logger.debug("servlet mapping Handler Name= " + sipSessionHandlerName);
							}
						}
					}
					if(sipSessionHandlerName!= null) {							
						try {
							sipSessionImpl.setHandler(sipSessionHandlerName);
						} catch (ServletException e) {
							// this should never happen
							throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while routing an initial request",e);
						} 
					}
				}
				try {
					callServlet(sipServletRequest);
					if(logger.isInfoEnabled()) {
						logger.info("Request event dispatched to " + sipContext.getApplicationName());
					}					
				} catch (ServletException e) {
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while routing the following initial request " + request, e);
				} catch (IOException e) {				
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected IO exception occured while routing the following initial request " + request, e);
				}
			} finally {
				// those information are only available for initial requests (not responses, nor subsequent requests) so putting them back to null
				// to save memory and avoid unecessary replication
				sipSessionImpl.setRoutingRegion(null);
				sipSessionImpl.setSipSubscriberURI(null);
				
				// exitSipAppHa completes the replication task. It might block for a while if the state is too big
				// We should never call exitAipApp before exitSipAppHa, because exitSipApp releases the lock on the
				// Application of SipSession (concurrency control lock). If this happens a new request might arrive
				// and modify the state during Serialization or other non-thread safe operation in the serialization
				sipContext.exitSipAppHa(sipServletRequest, null, batchStarted);
				sipContext.exitSipApp(appSession, sipSessionImpl);				
			}
			//nothing more needs to be done, either the app acted as UA, PROXY or B2BUA. in any case we stop routing							
		}		
	}
}
