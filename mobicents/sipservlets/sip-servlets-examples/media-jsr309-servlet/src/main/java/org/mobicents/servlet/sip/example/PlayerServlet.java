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
import java.util.Iterator;
import java.util.Properties;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.spi.DriverImpl;

/**
 * This example shows a simple usage of JSR 309.
 * 
 * @author amit bhayani
 * 
 */
public class PlayerServlet extends SipServlet implements ServletContextListener {
	private static final String MS_CONTROL_FACTORY = "MsControlFactory";
	private static Logger logger = Logger.getLogger(PlayerServlet.class);
	private static final long serialVersionUID = 1L;
	/**
	 * The JSR309 Impl is over the MGCP Stack and hence IP Address/Port of MGCP
	 * Stack of Call Agent (CA) and Media Gateway (MGW) needs to be passed to
	 * get instance of MsControlFactory
	 * 
	 * Each Sip Servlet Application can have it's own unique MGCP Stack by
	 * providing different MGCP_STACK_NAME
	 */

	// Property key for the Unique MGCP stack name for this application 
    public static final String MGCP_STACK_NAME = "mgcp.stack.name"; 
    // Property key for the IP address where CA MGCP Stack (SIP Servlet 
    // Container) is bound 
    public static final String MGCP_STACK_IP = "mgcp.server.address"; 
    // Property key for the port where CA MGCP Stack is bound 
    public static final String MGCP_STACK_PORT = "mgcp.local.port"; 
    // Property key for the IP address where MGW MGCP Stack (MMS) is bound 
    public static final String MGCP_PEER_IP = "mgcp.bind.address"; 
    // Property key for the port where MGW MGCP Stack is bound 
    public static final String MGCP_PEER_PORT = "mgcp.server.port"; 

	/**
	 * In this case MGW and CA are on same local host
	 */
	public static final String LOCAL_ADDRESS = System.getProperty(
			"jboss.bind.address", "127.0.0.1");
	protected static final String CA_PORT = "2727";

	public static final String PEER_ADDRESS = System.getProperty(
			"jboss.bind.address", "127.0.0.1");
	protected static final String MGW_PORT = "2427";

	protected boolean isBye = false;

