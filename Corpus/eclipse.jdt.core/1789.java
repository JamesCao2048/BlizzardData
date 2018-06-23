/*******************************************************************************
 * Copyright (c) 2013, 2015 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Implementation of 18.1.1 in JLS8
 */
public class InferenceVariable extends TypeVariableBinding {

    InvocationSite site;

    TypeBinding typeParameter;

    // one of TagBits.{AnnotationNonNull,AnnotationNullable} may steer inference into inferring nullness as well; set both bits to request avoidance.
    long nullHints;

    private InferenceVariable prototype;

    // this is used for constructing a source name like T#0. NB: varId and sourceName are mutable, to be updated when two InferenceContext18 are integrated.
    int varId;

    public  InferenceVariable(TypeBinding typeParameter, int parameterRank, int iVarId, InvocationSite site, LookupEnvironment environment, ReferenceBinding object) {
        this(typeParameter, parameterRank, site, CharOperation.concat(typeParameter.shortReadableName(), Integer.toString(iVarId).toCharArray(), '#'), environment, object);
        this.varId = iVarId;
    }

    private  InferenceVariable(TypeBinding typeParameter, int parameterRank, InvocationSite site, char[] sourceName, LookupEnvironment environment, ReferenceBinding object) {
        super(sourceName, /*declaringElement*/
        null, parameterRank, environment);
        this.site = site;
        this.typeParameter = typeParameter;
        this.tagBits |= typeParameter.tagBits & TagBits.AnnotationNullMASK;
        if (typeParameter.isTypeVariable()) {
            TypeVariableBinding typeVariable = (TypeVariableBinding) typeParameter;
            if (typeVariable.firstBound != null) {
                long boundBits = typeVariable.firstBound.tagBits & TagBits.AnnotationNullMASK;
                if (boundBits == TagBits.AnnotationNonNull)
                    // @NonNull must be preserved
                    this.tagBits |= boundBits;
                else
                    // @Nullable is only a hint
                    this.nullHints |= boundBits;
            }
        }
        this.superclass = object;
        this.prototype = this;
    }

    void updateSourceName(int newId) {
        int hashPos = CharOperation.indexOf('#', this.sourceName);
        this.varId = newId;
        this.sourceName = CharOperation.concat(CharOperation.subarray(this.sourceName, 0, hashPos), Integer.toString(this.varId).toCharArray(), '#');
    }

    @Override
    public TypeBinding clone(TypeBinding enclosingType) {
        InferenceVariable clone = new InferenceVariable(this.typeParameter, this.rank, this.site, this.sourceName, this.environment, this.superclass);
        clone.tagBits = this.tagBits;
        clone.nullHints = this.nullHints;
        clone.varId = this.varId;
        clone.prototype = this;
        return clone;
    }

    public InferenceVariable prototype() {
        return this.prototype;
    }

    public char[] constantPoolName() {
        throw new UnsupportedOperationException();
    }

    public PackageBinding getPackage() {
        throw new UnsupportedOperationException();
    }

    public boolean isCompatibleWith(TypeBinding right, Scope scope) {
        // treat it as a wildcard, compatible with any any and every type.
        return true;
    }

    public boolean isProperType(boolean admitCapture18) {
        return false;
    }

    TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
        if (TypeBinding.equalsEquals(this, var))
            return substituteType;
        return this;
    }

    void collectInferenceVariables(Set<InferenceVariable> variables) {
        variables.add(this);
    }

    public ReferenceBinding[] superInterfaces() {
        return Binding.NO_SUPERINTERFACES;
    }

    public char[] qualifiedSourceName() {
        throw new UnsupportedOperationException();
    }

    public char[] sourceName() {
        return this.sourceName;
    }

    public char[] readableName() {
        return this.sourceName;
    }

    public boolean hasTypeBit(int bit) {
        throw new UnsupportedOperationException();
    }

    public String debugName() {
        return String.valueOf(this.sourceName);
    }

    public String toString() {
        return debugName();
    }

    public int hashCode() {
        int code = this.typeParameter.hashCode() + 17 * this.rank;
        if (this.site != null)
            return 31 * code + this.site.hashCode();
        else
            return code;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InferenceVariable))
            return false;
        InferenceVariable other = (InferenceVariable) obj;
        return this.rank == other.rank && this.site == other.site && TypeBinding.equalsEquals(this.typeParameter, other.typeParameter);
    }

    public TypeBinding erasure() {
        // lazily initialize field that may be required in super.erasure():
        if (this.superclass == null)
            this.superclass = this.environment.getType(TypeConstants.JAVA_LANG_OBJECT);
        return super.erasure();
    }
}
