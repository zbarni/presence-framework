/*
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
package org.mobicents.servlet.sip.demo.jruby;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;

public class JRubySipServlet extends SipServlet {
	private static final long serialVersionUID = 1L;
	public static final String PR_JNDI_NAME = "media/trunk/PacketRelay/$";
	private static Logger logger = Logger.getLogger(JRubySipServlet.class);
//	private static final String AUDIO_DIR = "/audio";
		
	@Override
    	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		if (resp.getStatus() == SipServletResponse.SC_OK) {
			String audioFilePath = (String)getServletContext().getAttribute("audioFilePath");
			logger.info("audio file path is " + audioFilePath);
			try {
				MsPeer peer = MsPeerFactory.getPeer("org.mobicents.mscontrol.impl.MsPeerImpl");
				MsProvider provider = peer.getProvider();
				MsSession session = provider.createSession();
				MsConnection connection = session.createNetworkConnection(PR_JNDI_NAME);
				MediaConnectionListener listener = new MediaConnectionListener();
				listener.setResponse(resp);
				listener.setAudioFilePath(audioFilePath);
				connection.addConnectionListener(listener);
				Object sdpObj = resp.getContent();
				byte[] sdpBytes = (byte[]) sdpObj;
				String sdp = new String(sdpBytes); 
				connection.modify("$", sdp);
			} catch (ClassNotFoundException e) {
				logger.error("class not found ", e);
			}			
		}
	}
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		SipServletResponse ok = request.createResponse(SipServletResponse.SC_OK);
		ok.send();
	}
}
