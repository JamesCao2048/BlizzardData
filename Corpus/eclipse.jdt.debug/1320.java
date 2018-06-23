/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.macbundler;

import java.io.*;
import java.util.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.*;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;

class BundleDescription implements BundleAttributes {

    //$NON-NLS-1$
    private static final String STUB = "/System/Library/Frameworks/JavaVM.framework/Versions/A/Resources/MacOS/JavaApplicationStub";

    //$NON-NLS-1$
    private static final String ICON = "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Resources/GenericApp.icns";

    private static Set<String> RUN_MODE;

    {
        RUN_MODE = new HashSet<String>();
        RUN_MODE.add(ILaunchManager.RUN_MODE);
    }

    private ListenerList<IPropertyChangeListener> fListeners = new ListenerList();

    private Properties fProperties = new Properties();

    private List<ResourceInfo> fClassPath = new ArrayList<ResourceInfo>();

    private List<ResourceInfo> fResources = new ArrayList<ResourceInfo>();

    Properties fProperties2 = new Properties();

     BundleDescription() {
        clear();
    }

    void clear() {
        fProperties.clear();
        fClassPath.clear();
        fResources.clear();
        fProperties2.clear();
        //$NON-NLS-1$
        fProperties.put(SIGNATURE, "????");
        fProperties.put(ICONFILE, ICON);
    }

    void addResource(ResourceInfo ri, boolean onClasspath) {
        if (onClasspath)
            fClassPath.add(ri);
        else
            fResources.add(ri);
    }

    boolean removeResource(ResourceInfo ri, boolean onClasspath) {
        if (onClasspath)
            return fClassPath.remove(ri);
        return fResources.remove(ri);
    }

    ResourceInfo[] getResources(boolean onClasspath) {
        if (onClasspath)
            return fClassPath.toArray(new ResourceInfo[fClassPath.size()]);
        return fResources.toArray(new ResourceInfo[fResources.size()]);
    }

    void addListener(IPropertyChangeListener listener) {
        fListeners.add(listener);
    }

    void removeListener(IPropertyChangeListener listener) {
        fListeners.remove(listener);
    }

    String get(String key) {
        return fProperties.getProperty(key);
    }

    public String get(String key, String dflt) {
        return fProperties.getProperty(key, dflt);
    }

    public boolean get(String key, boolean dflt) {
        Boolean v = (Boolean) fProperties.get(key);
        if (v == null)
            return dflt;
        return v.booleanValue();
    }

    void setValue(String key, Object value) {
        fProperties.put(key, value);
    }

    private static AbstractJavaLaunchConfigurationDelegate getDelegate(ILaunchConfiguration lc) throws CoreException {
        ILaunchDelegate[] delegates = lc.getType().getDelegates(RUN_MODE);
        for (int i = 0; i < delegates.length; i++) {
            if (delegates[i].getDelegate() instanceof AbstractJavaLaunchConfigurationDelegate) {
                return (AbstractJavaLaunchConfigurationDelegate) delegates[i].getDelegate();
            }
        }
        //$NON-NLS-1$
        throw new CoreException(new Status(IStatus.ERROR, MacOSXUILaunchingPlugin.getUniqueIdentifier(), "Internal Error: missing Java launcher"));
    }

