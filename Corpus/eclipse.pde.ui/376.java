/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Bug 242461 - JSR045 support
 *******************************************************************************/
package org.eclipse.pde.internal.launching.sourcelookup;

import java.io.File;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jdt.debug.core.*;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TargetPlatformHelper;

public class PDESourceLookupQuery implements ISafeRunnable {

    //$NON-NLS-1$
    private static final String OSGI_CLASSLOADER = "org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader";

    /**
	 * Support for refactored bundle class loaders (bug 402005)
	 */
    //$NON-NLS-1$
    private static final String OSGI_LUNA_CLASSLOADER1 = "org.eclipse.osgi.internal.loader.ModuleClassLoader";

    //$NON-NLS-1$
    private static final String OSGI_LUNA_CLASSLOADER2 = "org.eclipse.osgi.internal.loader.EquinoxClassLoader";

    private static final String OSGI_CURRENT_CLASSLOADER;

    static {
        ClassLoader loader = PDESourceLookupQuery.class.getClassLoader();
        if (loader != null) {
            OSGI_CURRENT_CLASSLOADER = loader.getClass().getName();
        } else {
            //$NON-NLS-1$
            OSGI_CURRENT_CLASSLOADER = "unknown";
        }
    }

    //$NON-NLS-1$
    private static final String LEGACY_ECLIPSE_CLASSLOADER = "org.eclipse.core.runtime.adaptor.EclipseClassLoader";

    //$NON-NLS-1$
    private static final String MAIN_CLASS = "org.eclipse.core.launcher.Main";

    //$NON-NLS-1$
    private static final String MAIN_PLUGIN = "org.eclipse.platform";

    private Object fElement;

    private Object fResult;

    private PDESourceLookupDirector fDirector;

    public  PDESourceLookupQuery(PDESourceLookupDirector director, Object object) {
        fElement = object;
        fDirector = director;
    }

    @Override
    public void handleException(Throwable exception) {
    }

    @Override
    public void run() throws Exception {
        IJavaReferenceType declaringType = null;
        String sourcePath = null;
        if (fElement instanceof IJavaStackFrame) {
            IJavaStackFrame stackFrame = (IJavaStackFrame) fElement;
            declaringType = stackFrame.getReferenceType();
            // under JSR 45 source path from the stack frame is more precise than anything derived from the type:
            sourcePath = stackFrame.getSourcePath();
        } else if (fElement instanceof IJavaObject) {
            IJavaType javaType = ((IJavaObject) fElement).getJavaType();
            if (javaType instanceof IJavaReferenceType) {
                declaringType = (IJavaReferenceType) javaType;
            }
        } else if (fElement instanceof IJavaReferenceType) {
            declaringType = (IJavaReferenceType) fElement;
        }
        if (declaringType != null) {
            IJavaObject classLoaderObject = declaringType.getClassLoaderObject();
            String declaringTypeName = declaringType.getName();
            if (sourcePath == null) {
                String[] sourcePaths = declaringType.getSourcePaths(null);
                if (sourcePaths != null) {
                    sourcePath = sourcePaths[0];
                }
                if (sourcePath == null) {
                    sourcePath = generateSourceName(declaringTypeName);
                }
            }
            if (classLoaderObject != null) {
                IJavaClassType type = (IJavaClassType) classLoaderObject.getJavaType();
                String classLoaderName = type.getName();
                if (OSGI_CLASSLOADER.equals(classLoaderName)) {
                    if (fDirector.getOSGiRuntimeVersion() < 3.5) {
                        fResult = findSourceElement34(classLoaderObject, sourcePath);
                    } else {
                        fResult = findSourceElement(classLoaderObject, sourcePath);
                    }
                } else if (OSGI_LUNA_CLASSLOADER1.equals(classLoaderName) || OSGI_LUNA_CLASSLOADER2.equals(classLoaderName) || OSGI_CURRENT_CLASSLOADER.equals(classLoaderName)) {
                    fResult = findSourceElement(classLoaderObject, sourcePath);
                } else if (LEGACY_ECLIPSE_CLASSLOADER.equals(classLoaderName)) {
                    fResult = findSourceElement_legacy(classLoaderObject, sourcePath);
                } else if (MAIN_CLASS.equals(declaringTypeName)) {
                    IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(MAIN_PLUGIN);
                    if (model != null)
                        fResult = getSourceElement(model.getInstallLocation(), MAIN_PLUGIN, sourcePath, true);
                }
            }
        }
    }

    protected Object getResult() {
        return fResult;
    }

    private String getValue(IJavaObject object, String variable) throws DebugException {
        IJavaFieldVariable var = object.getField(variable, false);
        return var == null ? null : var.getValue().getValueString();
    }

    /**
	 * Finds a source element in a 3.4 OSGi runtime.
	 *
	 * @param object Bundle class loader object
	 * @param typeName fully qualified name of the source type being searched for
	 * @return source element
	 * @throws CoreException
	 */
    protected Object findSourceElement34(IJavaObject object, String typeName) throws CoreException {
        //$NON-NLS-1$
        IJavaObject manager = getObject(object, "manager", false);
        if (manager != null) {
            //$NON-NLS-1$
            IJavaObject data = getObject(manager, "data", false);
            if (data != null) {
                String location = getValue(//$NON-NLS-1$
                data, //$NON-NLS-1$
                "fileName");
                String id = getValue(//$NON-NLS-1$
                data, //$NON-NLS-1$
                "symbolicName");
                return getSourceElement(location, id, typeName, true);
            }
        }
        return null;
    }

