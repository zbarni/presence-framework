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

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mobicents.xdm.common.util.dom.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class PublicationContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the document published
	 */
	private String document;
	private String componentModel;
	private String cmKey;

	/**
	 * the type of document published
	 */
	private String contentType;
	private String contentSubType;

	/**
	 * unmarshalled version of the document
	 */
	private transient Document documentAsDOM;

	public PublicationContent() {

	}

	public PublicationContent(String document, String contentType,
			String contentSubType) {
		this.document = document;
		this.contentSubType = contentSubType;
		this.contentType = contentType;
		this.componentModel = "";
	}

	public String getContentSubType() {
		return contentSubType;
	}

	public void setContentSubType(String contentSubType) {
		this.contentSubType = contentSubType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getDocumentAsString() {
		if (document == null && documentAsDOM != null) {
			Source source = new DOMSource(documentAsDOM);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			try {
				DomUtils.TRANSFORMER_FACTORY.newTransformer().transform(source,
						result);
				document = stringWriter.getBuffer().toString();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
		return document;
	}

	public void setDocumentAsString(String document) {
		this.document = document;
	}

	public Document getDocumentAsDOM() {
		if (documentAsDOM == null && document != null) {
			StringReader reader = new StringReader(document);
			try {
				documentAsDOM = DomUtils.DOCUMENT_BUILDER_NS_AWARE_FACTORY
						.newDocumentBuilder().parse(new InputSource(reader));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				reader.close();
			}
		}
		return documentAsDOM;
	}

	/**
	 * Sets unmarshalled version of the content for caching, this is not
	 * persisted.
	 * 
	 * @return
	 */
	public void setDocumentAsDOM(Document document) {
		this.documentAsDOM = document;
	}
	
	public String getLog () {
		return "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&77\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&77\n";
	}
	
	/**
	 * Parses the published document and extracts the component model, storing 
	 * it in componentModel variable.  
	 */
	public boolean parseComponentModel() {
		NodeList nodeList = documentAsDOM.getElementsByTagName("note");
		if (nodeList.getLength() == 0) 
			return false;
		Node node = nodeList.item(0);
		
		Node ua = documentAsDOM.getElementsByTagName("ua").item(0);
		this.cmKey = ua.getAttributes().getNamedItem("id").getNodeValue();		
		
		try {
			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();

			writer.getDomConfig().setParameter("xml-declaration", false); // Set this to true if the declaration is needed to be outputted.
			String s = writer.writeToString(node);
			if (!s.contains("<ua")) {
				return false;
			}
			
			this.componentModel = s.replaceAll("<note(.*?)>", "").trim();
			this.componentModel = this.componentModel.replaceAll("</note>", "").trim();

		} catch (Exception te) {
			te.printStackTrace();
		}
		return true;
	}
	
	public String getCMKey() {
		return this.cmKey;
	}
	
	public String getComponentModelAsString() {
		return this.componentModel;
	}

	/**
	 * Forces the update of the marshalled document, to be used when there is an
	 * update using the unmarshalled content
	 */
	public void forceDocumentUpdate() {
		setDocumentAsString(null);
		getDocumentAsString();
	}

}
