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

package org.openxdm.xcap.server.slee;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.slee.ActivityContextInterface;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.enabler.userprofile.UserProfile;
import org.mobicents.slee.enabler.userprofile.UserProfileControlSbbLocalObject;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.http.HttpConstant;
import org.openxdm.xcap.server.slee.auth.RFC2617AuthQopDigest;
import org.openxdm.xcap.server.slee.auth.RFC2617ChallengeParamGenerator;

/**
 * 
 * @author aayush.bhatnagar
 * @author martins
 * 
 *         The authentication proxy only authenticates remote requests, if local
 *         uses asserted id if present, if not defines no user but does not
 *         fails.
 * 
 *         From the OMA-TS-XDM-core specification:
 * 
 *         The Aggregation Proxy SHALL act as an HTTP Proxy defined in [RFC2616]
 *         with the following clarifications. The Aggregation Proxy:
 * 
 *         1. SHALL be configured as an HTTP reverse proxy (see [RFC3040]);
 * 
 *         2. SHALL support authenticating the XDM Client; in case the GAA is
 *         used according to [3GPP TS 33.222], the mutual authentication SHALL
 *         be supported; or SHALL assert the XDM Client identity by inserting
 *         the X-XCAPAsserted- Identity extension header to the HTTP requests
 *         after a successful HTTP Digest Authentication as defined in Section
 *         6.3.2, in case the GAA is not used.
 * 
 *         3. SHALL forward the XCAP requests to the corresponding XDM Server,
 *         and forward the response back to the XDM Client;
 * 
 *         4. SHALL protect the XCAP traffic by enabling TLS transport security
 *         mechanism. The TLS resumption procedure SHALL be used as specified in
 *         [RFC2818].
 * 
 *         When realized with 3GPP IMS or 3GPP2 MMD networks, the Aggregation
 *         Proxy SHALL act as an Authentication Proxy defined in [3GPP TS
 *         33.222] with the following clarifications. The Aggregation Proxy:
 *         SHALL check whether an XDM Client identity has been inserted in
 *         X-3GPP-Intended-Identity header of HTTP request.
 * 
 *         � If the X-3GPP-Intended-Identity is included , the Aggregation Proxy
 *         SHALL check the value in the header is allowed to be used by the
 *         authenticated identity.
 * 
 *         � If the X-3GPP-Intended-Identity is not included, the Aggregation
 *         Proxy SHALL insert the authenticated identity in the
 *         X-3GPP-Asserted-Identity header of the HTTP request.
 * 
 *         TODO: GAA is not supported as of now. It is FFS on how we go about
 *         GAA support. TODO: TLS is not supported as of now.
 */
