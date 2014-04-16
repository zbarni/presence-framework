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

package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;

import org.apache.log4j.Logger;

/**
 * This example shows a simple websocket app
 * 
 * @author Barna Zajzon
 *
 */
public class ProxyServlet extends SipServlet 
		implements SipErrorListener, Servlet {
	private static Logger logger = Logger.getLogger(ProxyServlet.class);
	private static final long serialVersionUID = 1L;

	private static final String PSHOST = "128.59.21.232:5080";
	private static Registrar registrar;
	@Resource
	SipFactory sipFactory;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("Proxy has been started");
		registrar = new Registrar(sipFactory);
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
	IOException {
		if(!request.isInitial()){
			return;
		}
		ArrayList<UAinstance> calleeAddressList = registrar
				.getAllUserAddress((SipURI)request.getTo().getURI());
		SipURI uri = null;
		Proxy proxy = request.getProxy(true);
		proxy.setRecordRoute(true);
		for (UAinstance ua : calleeAddressList) {
			Address callee = ua.getContactAddress();
			logger.info("Iterating addr: " + callee);
			try {
				uri = (SipURI)callee.getURI();
			}
			catch (Exception e) {
				logger.error("Could not create uri",e);
			}
			request.pushRoute(callee);
			proxy.proxyTo(uri);
		}

//		if request URI is not registered proxy request
		if (calleeAddressList.size() == 0) {
			logger.info("Proxying addr: ");
			proxy.proxyTo(request.getRequestURI());
		}
	}

	@Override
	protected void doBranchResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("doBranchResponse callback was called.");
		resp.getApplicationSession().setAttribute("branchResponseReceived", "true");
		super.doBranchResponse(resp);
	}	

	public void noAckReceived(SipErrorEvent ee) {
		logger.error("noAckReceived.");
	}

	/**
	 * {@inheritDoc}
	 */
	public void noPrackReceived(SipErrorEvent ee) {
		logger.error("noPrackReceived.");
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {
		String method = response.getMethod();
		if(logger.isInfoEnabled()) {
			logger.info("SimpleProxyServlet: Got response:" + method);
		}

		if(!"PRACK".equals(response.getMethod()) && response.getProxy() != null 
				&& response.getProxy().getOriginalRequest() != null) {
			logger.info("Original Sip Session is :" + response.getProxy().getOriginalRequest().getSession(false));
		}
		super.doResponse(response);
	}

	/**
	 * 
	 */
	protected void doRegister(SipServletRequest request) throws ServletException,
	IOException {
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		Address contact = request.getAddressHeader("Contact");
		SipURI fromUri = (SipURI)request.getFrom().getURI();
		String expires = request.getHeader("Expires");

		if (expires == null || Integer.parseInt(expires) == 0) {
			registrar.removeUser(contact,fromUri);
		}
		else {
			// expires > 0
			Address addr = registrar.addUser(contact,fromUri);
			logger.info("New contact header : " + addr);
			try {
				sipServletResponse.addHeader("Contact", addr.toString());
			}
			catch (IllegalArgumentException e) {
				logger.error("Could not add contact header",e);
			}
		}
		sipServletResponse.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
	IOException {
		SipServletResponse sipServletResponse = request.createResponse(200);

		// If the branchResponse callback was called we are good otherwise fail by
		// not delivering OK to BYE
		String doBranchRespValue = (String) request.getApplicationSession().getAttribute("branchResponseReceived");
		if("true".equals(doBranchRespValue))
			sipServletResponse.send();
	}


	protected void doCancel(SipServletRequest request) 
			throws ServletException, IOException {
		logger.error("CANCEL seen at proxy " + request);
	}

	protected void doPublish(SipServletRequest request) {
		if(logger.isInfoEnabled()) {
			logger.info("Got PUBLISH");
		}

		if(!registrar.isUriRegistered((SipURI) request.getRequestURI())) {
			logger.info("Could not find uri: " + request.getRequestURI());
			// TODO send error response
			return;
		}

		try {
			Proxy proxy = request.getProxy(true);
			try {
				SipURI psURI = (SipURI)sipFactory.createURI("sip:" + PSHOST);
				request.pushRoute(psURI);
				proxy.proxyTo(request.getRequestURI());
			}
			catch (ServletParseException e) {
				logger.error("Could not create SIP uri",e);
			}
		}
		catch (TooManyHopsException e) {
			logger.error("Too many hops",e);
		}
	}

	protected void doSubscribe(SipServletRequest request) {
		if(!registrar.isUriRegistered((SipURI) request.getRequestURI())) {
			logger.info("Could not find uri: " + request.getRequestURI());
			// TODO send error response
//			return;
			try {
				Proxy proxy = request.getProxy(true);
//					SipURI psURI = (SipURI)sipFactory.createURI("sip:" + PSHOST);
//					request.pushRoute(psURI);
					proxy.proxyTo(request.getRequestURI());
			}
			catch (TooManyHopsException e) {
				logger.error("Too many hops",e);
			}
			return;
		}

		try {
			Proxy proxy = request.getProxy(true);
			try {
				SipURI psURI = (SipURI)sipFactory.createURI("sip:" + PSHOST);
				request.pushRoute(psURI);
				proxy.proxyTo(request.getRequestURI());
			}
			catch (ServletParseException e) {
				logger.error("Could not create SIP uri",e);
			}
		}
		catch (TooManyHopsException e) {
			logger.error("Too many hops",e);
		}
	}
}