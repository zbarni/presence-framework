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

package org.mobicents.servlet.sip.core;

import javax.sip.address.Hop;

/**
 * Hop implementation
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class HopImpl implements Hop {
	private String host;
	private int port;
	private String transport;
	
	/**
	 * @param host
	 * @param port
	 * @param transport
	 */
	public HopImpl(String host, int port, String transport) {
		this.host = host;
		this.port = port;
		this.transport = transport;
	}
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * @return the transport
	 */
	public String getTransport() {
		return transport;
	}	
	
	/**
     * Debugging println.
     */
    public String toString() {
        return host + ":" + port + "/" + transport;
    }
}
