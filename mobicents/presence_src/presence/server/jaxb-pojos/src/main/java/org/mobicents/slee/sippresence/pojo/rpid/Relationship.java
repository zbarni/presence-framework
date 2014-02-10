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
// Generated on: 2008.04.25 at 12:01:52 AM WEST 
//


package org.mobicents.slee.sippresence.pojo.rpid;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.mobicents.slee.sippresence.pojo.commonschema.Empty;
import org.mobicents.slee.sippresence.pojo.commonschema.NoteT;
import org.w3c.dom.Element;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="note" type="{urn:ietf:params:xml:ns:pidf:rpid}Note_t" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="assistant" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *           &lt;element name="associate" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *           &lt;element name="family" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *           &lt;element name="friend" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *           &lt;element name="other" type="{urn:ietf:params:xml:ns:pidf:rpid}Note_t" minOccurs="0"/>
 *           &lt;element name="self" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *           &lt;element name="supervisor" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *           &lt;element name="unknown" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *           &lt;any/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "note",
    "assistant",
    "associate",
    "family",
    "friend",
    "other",
    "self",
    "supervisor",
    "unknown",
    "any"
})
@XmlRootElement(name = "relationship")
public class Relationship {

    protected NoteT note;
    protected Empty assistant;
    protected Empty associate;
    protected Empty family;
    protected Empty friend;
    protected NoteT other;
    protected Empty self;
    protected Empty supervisor;
    protected Empty unknown;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Gets the value of the note property.
     * 
     * @return
     *     possible object is
     *     {@link NoteT }
     *     
     */
    public NoteT getNote() {
        return note;
    }

    /**
     * Sets the value of the note property.
     * 
     * @param value
     *     allowed object is
     *     {@link NoteT }
     *     
     */
    public void setNote(NoteT value) {
        this.note = value;
    }

    /**
     * Gets the value of the assistant property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getAssistant() {
        return assistant;
    }

    /**
     * Sets the value of the assistant property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setAssistant(Empty value) {
        this.assistant = value;
    }

    /**
     * Gets the value of the associate property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getAssociate() {
        return associate;
    }

    /**
     * Sets the value of the associate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setAssociate(Empty value) {
        this.associate = value;
    }

    /**
     * Gets the value of the family property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getFamily() {
        return family;
    }

    /**
     * Sets the value of the family property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setFamily(Empty value) {
        this.family = value;
    }

    /**
     * Gets the value of the friend property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getFriend() {
        return friend;
    }

    /**
     * Sets the value of the friend property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setFriend(Empty value) {
        this.friend = value;
    }

    /**
     * Gets the value of the other property.
     * 
     * @return
     *     possible object is
     *     {@link NoteT }
     *     
     */
    public NoteT getOther() {
        return other;
    }

    /**
     * Sets the value of the other property.
     * 
     * @param value
     *     allowed object is
     *     {@link NoteT }
     *     
     */
    public void setOther(NoteT value) {
        this.other = value;
    }

    /**
     * Gets the value of the self property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getSelf() {
        return self;
    }

    /**
     * Sets the value of the self property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setSelf(Empty value) {
        this.self = value;
    }

    /**
     * Gets the value of the supervisor property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getSupervisor() {
        return supervisor;
    }

    /**
     * Sets the value of the supervisor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setSupervisor(Empty value) {
        this.supervisor = value;
    }

    /**
     * Gets the value of the unknown property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getUnknown() {
        return unknown;
    }

    /**
     * Sets the value of the unknown property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setUnknown(Empty value) {
        this.unknown = value;
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
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

}
