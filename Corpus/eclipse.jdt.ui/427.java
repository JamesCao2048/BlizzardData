/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.correction;

import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation;

/**
 *
 */
public class ProblemLocation implements IProblemLocation {

    private final int fId;

    private final String[] fArguments;

    private final int fOffset;

    private final int fLength;

    private final boolean fIsError;

    private final String fMarkerType;

    public  ProblemLocation(int offset, int length, IJavaAnnotation annotation) {
        fId = annotation.getId();
        String[] arguments = annotation.getArguments();
        fArguments = arguments != null ? arguments : new String[0];
        fOffset = offset;
        fLength = length;
        fIsError = JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(annotation.getType());
        String markerType = annotation.getMarkerType();
        fMarkerType = markerType != null ? markerType : IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER;
    }

    public  ProblemLocation(int offset, int length, int id, String[] arguments, boolean isError, String markerType) {
        fId = id;
        fArguments = arguments;
        fOffset = offset;
        fLength = length;
        fIsError = isError;
        fMarkerType = markerType;
    }

    public  ProblemLocation(IProblem problem) {
        fId = problem.getID();
        fArguments = problem.getArguments();
        fOffset = problem.getSourceStart();
        fLength = problem.getSourceEnd() - fOffset + 1;
        fIsError = problem.isError();
        fMarkerType = problem instanceof CategorizedProblem ? ((CategorizedProblem) problem).getMarkerType() : IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER;
    }

    @Override
    public int getProblemId() {
        return fId;
    }

    @Override
    public String[] getProblemArguments() {
        return fArguments;
    }

    @Override
    public int getLength() {
        return fLength;
    }

    @Override
    public int getOffset() {
        return fOffset;
    }

    @Override
    public boolean isError() {
        return fIsError;
    }

    @Override
    public String getMarkerType() {
        return fMarkerType;
    }

    @Override
    public ASTNode getCoveringNode(CompilationUnit astRoot) {
        NodeFinder finder = new NodeFinder(astRoot, fOffset, fLength);
        return finder.getCoveringNode();
    }

    @Override
    public ASTNode getCoveredNode(CompilationUnit astRoot) {
        NodeFinder finder = new NodeFinder(astRoot, fOffset, fLength);
        return finder.getCoveredNode();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        //$NON-NLS-1$
        buf.append("Id: ").append(getErrorCode(fId)).append('\n');
        //$NON-NLS-1$
        buf.append('[').append(fOffset).append(", ").append(fLength).append(']').append('\n');
        String[] arg = fArguments;
        for (int i = 0; i < arg.length; i++) {
            buf.append(arg[i]);
            buf.append('\n');
        }
        return buf.toString();
    }

    private String getErrorCode(int code) {
        StringBuffer buf = new StringBuffer();
        if ((code & IProblem.TypeRelated) != 0) {
            //$NON-NLS-1$
            buf.append("TypeRelated + ");
        }
        if ((code & IProblem.FieldRelated) != 0) {
            //$NON-NLS-1$
            buf.append("FieldRelated + ");
        }
        if ((code & IProblem.ConstructorRelated) != 0) {
            //$NON-NLS-1$
            buf.append("ConstructorRelated + ");
        }
        if ((code & IProblem.MethodRelated) != 0) {
            //$NON-NLS-1$
            buf.append("MethodRelated + ");
        }
        if ((code & IProblem.ImportRelated) != 0) {
            //$NON-NLS-1$
            buf.append("ImportRelated + ");
        }
        if ((code & IProblem.Internal) != 0) {
            //$NON-NLS-1$
            buf.append("Internal + ");
        }
        if ((code & IProblem.Syntax) != 0) {
            //$NON-NLS-1$
            buf.append("Syntax + ");
        }
        if ((code & IProblem.Javadoc) != 0) {
            //$NON-NLS-1$
            buf.append("Javadoc + ");
        }
        buf.append(code & IProblem.IgnoreCategoriesMask);
        return buf.toString();
    }
}
