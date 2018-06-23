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
import org.eclipse.pde.internal.core.text.bundle.BundleSymbolicNameHeader;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.Constants;

public class BundleSymbolicNameTestCase extends BundleModelTestCase {

    public  BundleSymbolicNameTestCase() {
        super(Constants.BUNDLE_SYMBOLICNAME);
    }

    public static Test suite() {
        return new TestSuite(BundleSymbolicNameTestCase.class);
    }

    @Override
    public void testAbsentHeader() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        fDocument.set(buffer.toString());
        load();
        assertNull(fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME));
    }

    @Override
    public void testPresentHeader() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz\n");
        fDocument.set(buffer.toString());
        load();
        assertNotNull(fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME));
    }

    @Override
    public void testHeaderOffset1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz\n");
        fDocument.set(buffer.toString());
        load();
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertEquals(fDocument.getLineOffset(2), header.getOffset());
    }

    @Override
    public void testHeaderOffset2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz\n");
        buffer.append("Require-Bundle: com.example.abc\n");
        fDocument.set(buffer.toString());
        load();
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertEquals(fDocument.getLineOffset(2), header.getOffset());
    }

    @Override
    public void testHeaderLength() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz\n");
        fDocument.set(buffer.toString());
        load();
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertEquals(fDocument.getLineLength(2), header.getLength());
    }

    @Override
    public void testHeaderLengthWithWindowsDelimiter() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\r\n");
        buffer.append("Bundle-ManifestVersion: 2\r\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz\r\n");
        fDocument.set(buffer.toString());
        load();
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertEquals(fDocument.getLineLength(2), header.getLength());
    }

    public void testAddBundleSymbolicName() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Require-Bundle: com.example.xyz\n");
        fDocument.set(buffer.toString());
        load(true);
        fModel.getBundle().setHeader(Constants.BUNDLE_SYMBOLICNAME, "com.example.abc");
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertNotNull(header);
        assertEquals("Bundle-SymbolicName: com.example.abc\n", header.write());
        TextEdit[] ops = fListener.getTextOperations();
        assertEquals(1, ops.length);
        ops[0].apply(fDocument);
        assertEquals(buffer.toString() + header.write(), fDocument.get());
    }

    public void testRemoveBundleSymbolicName() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz\n");
        buffer.append("Require-Bundle: com.example.abc\n");
        fDocument.set(buffer.toString());
        load(true);
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertNotNull(header);
        ((BundleSymbolicNameHeader) header).setId("");
        TextEdit[] ops = fListener.getTextOperations();
        assertEquals(1, ops.length);
        ops[0].apply(fDocument);
        assertEquals(4, fDocument.getNumberOfLines());
    }

    public void testChangeBundleSymbolicName() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz\n");
        buffer.append("Require-Bundle: com.example.abc\n");
        fDocument.set(buffer.toString());
        load(true);
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        ((BundleSymbolicNameHeader) header).setId("com.example.core");
        TextEdit[] ops = fListener.getTextOperations();
        assertEquals(1, ops.length);
        ops[0].apply(fDocument);
        assertEquals(5, fDocument.getNumberOfLines());
        assertEquals(0, fDocument.getLineLength(4));
        int pos = fDocument.getLineOffset(2);
        int length = fDocument.getLineLength(2);
        assertEquals("Bundle-SymbolicName: com.example.core\n", fDocument.get(pos, length));
    }

    public void testReadSingletonDirective() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz; singleton:=true\n");
        buffer.append("Require-Bundle: com.example.abc\n");
        fDocument.set(buffer.toString());
        load(true);
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertNotNull(header);
        assertTrue(((BundleSymbolicNameHeader) header).isSingleton());
    }

    public void testAddSingletonDirective() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz\n");
        buffer.append("Require-Bundle: org.eclipse.osgi\n");
        fDocument.set(buffer.toString());
        load(true);
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertNotNull(header);
        ((BundleSymbolicNameHeader) header).setSingleton(true);
        TextEdit[] ops = fListener.getTextOperations();
        assertEquals(1, ops.length);
        ops[0].apply(fDocument);
        assertEquals(5, fDocument.getNumberOfLines());
        assertEquals(0, fDocument.getLineLength(4));
        int pos = fDocument.getLineOffset(2);
        int length = fDocument.getLineLength(2);
        assertEquals("Bundle-SymbolicName: com.example.xyz;singleton:=true\n", fDocument.get(pos, length));
    }

    public void testRemoveSingletonDirective() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Manifest-Version: 1.0\n");
        buffer.append("Bundle-ManifestVersion: 2\n");
        buffer.append("Bundle-SymbolicName: com.example.xyz; singleton:=true\n");
        buffer.append("Require-Bundle: org.eclipse.osgi;resolution:=optional\n");
        fDocument.set(buffer.toString());
        load(true);
        IManifestHeader header = fModel.getBundle().getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
        assertNotNull(header);
        ((BundleSymbolicNameHeader) header).setSingleton(false);
        TextEdit[] ops = fListener.getTextOperations();
        assertEquals(1, ops.length);
        ops[0].apply(fDocument);
        assertEquals(5, fDocument.getNumberOfLines());
        assertEquals(0, fDocument.getLineLength(4));
        int pos = fDocument.getLineOffset(2);
        int length = fDocument.getLineLength(2);
        assertEquals("Bundle-SymbolicName: com.example.xyz\n", fDocument.get(pos, length));
    }
}
