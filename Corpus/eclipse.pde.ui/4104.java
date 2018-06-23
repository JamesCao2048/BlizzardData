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
package org.eclipse.pde.ui.tests.util.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.*;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.pde.internal.core.XMLDefaultHandler;
import org.eclipse.pde.internal.core.util.SAXParserWrapper;
import org.eclipse.pde.ui.tests.PDETestsPlugin;
import org.eclipse.pde.ui.tests.util.DOMParserWrapper;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;

public class ParserWrapperTestCase extends TestCase {

    protected static final int FTHREADCOUNT = 5;

    protected static final int FSAX = 0;

    protected static final int FDOM = 1;

    protected static File fXMLFile;

    //$NON-NLS-1$
    protected static final String FFILENAME = "/plugin.xml";

    public static Test suite() {
        return new TestSuite(ParserWrapperTestCase.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PDETestsPlugin plugin = PDETestsPlugin.getDefault();
        if (plugin == null)
            //$NON-NLS-1$
            throw new Exception("ERROR:  Macro plug-in uninitialized");
        Bundle bundle = plugin.getBundle();
        if (bundle == null)
            //$NON-NLS-1$
            throw new Exception("ERROR:  Bundle uninitialized");
        URL url = bundle.getEntry(FFILENAME);
        if (url == null)
            //$NON-NLS-1$
            throw new Exception("ERROR:  URL not found:  " + FFILENAME);
        String path = FileLocator.resolve(url).getPath();
        if (//$NON-NLS-1$
        "".equals(path))
            //$NON-NLS-1$
            throw new Exception("ERROR:  URL unresolved:  " + FFILENAME);
        fXMLFile = new File(path);
    }

    public void testSAXParserWrapperConcurrency() throws Exception {
        ParserThread[] threads = new ParserThread[FTHREADCOUNT];
        for (int x = 0; x < FTHREADCOUNT; x++) {
            threads[x] = new ParserThread(FSAX, fXMLFile);
            threads[x].start();
        }
        for (int x = 0; x < FTHREADCOUNT; x++) {
            threads[x].join();
            assertFalse(threads[x].getError());
        }
    }

    public void testDOMParserWrapperConcurrency() throws Exception {
        ParserThread[] threads = new ParserThread[FTHREADCOUNT];
        for (int x = 0; x < FTHREADCOUNT; x++) {
            //$NON-NLS-1$
            threads[x] = new ParserThread(FDOM, fXMLFile);
            threads[x].start();
        }
        for (int x = 0; x < FTHREADCOUNT; x++) {
            threads[x].join();
            assertFalse(threads[x].getError());
        }
    }

    public class ParserThread extends Thread {

        protected final int FITERATIONS = 100;

        protected File fParserXMLFile;

        protected boolean fError;

        protected int fParserType;

        public  ParserThread(int parserType, File file) {
            fError = false;
            fParserType = parserType;
            fParserXMLFile = file;
        }

        @Override
        public void run() {
            if (fParserType == ParserWrapperTestCase.FSAX) {
                runSAX();
            } else {
                runDOM();
            }
        }

        public void runSAX() {
            for (int x = 0; x < FITERATIONS; x++) {
                try {
                    XMLDefaultHandler handler = new XMLDefaultHandler();
                    SAXParserWrapper parser = new SAXParserWrapper();
                    parser.parse(fParserXMLFile, handler);
                    parser.dispose();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                    fError = true;
                } catch (SAXException e) {
                    e.printStackTrace();
                    fError = true;
                } catch (FactoryConfigurationError e) {
                    e.printStackTrace();
                    fError = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    fError = true;
                }
                // Any type of exception experienced is bad
                if (fError)
                    return;
            }
        }

        public void runDOM() {
            for (int x = 0; x < FITERATIONS; x++) {
                try {
                    DOMParserWrapper parser = new DOMParserWrapper();
                    parser.parse(fParserXMLFile);
                    parser.dispose();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                    fError = true;
                } catch (SAXException e) {
                    e.printStackTrace();
                    fError = true;
                } catch (FactoryConfigurationError e) {
                    e.printStackTrace();
                    fError = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    fError = true;
                }
                // Any type of exception experienced is bad
                if (fError)
                    return;
            }
        }

        public boolean getError() {
            return fError;
        }
    }
}
