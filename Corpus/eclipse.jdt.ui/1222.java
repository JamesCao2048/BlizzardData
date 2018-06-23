/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert M. Fuhrer (rfuhrer@watson.ibm.com), IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets;

import java.util.Iterator;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

public class SingletonTypeSet extends TypeSet {

    private final TType fType;

    //TODO: encapsulate in factory method and return the same set for known types
    public  SingletonTypeSet(TType t, TypeSetEnvironment typeSetEnvironment) {
        super(typeSetEnvironment);
        Assert.isNotNull(t);
        fType = t;
    }

    @Override
    public boolean isUniverse() {
        return false;
    }

    @Override
    public TypeSet makeClone() {
        //new SingletonTypeSet(fType, getTypeSetEnvironment());
        return this;
    }

    @Override
    protected TypeSet specialCasesIntersectedWith(TypeSet s2) {
        if (s2.contains(fType))
            return this;
        else
            return getTypeSetEnvironment().getEmptyTypeSet();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public TypeSet upperBound() {
        // makeClone();
        return this;
    }

    @Override
    public TypeSet lowerBound() {
        // makeClone();
        return this;
    }

    @Override
    public boolean hasUniqueLowerBound() {
        return true;
    }

    @Override
    public boolean hasUniqueUpperBound() {
        return true;
    }

    @Override
    public TType uniqueLowerBound() {
        return fType;
    }

    @Override
    public TType uniqueUpperBound() {
        return fType;
    }

    @Override
    public boolean contains(TType t) {
        return fType.equals(t);
    }

    @Override
    public boolean containsAll(TypeSet s) {
        if (s.isEmpty())
            return true;
        if (s.isSingleton())
            return s.anyMember().equals(fType);
        return false;
    }

    @Override
    public Iterator<TType> iterator() {
        return new Iterator<TType>() {

            private boolean done = false;

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public TType next() {
                done = true;
                return fType;
            }
        };
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public TType anyMember() {
        return fType;
    }

    @Override
    public EnumeratedTypeSet enumerate() {
        EnumeratedTypeSet enumeratedTypeSet = new EnumeratedTypeSet(fType, getTypeSetEnvironment());
        enumeratedTypeSet.initComplete();
        return enumeratedTypeSet;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SingletonTypeSet) {
            SingletonTypeSet other = (SingletonTypeSet) o;
            return fType.equals(other.fType);
        } else if (o instanceof TypeSet) {
            TypeSet other = (TypeSet) o;
            return other.isSingleton() && other.anyMember().equals(fType);
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return fType.hashCode();
    }

    @Override
    public String toString() {
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return "{" + fID + ": " + fType.getPrettySignature() + "}";
    }
}
