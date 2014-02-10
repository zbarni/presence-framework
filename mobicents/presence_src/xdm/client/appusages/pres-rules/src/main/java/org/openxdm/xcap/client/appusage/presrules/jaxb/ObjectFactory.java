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
// Generated on: 2008.05.01 at 05:38:05 PM WEST 
//


package org.openxdm.xcap.client.appusage.presrules.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openxdm.xcap.client.appusage.presrules.jaxb package. 
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

    private final static QName _ProvidePlaceIs_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-place-is");
    private final static QName _Class_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "class");
    private final static QName _ProvideUnknownAttribute_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-unknown-attribute");
    private final static QName _ProvidePrivacy_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-privacy");
    private final static QName _ProvideClass_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-class");
    private final static QName _ProvideDevices_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-devices");
    private final static QName _ProvidePlaceType_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-place-type");
    private final static QName _ProvideServices_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-services");
    private final static QName _ServiceUri_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "service-uri");
    private final static QName _ProvideMood_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-mood");
    private final static QName _ProvidePersons_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-persons");
    private final static QName _ProvideActivities_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-activities");
    private final static QName _ProvideSphere_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-sphere");
    private final static QName _ProvideUserInput_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-user-input");
    private final static QName _ServiceUriScheme_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "service-uri-scheme");
    private final static QName _OccurrenceId_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "occurrence-id");
    private final static QName _ProvideRelationship_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-relationship");
    private final static QName _ProvideTimeOffset_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-time-offset");
    private final static QName _DeviceID_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "deviceID");
    private final static QName _ProvideNote_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-note");
    private final static QName _ProvideDeviceID_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-deviceID");
    private final static QName _ProvideStatusIcon_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "provide-status-icon");
    private final static QName _SubHandling_QNAME = new QName("urn:ietf:params:xml:ns:pres-rules", "sub-handling");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openxdm.xcap.client.appusage.presrules.jaxb
     * 
     */
    public ObjectFactory() {
    }


    /**
     * Create an instance of {@link ProvidePersonPermission.AllPersons }
     * 
     */
    public ProvidePersonPermission.AllPersons createProvidePersonPermissionAllPersons() {
        return new ProvidePersonPermission.AllPersons();
    }

    /**
     * Create an instance of {@link ProvidePersonPermission }
     * 
     */
    public ProvidePersonPermission createProvidePersonPermission() {
        return new ProvidePersonPermission();
    }

    /**
     * Create an instance of {@link ProvideDevicePermission.AllDevices }
     * 
     */
    public ProvideDevicePermission.AllDevices createProvideDevicePermissionAllDevices() {
        return new ProvideDevicePermission.AllDevices();
    }

    /**
     * Create an instance of {@link ProvideServicePermission }
     * 
     */
    public ProvideServicePermission createProvideServicePermission() {
        return new ProvideServicePermission();
    }

    /**
     * Create an instance of {@link ProvideDevicePermission }
     * 
     */
    public ProvideDevicePermission createProvideDevicePermission() {
        return new ProvideDevicePermission();
    }

    /**
     * Create an instance of {@link ProvideServicePermission.AllServices }
     * 
     */
    public ProvideServicePermission.AllServices createProvideServicePermissionAllServices() {
        return new ProvideServicePermission.AllServices();
    }

    /**
     * Create an instance of {@link UnknownBooleanPermission }
     * 
     */
    public UnknownBooleanPermission createUnknownBooleanPermission() {
        return new UnknownBooleanPermission();
    }

    /**
     * Create an instance of {@link ProvideAllAttributes }
     * 
     */
    public ProvideAllAttributes createProvideAllAttributes() {
        return new ProvideAllAttributes();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-place-is")
    public JAXBElement<Boolean> createProvidePlaceIs(Boolean value) {
        return new JAXBElement<Boolean>(_ProvidePlaceIs_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "class")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createClass(String value) {
        return new JAXBElement<String>(_Class_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnknownBooleanPermission }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-unknown-attribute")
    public JAXBElement<UnknownBooleanPermission> createProvideUnknownAttribute(UnknownBooleanPermission value) {
        return new JAXBElement<UnknownBooleanPermission>(_ProvideUnknownAttribute_QNAME, UnknownBooleanPermission.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-privacy")
    public JAXBElement<Boolean> createProvidePrivacy(Boolean value) {
        return new JAXBElement<Boolean>(_ProvidePrivacy_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-class")
    public JAXBElement<Boolean> createProvideClass(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideClass_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvideDevicePermission }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-devices")
    public JAXBElement<ProvideDevicePermission> createProvideDevices(ProvideDevicePermission value) {
        return new JAXBElement<ProvideDevicePermission>(_ProvideDevices_QNAME, ProvideDevicePermission.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-place-type")
    public JAXBElement<Boolean> createProvidePlaceType(Boolean value) {
        return new JAXBElement<Boolean>(_ProvidePlaceType_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvideServicePermission }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-services")
    public JAXBElement<ProvideServicePermission> createProvideServices(ProvideServicePermission value) {
        return new JAXBElement<ProvideServicePermission>(_ProvideServices_QNAME, ProvideServicePermission.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "service-uri")
    public JAXBElement<String> createServiceUri(String value) {
        return new JAXBElement<String>(_ServiceUri_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-mood")
    public JAXBElement<Boolean> createProvideMood(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideMood_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvidePersonPermission }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-persons")
    public JAXBElement<ProvidePersonPermission> createProvidePersons(ProvidePersonPermission value) {
        return new JAXBElement<ProvidePersonPermission>(_ProvidePersons_QNAME, ProvidePersonPermission.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-activities")
    public JAXBElement<Boolean> createProvideActivities(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideActivities_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-sphere")
    public JAXBElement<Boolean> createProvideSphere(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideSphere_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-user-input")
    public JAXBElement<String> createProvideUserInput(String value) {
        return new JAXBElement<String>(_ProvideUserInput_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "service-uri-scheme")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createServiceUriScheme(String value) {
        return new JAXBElement<String>(_ServiceUriScheme_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "occurrence-id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createOccurrenceId(String value) {
        return new JAXBElement<String>(_OccurrenceId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-relationship")
    public JAXBElement<Boolean> createProvideRelationship(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideRelationship_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-time-offset")
    public JAXBElement<Boolean> createProvideTimeOffset(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideTimeOffset_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "deviceID")
    public JAXBElement<String> createDeviceID(String value) {
        return new JAXBElement<String>(_DeviceID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-note")
    public JAXBElement<Boolean> createProvideNote(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideNote_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-deviceID")
    public JAXBElement<Boolean> createProvideDeviceID(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideDeviceID_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "provide-status-icon")
    public JAXBElement<Boolean> createProvideStatusIcon(Boolean value) {
        return new JAXBElement<Boolean>(_ProvideStatusIcon_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:pres-rules", name = "sub-handling")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createSubHandling(String value) {
        return new JAXBElement<String>(_SubHandling_QNAME, String.class, null, value);
    }

}