    void inititialize(ILaunchConfiguration lc) {
        AbstractJavaLaunchConfigurationDelegate lcd;
        try {
            lcd = getDelegate(lc);
        } catch (CoreException e) {
            return;
        }
        String appName = lc.getName();
        fProperties.put(APPNAME, appName);
        //$NON-NLS-1$
        fProperties.put(GETINFO, appName + Util.getString("BundleDescription.copyright.format"));
        try {
            fProperties.put(MAINCLASS, lcd.getMainTypeName(lc));
        } catch (CoreException e) {
            fProperties.put(MAINCLASS, "");
        }
        try {
            fProperties.put(ARGUMENTS, lcd.getProgramArguments(lc));
        } catch (CoreException e) {
            fProperties.put(ARGUMENTS, "");
        }
        String wd = null;
        try {
            wd = lcd.getWorkingDirectory(lc).getAbsolutePath();
        //			fProperties.put(WORKINGDIR, wd); //$NON-NLS-1$
        } catch (CoreException e) {
        }
        try {
            fProperties.put(MAINCLASS, lcd.getMainTypeName(lc));
        } catch (CoreException e) {
            fProperties.put(MAINCLASS, "");
        }
        try {
            String[] classpath = lcd.getClasspath(lc);
            for (int i = 0; i < classpath.length; i++) addResource(new ResourceInfo(classpath[i]), true);
        } catch (CoreException e) {
        }
        //$NON-NLS-1$
        String vmOptions2 = "";
        String vmOptions = null;
        try {
            vmOptions = lcd.getVMArguments(lc);
        } catch (CoreException e) {
        }
        if (vmOptions != null) {
            StringTokenizer st = new StringTokenizer(vmOptions);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int pos = token.indexOf('=');
                if (//$NON-NLS-1$
                pos > 2 && token.startsWith("-D")) {
                    String key = token.substring(2, pos).trim();
                    String value = token.substring(pos + 1).trim();
                    int l = value.length();
                    if (l >= 2 && value.charAt(0) == '"' && value.charAt(l - 1) == '"')
                        value = value.substring(1, l - 1);
                    if (//$NON-NLS-1$
                    "java.library.path".equals(//$NON-NLS-1$
                    key)) {
                        addDllDir(wd, value);
                    } else {
                        fProperties2.put(key, value);
                    }
                } else {
                    vmOptions2 = vmOptions2 + token + ' ';
                }
            }
        }
        fProperties.put(VMOPTIONS, vmOptions2);
        boolean isSWT = false;
        Iterator<ResourceInfo> iter = fResources.iterator();
        while (iter.hasNext()) {
            ResourceInfo ri = iter.next();
            if (//$NON-NLS-1$
            ri.fPath.indexOf("libswt-carbon") >= 0) {
                isSWT = true;
                break;
            }
        }
        fProperties.put(USES_SWT, Boolean.valueOf(isSWT));
        String launcher = null;
        if (isSWT)
            //$NON-NLS-1$
            launcher = System.getProperty("org.eclipse.swtlauncher");
        if (launcher == null) {
            //$NON-NLS-1$
            setValue(JVMVERSION, "1.4*");
            launcher = STUB;
        }
        setValue(LAUNCHER, launcher);
        IJavaProject p = null;
        try {
            p = lcd.getJavaProject(lc);
        } catch (CoreException e) {
        }
        if (p != null)
            fProperties.put(IDENTIFIER, p.getElementName());
        else
            //$NON-NLS-1$
            fProperties.put(IDENTIFIER, "");
        fireChange();
    }

    void fireChange() {
        PropertyChangeEvent e = new PropertyChangeEvent(this, ALL, null, null);
        for (IPropertyChangeListener listener : fListeners) {
            listener.propertyChange(e);
        }
    }

    private void addDllDir(String wd, String path) {
        File lib_dir;
        if (//$NON-NLS-1$
        path.startsWith("../")) {
            lib_dir = new File(wd, path);
        } else {
            lib_dir = new File(path);
        }
        if (lib_dir.isDirectory()) {
            File[] dlls = lib_dir.listFiles();
            for (int j = 0; j < dlls.length; j++) {
                try {
                    String name = dlls[j].getCanonicalPath();
                    if (//$NON-NLS-1$
                    name.endsWith(//$NON-NLS-1$
                    ".jnilib"))
                        addResource(new ResourceInfo(name), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static boolean verify(ILaunchConfiguration lc) {
        String name = lc.getName();
        if (//$NON-NLS-1$
        name.indexOf("jpage") >= 0)
            return false;
        AbstractJavaLaunchConfigurationDelegate lcd;
        try {
            lcd = getDelegate(lc);
            if (lcd.getMainTypeName(lc) == null)
                return false;
            return true;
        } catch (CoreException e) {
            return false;
        }
    }

    static boolean matches(ILaunchConfiguration lc, IJavaProject project) {
        AbstractJavaLaunchConfigurationDelegate lcd;
        try {
            lcd = getDelegate(lc);
        } catch (CoreException e) {
            return false;
        }
        IJavaProject p = null;
        try {
            p = lcd.getJavaProject(lc);
        } catch (CoreException e) {
            return false;
        }
        return project != null && project.equals(p);
    }
}
