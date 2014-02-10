/*
* Jopr Management Platform
* Copyright (C) 2005-2009 Red Hat, Inc.
* All rights reserved.
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License, version 2, as
* published by the Free Software Foundation, and/or the GNU Lesser
* General Public License, version 2.1, also as published by the Free
* Software Foundation.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License and the GNU Lesser General Public License
* for more details.
*
* You should have received a copy of the GNU General Public License
* and the GNU Lesser General Public License along with this program;
* if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/
package org.rhq.plugins.jbossas5.adapter.impl.configuration;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MetaValue;

import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionMap;
import org.rhq.plugins.jbossas5.adapter.api.AbstractPropertyMapAdapter;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapter;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapterFactory;

/**
 * This class provides code that maps back and forth between a {@link PropertyMap} and
 * a {@link CompositeValue}. Subclasses exist for the two different implementations
 * of <code>CompositeValue</code>. 
 *
 * @author Mark Spritzler
 * @author Ian Springer
 */
public abstract class AbstractPropertyMapToCompositeValueAdapter extends AbstractPropertyMapAdapter
        implements PropertyAdapter<PropertyMap, PropertyDefinitionMap> {
    private final Log log = LogFactory.getLog(this.getClass());

    public void populateMetaValueFromProperty(PropertyMap propMap, MetaValue metaValue, PropertyDefinitionMap propDefMap) {
        CompositeValue compositeValue = (CompositeValue)metaValue;
        for (String mapMemberPropName : propMap.getMap().keySet()) {
            Property mapMemberProp = propMap.get(mapMemberPropName);
            PropertyDefinition mapMemberPropDef = propDefMap.get(mapMemberPropName);
            MetaType mapMemberMetaType = compositeValue.getMetaType().getType(mapMemberPropName);
            if (mapMemberMetaType == null) {
            	// this will occur when new map properties are added since they are not present
            	// in the original metaValue which we are using
            	mapMemberMetaType = SimpleMetaType.STRING;
            }           
            PropertyAdapter adapter = PropertyAdapterFactory.getPropertyAdapter(mapMemberMetaType);
            MetaValue mapMemberMetaValue = adapter.convertToMetaValue(mapMemberProp, mapMemberPropDef, mapMemberMetaType);
            putValue(compositeValue, mapMemberPropName, mapMemberMetaValue);
        }
    }

    public MetaValue convertToMetaValue(PropertyMap propMap, PropertyDefinitionMap propDefMap, MetaType metaType) {
        CompositeValue compositeValue = createCompositeValue(propDefMap, metaType);
        populateMetaValueFromProperty(propMap, compositeValue, propDefMap);
        return compositeValue;
    }

    public void populatePropertyFromMetaValue(PropertyMap propMap, MetaValue metaValue, PropertyDefinitionMap propDefMap) {
        if (metaValue == null)
            return;
        CompositeValue compositeValue = (CompositeValue)metaValue;
        Set<String> mapMemberPropNames = compositeValue.getMetaType().keySet();
        // There won't be any keys when loading a Configuration for the first time.
        for (String mapMemberPropName : mapMemberPropNames) {
            Property mapMemberProp = propMap.get(mapMemberPropName);
            MetaValue mapMemberMetaValue = compositeValue.get(mapMemberPropName);
            MetaType mapMemberMetaType = compositeValue.getMetaType().getType(mapMemberPropName);
            PropertyAdapter propertyAdapter = PropertyAdapterFactory.getPropertyAdapter(mapMemberMetaType);
            PropertyDefinition mapMemberPropDef = propDefMap.get(mapMemberPropName);
            if (mapMemberProp == null) {
                if (mapMemberPropDef != null)
                    mapMemberProp = propertyAdapter.convertToProperty(mapMemberMetaValue, mapMemberPropDef);
                else {
                    // If the member prop has no associated prop def, this is an "open map".
                    if (!mapMemberMetaType.isSimple() && !mapMemberMetaType.isEnum()) {
                        log.debug("Map member prop [" + mapMemberMetaType + "] is not a simple type - skipping...");
                        continue;
                    }
                    // Create a PropertySimple and populate it.
                    mapMemberProp = new PropertySimple(mapMemberPropName, null);
                    // NOTE: It's ok that the propDef is null - PropertySimple*Adapter.populatePropertyFromMetaValue()
                    //       doesn't use it for anything.
                    propertyAdapter.populatePropertyFromMetaValue(mapMemberProp, mapMemberMetaValue, mapMemberPropDef);
                }
                propMap.put(mapMemberProp);
            }
        }
    }

    protected abstract void putValue(CompositeValue compositeValue, String key, MetaValue value);

    protected abstract CompositeValue createCompositeValue(PropertyDefinitionMap propDefMap, MetaType metaType);
}