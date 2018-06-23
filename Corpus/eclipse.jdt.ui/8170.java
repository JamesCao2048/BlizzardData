/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.jeview.properties;

import java.util.ArrayList;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class ResourceProperties implements IPropertySource {

    private static final String C_RESOURCE = "IResource";

    private static final String P_NAME = "org.eclipse.jdt.jeview.IResource.name";

    private static final String P_FULL_PATH = "org.eclipse.jdt.jeview.IResource.fullPath";

    private static final String P_LOCATION = "org.eclipse.jdt.jeview.IResource.location";

    private static final String P_PROJECT_RELATIVE_PATH = "org.eclipse.jdt.jeview.IResource.projectRelativePath";

    private static final String P_RAW_LOCATION = "org.eclipse.jdt.jeview.IResource.rawLocation";

    private static final String P_LOCAL_TIME_STAMP = "org.eclipse.jdt.jeview.IResource.localTimeStamp";

    private static final String P_MODIFICATION_STAMP = "org.eclipse.jdt.jeview.IResource.modificationStamp";

    private static final String P_EXISTS = "org.eclipse.jdt.jeview.IResource.exists";

    private static final String P_IS_ACCESSIBLE = "org.eclipse.jdt.jeview.IResource.isAccessible";

    private static final String P_IS_DERIVED = "org.eclipse.jdt.jeview.IResource.isDerived";

    private static final String P_IS_HIDDEN = "org.eclipse.jdt.jeview.IResource.isHidden";

    private static final String P_IS_LINKED = "org.eclipse.jdt.jeview.IResource.isLinked";

    private static final String P_IS_PHANTOM = "org.eclipse.jdt.jeview.IResource.isPhantom";

    private static final String P_IS_TEAM_PRIVATE_MEMBER = "org.eclipse.jdt.jeview.IResource.isTeamPrivateMember";

    private static final String P_IS_VIRTUAL = "org.eclipse.jdt.jeview.IResource.isVirtual";

    protected IResource fResource;

    private static final ArrayList<IPropertyDescriptor> RESOURCE_PROPERTY_DESCRIPTORS = new ArrayList();

    static {
        addResourceDescriptor(new PropertyDescriptor(P_NAME, "name"));
        addResourceDescriptor(new PropertyDescriptor(P_FULL_PATH, "fullPath"));
        addResourceDescriptor(new PropertyDescriptor(P_LOCATION, "location"));
        addResourceDescriptor(new PropertyDescriptor(P_PROJECT_RELATIVE_PATH, "projectRelativePath"));
        addResourceDescriptor(new PropertyDescriptor(P_RAW_LOCATION, "rawLocation"));
        addResourceDescriptor(new PropertyDescriptor(P_LOCAL_TIME_STAMP, "localTimeStamp"));
        addResourceDescriptor(new PropertyDescriptor(P_MODIFICATION_STAMP, "modificationStamp"));
        addResourceDescriptor(new PropertyDescriptor(P_EXISTS, "exists"));
        addResourceDescriptor(new PropertyDescriptor(P_IS_ACCESSIBLE, "isAccessible"));
        addResourceDescriptor(new PropertyDescriptor(P_IS_DERIVED, "isDerived"));
        addResourceDescriptor(new PropertyDescriptor(P_IS_HIDDEN, "isHidden"));
        addResourceDescriptor(new PropertyDescriptor(P_IS_LINKED, "isLinked"));
        addResourceDescriptor(new PropertyDescriptor(P_IS_PHANTOM, "isPhantom"));
        addResourceDescriptor(new PropertyDescriptor(P_IS_TEAM_PRIVATE_MEMBER, "isTeamPrivateMember"));
        addResourceDescriptor(new PropertyDescriptor(P_IS_VIRTUAL, "isVirtual"));
    }

    private static void addResourceDescriptor(PropertyDescriptor descriptor) {
        descriptor.setAlwaysIncompatible(true);
        descriptor.setCategory(C_RESOURCE);
        RESOURCE_PROPERTY_DESCRIPTORS.add(descriptor);
    }

    public  ResourceProperties(IResource resource) {
        fResource = resource;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        ArrayList<IPropertyDescriptor> result = new ArrayList(RESOURCE_PROPERTY_DESCRIPTORS);
        return result.toArray(new IPropertyDescriptor[result.size()]);
    }

    @Override
    public Object getPropertyValue(Object name) {
        if (name.equals(P_NAME)) {
            return fResource.getName();
        } else if (name.equals(P_FULL_PATH)) {
            return fResource.getFullPath();
        } else if (name.equals(P_LOCATION)) {
            return fResource.getLocation();
        } else if (name.equals(P_PROJECT_RELATIVE_PATH)) {
            return fResource.getProjectRelativePath();
        } else if (name.equals(P_RAW_LOCATION)) {
            return fResource.getRawLocation();
        } else if (name.equals(P_LOCAL_TIME_STAMP)) {
            return fResource.getLocalTimeStamp();
        } else if (name.equals(P_MODIFICATION_STAMP)) {
            return fResource.getModificationStamp();
        } else if (name.equals(P_EXISTS)) {
            return fResource.exists();
        } else if (name.equals(P_IS_ACCESSIBLE)) {
            return fResource.isAccessible();
        } else if (name.equals(P_IS_DERIVED)) {
            return fResource.isDerived();
        } else if (name.equals(P_IS_HIDDEN)) {
            return fResource.isHidden();
        } else if (name.equals(P_IS_LINKED)) {
            return fResource.isLinked();
        } else if (name.equals(P_IS_PHANTOM)) {
            return fResource.isPhantom();
        } else if (name.equals(P_IS_TEAM_PRIVATE_MEMBER)) {
            return fResource.isTeamPrivateMember();
        } else if (name.equals(P_IS_VIRTUAL)) {
            return fResource.isVirtual();
        }
        return null;
    }

    @Override
    public void setPropertyValue(Object name, Object value) {
    // do nothing
    }

    @Override
    public Object getEditableValue() {
        return this;
    }

    @Override
    public boolean isPropertySet(Object property) {
        return false;
    }

    @Override
    public void resetPropertyValue(Object property) {
    // do nothing
    }
}
