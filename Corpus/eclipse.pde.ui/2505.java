/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.pde.internal.core.PDECore;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SchemaUtil
 *
 */
public class SchemaUtil {

    /**
	 * Returns a URL connection that an input stream can be obtained from.  The
	 * URL Connection can handle urls of a variety of types including files, jar
	 * files and remote urls.
	 * <p>
	 * NOTE: If the connection is of type {@link JarURLConnection} the zip file
	 * should be independantly closed using {@link JarURLConnection#getJarFile()}.close()
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=326263
	 * </p>
	 *
	 * @param url URL to open connection to
	 * @return the url connection
	 * @throws MalformedURLException if the url is null
	 * @throws IOException if there is a problem accessing the resource specified by the url
	 */
    public static URLConnection getURLConnection(URL url) throws MalformedURLException, IOException {
        if (url == null) {
            //$NON-NLS-1$
            throw new MalformedURLException("URL specified is null");
        }
        URLConnection connection = url.openConnection();
        if (connection instanceof JarURLConnection) {
            connection.setUseCaches(false);
        }
        return connection;
    }

    public static void parseURL(URL url, DefaultHandler handler) {
        InputStream input = null;
        URLConnection connection = null;
        try {
            connection = getURLConnection(url);
            input = connection.getInputStream();
            SAXParserWrapper parser = new SAXParserWrapper();
            parser.parse(input, handler);
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            PDECore.logException(e);
        } catch (SAXException e) {
        } catch (ParserConfigurationException e) {
            PDECore.logException(e);
        } catch (FactoryConfigurationError e) {
            PDECore.logException(e);
        } finally {
            try {
                if (input != null)
                    input.close();
                if (connection instanceof JarURLConnection) {
                    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=326263
                    ((JarURLConnection) connection).getJarFile().close();
                }
            } catch (IOException e1) {
            }
        }
    }
}
