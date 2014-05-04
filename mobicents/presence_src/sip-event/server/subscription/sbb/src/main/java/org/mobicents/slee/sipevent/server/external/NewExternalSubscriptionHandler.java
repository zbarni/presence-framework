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

package org.mobicents.slee.sipevent.server.external;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.ExpiresHeader;
import javax.sip.message.Request;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.java.slee.resource.sip.DialogActivity;

import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *  
 * 
 */
public class NewExternalSubscriptionHandler {

	private static Tracer tracer;
	
	private ExternalSubscriptionHandler externalSubscriptionHandler;
	public static final String subscriber = "sip:presence@128.59.21.232";
	private final String eventPackage = "presence";
	private final String subscriberDisplayName = "The Zajzon Project";

	public NewExternalSubscriptionHandler(
			ExternalSubscriptionHandler externalSubscriptionHandler) {
		this.externalSubscriptionHandler = externalSubscriptionHandler;
		if (tracer == null) {
			tracer = externalSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}

	public void newExternalSubscription(RequestEvent event,
			 ActivityContextInterface aci) {

		if (tracer.isFineEnabled()) {
			tracer.fine("EXTERNAL()");
		}
		SubscriptionControlSbb sbb = externalSubscriptionHandler.sbb;
		tracer.info("\n\n\n################################ INITIATING EXTERNAL SUBSCRIPTION" );

		//expires 
		ExpiresHeader expiresHeader = event.getRequest().getExpires();
		int expires = expiresHeader == null ? sbb.getConfiguration()
				.getDefaultExpires() : expiresHeader.getExpires();
		// check if expires is not less than the allowed min expires
		if (expires >= sbb.getConfiguration().getMinExpires()) {
			// ensure expires is not bigger than max expires
			if (expires > sbb.getConfiguration().getMaxExpires()) {
				expires = sbb.getConfiguration().getMaxExpires();
			}
		} else {
			// expires is > 0 but < min expires, respond (Interval
			// Too Brief) with Min-Expires = MINEXPIRES
			return;
		}

		SipURI uri = (SipURI) event.getRequest().getRequestURI();
		String paramList = getParameters(uri);

		ArrayList<Address> userAgentList = getGruuList(stripAllParameters(uri));
		
		for (Address addr : userAgentList) {
			ClientTransaction clientTransaction = null;
			String notifier = addr.getURI().toString() + paramList;

			tracer.info("Susbcribing to : "+ notifier);
			// prepare request, we get a client transaction with dialog ID back
			try {
				clientTransaction = externalSubscriptionHandler.getSubscriber()
						.prepareInitialRequest(subscriber,notifier);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (clientTransaction == null) {
				tracer.info("Client transaction is null, can not create external subscription");
			}
			
			String subscriptionId = subscriber + ":" + notifier + ":" + eventPackage;
			SubscriptionKey key = new SubscriptionKey(clientTransaction.getDialog().getDialogId(),					
					eventPackage, subscriptionId);
			
			// find subscription
			Subscription subscription = sbb.getConfiguration().getDataSource().get(key);

			if (subscription != null) {
				// subscription exists, should notify subscribers, no?
				tracer.info("Subscription already exists");
			} 
			else {
				createAndSendSubscribe(event.getRequest(), notifier, key, subscriptionId, clientTransaction);
			}
		}
	}
	
	private void createAndSendSubscribe(Request request, 
			String notifierParam,
			SubscriptionKey key,
			String subscriptionId,
			ClientTransaction clientTransaction) {
		SubscriptionControlSbb sbb;
		Notifier notifier;
		ActivityContextInterface subscriptionAci = null;
		
		sbb = externalSubscriptionHandler.sbb;
		notifier = new Notifier(notifierParam);
		subscriptionAci = sbb.getSipActivityContextInterfaceFactory().getActivityContextInterface(clientTransaction);
		
		if (subscriptionAci == null) {
			tracer.severe("Failed to find dialog activity context interface");
		}

		try {
			sbb.getActivityContextNamingfacility().bind(subscriptionAci,key.toString());
		} catch (Exception e) {
			tracer.severe("Failed to create internal subscription aci", e);
			return;
		}
		
		Subscription newSubscription = new Subscription(key, subscriber, notifier, 
				Subscription.Status.pending, 
				subscriberDisplayName, 300, false, 
				externalSubscriptionHandler.sbb.getConfiguration().getDataSource());
		
		externalSubscriptionHandler.sbb.setSubscriptionTimerAndPersistSubscription(
				newSubscription, 299, subscriptionAci);
		
		if (tracer.isInfoEnabled()) {
			tracer.info("Created " + newSubscription);
		}
	}
	
	private String getParameters(SipURI uri) {
		String p = "";
		for (Iterator<String> it = uri.getParameterNames(); it.hasNext(); ) {
			String param = it.next();
			p += ";" + param + "=" + uri.getParameter(param); 
		}
		return p;
	}
	
	public SipURI stripAllParameters(SipURI uri) {
		SipURI newUri = (SipURI)uri.clone();
		for (Iterator<String> iter = uri.getParameterNames();iter.hasNext();) {
			newUri.removeParameter(iter.next());
		}
		return newUri;
	}
	
	public Document getDocumentFromString (String xmlString) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	    DocumentBuilder builder;
	    Document document = null;
	    try  
	    {  
	        builder = factory.newDocumentBuilder();  
	        document = builder.parse( new InputSource( new StringReader( xmlString ) ) );  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }
	    return document;
	}
	
	private ArrayList<Address> getGruuList(SipURI uri) {
		SubscriptionControlSbb sbb = externalSubscriptionHandler.sbb;
		ArrayList<Address> list = new ArrayList<Address>();
		try {
			PreparedStatement verifyStatement = sbb.getDBConnection().prepareStatement("SELECT * FROM master_documents WHERE sip_uri=?");
			verifyStatement.setString(1, uri.toString());
			ResultSet rs = verifyStatement.executeQuery();
			// previous doc exists
			while (rs.next()) {
				String masterDoc = rs.getString(2);
				
				Document document = getDocumentFromString(masterDoc); 
				NodeList uaList = document.getElementsByTagName("ua");
				for (int i=0; i < uaList.getLength(); ++i) {
					Node ua = uaList.item(i);
					Node idNode = ua.getAttributes().getNamedItem("id");
					// <ua> tag has id attribute, correct
					if (idNode != null) {
						Address gruuAddress = sbb.getAddressFactory().createAddress(idNode.getNodeValue());
						list.add(gruuAddress);
					}
				}
			}
		}
		catch (SQLException e) {
			tracer.info(e.toString());
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}
}