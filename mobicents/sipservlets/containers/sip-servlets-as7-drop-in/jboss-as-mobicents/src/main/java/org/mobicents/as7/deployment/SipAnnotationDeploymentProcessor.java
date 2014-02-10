/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.as7.deployment;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipApplication;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;
import org.mobicents.servlet.sip.annotation.ConcurrencyControl;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;

import org.jboss.annotation.javaee.Descriptions;
import org.jboss.annotation.javaee.DisplayNames;
import org.jboss.annotation.javaee.Icons;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.AnnotationIndexUtils;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.DescriptionImpl;
import org.jboss.metadata.javaee.spec.DescriptionsImpl;
import org.jboss.metadata.javaee.spec.DisplayNameImpl;
import org.jboss.metadata.javaee.spec.DisplayNamesImpl;
import org.jboss.metadata.javaee.spec.IconImpl;
import org.jboss.metadata.javaee.spec.IconsImpl;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.SessionConfigMetaData;
import org.jboss.vfs.VirtualFile;
import org.mobicents.metadata.sip.spec.ProxyConfigMetaData;
import org.mobicents.metadata.sip.spec.Sip11MetaData;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipApplicationKeyMethodInfo;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.mobicents.metadata.sip.spec.SipServletSelectionMetaData;
import org.mobicents.metadata.sip.spec.SipServletsMetaData;

/**
 * Web annotation deployment processor.
 *
 * @author Emanuel Muckenhuber
 * @author Remy Maucherat
 * @author josemrecio@gmail.com
 */
public class SipAnnotationDeploymentProcessor implements DeploymentUnitProcessor {

    private static final Logger logger = Logger.getLogger(SipAnnotationDeploymentProcessor.class);

//    // @RunAs
//    addTypeProcessor(new RunAsProcessor(finder));
//    // @DeclareRoles
//    addTypeProcessor(new DeclareRolesProcessor(finder));
//    // @SipServlet
//    addTypeProcessor(new SipServletProcessor(finder));
//    // @SipListener
//    addTypeProcessor(new SipListenerProcessor(finder));
//    // @ConcurrencyControl
//    addTypeProcessor(new ConcurrencyControlProcessor(finder));
//    // @SipApplicationKey
//    addMethodProcessor(new SipApplicationKeyProcessor(finder));

//    protected static final DotName webFilter = DotName.createSimple(WebFilter.class.getName());
    protected static final DotName sipListener = DotName.createSimple(SipListener.class.getName());
    protected static final DotName sipServlet = DotName.createSimple(SipServlet.class.getName());
    protected static final DotName sipApplication = DotName.createSimple(SipApplication.class.getName());
    protected static final DotName sipApplicationKey = DotName.createSimple(SipApplicationKey.class.getName());
    protected static final DotName concurrencyControl = DotName.createSimple(ConcurrencyControl.class.getName());
//    protected static final DotName webServlet = DotName.createSimple(WebServlet.class.getName());
//    protected static final DotName runAs = DotName.createSimple(RunAs.class.getName());
//    protected static final DotName declareRoles = DotName.createSimple(DeclareRoles.class.getName());
//    protected static final DotName multipartConfig = DotName.createSimple(MultipartConfig.class.getName());
//    protected static final DotName servletSecurity = DotName.createSimple(ServletSecurity.class.getName());

