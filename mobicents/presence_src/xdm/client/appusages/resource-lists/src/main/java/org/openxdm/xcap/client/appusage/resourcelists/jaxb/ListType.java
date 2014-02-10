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
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.05.01 at 05:17:03 PM WEST 
//


package org.openxdm.xcap.client.appusage.resourcelists.jaxb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for listType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="display-name" type="{urn:ietf:params:xml:ns:resource-lists}display-nameType" minOccurs="0"/>
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *           &lt;choice>
 *             &lt;element name="list">
 *               &lt;complexType>
 *                 &lt;complexContent>
 *                   &lt;extension base="{urn:ietf:params:xml:ns:resource-lists}listType">
 *                   &lt;/extension>
 *                 &lt;/complexContent>
 *               &lt;/complexType>
 *             &lt;/element>
 *             &lt;element name="external" type="{urn:ietf:params:xml:ns:resource-lists}externalType"/>
 *             &lt;element name="entry" type="{urn:ietf:params:xml:ns:resource-lists}entryType"/>
 *             &lt;element name="entry-ref" type="{urn:ietf:params:xml:ns:resource-lists}entry-refType"/>
 *             &lt;any/>
 *           &lt;/choice>
 *         &lt;/sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listType", propOrder = {
    "displayName",
    "listOrExternalOrEntry",
    "any"
})
@XmlSeeAlso({
    ListType.List.class
})
@XmlRootElement(name = "list", namespace = "urn:ietf:params:xml:ns:resource-lists")
public class ListType {

    @XmlElement(name = "display-name")
    protected DisplayNameType displayName;
    @XmlElementRefs({
        @XmlElementRef(name = "entry-ref", namespace = "urn:ietf:params:xml:ns:resource-lists", type = JAXBElement.class),
        @XmlElementRef(name = "entry", namespace = "urn:ietf:params:xml:ns:resource-lists", type = JAXBElement.class),
        @XmlElementRef(name = "external", namespace = "urn:ietf:params:xml:ns:resource-lists", type = JAXBElement.class),
        @XmlElementRef(name = "list", namespace = "urn:ietf:params:xml:ns:resource-lists", type = JAXBElement.class)
    })
    @XmlAnyElement(lax = true)
    protected java.util.List<Object> listOrExternalOrEntry;
    @XmlAnyElement(lax = true)
    protected java.util.List<Object> any;
    @XmlAttribute
    protected String name;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link DisplayNameType }
     *     
     */
    public DisplayNameType getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisplayNameType }
     *     
     */
    public void setDisplayName(DisplayNameType value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the listOrExternalOrEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listOrExternalOrEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListOrExternalOrEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link JAXBElement }{@code <}{@link ListType.List }{@code >}
     * {@link JAXBElement }{@code <}{@link EntryRefType }{@code >}
     * {@link JAXBElement }{@code <}{@link EntryType }{@code >}
     * {@link Object }
     * {@link JAXBElement }{@code <}{@link ExternalType }{@code >}
     * 
     * 
     */
    public java.util.List<Object> getListOrExternalOrEntry() {
        if (listOrExternalOrEntry == null) {
            listOrExternalOrEntry = new ArrayList<Object>();
        }
        return this.listOrExternalOrEntry;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public java.util.List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{urn:ietf:params:xml:ns:resource-lists}listType">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class List
        extends ListType
    {


    }

}
