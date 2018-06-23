/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import java.util.Collection;
import org.eclipse.jdt.apt.core.internal.NonEclipseImplementationException;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorObject;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorType;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.TypeParameterDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.type.ArrayTypeImpl;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ITypeBinding;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;
import com.sun.mirror.util.Types;

public class TypesUtil implements Types {

    private static final String[] NO_ARGS = new String[0];

    private final BaseProcessorEnv _env;

    public static void main(String[] args) {
    }

    public  TypesUtil(BaseProcessorEnv env) {
        _env = env;
        //$NON-NLS-1$
        assert env != null : "null environment.";
    }

    public ArrayType getArrayType(TypeMirror componentType) {
        if (componentType == null)
            return null;
        if (componentType instanceof EclipseMirrorType) {
            final EclipseMirrorType impl = (EclipseMirrorType) componentType;
            // the leaf type of the array
            final ITypeBinding leaf;
            final int dimension;
            switch(impl.kind()) {
                case TYPE_ERROR:
                case TYPE_VOID:
                    //$NON-NLS-1$
                    throw new IllegalArgumentException("Illegal element type for array");
                case TYPE_ARRAY:
                    final ITypeBinding array = ((ArrayTypeImpl) componentType).getTypeBinding();
                    dimension = array.getDimensions() + 1;
                    leaf = array.getElementType();
                    break;
                default:
                    leaf = impl.getTypeBinding();
                    dimension = 1;
                    break;
            }
            if (leaf == null || leaf.isParameterizedType())
                //$NON-NLS-1$
                throw new IllegalArgumentException("illegal component type: " + componentType);
            final String bindingKey = BindingKey.createArrayTypeBindingKey(leaf.getKey(), dimension);
            final ITypeBinding arrayType = _env.getTypeBindingFromKey(bindingKey);
            if (arrayType == null)
                return null;
            return (ArrayType) Factory.createTypeMirror(arrayType, _env);
        }
        throw new NonEclipseImplementationException(//$NON-NLS-1$
        "only applicable to eclipse type system objects." + " Found " + //$NON-NLS-1$
        componentType.getClass().getName());
    }

    /**
     * @param outer a type
     * @param inner the simple name of the nested class
     * @return the binding that correspond to <code>outer.getQualifiedName()</code>.<code>inner</code>
     * 		   or null if it cannot be located.
     */
    private ITypeBinding findMemberType(ITypeBinding outer, String inner) {
        if (outer == null || inner == null)
            return null;
        outer = outer.getTypeDeclaration();
        final ITypeBinding[] nestedTypes = outer.getDeclaredTypes();
        // first we search throw the ones that are directly declared within 'outer'
        for (ITypeBinding nestedType : nestedTypes) {
            if (inner.equals(nestedType.getName()))
                return nestedType;
        }
        // then we look up the hierachy chain.
        // first we search the super type
        ITypeBinding result = findMemberType(outer.getSuperclass(), inner);
        if (result != null)
            return result;
        // then the super interfaces
        final ITypeBinding[] interfaces = outer.getInterfaces();
        for (ITypeBinding interfaceType : interfaces) {
            result = findMemberType(interfaceType, inner);
            if (result != null)
                return result;
        }
        // can't find it.
        return null;
    }

    public com.sun.mirror.type.DeclaredType getDeclaredType(DeclaredType containing, TypeDeclaration decl, TypeMirror... typeArgs) {
        if (decl == null)
            return null;
        final ITypeBinding outerBinding = getTypeBinding(containing);
        final ITypeBinding memberBinding;
        if (containing == null)
            memberBinding = getTypeBinding(decl);
        else {
            if (outerBinding.isGenericType())
                //$NON-NLS-1$ //$NON-NLS-2$
                throw new IllegalArgumentException("[containing], " + containing + ", is a generic type.");
            // make sure 'decl' is a valid member of 'outerBinding'
            memberBinding = findMemberType(outerBinding, decl.getSimpleName());
            if (memberBinding == null)
                //$NON-NLS-1$
                throw new IllegalArgumentException(decl + " is not a member type of " + containing);
        }
        final int numArgs = typeArgs == null ? 0 : typeArgs.length;
        if (memberBinding.isGenericType()) {
            final String[] argKeys = numArgs == 0 ? NO_ARGS : new String[numArgs];
            for (int i = 0; i < numArgs; i++) {
                final ITypeBinding binding = getTypeBinding(typeArgs[i]);
                assert binding != null : "failed to get binding mirror type";
                argKeys[i] = binding.getKey();
            }
            final ITypeBinding[] typeParams = memberBinding.getTypeParameters();
            final int numTypeParams = typeParams == null ? 0 : typeParams.length;
            // number of type parameter and arguments don't agree.
            if (numTypeParams != numArgs && numArgs != 0)
                throw new IllegalArgumentException(//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "type, " + memberBinding.getQualifiedName() + ", require " + numTypeParams + " type arguments " + "but found " + //$NON-NLS-1$
                numArgs);
            final String typeKey = BindingKey.createParameterizedTypeBindingKey(memberBinding.getKey(), argKeys);
            final ITypeBinding resultBinding = _env.getTypeBindingFromKey(typeKey);
            return Factory.createReferenceType(resultBinding, _env);
        } else {
            if (numArgs != 0)
                //$NON-NLS-1$ //$NON-NLS-2$
                throw new IllegalArgumentException("type, " + memberBinding + " is not a generic type and cannot have type arguments.");
            // simple case, turning a non-generic TypeDeclaration into a DeclaredType
            return (DeclaredType) decl;
        }
    }