	public PlayerServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		if (logger.isDebugEnabled()) {
			logger.debug("the simple sip servlet has been started");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("MediaPlaybackServlet: Got request:\n"
				+ request.getMethod());

		isBye = false;

		SipServletResponse sipServletResponse = request
				.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();

		SipSession sipSession = request.getSession();

		try {
			MsControlFactory msControlFactory = (MsControlFactory) getServletContext().getAttribute(MS_CONTROL_FACTORY);
			// Create new media session and store in SipSession
			MediaSession mediaSession = (MediaSession) msControlFactory
					.createMediaSession();

			sipSession.setAttribute("MEDIA_SESSION", mediaSession);
			mediaSession.setAttribute("SIP_SESSION", sipSession);

			// Store INVITE so it can be responded to later
			sipSession.setAttribute("UNANSWERED_INVITE", request);

			// Create a new NetworkConnection and store in SipSession
			NetworkConnection conn = mediaSession
					.createNetworkConnection(NetworkConnection.BASIC);

			SdpPortManager sdpManag = conn.getSdpPortManager();

			NetworkConnectionListener ncListener = new NetworkConnectionListener();

			sdpManag.addListener(ncListener);

			byte[] sdpOffer = request.getRawContent();

			sdpManag.processSdpOffer(sdpOffer);

		} catch (MsControlException e) {
			logger.error(e);
			request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR)
					.send();
		}

	}

	protected void terminate(SipSession sipSession, MediaSession mediaSession) {
		SipServletRequest bye = sipSession.createRequest("BYE");
		try {
			bye.send();
			// Clean up media session
			mediaSession.release();
			sipSession.removeAttribute("MEDIA_SESSION");
		} catch (Exception e1) {
			log("Terminating: Cannot send BYE: " + e1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("MediaPlaybackServlet: Got BYE request:\n" + request);
		isBye = true;
		MediaSession mediaSession = (MediaSession) request.getSession()
				.getAttribute("MEDIA_SESSION");
		mediaSession.release();
		request.getSession().removeAttribute("MEDIA_SESSION");

		SipServletResponse sipServletResponse = request.createResponse(200);
		sipServletResponse.send();
		// releasing the media connection

	}

	private class NetworkConnectionListener implements
			MediaEventListener<SdpPortManagerEvent> {

		public void onEvent(SdpPortManagerEvent event) {

			SdpPortManager sdpmana = event.getSource();
			NetworkConnection conn = sdpmana.getContainer();
			MediaSession mediaSession = event.getSource().getMediaSession();

			SipSession sipSession = (SipSession) mediaSession
					.getAttribute("SIP_SESSION");

			SipServletRequest inv = (SipServletRequest) sipSession
					.getAttribute("UNANSWERED_INVITE");
			sipSession.removeAttribute("UNANSWERED_INVITE");

			if (event.isSuccessful()) {
				SipServletResponse resp = inv
						.createResponse(SipServletResponse.SC_OK);
				try {
					byte[] sdp = event.getMediaServerSdp();

					resp.setContent(sdp, "application/sdp");
					// Send 200 OK
					resp.send();
					if (logger.isDebugEnabled()) {
						logger.debug("Sent OK Response for INVITE");
					}

					sipSession.setAttribute("NETWORK_CONNECTION", conn);

				} catch (Exception e) {
					logger.error(e);

					// Clean up
					sipSession.getApplicationSession().invalidate();
					mediaSession.release();
				}
			} else {
				try {
					if (SdpPortManagerEvent.SDP_NOT_ACCEPTABLE.equals(event
							.getError())) {

						if (logger.isDebugEnabled()) {
							logger
									.debug("Sending SipServletResponse.SC_NOT_ACCEPTABLE_HERE for INVITE");
						}
						// Send 488 error response to INVITE
						inv.createResponse(
								SipServletResponse.SC_NOT_ACCEPTABLE_HERE)
								.send();
					} else if (SdpPortManagerEvent.RESOURCE_UNAVAILABLE
							.equals(event.getError())) {
						if (logger.isDebugEnabled()) {
							logger
									.debug("Sending SipServletResponse.SC_BUSY_HERE for INVITE");
						}
						// Send 486 error response to INVITE
						inv.createResponse(SipServletResponse.SC_BUSY_HERE)
								.send();
					} else {
						if (logger.isDebugEnabled()) {
							logger
									.debug("Sending SipServletResponse.SC_SERVER_INTERNAL_ERROR for INVITE");
						}
						// Some unknown error. Send 500 error response to INVITE
						inv.createResponse(
								SipServletResponse.SC_SERVER_INTERNAL_ERROR)
								.send();
					}
					// Clean up media session
					sipSession.removeAttribute("MEDIA_SESSION");
					mediaSession.release();
				} catch (Exception e) {
					logger.error(e);

					// Clean up
					sipSession.getApplicationSession().invalidate();
					mediaSession.release();
				}
			}
		}

	}
	Properties properties = null;

	public void contextDestroyed(ServletContextEvent event) {
		Iterator<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasNext()) {
			Driver driver = drivers.next();
			DriverManager.deregisterDriver(driver);
			DriverImpl impl = (DriverImpl) driver;
			impl.shutdown();
		}
	}

	public void contextInitialized(ServletContextEvent event) {
		if(event.getServletContext().getAttribute(MS_CONTROL_FACTORY) == null) {
			properties = new Properties();
			properties.setProperty(MGCP_STACK_NAME, "SipServlets");
			properties.setProperty(MGCP_PEER_IP, PEER_ADDRESS);
			properties.setProperty(MGCP_PEER_PORT, MGW_PORT);
	
			properties.setProperty(MGCP_STACK_IP, LOCAL_ADDRESS);
			properties.setProperty(MGCP_STACK_PORT, CA_PORT);
	
			try {
				// create the Media Session Factory
                final MsControlFactory msControlFactory = new DriverImpl().getFactory(properties); 
				event.getServletContext().setAttribute(MS_CONTROL_FACTORY, msControlFactory);
				logger.info("started MGCP Stack on " + LOCAL_ADDRESS + "and port " + CA_PORT);
			} catch (Exception e) {
				logger.error("couldn't start the underlying MGCP Stack", e);
			}
		} else {
			logger.info("MGCP Stack already started on " + LOCAL_ADDRESS + "and port " + CA_PORT);
		}
	}
}
