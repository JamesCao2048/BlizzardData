/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.refactoring.infra;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public final class DebugUtils {

    private  DebugUtils() {
    }

    public static void dumpCharCharArray(String msg, char[][] o) {
        //$NON-NLS-1$
        dump("DUMPING char[][]:" + msg);
        for (int i = 0; i < o.length; i++) {
            dump(new String(o[i]));
        }
    }

    public static void dumpArray(String msg, Object[] refs) {
        //$NON-NLS-1$
        System.out.println("DUMPING array: " + msg);
        if (refs == null) {
            //$NON-NLS-1$
            System.out.println("null");
            return;
        }
        for (int i = 0; i < refs.length; i++) System.out.println(refs[i].toString());
    }

    public static void dumpCollectionCollection(Collection<?> c) {
        for (Iterator<?> iter = c.iterator(); iter.hasNext(); ) {
            //$NON-NLS-1$
            dumpCollection("", (List<?>) iter.next());
        }
    }

    public static void dumpCollection(String msg, Collection<?> c) {
        //$NON-NLS-1$
        System.out.println("DUMPING collection: " + msg);
        if (c == null) {
            //$NON-NLS-1$
            System.out.println("null");
            return;
        }
        for (Iterator<?> iter = c.iterator(); iter.hasNext(); ) {
            System.out.println(iter.next().toString());
        }
    }

    public static void dumpIMethod(IMethod method) {
        try {
            if (method == null) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "DUMPING method: null");
                return;
            }
            System.out.println(//$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
            "DUMPING method:" + method.getElementName() + "\n " + method.getSignature() + "\n declared in " + method.getDeclaringType().getFullyQualifiedName('.') + "\nreturnType:" + //$NON-NLS-1$
            method.getReturnType());
            //$NON-NLS-1$
            dumpArray("paramTypes:", method.getParameterTypes());
            //$NON-NLS-1$
            dumpArray("exceptions:", method.getExceptionTypes());
        } catch (JavaModelException e) {
            System.out.println("JavaModelException: " + e.getMessage());
        }
    }

    public static void dumpIMethodList(String msg, List<?> l) {
        //$NON-NLS-1$
        System.out.println("DUMPING IMethodList: " + msg);
        if (l == null) {
            //$NON-NLS-1$
            System.out.println("null");
            return;
        }
        Iterator<?> iter = l.iterator();
        while (iter.hasNext()) {
            dumpIMethod((IMethod) iter.next());
        }
    }

    public static void dumpIType(String msg, IType type) {
        //$NON-NLS-1$
        System.out.println("DUMPING IType:" + msg);
        //$NON-NLS-1$
        System.out.println("exists:" + type.exists());
        try {
            //$NON-NLS-1$
            System.out.println("correspondingResource:" + type.getCorrespondingResource());
            //$NON-NLS-1$
            System.out.println("underResource:" + type.getUnderlyingResource());
            //$NON-NLS-1$
            System.out.println("source:\n" + type.getSource());
            //System.out.println("cu.orig.under" + type.getCompilationUnit().getOriginalElement().getUnderlyingResource());
            //$NON-NLS-1$
            System.out.println("cu:" + type.getCompilationUnit().getSource());
        } catch (JavaModelException e) {
            System.out.println("JavaModelException: " + e.getMessage());
        }
    }

    public static void dumpIResource(String msg, IResource res) {
        //$NON-NLS-1$
        System.out.println("DUMPING IResource:" + msg);
        //$NON-NLS-1$
        System.out.println("name:" + res.getFullPath().toString());
        //$NON-NLS-1$
        System.out.println("exists" + res.exists());
    }

    public static void dump(Object o) {
        if (o == null)
            //$NON-NLS-1$
            dump("null");
        else
            dump(o.toString());
    }

    public static void dump(String msg) {
        //$NON-NLS-1$
        System.out.println("DUMP:" + msg);
    }

    public static void dumpImports(ICompilationUnit cu) throws JavaModelException {
        IImportDeclaration[] imports = cu.getImports();
        if (imports == null)
            return;
        //$NON-NLS-1$
        DebugUtils.dump("Compilation Unit: " + cu.getElementName());
        for (int k = 0; k < imports.length; k++) {
            //$NON-NLS-2$ //$NON-NLS-1$
            DebugUtils.dump("import " + imports[k].getElementName() + " on demand: " + imports[k].isOnDemand());
        }
    }

    public static void dumpImports(IPackageFragment pack) throws JavaModelException {
        ICompilationUnit[] cus = pack.getCompilationUnits();
        if (cus == null)
            return;
        //DebugUtils.dump("Package " + pack.getElementName());
        for (int j = 0; j < cus.length; j++) {
            dumpImports(cus[j]);
        }
    }

    public static void dumpImports(IJavaProject project) throws JavaModelException {
        IPackageFragment[] packages = project.getPackageFragments();
        if (packages == null)
            return;
        //DebugUtils.dump("Project " + project.getElementName());
        for (int i = 0; i < packages.length; i++) {
            dumpImports(packages[i]);
        }
    }
}
