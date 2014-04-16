///*
// * JBoss, Home of Professional Open Source
// * Copyright 2011, Red Hat, Inc. and individual contributors
// * by the @authors tag. See the copyright.txt in the distribution for a
// * full listing of individual contributors.
// *
// * This is free software; you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as
// * published by the Free Software Foundation; either version 2.1 of
// * the License, or (at your option) any later version.
// *
// * This software is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this software; if not, write to the Free
// * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
// * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
// */
//
//package org.mobicents.servlet.sip.example;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import javax.annotation.Resource;
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
//import javax.servlet.sip.Address;
//import javax.servlet.sip.Proxy;
//import javax.servlet.sip.ServletParseException;
//import javax.servlet.sip.ServletTimer;
//import javax.servlet.sip.SipApplicationSession;
//import javax.servlet.sip.SipErrorEvent;
//import javax.servlet.sip.SipFactory;
//import javax.servlet.sip.SipServlet;
//import javax.servlet.sip.SipServletRequest;
//import javax.servlet.sip.SipServletResponse;
//import javax.servlet.sip.SipSession;
//import javax.servlet.sip.SipSession.State;
//import javax.servlet.sip.SipURI;
//import javax.servlet.sip.TimerListener;
//import javax.servlet.sip.TooManyHopsException;
//
//import org.apache.log4j.Logger;
//
///**
// * This example shows a simple websocket app
// * 
// * @author Vladimir Ralev
// *
// */
//public class b2buabackup extends SipServlet implements TimerListener {
//	private static Logger logger = Logger.getLogger(b2buabackup.class);
//	private static final long serialVersionUID = 1L;
//	private static final String ATTR_200_OK = "200_OK";
//	private static final String ATTR_CALL_PARTNER = "CALL";
//	private static final String ATTR_LAST_REQUEST = "LAST_REQUEST";
//	private static final String ATTR_LAST_RESPONSE = "LAST_RESPONSE";
//
//	private static final String publicContactURI = "<sip:128.59.21.232:5060>";
//	private static final String PSHOST = "128.59.21.232:5080";
//	private static Registrar registrar;
//	@Resource
//	SipFactory sipFactory;
//
//	HashMap<SipSession, ArrayList<SipSession>> sessions= new HashMap<SipSession, ArrayList<SipSession>>();
//	HashMap<SipSession, SipSession> outSessions= new HashMap<SipSession, SipSession>();
//	HashMap<SipSession, SipSession> callSessions= new HashMap<SipSession, SipSession>();
//	ArrayList<SipSession> publishRequests = new ArrayList<SipSession>();
//
//
//	@Override
//	public void init(ServletConfig servletConfig) throws ServletException {
//		logger.info("Proxy has been started");
//		registrar = new Registrar(sipFactory);
//		super.init(servletConfig);
//	}
//
//	private void addSession(SipSession key, SipSession value) {
//		ArrayList<SipSession> list = sessions.get(key);
//		if (list == null) {
//			list = new ArrayList<SipSession>();
//			sessions.put(key, list);
//		}
//		list.add(value);
//	}
//
//	private void getSession(SipSession key, SipSession value) {
//		ArrayList<SipSession> list = sessions.get(key);
//		if (list == null) {
//			list = new ArrayList<SipSession>();
//			sessions.put(key, list);
//		}
//		list.add(value);
//	}
//
//
//	/**
//	 * {@inheritDoc}
//	 */
//	protected void doInvite(SipServletRequest request) throws ServletException,
//	IOException {
//		if(!request.isInitial()){
//			return;
//		}
//
//		request.getSession().setAttribute(ATTR_LAST_REQUEST, request);
//		ArrayList<UAinstance> calleeAddressList = registrar
//				.getAllUserAddress((SipURI)request.getTo().getURI());
//		ArrayList<SipURI> uris = new ArrayList<SipURI>();
//		SipURI uri = null;
//		Proxy proxy = request.getProxy();
//		proxy.setRecordRoute(true);
//		for (UAinstance ua : calleeAddressList) {
//			Address callee = ua.getContactAddress();
//			logger.info("Iterating addr: " + callee);
//			try {
//				uri = (SipURI)callee.getURI();
//			}
//			catch (Exception e) {
//				logger.error("Could not create uri",e);
//			}
//
//			//			SipServletRequest outRequest = sipFactory.createRequest(
//			//					request.getApplicationSession(),
//			//					"INVITE", request.getFrom().getURI(), 
//			//					request.getTo().getURI());
//			//			outRequest.setRequestURI(callee.getURI());
//			//			if(request.getContent() != null) {
//			//				outRequest.setContent(request.getContent(), request.getContentType());
//			//			}
//			//			outRequest.getSession().setAttribute(ATTR_LAST_REQUEST, outRequest);
//			//			logger.info("############################## in INVITE session: " + request.getSession());
//			//			logger.info("############################## Outrequest INVITE session: " + outRequest.getSession() + "\nuri : " + outRequest.getRequestURI());
//			//			outRequest.send();
//			//			
//			//			addSession(request.getSession(), outRequest.getSession());
//			//			outSessions.put(outRequest.getSession(), request.getSession());
//			request.pushRoute(callee);
//			proxy.proxyTo(uri);
//		}
//		//		proxy.setParallel(true);			
//		// if request URI is not registered proxy request
//		//		if (calleeAddressList.size() == 0) {
//		//			logger.info("Proxying addr: ");
//		//			Proxy proxy = request.getProxy();
//		//			proxy.setRecordRoute(true);
//		//			proxy.proxyTo(request.getRequestURI());
//		//		}
//	}
//
//	private void addCallSession (SipSession s1, SipSession s2) {
//		callSessions.put(s1, s2);
//		callSessions.put(s2, s1);
//	}
//
//	public void noAckReceived(SipErrorEvent ee) {
//		logger.error("noAckReceived.");
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	public void noPrackReceived(SipErrorEvent ee) {
//		logger.error("noPrackReceived.");
//	}
//
//	protected void doAck(SipServletRequest request) throws ServletException,
//	IOException {
//		if(logger.isInfoEnabled()) {
//			logger.info("Got : " + request.getMethod());
//		}
//		logger.info("[ACK] request session: " + request.getSession());
//		SipServletResponse response = (SipServletResponse) request.getSession().getAttribute(ATTR_200_OK);
//		if (response != null) {
//			response.createAck().send();
//			SipApplicationSession sipApplicationSession = request.getApplicationSession();
//			// Defaulting the sip application session to 1h
//			sipApplicationSession.setExpires(60);
//			addCallSession(request.getSession(), response.getSession());
//			// remove other forked sessions
//			for (SipSession outSession : sessions.get(request.getSession())) {
//				if (outSession.equals(request.getSession())) {
//					continue;
//				}
//				SipServletRequest forkedRequest = (SipServletRequest) outSession.getAttribute(ATTR_LAST_REQUEST);
//				forkedRequest.createCancel();
//			}
//		}
//		else {
//			logger.info("[ACK] no last response found");
//		}
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	protected void doResponse(SipServletResponse response)
//			throws ServletException, IOException {
//		String method = response.getMethod();
//		if(logger.isInfoEnabled()) {
//			logger.info("SimpleProxyServlet: Got response:" + method);
//		}
//
//		if (method.equals("SUBSCRIBE") || method.equals("PUBLISH") || true) {
//			if(!"PRACK".equals(response.getMethod()) && response.getProxy() != null && response.getProxy().getOriginalRequest() != null) {
//				logger.info("Original Sip Session is :" + response.getProxy().getOriginalRequest().getSession(false));
//			}
//			super.doResponse(response);
//		}
//		else {
//			response.getSession().setAttribute(ATTR_LAST_RESPONSE, response);
//			SipServletRequest request;
//
//			// some switching
//			if (response.getMethod().equals("BYE")) {
//				request = (SipServletRequest) callSessions.get(response.
//						getSession()).getAttribute(ATTR_LAST_REQUEST);
//			} else {
//				request = (SipServletRequest) outSessions.get(response
//						.getSession()).getAttribute(ATTR_LAST_REQUEST);
//			}
//
//			SipServletResponse resp = request.createResponse(response.getStatus());
//			if (resp.getStatus() == 200 && resp.getMethod() == "INVITE") {
//				// setting the first 200 OK attribute in the incoming session
//				if (outSessions.get(response.getSession()).getAttribute(ATTR_200_OK) == null) {
//					outSessions.get(response.getSession()).setAttribute(ATTR_200_OK,response);
//				}
//			}
//			if(response.getContent() != null) {
//				resp.setContent(response.getContent(), response.getContentType());
//			}
//			resp.send();
//		}
//	}
////
////	private void doInviteResponse() {
////		
////	}
////	private void doByeResponse() {
////		
////	}
//	
//	protected void doRegister(SipServletRequest request) throws ServletException,
//	IOException {
//		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
//		Address contact = request.getAddressHeader("Contact");
//		SipURI fromUri = (SipURI)request.getFrom().getURI();
//		String expires = request.getHeader("Expires");
//
//		if (expires == null || Integer.parseInt(expires) == 0) {
//			registrar.removeUser(contact,fromUri);
//		}
//		else {
//			// expires > 0
//			Address addr = registrar.addUser(contact,fromUri);
//			logger.info("New contact header : " + addr);
//			try {
//				sipServletResponse.addHeader("Contact", addr.toString());
//			}
//			catch (IllegalArgumentException e) {
//				logger.error("Could not add contact header",e);
//			}
//		}
//		sipServletResponse.send();
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	protected void doBye(SipServletRequest request) throws ServletException,
//	IOException {
//		request.getSession().setAttribute(ATTR_LAST_REQUEST, request);
//		if(logger.isInfoEnabled()) {
//			logger.info("SimpleProxyServlet: Got BYE request:\n" + request.getMethod());
//		}
//		callSessions.get(request.getSession()).createRequest("BYE").send();
//	}
//
//
//	protected void doCancel(SipServletRequest request) 
//			throws ServletException, IOException {
//		if(logger.isInfoEnabled()) {
//			logger.info("Got CANCEL: " + request.toString());
//		}
//		request.getSession().setAttribute(ATTR_LAST_REQUEST, request);
//
//		for (SipSession session : sessions.get(request.getSession())) {
//			SipServletRequest req = (SipServletRequest)session.getAttribute(ATTR_LAST_REQUEST);
//			if (req != null) {
//				SipServletRequest cancel = req.createCancel();
//				cancel.send();
//			}
//		}
//	}
//
//	protected void doPublish(SipServletRequest request) {
//		if(logger.isInfoEnabled()) {
//			logger.info("Got PUBLISH");
//		}
//
//		if(!registrar.isUriRegistered((SipURI) request.getRequestURI())) {
//			logger.info("Could not find uri: " + request.getRequestURI());
//			// TODO send error response
//			return;
//		}
//
//		try {
//			Proxy proxy = request.getProxy(true);
//			try {
//				SipURI psURI = (SipURI)sipFactory.createURI("sip:" + PSHOST);
//				request.pushRoute(psURI);
//				proxy.proxyTo(request.getRequestURI());
//			}
//			catch (ServletParseException e) {
//				logger.error("Could not create SIP uri",e);
//			}
//		}
//		catch (TooManyHopsException e) {
//			logger.error("Too many hops",e);
//		}
//	}
//
//	protected void doSubscribe(SipServletRequest request) {
//		if(!registrar.isUriRegistered((SipURI) request.getRequestURI())) {
//			logger.info("Could not find uri: " + request.getRequestURI());
//			// TODO send error response
//			return;
//		}
//
//		try {
//			Proxy proxy = request.getProxy(true);
//			try {
//				SipURI psURI = (SipURI)sipFactory.createURI("sip:" + PSHOST);
//				request.pushRoute(psURI);
//				proxy.proxyTo(request.getRequestURI());
//			}
//			catch (ServletParseException e) {
//				logger.error("Could not create SIP uri",e);
//			}
//		}
//		catch (TooManyHopsException e) {
//			logger.error("Too many hops",e);
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see javax.servlet.sip.TimerListener#timeout(javax.servlet.sip.ServletTimer)
//	 */
//	public void timeout(ServletTimer servletTimer) {
//		SipSession sipSession = servletTimer.getApplicationSession().getSipSession((String)servletTimer.getInfo());
//		if(!State.TERMINATED.equals(sipSession.getState())) {
//			try {
//				sipSession.createRequest("BYE").send();
//			} catch (IOException e) {
//				logger.error("An unexpected exception occured while sending the BYE", e);
//			}				
//		}
//	}
//}