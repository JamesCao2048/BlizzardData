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
package org.eclipse.debug.jdi.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NullConsoleReader extends AbstractReader {

    private InputStream fInput;

    /**
	 * Constructor
	 * @param name
	 * @param input
	 */
    public  NullConsoleReader(String name, InputStream input) {
        super(name);
        fInput = input;
    }

    /**
	 * Continuously reads events that are coming from the event queue.
	 */
    @Override
    protected void readerLoop() {
        java.io.BufferedReader input = new BufferedReader(new InputStreamReader(fInput));
        try {
            int read = 0;
            while (!fIsStopping && read != -1) {
                read = input.read();
            }
        } catch (IOException e) {
        }
    }
}
