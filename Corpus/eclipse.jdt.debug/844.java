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
package org.eclipse.debug.jdi.tests;

/**
 * An abstract reader that continuously reads.
 */
public abstract class AbstractReader {

    protected String fName;

    protected Thread fReaderThread;

    protected boolean fIsStopping = false;

    /**
	 * Constructor
	 * @param name
	 */
    public  AbstractReader(String name) {
        fName = name;
    }

    /**
	 * Continuously reads. Note that if the read involves waiting
	 * it can be interrupted and a InterruptedException will be thrown.
	 */
    protected abstract void readerLoop();

    /**
	 * Start the thread that reads events.
	 * 
	 */
    public void start() {
        fReaderThread = new Thread(new Runnable() {

            @Override
            public void run() {
                readerLoop();
            }
        }, fName);
        fReaderThread.setDaemon(true);
        fReaderThread.start();
    }

    /**
	 * Tells the reader loop that it should stop.
	 */
    public void stop() {
        fIsStopping = true;
        if (fReaderThread != null) {
            fReaderThread.interrupt();
        }
    }
}
