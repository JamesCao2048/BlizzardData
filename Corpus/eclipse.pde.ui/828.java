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
package org.eclipse.pde.internal.ui.wizards.templates;

import java.util.Iterator;
import java.util.Stack;
import org.eclipse.pde.ui.templates.IVariableProvider;

public class ControlStack {

    private Stack<Entry> stack;

    private PreprocessorParser parser;

    class Entry {

        boolean value;
    }

    public  ControlStack() {
        stack = new Stack();
        parser = new PreprocessorParser();
    }

    public void setValueProvider(IVariableProvider provider) {
        parser.setVariableProvider(provider);
    }

    public void processLine(String line) {
        if (//$NON-NLS-1$
        line.startsWith("if")) {
            String expression = line.substring(2).trim();
            boolean result = false;
            try {
                result = parser.parseAndEvaluate(expression);
            } catch (Exception e) {
            }
            Entry entry = new Entry();
            entry.value = result;
            stack.push(entry);
        } else if (//$NON-NLS-1$
        line.startsWith("else")) {
            if (stack.isEmpty() == false) {
                Entry entry = stack.peek();
                entry.value = !entry.value;
            }
        } else if (//$NON-NLS-1$
        line.startsWith("endif")) {
            // pop the stack
            if (!stack.isEmpty())
                stack.pop();
        } else {
        // a preprocessor comment - ignore it
        }
    }

    public boolean getCurrentState() {
        if (stack.isEmpty())
            return true;
        // return result==true
        for (Iterator<Entry> iter = stack.iterator(); iter.hasNext(); ) {
            Entry entry = iter.next();
            if (!entry.value)
                return false;
        }
        return true;
    }
}
