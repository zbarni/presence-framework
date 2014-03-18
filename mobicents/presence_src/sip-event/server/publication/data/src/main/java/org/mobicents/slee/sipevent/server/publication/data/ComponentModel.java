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

package org.mobicents.slee.sipevent.server.publication.data;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *  
 *     
 * @author barna zajzon
 *
 */
public class ComponentModel extends PublicationContent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8020033417766370446L;

	/**
	 * key is the base sip uri
	 */   
	private final String entityKey;
	private Document document;
	
	private static Logger logger = Logger.getLogger(ComponentModel.class);
	
	public static ComponentModel fromPublication(Publication p) {
		final ComponentModel cm = new ComponentModel(p.getPublicationKey().getEntity());
		cm.setDocumentAsString(p.getDocumentAsString());
		cm.setContentType(p.getContentType());
		cm.setContentSubType(p.getContentSubType());
		cm.setDocumentAsDOM(p.getDocumentAsDOM());
		cm.parseComponentModel();
		
		return cm;
	}
	
	public ComponentModel(String entity) {
		super();
		this.entityKey = entity;
		try {
			this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e) {
			logger.error("Could not create DOM document", e);
		}
	}
	
	public boolean addPublication(Publication p) {
		// check if publication is already in DOM
		Element ua = document.getElementById(p.getCMKey());
		if (ua == null) 
			return false;
		
		return true;
	}
	
	public void removePublication(Publication p) {
		
	}

	// -- GETTERS AND SETTERS

}