    public com.sun.mirror.type.DeclaredType getDeclaredType(TypeDeclaration decl, TypeMirror... typeArgs) {
        return getDeclaredType(null, decl, typeArgs);
    }

    public TypeMirror getErasure(TypeMirror t) {
        if (t == null)
            return null;
        if (t instanceof EclipseMirrorType) {
            final EclipseMirrorType impl = (EclipseMirrorType) t;
            final ITypeBinding binding;
            switch(impl.kind()) {
                case TYPE_PRIMITIVE:
                case TYPE_VOID:
                case TYPE_ERROR:
                    return t;
                default:
                    binding = impl.getTypeBinding();
            }
            final ITypeBinding erasure = binding.getErasure();
            //$IDENTITY-COMPARISON$
            if (erasure == binding)
                return t;
            TypeMirror m_erasure = Factory.createTypeMirror(erasure, impl.getEnvironment());
            if (m_erasure == null)
                return Factory.createErrorClassType(erasure);
            return m_erasure;
        }
        throw new NonEclipseImplementationException(//$NON-NLS-1$
        "only applicable to eclipse type system objects." + " Found " + //$NON-NLS-1$
        t.getClass().getName());
    }

    public PrimitiveType getPrimitiveType(PrimitiveType.Kind kind) {
        if (kind == null)
            return null;
        switch(kind) {
            case BOOLEAN:
                return _env.getBooleanType();
            case BYTE:
                return _env.getByteType();
            case CHAR:
                return _env.getCharType();
            case DOUBLE:
                return _env.getDoubleType();
            case FLOAT:
                return _env.getFloatType();
            case INT:
                return _env.getIntType();
            case LONG:
                return _env.getLongType();
            case SHORT:
                return _env.getShortType();
            //$NON-NLS-1$
            default:
                throw new IllegalStateException("unknown primitive kind : " + kind);
        }
    }

    public TypeVariable getTypeVariable(TypeParameterDeclaration tparam) {
        if (tparam == null)
            return null;
        if (tparam instanceof TypeParameterDeclarationImpl)
            return (TypeVariable) tparam;
        throw new NonEclipseImplementationException(//$NON-NLS-1$
        "only applicable to eclipse type system objects." + " Found " + //$NON-NLS-1$
        tparam.getClass().getName());
    }

    public VoidType getVoidType() {
        return _env.getVoidType();
    }

    public WildcardType getWildcardType(Collection<ReferenceType> upperBounds, Collection<ReferenceType> lowerBounds) {
        final String boundKey;
        final char boundKind;
        final int upperBoundCount = upperBounds == null ? 0 : upperBounds.size();
        final int lowerBoundCount = lowerBounds == null ? 0 : lowerBounds.size();
        if (upperBoundCount == 0 && lowerBoundCount == 0) {
            boundKey = null;
            boundKind = Signature.C_STAR;
        } else if (upperBoundCount == 1 && lowerBoundCount == 0) {
            final ITypeBinding binding = getTypeBinding(upperBounds.iterator().next());
            boundKey = binding.getKey();
            boundKind = Signature.C_EXTENDS;
        } else if (lowerBoundCount == 1 && upperBoundCount == 0) {
            final ITypeBinding binding = getTypeBinding(lowerBounds.iterator().next());
            boundKey = binding.getKey();
            boundKind = Signature.C_SUPER;
        } else
            //$NON-NLS-1$
            throw new IllegalArgumentException("Wildcard can only have a upper bound, a lower bound or be unbounded.");
        final String wildcardkey = BindingKey.createWilcardTypeBindingKey(boundKey, boundKind);
        final ITypeBinding wildcard = _env.getTypeBindingFromKey(wildcardkey);
        return (WildcardType) Factory.createTypeMirror(wildcard, _env);
    }

    /**
     * @return true iff t2 = t1 does not require explicit casting and not cause an error.
     */
    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        EclipseMirrorType left = (EclipseMirrorType) t1;
        EclipseMirrorType right = (EclipseMirrorType) t2;
        return left.isAssignmentCompatible(right);
    }

    public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
        EclipseMirrorType left = (EclipseMirrorType) t1;
        EclipseMirrorType right = (EclipseMirrorType) t2;
        return left.isSubTypeCompatible(right);
    }

    /**
     * @return the binding correspond to the given type.
     *         Return null if the type is an error marker.
     * @throws NonEclipseImplementationException
     */
    private static ITypeBinding getTypeBinding(TypeMirror type) throws NonEclipseImplementationException {
        if (type == null)
            return null;
        if (type instanceof EclipseMirrorType) {
            return ((EclipseMirrorType) type).getTypeBinding();
        }
        throw new NonEclipseImplementationException(//$NON-NLS-1$
        "only applicable to eclipse type system objects." + " Found " + //$NON-NLS-1$
        type.getClass().getName());
    }

    /**
     * @return the binding correspond to the given type.
     * @throws NonEclipseImplementationException
     */
    public static ITypeBinding getTypeBinding(TypeDeclaration type) throws NonEclipseImplementationException {
        if (type == null)
            return null;
        if (type instanceof EclipseMirrorObject) {
            return ((TypeDeclarationImpl) type).getTypeBinding();
        }
        throw new NonEclipseImplementationException(//$NON-NLS-1$
        "only applicable to eclipse type system objects." + " Found " + //$NON-NLS-1$
        type.getClass().getName());
    }
}