    /**
	 * Finds source in a 3.5 runtime. In 3.5, the OSGi runtime provides hooks to properly
	 * lookup source in fragments that replace/prepend jars in their host.
	 *
	 * @param object Bundle class loader object
	 * @param typeName fully qualified name of the source type being searched for
	 * @return source element
	 * @throws CoreException
	 */
    protected Object findSourceElement(IJavaObject object, String typeName) throws CoreException {
        //$NON-NLS-1$
        IJavaObject manager = getObject(object, "manager", false);
        if (manager != null) {
            // search manager's class path for location
            Object result = searchClasspathEntries(manager, typeName);
            if (result != null) {
                return result;
            }
            // then check its fragments
            //$NON-NLS-1$
            IJavaObject frgArray = getObject(manager, "fragments", false);
            if (frgArray instanceof IJavaArray) {
                IJavaArray fragments = (IJavaArray) frgArray;
                for (int i = 0; i < fragments.getLength(); i++) {
                    IJavaObject fragment = (IJavaObject) fragments.getValue(i);
                    if (!fragment.isNull()) {
                        // search fragment class path
                        result = searchClasspathEntries(fragment, typeName);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
	 * Search a bundle's class path entries for source for the type of given name.
	 * This is used for 3.5 and greater.
	 *
	 * @param entriesOwner
	 * @param typeName
	 * @return source object or <code>null</code>
	 * @throws CoreException
	 */
    private Object searchClasspathEntries(IJavaObject entriesOwner, String typeName) throws CoreException {
        //$NON-NLS-1$
        IJavaObject cpeArray = getObject(entriesOwner, "entries", false);
        if (cpeArray instanceof IJavaArray) {
            IJavaArray entries = (IJavaArray) cpeArray;
            for (int i = 0; i < entries.getLength(); i++) {
                IJavaObject entry = (IJavaObject) entries.getValue(i);
                if (!entry.isNull()) {
                    IJavaObject baseData = getObject(//$NON-NLS-1$
                    entry, //$NON-NLS-1$
                    "data", //$NON-NLS-1$
                    false);
                    if (baseData != null && !baseData.isNull()) {
                        IJavaObject fileName = getObject(//$NON-NLS-1$
                        baseData, //$NON-NLS-1$
                        "fileName", //$NON-NLS-1$
                        false);
                        if (fileName != null && !fileName.isNull()) {
                            String location = fileName.getValueString();
                            String symbolicName = getValue(//$NON-NLS-1$
                            baseData, //$NON-NLS-1$
                            "symbolicName");
                            Object el = getSourceElement(location, symbolicName, typeName, false);
                            if (el != null) {
                                return el;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private IJavaObject getObject(IJavaObject object, String field, boolean superfield) throws DebugException {
        IJavaFieldVariable variable = object.getField(field, superfield);
        if (variable != null) {
            IValue value = variable.getValue();
            if (value instanceof IJavaObject)
                return (IJavaObject) value;
        }
        return null;
    }

    private Object findSourceElement_legacy(IJavaObject object, String typeName) throws CoreException {
        //$NON-NLS-1$
        IJavaObject hostdata = getObject(object, "hostdata", true);
        if (hostdata != null) {
            //$NON-NLS-1$
            String location = getValue(hostdata, "fileName");
            //$NON-NLS-1$
            String id = getValue(hostdata, "symbolicName");
            return getSourceElement(location, id, typeName, true);
        }
        return null;
    }

    /**
	 * Looks up source in the source containers associated with the bundle at the given location.
	 * Searches associated fragments if source is not found in that location only if
	 * <code>chechFragments</code> is <code>true</code> (which should only be done when < 3.5,
	 * as this is just a guess in random order).
	 *
	 * @param location location of bundle jar / class file folder
	 * @param id symbolic name of bundle or fragment
	 * @param typeName qualified name of source
	 * @param checkFragments whether to guess at fragments
	 * @return source element or <code>null</code>
	 * @throws CoreException
	 */
    private Object getSourceElement(String location, String id, String typeName, boolean checkFragments) throws CoreException {
        if (location != null && id != null) {
            Object result = findSourceElement(getSourceContainers(location, id), typeName);
            if (result != null)
                return result;
            // don't give up yet, search fragments attached to this host
            if (checkFragments) {
                State state = TargetPlatformHelper.getState();
                BundleDescription desc = state.getBundle(id, null);
                if (desc != null) {
                    BundleDescription[] fragments = desc.getFragments();
                    for (BundleDescription fragment : fragments) {
                        location = fragment.getLocation();
                        id = fragment.getSymbolicName();
                        result = findSourceElement(getSourceContainers(location, id), typeName);
                        if (result != null)
                            return result;
                    }
                }
            }
        }
        return null;
    }

    private Object findSourceElement(ISourceContainer[] containers, String typeName) throws CoreException {
        for (ISourceContainer container : containers) {
            Object[] result = container.findSourceElements(typeName);
            if (result.length > 0)
                return result[0];
        }
        return null;
    }

    protected ISourceContainer[] getSourceContainers(String location, String id) throws CoreException {
        return fDirector.getSourceContainers(location, id);
    }

    /**
	 * Generates and returns a source file path based on a qualified type name.
	 * For example, when <code>java.lang.String</code> is provided,
	 * the returned source name is <code>java/lang/String.java</code>.
	 *
	 * @param qualifiedTypeName fully qualified type name that may contain inner types
	 *  denoted with <code>$</code> character
	 * @return a source file path corresponding to the type name
	 */
    private static String generateSourceName(String qualifiedTypeName) {
        int index = qualifiedTypeName.indexOf('$');
        if (index >= 0)
            qualifiedTypeName = qualifiedTypeName.substring(0, index);
        //$NON-NLS-1$
        return qualifiedTypeName.replace('.', File.separatorChar) + ".java";
    }
}
