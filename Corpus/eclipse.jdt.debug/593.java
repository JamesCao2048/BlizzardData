/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper Steen Møller <jesper@selskabet.org> - Bug 430839
 *******************************************************************************/
package com.sun.jdi;

import java.util.List;

/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/InterfaceType.html and
 * http://docs.oracle.com/javase/8/docs/jdk/api/jpda/jdi/com/sun/jdi/InterfaceType.html
 */
public interface InterfaceType extends ReferenceType {

    public List<ClassType> implementors();

    public List<InterfaceType> subinterfaces();

    public List<InterfaceType> superinterfaces();

    public /* default */
    Value invokeMethod(ThreadReference thread, Method method, List<? extends Value> arguments, int options) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException;
}
