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


package org.mobicents.slee.sippresence.pojo.pidf.uacaps;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for actortype complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="actortype">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="supported" type="{urn:ietf:params:xml:ns:pidf:caps}actortypes" minOccurs="0"/>
 *         &lt;element name="notsupported" type="{urn:ietf:params:xml:ns:pidf:caps}actortypes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "actortype", propOrder = {
    "supported",
    "notsupported"
})
public class Actortype {

    protected Actortypes supported;
    protected Actortypes notsupported;

    /**
     * Gets the value of the supported property.
     * 
     * @return
     *     possible object is
     *     {@link Actortypes }
     *     
     */
    public Actortypes getSupported() {
        return supported;
    }

    /**
     * Sets the value of the supported property.
     * 
     * @param value
     *     allowed object is
     *     {@link Actortypes }
     *     
     */
    public void setSupported(Actortypes value) {
        this.supported = value;
    }

    /**
     * Gets the value of the notsupported property.
     * 
     * @return
     *     possible object is
     *     {@link Actortypes }
     *     
     */
    public Actortypes getNotsupported() {
        return notsupported;
    }

    /**
     * Sets the value of the notsupported property.
     * 
     * @param value
     *     allowed object is
     *     {@link Actortypes }
     *     
     */
    public void setNotsupported(Actortypes value) {
        this.notsupported = value;
    }

}