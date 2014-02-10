/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.message;

import gov.nist.javax.sip.header.HeaderExt;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.Transaction;
import javax.sip.header.AcceptLanguageHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentLanguageHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderAddress;
import javax.sip.header.Parameters;
import javax.sip.header.RequireHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.ReplicationStrategy;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;
import org.mobicents.servlet.sip.address.ParameterableHeaderImpl;
import org.mobicents.servlet.sip.core.MobicentsExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletMessage;
import org.mobicents.servlet.sip.core.security.SipPrincipal;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * Implementation of SipServletMessage
 * 
 * @author mranga
 * @author jean.deruelle@telestax.com
 * 
 */
public abstract class SipServletMessageImpl implements MobicentsSipServletMessage, Externalizable {

	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SipServletMessageImpl.class
			.getCanonicalName());
	
	private static final String CONTENT_TYPE_TEXT = "text";
	private static final String CONTENT_TYPE_MULTIPART = "multipart";
	private static final String MULTIPART_BOUNDARY = "boundary";
	private static final String MULTIPART_START = "start";
	private static final String MULTIPART_BOUNDARY_DELIM = "--";
	private static final String LINE_RETURN_DELIM = "\n";
	public static final String REL100_OPTION_TAG = "100rel";
//	private static final String HCOLON = " : ";
	
	protected Message message;
	protected SipFactoryImpl sipFactoryImpl;
	protected MobicentsSipSessionKey sessionKey;
	//lazy loaded and not serialized to avoid unecessary replication
	protected transient MobicentsSipSession sipSession;

	protected Map<String, Object> attributes;
	// Made it transient for Issue 1523 : http://code.google.com/p/mobicents/issues/detail?id=1523
	// NotSerializableException happens if a message is stored in the sip session during HA
	private transient Transaction transaction;
	// used for failover to recover the transaction
	private String transactionId;
	private boolean transactionType;
	
	// We need this object separate from transaction.getApplicationData, because the actualy transaction
	// may be create later and we still need to accumulate useful data. Also the transaction might be
	// cleaned up earlier. The transaction and this object have different lifecycle.
	protected TransactionApplicationData transactionApplicationData;		

	protected HeaderForm headerForm = HeaderForm.DEFAULT;
	
	// IP address of the next upstream/downstream hop from which this message
	// was received. Applications can determine the actual IP address of the UA
	// that originated the message from the message Via header fields.
	// But for upstream - thats a proxy stuff, fun with ReqURI, RouteHeader
	protected transient InetAddress remoteAddr = null;

	protected transient int remotePort = -1;

	protected transient String transport = null;

	protected String currentApplicationName = null;
	
	protected transient SipPrincipal userPrincipal;
	
	protected boolean isMessageSent;
	// Made it transient for Issue 1523 : http://code.google.com/p/mobicents/issues/detail?id=1523
	// NotSerializableException happens if a message is stored in the sip session during HA
	protected transient Dialog dialog;
	
	protected transient String method;
	
	// needed for orphan routing
	boolean orphan;
	private String appSessionId;
	
	// needed for externalizable
	public SipServletMessageImpl () {}
	
	protected SipServletMessageImpl(Message message,
			SipFactoryImpl sipFactoryImpl, Transaction transaction,
			MobicentsSipSession sipSession, Dialog dialog) {
		if (sipFactoryImpl == null)
			throw new NullPointerException("Null factory");
		if (message == null)
			throw new NullPointerException("Null message");
//		if (sipSession == null)
//			throw new NullPointerException("Null session");
		this.sipFactoryImpl = sipFactoryImpl;
		this.message = message;
		this.transaction = transaction;
		if(sipSession != null) {
			this.sessionKey = sipSession.getKey();
		}
		if(transaction != null && getMethod().equals(Request.INVITE)) {
			if(transaction.getApplicationData() != null) {
				this.transactionApplicationData = (TransactionApplicationData) transaction.getApplicationData();
			}
		} 
		if(transactionApplicationData == null){
			this.transactionApplicationData = new TransactionApplicationData(this);
		}
		isMessageSent = false;
		this.dialog = dialog;
		
		if(sipSession != null && dialog != null) {
			sipSession.setSessionCreatingDialog(dialog);
			if(dialog.getApplicationData() == null) {
				dialog.setApplicationData(transactionApplicationData);
			}
		}
		
		// good behaviour, lets make some default
		//seems like bad behavior finally 
		//check http://forums.java.net/jive/thread.jspa?messageID=260944
		// => commented out
//		if (this.message.getContentEncoding() == null)
//			try {
//				this.message.addHeader(this.headerFactory
//						.createContentEncodingHeader(this.defaultEncoding));
//			} catch (ParseException e) {
//				logger.debug("Couldnt add deafualt enconding...");
//				e.printStackTrace();
//			}

		if (transaction != null && transaction.getApplicationData() == null) {
			transaction.setApplicationData(transactionApplicationData);
		}
	}

	private void checkCommitted() {
		if(this.isCommitted()) {
			throw new IllegalStateException("This message is in committed state. You can not modify it");
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#addAcceptLanguage(java.util.Locale)
	 */
	public void addAcceptLanguage(Locale locale) {
		checkCommitted();
		AcceptLanguageHeader ach = SipFactoryImpl.headerFactory
				.createAcceptLanguageHeader(locale);
		message.addHeader(ach);

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#addAddressHeader(java.lang.String, javax.servlet.sip.Address, boolean)
	 */
	public void addAddressHeader(String name, Address addr, boolean first)
			throws IllegalArgumentException {

		checkCommitted();
		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled()) {
			logger.debug("Adding address header [" + hName + "] as first ["
					+ first + "] value [" + addr + "]");
		}

		//we should test for 
		//This method can be used with headers which are defined to contain one 
		//or more entries matching (name-addr | addr-spec) *(SEMI generic-param) as defined in RFC 3261
//		if (!isAddressTypeHeader(hName)) {
//			logger.error("Header [" + hName + "] is not address type header");
//			throw new IllegalArgumentException("Header[" + hName
//					+ "] is not of an address type");
//		}
		
		if (isSystemHeader(getModifiableRule(hName))) {
			logger.error("Error, can't add system header [" + hName + "]");
			throw new IllegalArgumentException("Header[" + hName
					+ "] is system header, cant add, modify it!!!");
		}

		if (hName.equalsIgnoreCase("From") || hName.equalsIgnoreCase("To")) {
			logger.error("Error, can't add From or To header [" + hName + "]");
			throw new IllegalArgumentException(
					"Can't add From or To header, see JSR 289 Section 4.1.2");
		}

		try {
			String nameToAdd = getCorrectHeaderName(hName);
			Header h = SipFactoryImpl.headerFactory.createHeader(nameToAdd, addr.toString());

			if (first) {
				this.message.addFirst(h);
			} else {
				this.message.addLast(h);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Error adding header", e);
		}

	}

	public void addHeaderInternal(String name, String value, boolean bypassSystemHeaderCheck) {
		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Adding header under name [" + hName + "]");

		if (!bypassSystemHeaderCheck && isSystemHeader(getModifiableRule(hName))) {

			logger.error("Cant add system header [" + hName + "]");

			throw new IllegalArgumentException("Header[" + hName
					+ "] is system header, cant add,cant modify it!!!");
		}

		String nameToAdd = getCorrectHeaderName(hName);
		
		try {
			// Fix to Issue 1015 by alexander.kozlov.IV			
			if(JainSipUtils.SINGLETON_HEADER_NAMES.contains(name)) {
				Header header = SipFactory.getInstance().createHeaderFactory().createHeader(nameToAdd, value);
				this.message.setHeader(header);				
			} else {	
				// Dealing with Allow:INVITE, ACK, CANCEL, OPTIONS, BYE kind of values
				if(JainSipUtils.LIST_HEADER_NAMES.contains(name)) {
					List<Header> headers = SipFactory.getInstance().createHeaderFactory()
						.createHeaders(name + ":" + value);
					for (Header header : headers) {
						this.message.addHeader(header);
					}
				} else {
					// Extension Header: those cannot be lists. See jain sip issue 270
					Header header = SipFactory.getInstance().createHeaderFactory()
						.createHeader(name, value);
					this.message.addLast(header);
				}				
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied ", ex);
		}
	}
	
	public void setHeaderInternal(String name, String value, boolean bypassSystemHeaderCheck) {
		if(name == null) {
			throw new NullPointerException ("name parameter is null");
		}
		if(value == null) {
			throw new NullPointerException ("value parameter is null");
		}
		if(!bypassSystemHeaderCheck && isSystemHeader(getModifiableRule(name))) {
			throw new IllegalArgumentException(name + " is a system header !");
		}		
		
		try {
			Header header = SipFactory.getInstance().createHeaderFactory()
				.createHeader(name, value);
			this.message.setHeader(header);				
		} catch (Exception e) {
			throw new IllegalArgumentException("Error creating header!", e);
		}
	}
	
	//check if the submitted value is of the form header-value *(COMMA header-value)
//	private boolean isMultipleValue(String value) {
//		StringTokenizer tokenizer = new StringTokenizer(value, ",");
//		tokenizer.nextToken();
//		return tokenizer.hasMoreTokens();
//	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String name, String value) {
		checkCommitted();
		addHeaderInternal(name, value, false);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#addParameterableHeader(java.lang.String, javax.servlet.sip.Parameterable, boolean)
	 */
	public void addParameterableHeader(String name, Parameterable param,
			boolean first) {
		checkCommitted();
		try {
			String hName = getFullHeaderName(name);

			if (logger.isDebugEnabled())
				logger.debug("Adding parametrable header under name [" + hName
						+ "] as first [" + first + "] value [" + param + "]");

			String body = param.toString();

			String nameToAdd = getCorrectHeaderName(hName);

			Header header = SipFactoryImpl.headerFactory.createHeader(nameToAdd,
					body);
			if (first)
				this.message.addFirst(header);
			else
				this.message.addLast(header);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAcceptLanguage()
	 */
	public Locale getAcceptLanguage() {
		// See section 14.4 of RFC 2616 (HTTP/1.1) for more information about how the Accept-Language header 
		// must interpreted to determine the preferred language of the client.
		Locale preferredLocale = null;
		float q = 0;
		Iterator<Header> it = (Iterator<Header>) this.message
			.getHeaders(AcceptLanguageHeader.NAME);
		while (it.hasNext()) {
			AcceptLanguageHeader alh = (AcceptLanguageHeader) it.next();
			if(preferredLocale == null) {
				preferredLocale = alh.getAcceptLanguage();
				q = alh.getQValue();
			} else {
				if(alh.getQValue() > q) {
					preferredLocale = alh.getAcceptLanguage();
					q = alh.getQValue();
				}
			}
		}
		return preferredLocale;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAcceptLanguages()
	 */
	public Iterator<Locale> getAcceptLanguages() {
		LinkedList<Locale> ll = new LinkedList<Locale>();
		Iterator<Header> it = (Iterator<Header>) this.message
				.getHeaders(AcceptLanguageHeader.NAME);
		while (it.hasNext()) {
			AcceptLanguageHeader alh = (AcceptLanguageHeader) it.next();
			ll.add(alh.getAcceptLanguage());
		}
		return ll.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAddressHeader(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Address getAddressHeader(String name) throws ServletParseException {
		if (name == null)
			throw new NullPointerException();

		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Fetching address header for name [" + hName + "]");

//		if (!isAddressTypeHeader(hName)) {
//			logger.error("Header of name [" + hName
//					+ "] is not address type header!!!");
//			throw new ServletParseException("Header of type [" + hName
//					+ "] cant be parsed to address, wrong content type!!!");
//		}
		String nameToSearch = getCorrectHeaderName(hName);
		ListIterator<Header> headers = (ListIterator<Header>) this.message
				.getHeaders(nameToSearch);
		ListIterator<Header> lit = headers;

		if (lit != null && lit.hasNext()) {
			Header first = lit.next();
			if (first instanceof HeaderAddress) {
				try {
					if(this.isCommitted()) {
						return new AddressImpl((HeaderAddress) first, ModifiableRule.NotModifiable);
					} else {
						return new AddressImpl((HeaderAddress) first, getModifiableRule(hName));
					}
				} catch (ParseException e) {
					throw new ServletParseException("Bad address " + first);
				}
			} else {
				Parameterable parametrable = createParameterable(first, first.getName(), message instanceof Request);
				try {
					logger.debug("parametrable Value " + parametrable.getValue());					
					if(this.isCommitted()) {
						return new AddressImpl(SipFactoryImpl.addressFactory.createAddress(parametrable.getValue()), ((ParameterableHeaderImpl)parametrable).getInternalParameters(), ModifiableRule.NotModifiable);
					} else {
						return new AddressImpl(SipFactoryImpl.addressFactory.createAddress(parametrable.getValue()), ((ParameterableHeaderImpl)parametrable).getInternalParameters(), getModifiableRule(hName));
					}
				} catch (ParseException e) {
					throw new ServletParseException("Impossible to parse the following header " + name + " as an address.", e);
				}
			}
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAddressHeaders(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public ListIterator<Address> getAddressHeaders(String name)
			throws ServletParseException {

		String hName = getFullHeaderName(name);

		// Fix from Thomas Leseney from Nexcom systems
//		if (!isAddressTypeHeader(hName)) {
//			throw new ServletParseException(
//					"Header [" + hName + "] is not address type header");
//		}
		LinkedList<Address> retval = new LinkedList<Address>();
		String nameToSearch = getCorrectHeaderName(hName);

		for (Iterator<Header> it = this.message.getHeaders(nameToSearch); it
				.hasNext();) {
			Header header = (Header) it.next();
			if (header instanceof HeaderAddress) {
				HeaderAddress aph = (HeaderAddress) header;
				try {
					AddressImpl addressImpl = new AddressImpl(
							aph, getModifiableRule(hName));
					retval.add(addressImpl);
				} catch (ParseException ex) {
					throw new ServletParseException("Bad header", ex);
				}
			}  else {
				Parameterable parametrable = createParameterable(header, header.getName(), message instanceof Request);
				try {
					AddressImpl addressImpl = new AddressImpl(SipFactoryImpl.addressFactory.createAddress(parametrable.getValue()), ((ParameterableHeaderImpl)parametrable).getInternalParameters(), getModifiableRule(hName));
					retval.add(addressImpl);
				} catch (ParseException e) {
					throw new ServletParseException("Impossible to parse the following header " + name + " as an address.", e);
				}
			}
		}
		return retval.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() {
		return getApplicationSession(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getApplicationSession(boolean)
	 */
	public SipApplicationSession getApplicationSession(boolean create) {
		MobicentsSipSession sipSession = getSipSession();
		if(sipSession != null) {
			MobicentsSipApplicationSession sipApplicationSession = sipSession.getSipApplicationSession();
			if(sipApplicationSession != null) {
				return sipApplicationSession;
			}
		}
		
		String applicationName = getCurrentApplicationName();
		if(sessionKey != null) {
			applicationName = sessionKey.getApplicationName();
		} else {
			if(this instanceof SipServletRequestImpl && isOrphan()) {
				if(logger.isDebugEnabled()) {
					logger.debug("Orphans session " + applicationName + " " + sessionKey);
				}
				orphan = true;
				sessionKey = SessionManagerUtil.getSipSessionKey(
						SessionManagerUtil.getSipApplicationSessionKey(applicationName, getAppSessionId(), null).getId(),
						applicationName, message, false);
			}
		}
		if(applicationName != null && sessionKey != null) {
			final SipContext sipContext = sipFactoryImpl.getSipApplicationDispatcher().findSipApplication(applicationName);
			//call id not needed anymore since the sipappsessionkey is not a callid anymore but a random uuid
			final SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
					applicationName, 
					sessionKey.getApplicationSessionId(), null);
			MobicentsSipApplicationSession applicationSession =  sipContext.getSipManager().getSipApplicationSession(sipApplicationSessionKey, create);
			if(applicationSession != null) {
				// application session can be null if create is false and it is an orphan request
				applicationSession.setOrphan(isOrphan());
			}			
			return applicationSession;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		if (name == null)
			throw new NullPointerException("Attribute name can not be null.");
		return this.getAttributeMap().get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		Vector<String> names = new Vector<String>(this.getAttributeMap().keySet());
		return names.elements();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getCallId()
	 */
	public String getCallId() {

		CallIdHeader id = (CallIdHeader) this.message
				.getHeader(getCorrectHeaderName(CallIdHeader.NAME));
		if (id != null)
			return id.getCallId();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {

		if (this.message.getContentEncoding() != null) {
			return this.message.getContentEncoding().getEncoding();
		} else {
			ContentTypeHeader cth = (ContentTypeHeader)
				this.message.getHeader(ContentTypeHeader.NAME);
			if(cth == null) return null;
			return cth.getParameter("charset");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getContent()
	 */
	public Object getContent() throws IOException, UnsupportedEncodingException {
		ContentTypeHeader contentTypeHeader = (ContentTypeHeader) 
 			this.message.getHeader(ContentTypeHeader.NAME);
		if(contentTypeHeader != null && logger.isDebugEnabled()) {
			logger.debug("Content type " + contentTypeHeader.getContentType());
			logger.debug("Content sub type " + contentTypeHeader.getContentSubType());
		}		
		if(contentTypeHeader!= null && CONTENT_TYPE_TEXT.equals(contentTypeHeader.getContentType())) {
			String content = null;
			if(message.getRawContent() != null) {
				String charset = this.getCharacterEncoding();
				if(charset == null) {
					content = new String(message.getRawContent());	
				} else {
					content = new String(message.getRawContent(), charset);
				}
			} else {
				content = "";
			}
			return content;
		} else if(contentTypeHeader!= null && CONTENT_TYPE_MULTIPART.equals(contentTypeHeader.getContentType())) {
			try {
				return new MimeMultipart(new ByteArrayDataSource(message.getRawContent(), 
						contentTypeHeader.toString().replaceAll(ContentTypeHeader.NAME+": ", "")));
			} catch (MessagingException e) {
				logger.warn("Problem with multipart message.", e);
				return this.message.getRawContent();
			}
		} else {
			return this.message.getRawContent();
		}
	}

	/**
	 * Return a mimemultipart from raw Content
	 * FIXME Doesn't support nested multipart in the body content
	 * @param contentTypeHeader content type header related to the rawContent
	 * @param rawContent body content
	 * @return a mimemultipart from raw Content
	 */
	private static MimeMultipart getContentAsMimeMultipart(ContentTypeHeader contentTypeHeader, byte[] rawContent) {
		// Issue 1123 : http://code.google.com/p/mobicents/issues/detail?id=1123 : Multipart type is supported
		String delimiter = contentTypeHeader.getParameter(MULTIPART_BOUNDARY);
		String start = contentTypeHeader.getParameter(MULTIPART_START);
		
		MimeMultipart mimeMultipart = new MimeMultipart(contentTypeHeader.getContentSubType());
		if (delimiter == null) {
			MimeBodyPart mbp = new MimeBodyPart();			
		    DataSource ds = new ByteArrayDataSource(rawContent, contentTypeHeader.getContentSubType());
		    try {
				mbp.setDataHandler(new DataHandler(ds));
				mimeMultipart.addBodyPart(mbp);				
			} catch (MessagingException e) {
				throw new IllegalArgumentException("couldn't create the multipart object from the message content " + rawContent, e);
			}
		} else {
			// splitting the body content by delimiter
		    String[] fragments = new String(rawContent).split(MULTIPART_BOUNDARY_DELIM + delimiter);
			for (String fragment: fragments) { 
				final String trimmedFragment = fragment.trim();
				// skipping empty fragment and ending fragment looking like --
				if(trimmedFragment.length() > 0 && !MULTIPART_BOUNDARY_DELIM.equals(trimmedFragment)) {
					String fragmentHeaders = null; 
					String fragmentBody = fragment;
					// if there is a start, it means that there is probably headers before the content that need to be added to the mime body part
					// so we split headers from body content
					if(start != null && start.length() > 0) {
						int indexOfStart = fragment.indexOf(start);
						if(indexOfStart != -1) {
							fragmentHeaders = fragmentBody.substring(0, indexOfStart + start.length());
							fragmentBody = fragmentBody.substring(indexOfStart + start.length()).trim();    							
						}
					}
					MimeBodyPart mbp = new MimeBodyPart();	    				
				    try {
				    	String contentType = contentTypeHeader.getContentSubType();
				    	// check if the body content start with a Content-Type header
				    	// if so we strip it from the content body
				    	if(fragmentBody.startsWith(ContentTypeHeader.NAME)) {
				    		int indexOfLineReturn =  fragmentBody.indexOf(LINE_RETURN_DELIM);
				    		contentType = fragmentBody.substring(0, indexOfLineReturn -1).trim();	    			    		
				    		fragmentBody = fragmentBody.substring(indexOfLineReturn).trim();
				    	}
				    	// setting the content body stripped from the headers
				    	mbp.setContent(fragmentBody, contentType);
				    	// adding the headers to the body part
				    	mbp.addHeaderLine(contentType);
				    	if(fragmentHeaders != null) {
				    		StringTokenizer stringTokenizer = new StringTokenizer(fragmentHeaders, LINE_RETURN_DELIM);
				    		while (stringTokenizer.hasMoreTokens()) {
				    			String token = stringTokenizer.nextToken().trim();
				    			if(token != null && token.length() > 0) {
				    				mbp.addHeaderLine(token);
				    			}
							}
				    	}
						mimeMultipart.addBodyPart(mbp);				    					
					} catch (MessagingException e) {
						throw new IllegalArgumentException("couldn't create the multipart object from the message content " + rawContent, e);
					}
				}
			}					    			
		}
		return mimeMultipart;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getContentLanguage()
	 */
	public Locale getContentLanguage() {
		if (this.message.getContentLanguage() != null)
			return this.message.getContentLanguage().getContentLanguage();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getContentLength()
	 */
	public int getContentLength() {
		if (this.message.getContentLength() != null) {
			return this.message.getContentLength().getContentLength();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getContentType()
	 */
	public String getContentType() {
		ContentTypeHeader cth = (ContentTypeHeader) this.message
				.getHeader(getCorrectHeaderName(ContentTypeHeader.NAME));
		if (cth != null) {
			// Fix For Issue http://code.google.com/p/mobicents/issues/detail?id=2659
			// getContentType doesn't return the full header value
//			String contentType = cth.getContentType();
//			String contentSubType = cth.getContentSubType();
//			if(contentSubType != null) 
//				return contentType + "/" + contentSubType;
			return ((HeaderExt)cth).getValue();
		} 
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getExpires()
	 */
	public int getExpires() {
		if (this.message.getExpires() != null)
			return this.message.getExpires().getExpires();
		else
			return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getFrom()
	 */
	public Address getFrom() {
		FromHeader from = (FromHeader) this.message
				.getHeader(getCorrectHeaderName(FromHeader.NAME));
		AddressImpl address = new AddressImpl(from.getAddress(), AddressImpl.getParameters((Parameters)from), ModifiableRule.From);
		return address;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeader(java.lang.String)
	 */
	public String getHeader(String name) {

		String nameToSearch = getCorrectHeaderName(name);
		String value = null;
		if (this.message.getHeader(nameToSearch) != null) {
			value = ((SIPHeader) this.message.getHeader(nameToSearch))
					.getValue();
		}
//		if(logger.isDebugEnabled()) {
//			logger.debug("getHeader "+ name+ ", value="+ value	);
//		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeaderForm()
	 */
	public HeaderForm getHeaderForm() {
		return this.headerForm;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeaderNames()
	 */
	public Iterator<String> getHeaderNames() {
		return this.message.getHeaderNames();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeaders(java.lang.String)
	 */
	public ListIterator<String> getHeaders(String name) {
		String nameToSearch = getCorrectHeaderName(name);
		ArrayList<String> result = new ArrayList<String>();

		try {
			ListIterator<Header> list = this.message.getHeaders(nameToSearch);
			while (list != null && list.hasNext()) {
				Header h = list.next();
				result.add(((SIPHeader)h).getHeaderValue());
			}
		} catch (Exception e) {
			logger.fatal("Couldnt fetch headers, original name[" + name
					+ "], name searched[" + nameToSearch + "]", e);
			return result.listIterator();
		}
		return result.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getMethod()
	 */
	public final String getMethod() {
		if(method == null) {
			method = message instanceof Request ? ((Request) message).getMethod()
				: ((CSeqHeader) message.getHeader(CSeqHeader.NAME)).getMethod();
		}
		return method;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getParameterableHeader(java.lang.String)
	 */
	public Parameterable getParameterableHeader(String name)
			throws ServletParseException {

		if (name == null)
			throw new NullPointerException(
					"Parametrable header name cant be null!!!");

		String nameToSearch = getCorrectHeaderName(name);

		Header h = this.message.getHeader(nameToSearch);
		
		if(!isParameterable(name)) {
			throw new ServletParseException(name + " header is not parameterable !");
		}
		
		if(h == null) {
			return null;
		}
		
		return createParameterable(h, getFullHeaderName(name), message instanceof Request);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getParameterableHeaders(java.lang.String)
	 */
	public ListIterator<Parameterable> getParameterableHeaders(String name)
			throws ServletParseException {

		ListIterator<Header> headers = this.message
				.getHeaders(getCorrectHeaderName(name));

		ArrayList<Parameterable> result = new ArrayList<Parameterable>();

		while (headers != null && headers.hasNext())
			result.add(createParameterable(headers.next(),
					getFullHeaderName(name), message instanceof Request));

		if(!isParameterable(name)) {
			throw new ServletParseException(name + " header is not parameterable !");
		}			
		
		return result.listIterator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getProtocol()
	 */
	public String getProtocol() {
		// For this version of the SIP Servlet API this is always "SIP/2.0"
		return "SIP/2.0";
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getRawContent()
	 */
	public byte[] getRawContent() throws IOException {		
		if (message != null)
			return message.getRawContent();
		else
			return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getInitialRemoteAddr() {
		return transactionApplicationData.getInitialRemoteHostAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInitialRemotePort() {
		return transactionApplicationData.getInitialRemotePort();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInitialTransport() {		
		return transactionApplicationData.getInitialRemoteTransport();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		if(getTransaction() != null) {
			if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null &&
					((SIPTransaction)getTransaction()).getPeerPacketSourceAddress().getHostAddress() != null) {
				return ((SIPTransaction)getTransaction()).getPeerPacketSourceAddress().getHostAddress();
			} else {
				return ((SIPTransaction)getTransaction()).getPeerAddress();
			}
		} else {
			ViaHeader via = (ViaHeader) message.getHeader(ViaHeader.NAME);
			if(via == null) {
				return null;
			} else {
				return via.getHost();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getRemotePort()
	 */
	public int getRemotePort() {
		int port = -1;

		if(getTransaction() != null) {
			if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null) {
				port = ((SIPTransaction)getTransaction()).getPeerPacketSourcePort();
			} else {
				port = ((SIPTransaction)getTransaction()).getPeerPort();
			}
		} else {
			ViaHeader via = (ViaHeader) message.getHeader(ViaHeader.NAME);
			if(via != null) {
				port = via.getPort();
			}
		}
		if(port<=0) {
			return 5060;
		}
		else {
			return port;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getTransport()
	 */
	public String getTransport() {
		if(getTransaction() != null) {
			return ((SIPTransaction)getTransaction()).getTransport();
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getRemoteUser()
	 */
	public String getRemoteUser() {
		// This method returns non-null only if the user is authenticated
		if(this.userPrincipal != null)
			return this.userPrincipal.getName();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getSession()
	 */
	public SipSession getSession() {
		return getSession(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getSession(boolean)
	 */
	public SipSession getSession(boolean create) {
		
		MobicentsSipSession session = getSipSession();
		if (session == null && create) {
			MobicentsSipApplicationSession sipApplicationSessionImpl = (MobicentsSipApplicationSession)getApplicationSession(create);
			MobicentsSipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(sipApplicationSessionImpl.getKey().getId(), currentApplicationName, message, false);
			session = sipApplicationSessionImpl.getSipContext().getSipManager().getSipSession(sessionKey, create,
					sipFactoryImpl, sipApplicationSessionImpl);
			session.setSessionCreatingTransactionRequest(this);
			session.setOrphan(isOrphan());
			sessionKey = session.getKey();
		}
		
		if(session != null) {
			return session.getFacade();
		}
		return null;
	}
	
	/**
	 * Retrieve the sip session implementation
	 * @return the sip session implementation
	 */
	public final MobicentsSipSession getSipSession() {	
		if(sipSession == null && sessionKey != null) {
			final String applicationName = sessionKey.getApplicationName(); 
			final SipContext sipContext = sipFactoryImpl.getSipApplicationDispatcher().findSipApplication(applicationName);
			SipApplicationSessionKey sipApplicationSessionKey = new SipApplicationSessionKey(sessionKey.getApplicationSessionId(), sessionKey.getApplicationName(), null);
			MobicentsSipApplicationSession sipApplicationSession = sipContext.getSipManager().getSipApplicationSession(sipApplicationSessionKey, false);
			sipSession = sipContext.getSipManager().getSipSession(sessionKey, false, sipFactoryImpl, sipApplicationSession);	
		} 
		return sipSession; 
	}

	/**
	 * @param session the session to set
	 */
	public void setSipSession(MobicentsSipSession session) {
		// we store the session in JVM to cope with race conditions on session invalidation 
		// See Issue 1294 http://code.google.com/p/mobicents/issues/detail?id=1294
		// but it will not be persisted to avoid unecessary replication if the message is persisted
		this.sipSession = session;
        if (session != null){
            this.sessionKey = session.getKey();
        } else {
            this.sessionKey = null;
        }
	}

	/**
	 * @param session the session to set
	 */
	public MobicentsSipSessionKey getSipSessionKey() {
		return this.sessionKey;
	}

	public void setSipSessionKey(MobicentsSipSessionKey sessionKey) {
		this.sessionKey = sessionKey;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getTo()
	 */
	public Address getTo() {
		ToHeader to = (ToHeader) this.message
			.getHeader(getCorrectHeaderName(ToHeader.NAME));
		return new AddressImpl(to.getAddress(), AddressImpl.getParameters((Parameters)to), ModifiableRule.To);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getUserPrincipal()
	 */
	public SipPrincipal getUserPrincipal() {
		if(this.userPrincipal == null) {
			if(this.getSipSession() != null) {
				this.userPrincipal = this.getSipSession().getUserPrincipal();
			}
		}
		return this.userPrincipal;
	}
	
	public void setUserPrincipal(SipPrincipal principal) {
		this.userPrincipal = principal;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#isSecure()
	 */
	public boolean isSecure() {		
		return ListeningPoint.TLS.equalsIgnoreCase(JainSipUtils.findTransport(message));
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String role) {
		if(this.userPrincipal != null) {
			return this.userPrincipal.isUserInRole(role);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		if(attributes  != null) {
			this.attributes.remove(name);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#removeHeader(java.lang.String)
	 */
	public void removeHeader(String name) {
		checkCommitted();
		String hName = getFullHeaderName(name);

		if (isSystemHeader(getModifiableRule(hName))) {
			throw new IllegalArgumentException("Cant remove system header["
					+ hName + "]");
		}
		
		if (hName.equalsIgnoreCase("From") || hName.equalsIgnoreCase("To")) {
			logger.error("Error, can't remove From or To header [" + hName + "]");
			throw new IllegalArgumentException(
					"Cant remove From or To header, see JSR 289 Section 4.1.2");
		}

		String nameToSearch = getCorrectHeaderName(hName);

		this.message.removeHeader(nameToSearch);

	}
	
	public void removeHeaderInternal(String name, boolean bypassSystemHeaderCheck) {
		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Removing header under name [" + hName + "]");

		if (!bypassSystemHeaderCheck && isSystemHeader(getModifiableRule(hName))) {

			logger.error("Cant remove system header [" + hName + "]");

			throw new IllegalArgumentException("Header[" + hName
					+ "] is system header, can't remove it!!!");
		}

		String nameToRemove = getCorrectHeaderName(hName);
		try {
			message.removeHeader(nameToRemove);			
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied ", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#send()
	 */
	public abstract void send() throws IOException;

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setAcceptLanguage(java.util.Locale)
	 */
	public void setAcceptLanguage(Locale locale) {
		checkCommitted();
		AcceptLanguageHeader alh = SipFactoryImpl.headerFactory
				.createAcceptLanguageHeader(locale);

		this.message.setHeader(alh);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setAddressHeader(java.lang.String, javax.servlet.sip.Address)
	 */
	public void setAddressHeader(String name, Address addr) {
		checkCommitted();
		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Setting address header [" + name + "] to value ["
					+ addr + "]");

		if (isSystemHeader(getModifiableRule(hName))) {
			logger.error("Error, can't set system header [" + hName + "]");
			throw new IllegalArgumentException(
					"Cant set system header, it is maintained by container!!");
		}

		if (hName.equalsIgnoreCase("From") || hName.equalsIgnoreCase("To")) {
			logger.error("Error, can't set From or To header [" + hName + "]");
			throw new IllegalArgumentException(
					"Cant set From or To header, see JSR 289 Section 4.1.2");
		}

//		if (!isAddressTypeHeader(hName)) {
//			logger.error("Error, set header, its not address type header ["
//					+ hName + "]!!");
//			throw new IllegalArgumentException(
//					"This header is not address type header");
//		}
		Header h;
		String headerNameToAdd = getCorrectHeaderName(hName);
		try {
			h = SipFactoryImpl.headerFactory.createHeader(headerNameToAdd, addr
					.toString());
			this.message.setHeader(h);
		} catch (ParseException e) {
			logger.error("Parsing problem while setting address header with name "
					+ name + " and address "+ addr, e);			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		if (name == null)
			throw new NullPointerException("Attribute name can not be null.");
		this.getAttributeMap().put(name, o);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		new String("testEncoding".getBytes(),enc);
		checkCommitted();
		try {			
			this.message.setContentEncoding(SipFactoryImpl.headerFactory
					.createContentEncodingHeader(enc));
		} catch (Exception ex) {
			throw new UnsupportedEncodingException(enc);
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setContent(java.lang.Object, java.lang.String)
	 */
	public void setContent(Object content, String contentType)
			throws UnsupportedEncodingException {		
		checkMessageState();
		checkContentType(contentType);
		checkCommitted();
		
		if(contentType != null && contentType.length() > 0) {
			this.addHeader(ContentTypeHeader.NAME, contentType);
			ContentTypeHeader contentTypeHeader = (ContentTypeHeader)this.message.getHeader(ContentTypeHeader.NAME);			
			String charset = this.getCharacterEncoding();
			try {		
				if(contentType.contains(CONTENT_TYPE_MULTIPART)) {
					// Fix for Issue 2667 : Correct Handling of MimeMultipart
					Multipart multipart = (Multipart) content;
					OutputStream os = new ByteArrayOutputStream();
					multipart.writeTo(os);
					this.message.setContent(os.toString(), contentTypeHeader);
				} else {
					Object tmpContent = content;
					if(tmpContent instanceof String  && charset != null) {
						//test for unsupportedencoding exception
						new String("testEncoding".getBytes(charset));
						tmpContent = new String(((String)tmpContent).getBytes());
					}
					this.message.setContent(content, contentTypeHeader);
				}
			} catch (UnsupportedEncodingException uee) {
				throw uee;
			} catch (Exception e) {
				throw new IllegalArgumentException("Parse error reading content " + content + " with content type " + contentType, e);				
			} 
		}
	}
	
	protected abstract void checkMessageState();

	/**
	 * Check the content type against the list defined by the iana
	 * http://www.iana.org/assignments/media-types/
	 * @param contentType
	 */
	private void checkContentType(String contentType) {
		if(contentType == null) {
			throw new IllegalArgumentException("the content type cannot be null");
		}
		int indexOfSlash = contentType.indexOf("/");
		if(indexOfSlash != -1) { 
			if(!JainSipUtils.IANA_ALLOWED_CONTENT_TYPES.contains(contentType.substring(0, indexOfSlash))) {
				throw new IllegalArgumentException("the given content type " + contentType + " is not allowed");
			}
		} else if(!JainSipUtils.IANA_ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
			throw new IllegalArgumentException("the given content type " + contentType + " is not allowed");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setContentLanguage(java.util.Locale)
	 */
	public void setContentLanguage(Locale locale) {
		checkCommitted();
		ContentLanguageHeader contentLanguageHeader = 
			SipFactoryImpl.headerFactory.createContentLanguageHeader(locale);
		this.message.setContentLanguage(contentLanguageHeader);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setContentLength(int)
	 */
	public void setContentLength(int len) {
		checkMessageState();
		checkCommitted();
		try {
			ContentLengthHeader h = SipFactoryImpl.headerFactory.createContentLengthHeader(len);
			this.message.setHeader(h);
		} catch (InvalidArgumentException e) {
			throw new IllegalStateException("Impossible to set a content length lower than 0", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setContentType(java.lang.String)
	 */
	public void setContentType(String type) {
		checkContentType(type);
		checkCommitted();
		String name = getCorrectHeaderName(ContentTypeHeader.NAME);
		try {
			Header h = SipFactoryImpl.headerFactory.createHeader(name, type);
			this.message
					.removeHeader(getCorrectHeaderName(ContentTypeHeader.NAME));
			this.message.addHeader(h);
		} catch (ParseException e) {
			logger.error("Error while setting content type header !!!", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setExpires(int)
	 */
	public void setExpires(int seconds) {
		try {
			ExpiresHeader expiresHeader = 
				SipFactoryImpl.headerFactory.createExpiresHeader(seconds);			
			expiresHeader.setExpires(seconds);
			this.message.setExpires(expiresHeader);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error setting expiration header!", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String name, String value) {
		if(name == null) {
			throw new NullPointerException ("name parameter is null");
		}
		if(value == null) {
			throw new NullPointerException ("value parameter is null");
		}
		if(isSystemHeader(getModifiableRule(name))) {
			throw new IllegalArgumentException(name + " is a system header !");
		}
		checkCommitted();
		
		try {
			// Dealing with Allow:INVITE, ACK, CANCEL, OPTIONS, BYE kind of headers
			if(JainSipUtils.LIST_HEADER_NAMES.contains(name)) {
				this.message.removeHeader(name);
				List<Header> headers = SipFactory.getInstance().createHeaderFactory()
					.createHeaders(name + ":" + value);
				for (Header header : headers) {
					this.message.addHeader(header);
				}
			} else {
				// dealing with non list headers and extension header
				Header header = SipFactory.getInstance().createHeaderFactory()
					.createHeader(name, value);
				this.message.setHeader(header);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Error creating header!", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setHeaderForm(javax.servlet.sip.SipServletMessage.HeaderForm)
	 */
	public void setHeaderForm(HeaderForm form) {

		this.headerForm = form;
		// When form changes to HeaderForm.COMPACT or HeaderForm.LONG - all
		// names must transition, if it is changed to HeaderForm.DEFAULT, no
		// action is performed
		if(form == HeaderForm.DEFAULT)
			return;
		
//		if(form == HeaderForm.COMPACT) {			 
//			for(String fullName : headerFull2CompactNamesMappings.keySet()) {
//				if(message.getHeader(fullName) != null) {
//					try {
						//Handle the case where mutliple headers for the same header name
//						Header header = SipFactoryImpl.headerFactory.createHeader(headerCompact2FullNamesMappings.get(fullName), ((SIPHeader)message.getHeader(fullName)).getHeaderValue());
//						message.removeHeader(fullName);
//						message.addHeader(header);
//					} catch (ParseException e) {
//						logger.error("Impossible to parse the header " + fullName + " to its compact form");
//					}
//				}
//			}
//		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setParameterableHeader(java.lang.String, javax.servlet.sip.Parameterable)
	 */
	public void setParameterableHeader(String name, Parameterable param) {
		checkCommitted();
		if(isSystemHeader(getModifiableRule(name))) {
			throw new IllegalArgumentException(name + " is a system header !");
		}
		try {
			this.message.setHeader(SipFactoryImpl.headerFactory.createHeader(name, param.toString()));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Impossible to set this parameterable header", e);
		}
	}

	/**
	 * Applications must not add, delete, or modify so-called "system" headers.
	 * These are header fields that the servlet container manages: From, To,
	 * Call-ID, CSeq, Via, Route (except through pushRoute), Record-Route.
	 * Contact is a system header field in messages other than REGISTER requests
	 * and responses, 3xx and 485 responses, and 200/OPTIONS responses.
	 * Additionally, for containers implementing the reliable provisional
	 * responses extension, RAck and RSeq are considered system headers also.
	 * 
	 * This method should return true if passed name - full or compact is name
	 * of system header in context of this message. Each subclass has to
	 * implement it in the manner that it conforms to semantics of wrapping
	 * class
	 * 
	 * @param headerName -
	 *            either long or compact header name
	 * @return
	 */
	public abstract ModifiableRule getModifiableRule(String headerName);

	/**
	 * Applications must not add, delete, or modify so-called "system" headers.
	 * These are header fields that the servlet container manages: From, To,
	 * Call-ID, CSeq, Via, Route (except through pushRoute), Record-Route.
	 * Contact is a system header field in messages other than REGISTER requests
	 * and responses, 3xx and 485 responses, and 200/OPTIONS responses.
	 * Additionally, for containers implementing the reliable provisional
	 * responses extension, RAck and RSeq are considered system headers also.
	 * 
	 * This method should return true if passed name - full or compact is name
	 * of system header in context of this message. Each subclass has to
	 * implement it in the manner that it conforms to semantics of wrapping
	 * class
	 * 
	 * @param headerName -
	 *            either long or compact header name
	 * @return
	 */
	public static boolean isSystemHeader(ModifiableRule modifiableRule) {
		if (modifiableRule == ModifiableRule.NotModifiable || modifiableRule == ModifiableRule.ContactSystem || modifiableRule == ModifiableRule.ProxyRecordRouteNotModifiable) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method checks if passed name is name of address type header -
	 * according to rfc 3261
	 * 
	 * @param headerName -
	 *            name of header - either full or compact
	 * @return
	 */
	public static boolean isAddressTypeHeader(String headerName) {

		return JainSipUtils.ADDRESS_HEADER_NAMES.contains(getFullHeaderName(headerName));

	}

	/**
	 * This method tries to resolve header name - meaning if it is compact - it
	 * returns full name, if its not, it returns passed value.
	 * 
	 * @param headerName
	 * @return
	 */
	protected static String getFullHeaderName(String headerName) {

		String fullName = null;
		if (JainSipUtils.HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.containsKey(headerName)) {
			fullName = JainSipUtils.HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.get(headerName);
		} else {
			fullName = headerName;
		}
		if (logger.isDebugEnabled())
			logger.debug("Fetching full header name for [" + headerName
					+ "] returning [" + fullName + "]");

		return fullName;
	}

	/**
	 * This method tries to determine compact header name - if passed value is
	 * compact form it is returned, otherwise method tries to find compact name -
	 * if it is found, string rpresenting compact name is returned, otherwise
	 * null!!!
	 * 
	 * @param headerName
	 * @return
	 */
	public static String getCompactName(String headerName) {

		String compactName = null;
		if (JainSipUtils.HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.containsKey(headerName)) {
			compactName = JainSipUtils.HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.get(headerName);
		} else {
			// This can be null if there is no mapping!!!
			compactName = JainSipUtils.HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.get(headerName);
		}
		if (logger.isDebugEnabled())
			logger.debug("Fetching compact header name for [" + headerName
					+ "] returning [" + compactName + "]");

		return compactName;

	}

	public String getCorrectHeaderName(String name) {
		return getCorrectHeaderName(name, this.headerForm);

	}

	protected static String getCorrectHeaderName(String name, HeaderForm form) {

		if (form == HeaderForm.DEFAULT) {
			return name;
		} else if (form == HeaderForm.COMPACT) {

			String compact = getCompactName(name);
			if (compact != null)
				return compact;
			else
				return name;
		} else if (form == HeaderForm.LONG) {
			return getFullHeaderName(name);
		} else {
			// ERROR ? - this shouldnt happen
			throw new IllegalStateException(
					"No default form of a header set!!!");
		}

	}

	public Transaction getTransaction() {
		if (logger.isDebugEnabled()) {
			logger.debug("transaction " + transaction + " transactionId = " + transactionId + " transactionType " + transactionType);
		}
		if(transaction == null && transactionId != null) {            
			// used for early dialog failover purposes, lazily load the transaction
            setTransaction(((ClusteredSipStack)StaticServiceHolder.sipStandardService.getSipStack()).findTransaction(transactionId, transactionType));
            if(transaction != null) {
            	transactionApplicationData = (TransactionApplicationData) transaction.getApplicationData();
            }
        }
		return this.transaction;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.message.toString();
	}

	public TransactionApplicationData getTransactionApplicationData() {
		return this.transactionApplicationData;
	}

	public Message getMessage() {
		return message;
	}

	public Dialog getDialog() {
		if (this.dialog != null) {
			return dialog;
		}
		if (this.getTransaction() != null) {
			return this.getTransaction().getDialog();
		}
		return null;
	}

	/**
	 * @param transaction
	 *            the transaction to set
	 */
	public void setTransaction(Transaction transaction) {
		if (logger.isDebugEnabled())
			logger.debug("Setting transaction " + transaction + " on message " + this);
		this.transaction = transaction;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}
	
	protected static int countChars(String string, char c) {
		int count = 0;
		for(int w=0; w<string.length(); w++) {
			if(string.charAt(w) == c) {
				count ++;
			}
		}
		return count;
	}

	protected static Parameterable createParameterable(Header header, String hName, boolean isRequest)
			throws ServletParseException {
		String whole = header.toString();
		if (logger.isDebugEnabled())
			logger.debug("Creating parametrable for [" + hName + "] from ["
					+ whole + "]");
		// Remove name
		String stringHeader = whole.substring(whole.indexOf(":") + 1).trim();
//		if (!stringHeader.contains("<") || !stringHeader.contains(">")
//				|| !isParameterable(getFullHeaderName(hName))) {
		
		Map<String, String> paramMap = new HashMap<String, String>();
		String value = stringHeader;
		String displayName = null;
		// Issue 2201 : javax.servlet.sip.ServletParseException: Impossible to parse the following header Remote-Party-ID as an address.
		// Need to handle the display name
		if(stringHeader.trim().indexOf("\"") == 0) {
			String displayNameString = stringHeader.substring(1);
			int nextIndexOfDoubleQuote = displayNameString.indexOf("\"");
			displayName = stringHeader.substring(0, nextIndexOfDoubleQuote + 2);
			stringHeader = stringHeader.substring(nextIndexOfDoubleQuote + 2).trim();			
		}
		
		boolean hasLaRaQuotes = false;
		if(stringHeader.trim().indexOf("<") == 0) {
			
			stringHeader = stringHeader.substring(1);
			int indexOfBracket = stringHeader.indexOf(">");
			if(indexOfBracket != -1) {
				hasLaRaQuotes = true;
			}
			//String[] split = stringHeader.split(">");
			
			value = stringHeader.substring(0, indexOfBracket);//split[0];	
			String restOfHeader = stringHeader.substring(indexOfBracket+1) ;
			
			if (restOfHeader.length() > 1 && restOfHeader.contains(";")) {
				// repleace first ";" with "" because it separates us from the URI that we romved
				restOfHeader = restOfHeader.replaceFirst(";", "");
				String[] split = restOfHeader.split(";");
				
				// Now concatenate the items that have quotes that represent nested parameters http://code.google.com/p/sipservlets/issues/detail?id=105
				// example <sip:1.2.3.4:5061>;expires=500;+sip.instance="<urn:uuid:00000000-0000-0000-0000-000000000000>";gruu="sip:100@ocs14.com;opaque=user:epid:xxxxxxxxxxxxxxxxxxxxxxxx;gruu" 
				ArrayList<StringBuffer> resplitListWithQuotes = new ArrayList<StringBuffer>(); // here we collect the items and append related entries that are part of the same quotation zone
				int resplitIndex = 0;
				boolean addToPrevious = false;
				for(int q=0; q<split.length; q++) {
					int countQuotes = countChars(split[q], '\"'); // how many " signs are there
					if(countQuotes%2!=0) { // if not mod 2 then there is unclosed quote in that item
						if(addToPrevious) { // if we alreay have seen one it means this quote is closing quote
							resplitListWithQuotes.get(resplitIndex-1).append(";" + split[q]); // must reappend the ";" symbol because we lost it in the split
							addToPrevious = false;
						} else { // otherwise it's an opening quote
							resplitListWithQuotes.add(new StringBuffer(split[q]));
							resplitIndex++;
							addToPrevious = true;
						}
					} else { // if mod 2 then there are no unclosed quotes in that item
						if(addToPrevious) { // we will add any data 
							resplitListWithQuotes.get(resplitIndex-1).append(";" + split[q]); // must reappend the ";" symbol because we lost it in the split
						} else {
							resplitListWithQuotes.add(new StringBuffer(split[q])); // this is the normal case without quotes to take care of
							resplitIndex++;
						}
					}
				}
				
				if(addToPrevious) {
					// Lets warn if something is really that wrong
					throw new RuntimeException("Unclosed quote sign in this string " + whole);
				}
				
				// Change the split array now with the adjusted array
				String[] newSplit = new String[resplitListWithQuotes.size()];
				for(int q=0; q<resplitListWithQuotes.size(); q++) {
					newSplit[q] = resplitListWithQuotes.get(q).toString();
				}
				split = newSplit;
				
				// And process with the usual algorithm
				for (String pair : split) {
					int indexOfEq = pair.indexOf('=');
					String key = null;
					String val = null;
					if(indexOfEq<0) {
						key = pair;
						val = "";
					} else {
						key = pair.substring(0, indexOfEq);
						if(indexOfEq+1>pair.length()) {
							val = "";
						} else {
							val = pair.substring(indexOfEq+1);
						}
					}
					
					// Fix to Issue 1010 (http://code.google.com/p/mobicents/issues/detail?id=1010) : Unable to set flag parameter to parameterable header
					// from Alexander Kozlov from Codeminders
					paramMap.put(key, val);
				}
			}
		} else {
			if (value.length() > 1 && value.contains(";")) {
				// repleace first ";" with ""
				String parameters = value.substring(value.indexOf(";") + 1);
				value = value.substring(0, value.indexOf(";"));				
				String[] split = parameters.split(";");
	
				for (String pair : split) {
					String[] vals = pair.split("=");
					if (vals.length > 2) {
						logger
								.error("Wrong parameter format, expected value and name, got ["
										+ pair + "]");
						throw new ServletParseException(
								"Wrong parameter format, expected value or name["
										+ pair + "]");
					}
					// Fix to Issue 1010 (http://code.google.com/p/mobicents/issues/detail?id=1010) : Unable to set flag parameter to parameterable header 
					// from Alexander Kozlov from Codeminders
					paramMap.put(vals[0], vals.length == 2 ? vals[1] : "");
				}
			} else if (value.length() > 1 && value.contains(",")) {
			    // Deals with https://code.google.com/p/sipservlets/issues/detail?id=239
                // repleace first ";" with ""
//                String parameters = value.substring(value.indexOf(",") + 1);
//                value = value.substring(0, value.indexOf(","));             
                String[] split = value.split(",");
    
                for (String pair : split) {
                    String[] vals = pair.split("=");
                    if (vals.length > 2) {
                        logger
                                .error("Wrong parameter format, expected value and name, got ["
                                        + pair + "]");
                        throw new ServletParseException(
                                "Wrong parameter format, expected value or name["
                                        + pair + "]");
                    }
                    String paramValue = vals[1];
                    if(vals.length < 2) {
                        paramValue ="";
                    } else if(vals.length == 2 && vals[1].indexOf('\"') == 0 && vals[1].lastIndexOf('\"') == vals[1].length()-1) {
                        paramValue  = vals[1].substring(1, vals[1].length()-1);
                    }
                    paramMap.put(vals[0], paramValue);
                }
            }
		}		
		
		// quotes are parts of the value as well as the display Name
		if(hasLaRaQuotes) {
			value = "<" + value + ">";
		}
		
		// if a display name is present then we need add the quote back
		if(displayName != null) {
			value = displayName.concat(value);
		}
		final String headerName = header.getName();
		final boolean isNotModifiable = JainSipUtils.SYSTEM_HEADERS.contains(headerName);
		ModifiableRule modifiableRule = isNotModifiable ? ModifiableRule.NotModifiable : ModifiableRule.Modifiable;
		if(headerName.equalsIgnoreCase(FromHeader.NAME)) {
			if(isRequest) {
				modifiableRule = ModifiableRule.From;
			} else {
				modifiableRule = ModifiableRule.NotModifiable;
			}
		}
		if(headerName.equalsIgnoreCase(ToHeader.NAME)) {
			if(isRequest) {
				modifiableRule = ModifiableRule.To;
			} else {
				modifiableRule = ModifiableRule.NotModifiable;
			}
		}
		if(headerName.equalsIgnoreCase(ViaHeader.NAME)) {
			if(isRequest) {
				modifiableRule = ModifiableRule.Via;
			} else {
				modifiableRule = ModifiableRule.NotModifiable;
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("modifiableRule for [" + hName + "] from ["
					+ whole + "] is " + modifiableRule);
		ParameterableHeaderImpl parameterable = new ParameterableHeaderImpl(
				header, value, paramMap, modifiableRule);
		return parameterable;
	}

	public static boolean isParameterable(String header) {
		if(JainSipUtils.PARAMETERABLE_HEADER_NAMES.contains(header)) {
			return true;
		}
		return false;
	}

	/**
	 * @return the currentApplicationName
	 */
	public String getCurrentApplicationName() {
		return currentApplicationName;
	}

	/**
	 * @param currentApplicationName the currentApplicationName to set
	 */
	public void setCurrentApplicationName(String currentApplicationName) {
		this.currentApplicationName = currentApplicationName;
	}	
	

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getLocalAddr()
	 */
	public String getLocalAddr() {
		final SIPTransaction sipTransaction = (SIPTransaction)getTransaction();
		if(sipTransaction != null) {
			return sipTransaction.getHost();
		} else {
			final String transport = JainSipUtils.findTransport(message);
			final MobicentsExtendedListeningPoint listeningPoint = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(transport, false);		
			return listeningPoint.getHost(true);
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getLocalPort()
	 */
	public int getLocalPort() {
		final SIPTransaction sipTransaction = (SIPTransaction)getTransaction();
		if(sipTransaction != null) {
			return sipTransaction.getPort();
		} else {
			final String transport = JainSipUtils.findTransport(message);
			final MobicentsExtendedListeningPoint listeningPoint = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(transport, false);		
			return listeningPoint.getPort();
		}
	}
	// Fix for Issue 1552 http://code.google.com/p/mobicents/issues/detail?id=1552
	// Container does not recognise 100rel if there are other extensions on the Require or Supported line
	// we check all the values of Require and Supported headers to make sure the 100rel is present	
	protected boolean containsRel100(Message message) {
		ListIterator<SIPHeader> requireHeaders = message.getHeaders(RequireHeader.NAME);
		if(requireHeaders != null) {
			while (requireHeaders.hasNext()) {
				if(REL100_OPTION_TAG.equals(requireHeaders.next().getValue())) {
					return true;
				}
			}
		}
		ListIterator<SIPHeader> supportedHeaders = message.getHeaders(SupportedHeader.NAME);
		if(supportedHeaders != null) {
			while (supportedHeaders.hasNext()) {
				if(REL100_OPTION_TAG.equals(supportedHeaders.next().getValue())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public abstract void cleanUp();
	
	protected Map<String, Object> getAttributeMap() {
		if(this.attributes == null) {
			this.attributes = new ConcurrentHashMap<String, Object>();
		}
		return this.attributes;
	}
	
	// Issue 2354
	protected void setAttributeMap(Map<String, Object> atttributes) {
		this.attributes = atttributes;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {		
		sipFactoryImpl = (SipFactoryImpl) in.readObject();
		String sessionKeyString = in.readUTF();
		if (sessionKeyString.length() > 0) {
			try {
				sessionKey = SessionManagerUtil.parseSipSessionKey(sessionKeyString);
			} catch (ParseException e) {
				throw new IllegalArgumentException("SIP Sesion Key " + sessionKeyString + " previously serialized could not be reparsed", e);
			}
		}
		int attributesSize = in.readInt();
		if(attributesSize > 0) {
			Object[][] attributesArray = (Object[][] )in.readObject();
			attributes = new ConcurrentHashMap<String, Object>();
			for (int i = 0; i < attributesSize; i++) {
				String key = (String) attributesArray[0][i];
				Object value = attributesArray[1][i];
				attributes.put(key, value);
			}
		}
		if(in.readBoolean()) {
			transactionApplicationData = (TransactionApplicationData) in.readObject();
		}
		headerForm = HeaderForm.valueOf(in.readUTF());
		currentApplicationName = in.readUTF();
		if(currentApplicationName.equals("")) {
			currentApplicationName = null;
		}
		isMessageSent = in.readBoolean();
		if(ReplicationStrategy.EarlyDialog == StaticServiceHolder.sipStandardService.getReplicationStrategy()) {
			transactionId = in.readUTF();
			if (logger.isDebugEnabled()) {
				logger.debug("readExternal transactionId = " + transactionId);
			}
			if(transactionId != null) {
				if(transactionId.equals("")) {
					transactionId = null;
				} else { 
					transactionType = in.readBoolean();
					if (logger.isDebugEnabled()) {
						logger.debug("readExternal transactionType = " + transactionType);
					}
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sipFactoryImpl);
		if(sessionKey != null) {
			out.writeUTF(sessionKey.toString());
		} else {
			if (null == sipSession) {
				out.writeUTF("");
			} else {
				out.writeUTF(sipSession.getId());
			}
		}
		if(attributes != null && attributes.size() > 0) {
			out.writeInt(attributes.size());
			Object[][] attributesArray = new Object[2][attributes.size()];
			int i = 0;
			for (Entry<String, Object> entry : attributes.entrySet()) {
				attributesArray [0][i] = entry.getKey(); 
				attributesArray [1][i] = entry.getValue();
				i++;
			}		
			out.writeObject(attributesArray);
		} else {
			out.writeInt(0);
		}
		if(transactionApplicationData != null) {
			out.writeBoolean(true);
			out.writeObject(transactionApplicationData);
		} else {
			out.writeBoolean(false);
		}
		out.writeUTF(headerForm.toString());
		if(currentApplicationName != null) {
			out.writeUTF(currentApplicationName);
		} else {
			out.writeUTF("");
		}
		out.writeBoolean(isMessageSent);		
		if(ReplicationStrategy.EarlyDialog == StaticServiceHolder.sipStandardService.getReplicationStrategy()) {
			if (logger.isDebugEnabled()) {
				logger.debug("writeExternal transaction = " + transaction);
			}
			if(transaction == null) {
				out.writeUTF("");
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("writeExternal transactionId = " + transaction.getBranchId() + " transactionType " + (transaction instanceof ServerTransaction));
				}
				out.writeUTF(transaction.getBranchId());
				out.writeBoolean(transaction instanceof ServerTransaction);
			}
		}
		out.writeUTF(message.toString());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SipServletMessageImpl other = (SipServletMessageImpl) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

	
	
//	public void cleanUp() {
//		if(logger.isDebugEnabled()) {
//			logger.debug("cleaning up the message " + message);
//		}
//		if(attributes != null) {
//			attributes.clear();
//			attributes = null;
//		}
//		currentApplicationName = null;
//		dialog = null;
//		headerForm= null;
//		message= null;
//		
//		remoteAddr = null;
////		sessionKey = null;
////		sipFactoryImpl = null;		
//		sipSession = null;
//		method = null;
//		
////		if(transactionApplicationData != null) {
////			transactionApplicationData.cleanUp(false);
//			transactionApplicationData = null;
////		}
//		transaction = null;
//		transport= null;
//		userPrincipal= null;
//	}
	public void setOrphan(boolean orphan) {
		this.orphan = orphan;
	}

	public boolean isOrphan() {
		return orphan;
	}	

	public String getAppSessionId() {
		return appSessionId;
	}
	
	public void setAppSessionId(String appSessionId) {
		this.appSessionId = appSessionId;
	}
}
