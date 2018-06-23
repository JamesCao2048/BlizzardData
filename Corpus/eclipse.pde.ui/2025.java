/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.model.bundle;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.BundleVersionHeader;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.Constants;

public class BundleVersionTestCase extends BundleModelTestCase {

    public  BundleVersionTestCase() {
        super(Constants.BUNDLE_VERSION);
    }

    public static Test suite() {
        return new TestSuite(BundleVersionTestCase.class);
    }

    public void testAddVersion() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymoblicName: com.example.xyz\n");
        fDocument.set(buffer.toString());
        load(true);
        fModel.getBundle().setHeader(Constants.BUNDLE_VERSION, "3.2.0.1");
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_VERSION);
        assertNotNull(header);
        assertEquals("Bundle-Version: 3.2.0.1\n", header.write());
        TextEdit[] ops = fListener.getTextOperations();
        assertEquals(1, ops.length);
        ops[0].apply(fDocument);
        assertEquals(buffer.toString() + header.write(), fDocument.get());
    }

    public void testRemoveVersion() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymoblicName: com.example.xyz\n");
        buffer.append("Bundle-Version: 3.2.0\n");
        fDocument.set(buffer.toString());
        load(true);
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_VERSION);
        assertNotNull(header);
        ((BundleVersionHeader) header).setVersionRange(null);
        TextEdit[] ops = fListener.getTextOperations();
        assertEquals(1, ops.length);
        ops[0].apply(fDocument);
        assertEquals(4, fDocument.getNumberOfLines());
    }

    public void testChangeVersion() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymoblicName: com.example.xyz\n");
        buffer.append("Bundle-Version: 2.1.0\n");
        fDocument.set(buffer.toString());
        load(true);
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_VERSION);
        ((BundleVersionHeader) header).setVersionRange("3.2.0");
        TextEdit[] ops = fListener.getTextOperations();
        assertEquals(1, ops.length);
        ops[0].apply(fDocument);
        assertEquals(5, fDocument.getNumberOfLines());
        assertEquals(0, fDocument.getLineLength(4));
        int pos = fDocument.getLineOffset(3);
        int length = fDocument.getLineLength(3) + fDocument.getLineLength(4);
        assertEquals("Bundle-Version: 3.2.0\n", fDocument.get(pos, length));
    }
}
