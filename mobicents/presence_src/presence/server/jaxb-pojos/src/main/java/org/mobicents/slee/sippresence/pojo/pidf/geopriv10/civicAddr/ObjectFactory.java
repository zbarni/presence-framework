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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.15 at 03:36:58 AM WET 
//


package org.mobicents.slee.sippresence.pojo.pidf.geopriv10.civicAddr;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.mobicents.slee.sippresence.pojo.pidf.geopriv10.civicAddr package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CivicAddress_QNAME = new QName("urn:ietf:params:xml:ns:pidf:geopriv10:civicAddr", "civicAddress");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.mobicents.slee.sippresence.pojo.pidf.geopriv10.civicAddr
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CaType }
     * 
     */
    public CaType createCaType() {
        return new CaType();
    }

    /**
     * Create an instance of {@link CivicAddress }
     * 
     */
    public CivicAddress createCivicAddress() {
        return new CivicAddress();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CivicAddress }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pidf:geopriv10:civicAddr", name = "civicAddress")
    public JAXBElement<CivicAddress> createCivicAddress(CivicAddress value) {
        return new JAXBElement<CivicAddress>(_CivicAddress_QNAME, CivicAddress.class, null, value);
    }

}