    /**
     * Process web annotations.
     */
    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        // Commented for http://code.google.com/p/sipservlets/issues/detail?id=168
    	// When no sip.xml but annotations only, Application is not recognized as SIP App by AS7
//        SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
//        if (sipMetaData == null) {
//            return;
//        }
        SipAnnotationMetaData sipAnnotationsMetaData = deploymentUnit.getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);
        if (sipAnnotationsMetaData == null) {
            sipAnnotationsMetaData = new SipAnnotationMetaData();
            deploymentUnit.putAttachment(SipAnnotationMetaData.ATTACHMENT_KEY, sipAnnotationsMetaData);
        }
        Map<ResourceRoot, Index> indexes = AnnotationIndexUtils.getAnnotationIndexes(deploymentUnit);

        // Process components
        for (final Entry<ResourceRoot, Index> entry : indexes.entrySet()) {
            if (logger.isDebugEnabled()) logger.debug("doDeploy(): processing annotations from " + entry.getKey().getRootName());
            final Index jarIndex = entry.getValue();
            sipAnnotationsMetaData.put(entry.getKey().getRootName(), processAnnotations(sipAnnotationsMetaData, jarIndex));
        }
    }

    /**
     * Process a single index.
     *
     * @param index the annotation index
     * @throws DeploymentUnitProcessingException
     */
    protected SipMetaData processAnnotations(SipAnnotationMetaData sipAnotationsMetaData, Index index) throws DeploymentUnitProcessingException {
        Sip11MetaData sipMetaData = new Sip11MetaData();
        // @SipListener
        final List<AnnotationInstance> sipListenerAnnotations = index.getAnnotations(sipListener);
        if (sipListenerAnnotations != null && sipListenerAnnotations.size() > 0) {
            List<ListenerMetaData> listeners = new ArrayList<ListenerMetaData>();
            for (final AnnotationInstance annotation : sipListenerAnnotations) {
                if (logger.isDebugEnabled()) logger.debug("processAnnotations(): @SipListener: " + annotation);
                ListenerMetaData listener = new ListenerMetaData();
                AnnotationTarget target = annotation.target();
                if (!(target instanceof ClassInfo)) {
                    throw new DeploymentUnitProcessingException("@SipListener is only allowed at class level " + target);
                }
                ClassInfo classInfo = ClassInfo.class.cast(target);
                listener.setListenerClass(classInfo.toString());
                AnnotationValue descriptionValue = annotation.value();
                if (descriptionValue != null) {
                    DescriptionGroupMetaData descriptionGroup = getDescriptionGroup(descriptionValue.asString());
                    if (descriptionGroup != null) {
                        listener.setDescriptionGroup(descriptionGroup);
                    }
                }
                for (AnnotationValue value: annotation.values()) {
                    if (value.name().compareTo("name") == 0) {
                        // listener name not supported
                        //listener.setName(value.asString());
                    }
                    else if (value.name().compareTo("applicationName") == 0) {
                        if (sipMetaData.getApplicationName() == null) {
                            sipMetaData.setApplicationName(value.asString());
                        }
                        else if ((sipMetaData.getApplicationName() != null) && (sipMetaData.getApplicationName().compareTo(value.asString()) != 0)) {
                            throw (new DeploymentUnitProcessingException("Sip Application Name mismatch: already loaded: " + sipMetaData.getApplicationName() + " - from @SipListener annotation (" + listener.getListenerClass() + "): " + value.asString()));
                        }
                    }
                }
                if (logger.isDebugEnabled()) logger.debug("processAnnotations(): sipListener added " + listener);
                listeners.add(listener);
            }
            if (logger.isDebugEnabled()) logger.debug("processAnnotations(): " + listeners.size() + " sipListeners added");
            sipMetaData.setListeners(listeners);
        }

        // @SipServlet
        final List<AnnotationInstance> sipServletAnnotations = index.getAnnotations(sipServlet);
        if (sipServletAnnotations != null && sipServletAnnotations.size() > 0) {
            SipServletsMetaData sipServlets = new SipServletsMetaData();
            // @SipApplication
            final List<AnnotationInstance> sipApplicationAnnotations = index.getAnnotations(sipApplication);
            boolean sipApplicationPresent = false;
            String parsedAnnotatedPackage = null;
            if (sipApplicationAnnotations != null && sipApplicationAnnotations.size() > 0) {
                sipApplicationPresent = true;
            }
            for (final AnnotationInstance annotation : sipServletAnnotations) {
                if (logger.isDebugEnabled()) logger.debug("processAnnotations(): @SipServlet: " + annotation);
                ServletMetaData servlet = new ServletMetaData();
                AnnotationTarget target = annotation.target();
                if (!(target instanceof ClassInfo)) {
                    throw new DeploymentUnitProcessingException("@SipServlet is only allowed at class level " + target);
                }
                ClassInfo classInfo = ClassInfo.class.cast(target);
                servlet.setServletClass(classInfo.toString());
                AnnotationValue descriptionValue = annotation.value();
                if (descriptionValue != null) {
                    DescriptionGroupMetaData descriptionGroup = getDescriptionGroup(descriptionValue.asString());
                    if (descriptionGroup != null) {
                        servlet.setDescriptionGroup(descriptionGroup);
                    }
                }
                for (AnnotationValue value: annotation.values()) {
                    if (value.name().compareTo("name") == 0) {
                        servlet.setName(value.asString());
                    }
                    else if (value.name().compareTo("loadOnStartup") == 0) {
                        servlet.setLoadOnStartup(value.asString());
                    }
                    else if (value.name().compareTo("applicationName") == 0) {
                        if (sipMetaData.getApplicationName() == null) {
                            sipMetaData.setApplicationName(value.asString());
                        }
                        else if ((sipMetaData.getApplicationName() != null) && (sipMetaData.getApplicationName().compareTo(value.asString()) != 0)) {
                            throw (new DeploymentUnitProcessingException("Sip Application Name mismatch: already loaded: " + sipMetaData.getApplicationName() + " - from @SipServlet annotation (" + servlet.getServletClass() + "): " + value.asString()));
                        }
                    }
                }
                if (servlet.getName() == null) {
                    // must have a name
                    servlet.setName(servlet.getServletClass());
                }
                if (sipApplicationPresent) {
                	sipAnotationsMetaData.setSipApplicationAnnotationPresent(true);
                    AnnotationInstance sipAppAnnotation = sipApplicationAnnotations.get(0);
                    if (logger.isDebugEnabled()) logger.debug("processAnnotations(): @SipAnnotation: " + annotation);
                    AnnotationTarget sipAppTarget = sipAppAnnotation.target();
                    if (!(target instanceof ClassInfo)) {
                        throw new DeploymentUnitProcessingException("@SipAnnotation is only allowed at class level " + target);
                    }
                    ClassInfo sipAppClassInfo = ClassInfo.class.cast(sipAppTarget);
                    String packageName = sipAppClassInfo.toString().substring(0,sipAppClassInfo.toString().lastIndexOf('.'));
                    if (parsedAnnotatedPackage != null && !parsedAnnotatedPackage.equals(packageName)) {
                        throw new DeploymentUnitProcessingException("Cant have two different applications in a single context - " + packageName + " and " + parsedAnnotatedPackage);
                    }
                    if (parsedAnnotatedPackage == null) {
                        parsedAnnotatedPackage = packageName;
                        parseSipApplication(sipMetaData,sipAppAnnotation, packageName);
                    }
                }
                if (logger.isDebugEnabled()) logger.debug("processAnnotations(): sipServlet added " + servlet);
                sipServlets.add(servlet);
            }
            if (logger.isDebugEnabled()) logger.debug("processAnnotations(): " + sipServlets.size() + " sipServlets added");
            sipMetaData.setSipServlets(sipServlets);
        }

        //@SipApplicationKey
        final List<AnnotationInstance> sipApplicationKeyAnnotations = index.getAnnotations(sipApplicationKey);
        if (sipApplicationKeyAnnotations != null && sipApplicationKeyAnnotations.size() > 0) {
            for (final AnnotationInstance annotation : sipApplicationKeyAnnotations) {
                if (logger.isDebugEnabled()) logger.debug("processAnnotations(): @SipApplicationKey: " + annotation);
                final AnnotationTarget target = annotation.target();
                if (!(target instanceof MethodInfo)) {
                    throw new DeploymentUnitProcessingException("@SipApplicationKey is only allowed at method level " + target);
                }
                final MethodInfo methodInfo = MethodInfo.class.cast(target);
                if(!Modifier.isStatic(methodInfo.flags()) || !Modifier.isPublic(methodInfo.flags())) {
                	throw new DeploymentUnitProcessingException("A method annotated with the @SipApplicationKey annotation MUST be public and static");
                }
                if(!methodInfo.returnType().name().toString().equals(String.class.getName())) {
                	throw new DeploymentUnitProcessingException("A method annotated with the @SipApplicationKey annotation MUST return a String");
                }
                final Type[] types = methodInfo.args();
                if(types.length != 1 || !types[0].name().toString().equals(SipServletRequest.class.getName())) {
                	throw new DeploymentUnitProcessingException("A method annotated with the @SipApplicationKey annotation MUST have a single argument of type SipServletRequest");
                }
                if(sipMetaData.getSipApplicationKeyMethodInfo() != null) {
                	throw new DeploymentUnitProcessingException("More than one SipApplicationKey annotated method is not allowed.");
                }
                final SipApplicationKeyMethodInfo sipApplicationKeyMethodInfo = new SipApplicationKeyMethodInfo(methodInfo.declaringClass().name().toString(), methodInfo.name());
                sipMetaData.setSipApplicationKeyMethodInfo(sipApplicationKeyMethodInfo);
            	if (logger.isDebugEnabled()) logger.debug("processAnnotations(): added " + sipApplicationKeyMethodInfo + " as @SipApplicationKey method");
            }
        }
        //@ConcurrencyControl
        final List<AnnotationInstance> concurrencyControlAnnotations = index.getAnnotations(concurrencyControl);
        if (concurrencyControlAnnotations != null && concurrencyControlAnnotations.size() > 0) {
            for (final AnnotationInstance annotation : concurrencyControlAnnotations) {
                if (logger.isDebugEnabled()) logger.debug("processAnnotations(): @ConcurrencyControl: " + annotation);
                final AnnotationTarget target = annotation.target();
                if (!(target instanceof ClassInfo)) {
                    throw new DeploymentUnitProcessingException("@ConcurrencyControl is only allowed at package level " + target);
                }
                ConcurrencyControlMode mode = ConcurrencyControlMode.valueOf(annotation.value("mode").asString());
                sipMetaData.setConcurrencyControlMode(mode);
            	if (logger.isDebugEnabled()) logger.debug("processAnnotations(): ConcurrencyControl set to " + mode);
            }
        }
        return sipMetaData;
    }

    protected Descriptions getDescription(String description) {
        DescriptionsImpl descriptions = null;
        if (description.length() > 0) {
            DescriptionImpl di = new DescriptionImpl();
            di.setDescription(description);
            descriptions = new DescriptionsImpl();
            descriptions.add(di);
        }
        return descriptions;
    }

    protected DisplayNames getDisplayName(String displayName) {
        DisplayNamesImpl displayNames = null;
        if (displayName.length() > 0) {
            DisplayNameImpl dn = new DisplayNameImpl();
            dn.setDisplayName(displayName);
            displayNames = new DisplayNamesImpl();
            displayNames.add(dn);
        }
        return displayNames;
    }

    protected Icons getIcons(String smallIcon, String largeIcon) {
        IconsImpl icons = null;
        if (smallIcon.length() > 0 || largeIcon.length() > 0) {
            IconImpl i = new IconImpl();
            i.setSmallIcon(smallIcon);
            i.setLargeIcon(largeIcon);
            icons = new IconsImpl();
            icons.add(i);
        }
        return icons;
    }

    protected DescriptionGroupMetaData getDescriptionGroup(String description) {
        DescriptionGroupMetaData dg = null;
        if (description.length() > 0) {
            dg = new DescriptionGroupMetaData();
            Descriptions descriptions = getDescription(description);
            dg.setDescriptions(descriptions);
        }
        return dg;
    }

    protected DescriptionGroupMetaData getDescriptionGroup(String description, String displayName, String smallIcon,
            String largeIcon) {
        DescriptionGroupMetaData dg = null;
        if (description.length() > 0 || displayName.length() > 0 || smallIcon.length() > 0 || largeIcon.length() > 0) {
            dg = new DescriptionGroupMetaData();
            Descriptions descriptions = getDescription(description);
            if (descriptions != null)
                dg.setDescriptions(descriptions);
            DisplayNames displayNames = getDisplayName(displayName);
            if (displayNames != null)
                dg.setDisplayNames(displayNames);
            Icons icons = getIcons(smallIcon, largeIcon);
            if (icons != null)
                dg.setIcons(icons);
        }
        return dg;
    }

    private static SipApplication getApplicationAnnotation(Package pack) {
        if(pack == null) return null;
        SipApplication sipApp = (SipApplication) pack.getAnnotation(SipApplication.class);
        if(sipApp != null) {
            return sipApp;
        }
        return null;
    }

    private void parseSipApplication(SipMetaData sipMetaData, AnnotationInstance sipAppAnnInstance, String packageName) throws DeploymentUnitProcessingException {
        String description = null;
        String displayName = null;
        String largeIcon = null;
        String smallIcon = null;

        for (AnnotationValue value: sipAppAnnInstance.values()) {
            // application name
            if (value.name().compareTo("name") == 0) {
                if (sipMetaData.getApplicationName() == null) {
                	if (logger.isDebugEnabled()) logger.debug("parseSipApplication(): @SipApplication: " + value.asString());
                    sipMetaData.setApplicationName(value.asString());
                }
                else if ((sipMetaData.getApplicationName() != null) && (sipMetaData.getApplicationName().compareTo(value.asString()) != 0)) {
                    throw (new DeploymentUnitProcessingException("Sip Application Name mismatch! Already defined: " + sipMetaData.getApplicationName() + " - from @SipAnnotation (package " + packageName + "): " + value.asString()));
                }
            }
            // description
            else if (value.name().compareTo("description") == 0) {
                DescriptionImpl di = new DescriptionImpl();
                di.setDescription(value.asString());
                getOrCreateDs(getOrCreateDG(sipMetaData)).add(di);
            }
            // display name
            else if (value.name().compareTo("displayName") == 0) {
                DisplayNameImpl dn = new DisplayNameImpl();
                dn.setDisplayName(value.asString());
                getOrCreateDn(getOrCreateDG(sipMetaData)).add(dn);
            }
            // distributable
            else if (value.name().compareTo("distributable") == 0) {
                throw (new DeploymentUnitProcessingException("Distributable not supported yet"));
                // sipMetaData.setDistributable(new EmptyMetaData());
            }
            // large icon
            else if (value.name().compareTo("largeIcon") == 0) {
                largeIcon = value.asString();
            }
            // small icon
            else if (value.name().compareTo("smallIcon") == 0) {
                smallIcon = value.asString();
            }
            else if (value.name().compareTo("mainServlet") == 0) {
                if (sipMetaData.getServletSelection() == null) {
                    sipMetaData.setServletSelection(new SipServletSelectionMetaData());
                }
                sipMetaData.getServletSelection().setMainServlet(value.asString());
            }
            else if (value.name().compareTo("proxyTimeout") == 0) {
                if(sipMetaData.getProxyConfig() == null) {
                    sipMetaData.setProxyConfig(new ProxyConfigMetaData());
                }
                sipMetaData.getProxyConfig().setProxyTimeout(value.asInt());
            }
            else if (value.name().compareTo("sessionTimeout") == 0) {
                if(sipMetaData.getSessionConfig() == null) {
                    sipMetaData.setSessionConfig(new SessionConfigMetaData());
                }
                sipMetaData.getSessionConfig().setSessionTimeout(value.asInt());
            }
        }
        if (smallIcon != null || largeIcon != null) {
            IconImpl icon = new IconImpl();
            if (smallIcon != null)
                icon.setSmallIcon(smallIcon);
            if (largeIcon != null)
                icon.setLargeIcon(largeIcon);
            getOrCreateIs(getOrCreateDG(sipMetaData)).add(icon);
        }
        if (sipMetaData.getApplicationName() == null) {
        	if (logger.isDebugEnabled()) logger.debug("parseSipApplication(): @SipApplication: " + packageName);
            sipMetaData.setApplicationName(packageName);
        }
    }

    private static DescriptionGroupMetaData getOrCreateDG(SipMetaData sipMetaData) {
        DescriptionGroupMetaData dg = sipMetaData.getDescriptionGroup();
        if (dg == null) {
            dg = new DescriptionGroupMetaData();
            sipMetaData.setDescriptionGroup(dg);
        }
        return dg;
    }

    private static DescriptionsImpl getOrCreateDs(DescriptionGroupMetaData dg) {
        DescriptionsImpl ds = (DescriptionsImpl) dg.getDescriptions();
        if (ds == null) {
            ds = new DescriptionsImpl();
            dg.setDescriptions(ds);
        }
        return ds;
    }

    private static DisplayNamesImpl getOrCreateDn(DescriptionGroupMetaData dg) {
        DisplayNamesImpl dn = (DisplayNamesImpl) dg.getDisplayNames();
        if (dn == null) {
            dn = new DisplayNamesImpl();
            dg.setDisplayNames(dn);
        }
        return dn;
    }

    private static IconsImpl getOrCreateIs(DescriptionGroupMetaData dg) {
        IconsImpl ii = (IconsImpl) dg.getIcons();
        if (ii == null) {
            ii = new IconsImpl();
            dg.setIcons(ii);
        }
        return ii;
    }


    static final String SIP_XML = "WEB-INF/sip.xml";

    protected boolean canHandle(DeploymentUnit deploymentUnit) {
        //return DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit);
        final ResourceRoot deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
        final VirtualFile sipXml = deploymentRoot.getRoot().getChild(SIP_XML);
        if (DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit) && sipXml.exists()) {
            return true;
        }
        return false;
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }
}
