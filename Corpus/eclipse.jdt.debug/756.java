/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.DebugEvent;

public class DebugElementKindEventWaiter extends DebugEventWaiter {

    protected Class<?> fElementClass;

    /**
	 * Constructor
	 * @param eventKind
	 * @param elementClass
	 */
    public  DebugElementKindEventWaiter(int eventKind, Class<?> elementClass) {
        super(eventKind);
        fElementClass = elementClass;
    }

    /**
	 * @see org.eclipse.jdt.debug.testplugin.DebugEventWaiter#accept(org.eclipse.debug.core.DebugEvent)
	 */
    @Override
    public boolean accept(DebugEvent event) {
        Object o = event.getSource();
        return super.accept(event) && fElementClass.isInstance(o);
    }
}
