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

package org.openxdm.xcap.common.uri;

import org.openxdm.xcap.common.xml.XMLValidator;

/**
 * An element selector step is part of the element selector. This step extends
 * ElementSelectorStep by containing attribute information, that is used to
 * select an element from a xml document, and it's defined in XCAP specs by the
 * regular expression:
 * 
 * by-attr = NameorAny "[" attr-test "]" NameorAny = QName / "*" attr-test = "@"
 * att-name "=" att-value att-name = QName att-value = AttValue
 * 
 * @author Eduardo Martins
 * 
 */
public class ElementSelectorStepByAttr extends ElementSelectorStep {

	private String attrName = null;

	private String attrValue = null;

	/**
	 * Creates a new step from the specified element name, attribute name and
	 * attribute value. Besideds possible limitations defined by
	 * ElementSelectorStep, this constructor throws IllegalArgumentException if
	 * the attribute name is not a valid QName.
	 * 
	 * @param name
	 *            the element name of the step to be created.
	 * @param attrName
	 *            the attribute name.
	 * @param attrValue
	 *            the attribute value.
	 */
	public ElementSelectorStepByAttr(String name, String attrName,
			String attrValue) {
		super(name);
		if (XMLValidator.isQName(attrName)) {
			this.attrName = attrName;
		} else {
			throw new IllegalArgumentException(
					"attribute name must be a QName.");
		}
		this.attrValue = attrValue;
	}

	/**
	 * Retreives the attribute name of this step.
	 * 
	 * @return
	 */
	public String getAttrName() {
		return attrName;
	}

	/**
	 * Retrieves the attribute value of this step.
	 * 
	 * @return
	 */
	public String getAttrValue() {
		return attrValue;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append("[@").append(getAttrName()).append("='").append(getAttrValue()).append("']");
		return sb.toString();
	}
}