public abstract class AuthenticationProxySbb implements javax.slee.Sbb,
		AuthenticationProxy {

	private static Tracer logger;

	private static final RFC2617ChallengeParamGenerator challengeParamGenerator = new RFC2617ChallengeParamGenerator();
	
	private static final ServerConfiguration CONFIGURATION = ServerConfiguration.getInstance();
	
	public static final String HEADER_X_3GPP_Asserted_Identity = "X-3GPP-Asserted-Identity";
	public static final String HEADER_X_XCAP_Asserted_Identity = "X-XCAP-Asserted-Identity";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openxdm.xcap.server.slee.AuthenticationProxySbbLocalObject#authenticate
	 * (javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	public String authenticate(HttpServletRequest request,
			HttpServletResponse response) throws InternalServerErrorException {

		if (logger.isFineEnabled()) {
			logger.fine("Authenticating request");
		}

		/**
		 * On receiving an HTTP request that does not contain the Authorization
		 * header field, the AP shall: a) challenge the user by generating a 401
		 * Unauthorized response according to the procedures specified in TS 133
		 * 222 [6] and RFC 2617 [3]; and b) forward the 401 Unauthorized
		 * response to the sender of the HTTP request.
		 */
		try {
			String user = null;
			if(!CONFIGURATION.getLocalXcapAuthentication() && request.getRemoteAddr().equals(request.getLocalAddr())) {
				if (logger.isInfoEnabled()) {
					logger.info("Skipping authentication for local request.");
				}
				if (CONFIGURATION.getAllowAssertedUserIDs()) {
					// use asserted id header if present
					user = request.getHeader(HEADER_X_3GPP_Asserted_Identity);
					if (user == null) {
						user = request.getHeader(HEADER_X_XCAP_Asserted_Identity);					
					}		
					if (logger.isInfoEnabled()) {
						logger.info("Asserted user: "+user);
					}
				}
			}
			else {
				if (CONFIGURATION.getAllowAssertedUserIDs()) {
					// use asserted id header if present
					user = request.getHeader(HEADER_X_3GPP_Asserted_Identity);
					if (user == null) {
						user = request.getHeader(HEADER_X_XCAP_Asserted_Identity);					
					}		
					if (logger.isInfoEnabled()) {
						logger.info("Asserted user: "+user);
					}
				}
				if (user == null) {
					// use http digest authentication
					if (logger.isInfoEnabled()) {
						logger.info("Remote request without asserted user, using http digest authentication");
					}
					if (request.getHeader(HttpConstant.HEADER_AUTHORIZATION) == null) {
						challengeRequest(request, response);
					} else {
						user = checkAuthenticatedCredentials(request, response); 
						if (user != null) {
							if (logger.isFineEnabled()) {
								logger.fine("Authentication suceed");
							}
						} else {
							if (logger.isFineEnabled()) {
								logger.fine("Authentication failed");
							}
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.getWriter().close();
						}
					}
				}
			}
			return user;			
		} catch (Throwable e) {
			throw new InternalServerErrorException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InternalServerErrorException 
	 */
	private void challengeRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException,
			NoSuchAlgorithmException, InternalServerErrorException {

		if (logger.isFineEnabled())
			logger
					.fine("Authorization header is missing...challenging the request");

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		/**
		 * If a qop directive is sent by the server in the challenge, then the
		 * challenge response MUST contain the nonce-count and cnonce
		 * parameters. This will be checked later on.
		 */
		String opaque = challengeParamGenerator.generateOpaque();
		final String challengeParams = "Digest nonce=\"" + challengeParamGenerator.getNonce(opaque)
				+ "\", realm=\"" + getRealm()
				+ "\", opaque=\"" + opaque
				+ "\", qop=\"auth\"";

		response.setHeader(HttpConstant.HEADER_WWW_AUTHENTICATE,
				challengeParams);

		if (logger.isFineEnabled()) {
			logger.fine("Sending response with header "+HttpConstant.HEADER_WWW_AUTHENTICATE+" challenge params: "+challengeParams);
		}
		
		// send to client
		response.getWriter().close();
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @return null if authentication failed, authenticated user@domain otherwise
	 * @throws InternalServerErrorException 
	 */
	private String checkAuthenticatedCredentials(HttpServletRequest request,
			HttpServletResponse response) throws InternalServerErrorException {

		/**
		 * On receiving an HTTP request that contains the Authorization header
		 * field, the AP shall:
		 * 
		 * a)use the value of that username parameter of the Authorization
		 * header field to authenticate the user;
		 * 
		 * b)apply the procedures specified in RFC 2617 [3] for authentication;
		 * 
		 * c)if the HTTP request contains an X 3GPP Intended Identity header
		 * field (TS 124 109 [5]), then the AP may verify that the user identity
		 * belongs to the subscriber. This verification of the user identity
		 * shall be performed dependant on the subscriber's application specific
		 * or AP specific user security settings;
		 * 
		 * d)if authentication is successful, remove the Authorization header
		 * field from the HTTP request;
		 * 
		 * e)insert an HTTP X 3GPP Asserted Identity header field (TS 124 109
		 * [5]) that contains the asserted identity or a list of identities;
		 * 
		 * We wont be implementing points d and e, as they are applicable only
		 * if the Authentication Proxy had to forward the request to the XDM
		 * over the network. Here it is co-located with the XDM server.
		 */
		String authHeaderParams = request
				.getHeader(HttpConstant.HEADER_AUTHORIZATION);
		
		if (logger.isFineEnabled()) {
			logger.fine("Authorization header included with value: "+authHeaderParams);
		}
		
		// 6 is "Digest".length(), lets skip the header value till that index
		final int digestParamsStart = 6;
		if (authHeaderParams.length() > digestParamsStart) {
			authHeaderParams = authHeaderParams.substring(digestParamsStart);
		}
		
		String username = null;
		String password = null;
		String realm = null;
		String nonce = null;
		String uri = null;
		String cnonce = null;
		String nc = null;
		String qop = null;
		String resp = null;
		String opaque = null;

		for(String param : authHeaderParams.split(",")) {
			int i = param.indexOf('=');
			if (i > 0 && i < (param.length()-1)) {
				String paramName = param.substring(0,i).trim();
				String paramValue = param.substring(i+1).trim();
				if (paramName.equals("username")) {
					if (paramValue.length()>2) {
						username = paramValue.substring(1, paramValue.length()-1);
						if (logger.isFineEnabled()) {
							logger.fine("Username param with value "+username);
						}
					}
					else {
						if (logger.isFineEnabled()) {
							logger.fine("Ignoring invalid param "+paramName+" value "+paramValue);
						}
					}					
				}
				else if (paramName.equals("nonce")) {
					if (paramValue.length()>2) {
						nonce = paramValue.substring(1, paramValue.length()-1);
						if (logger.isFineEnabled()) {
							logger.fine("Nonce param with value "+nonce);
						}
					}
					else {
						if (logger.isFineEnabled()) {
							logger.fine("Ignoring invalid param "+paramName+" value "+paramValue);
						}
					}
				}
				else if (paramName.equals("cnonce")) {
					if (paramValue.length()>2) {
						cnonce = paramValue.substring(1, paramValue.length()-1);
						if (logger.isFineEnabled()) {
							logger.fine("CNonce param with value "+cnonce);
						}
					}
					else {
						if (logger.isFineEnabled()) {
							logger.fine("Ignoring invalid param "+paramName+" value "+paramValue);
						}
					}
				}
				else if (paramName.equals("realm")) {
					if (paramValue.length()>2) {
						realm = paramValue.substring(1, paramValue.length()-1);
						if (logger.isFineEnabled()) {
							logger.fine("Realm param with value "+realm);
						}
					}
					else {
						if (logger.isFineEnabled()) {
							logger.fine("Ignoring invalid param "+paramName+" value "+paramValue);
						}
					}
				}
				else if (paramName.equals("nc")) {
					nc = paramValue;
					if (logger.isFineEnabled()) {
						logger.fine("Nonce-count param with value "+nc);
					}
				}
				else if (paramName.equals("response")) {
					if (paramValue.length()>2) {
						resp = paramValue.substring(1, paramValue.length()-1);
						if (logger.isFineEnabled()) {
							logger.fine("Response param with value "+resp);
						}
					}
					else {
						if (logger.isFineEnabled()) {
							logger.fine("Ignoring invalid param "+paramName+" value "+paramValue);
						}
					}
				}
				else if (paramName.equals("uri")) {
					if (paramValue.length()>2) {
						uri = paramValue.substring(1, paramValue.length()-1);
						if (logger.isFineEnabled()) {
							logger.fine("Digest uri param with value "+uri);
						}
					}
					else {
						if (logger.isFineEnabled()) {
							logger.fine("Ignoring invalid param "+paramName+" value "+paramValue);
						}
					}
				}
				else if (paramName.equals("opaque")) {
					if (paramValue.length()>2) {
						opaque = paramValue.substring(1, paramValue.length()-1);
						if (logger.isFineEnabled()) {
							logger.fine("Opaque param with value "+opaque);
						}
					}
					else {
						if (logger.isFineEnabled()) {
							logger.fine("Ignoring invalid param "+paramName+" value "+paramValue);
						}
					}
				}
				else if (paramName.equals("qop")) {
					if (paramValue.charAt(0) == '"') {
						if (paramValue.length()>2) {
							qop = paramValue.substring(1, paramValue.length()-1);
						}
						else {
							if (logger.isFineEnabled()) {
								logger.fine("Ignoring invalid param "+paramName+" value "+paramValue);
							}
						}
					}
					else {
						qop = paramValue;
					}
					if (logger.isFineEnabled()) {
						logger.fine("Qop param with value "+qop);
					}
				}
			}
			else {
				if (logger.isFineEnabled()) {
					logger.fine("Ignoring invalid param "+param);
				}
			}
		}
		/**
		 * The client response to a WWW-Authenticate challenge for a protection
		 * space starts an authentication session with that protection space.
		 * The authentication session lasts until the client receives another
		 * WWW-Authenticate challenge from any server in the protection space. A
		 * client should remember the username, password, nonce, nonce count and
		 * opaque values associated with an authentication session to use to
		 * construct the Authorization header in future requests within that
		 * protection space.
		 */
		if (username == null || realm == null || nonce == null || cnonce == null || nc == null
				|| uri == null || resp == null || opaque == null) {
			logger
					.severe("A required parameter is missing in the challenge response");
			// FIXME should be replied with BAD REQUEST 400
			return null;
		}
		
		// verify opaque vs nonce
		if (challengeParamGenerator.getNonce(opaque).equals(nonce)) {
			if (logger.isFineEnabled())
				logger.fine("Nonce provided matches the one generated using opaque as seed");
			
		}
		else {
			if (logger.isFineEnabled())
				logger.fine("Authentication failed, nonce provided doesn't match the one generated using opaque as seed");
			return null;
		}
		
		if (!qop.equals("auth")) {
			if (logger.isFineEnabled())
				logger.fine("Authentication failed, qop value "+qop+" unsupported");
			return null;
		}
		
		// get user password
		UserProfile userProfile = getUserProfileControlSbb().find(username);
		if (userProfile == null) {
			if (logger.isFineEnabled())
				logger.fine("Authentication failed, profile not found for user "+username);
			return null;
		}
		else {
			password = userProfile.getPassword();
		}
		
		final String digest = new RFC2617AuthQopDigest(username, realm, password, nonce, nc, cnonce, request.getMethod().toUpperCase(), uri).digest();
		
		if (digest != null && digest.equals(resp)) {
			if (logger.isFineEnabled())
				logger.fine("authentication response is matching");

			/**
			 * Add the cnonce,nc and qop as received in the Authorization header
			 * of the request. We need to add the Authentication-Info header and
			 * set these values.
			 * 
			 * Authentication-Info: qop=auth-int,
			 * rspauth="6629fae49394a05397450978507c4ef1",
			 * cnonce="6629fae49393a05397450978507c4ef1", nc=00000001
			 */
			String params = "cnonce=\"" + cnonce + "\", nc=" + nc + ", qop="
					+ qop + ", rspauth=\"" + digest+"\"";

			response.addHeader("Authentication-Info", params);
			return username;
		} else {
			if (logger.isFineEnabled())
				logger.fine("authentication response digest received ("+resp+") didn't match the one calculated ("+digest+")");

			return null;
		}
	}

	/**
	 * Get the authentication scheme
	 * 
	 * @return the scheme name
	 */
	public String getScheme() {
		return "Digest";
	}

	/**
	 * get the authentication realm
	 * 
	 * @return the realm name
	 */
	public String getRealm() {
		return CONFIGURATION.getAuthenticationRealm();
	}	
	
	// -- user profile enabler child relation
	
	public abstract ChildRelationExt getUserProfileControlChildRelation();

	protected UserProfileControlSbbLocalObject getUserProfileControlSbb() {
			try {
				return (UserProfileControlSbbLocalObject) getUserProfileControlChildRelation()
						.create(ChildRelationExt.DEFAULT_CHILD_NAME);
			} catch (Exception e) {
				logger.severe("Failed to create child sbb", e);
				return null;
			}
	}
	
	// -- sbb object lifecycle
	
	public void setSbbContext(SbbContext context) {
		sbbContext = context;
		if (logger == null) {
			logger = sbbContext.getTracer(this.getClass().getSimpleName());
		}
	}

	public void unsetSbbContext() {
	}

	public void sbbCreate() throws javax.slee.CreateException {
	}

	public void sbbPostCreate() throws javax.slee.CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbRemove() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface activity) {
	}

	public void sbbRolledBack(RolledBackContext context) {
	}

	protected SbbContext getSbbContext() {
		return sbbContext;
	}

	private SbbContext sbbContext; // This SBB's SbbContext

}
