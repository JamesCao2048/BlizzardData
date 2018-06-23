/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak (brockj_eclipse@ihug.com.au) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=83697
 *     Benjamin Muskalla <b.muskalla@gmx.net> - [navigation][hovering] Javadoc view cannot find URL with anchor - https://bugs.eclipse.org/bugs/show_bug.cgi?id=70870
 *******************************************************************************/
package org.eclipse.jdt.text.tests;

import java.io.Reader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.javadoc.JavaDocCommentReader;
import org.eclipse.jdt.internal.ui.text.javadoc.JavaDoc2HTMLTextReader;

public class JavaDoc2HTMLTextReaderTester extends TestCase {

    private static final boolean DEBUG = false;

    public  JavaDoc2HTMLTextReaderTester(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(JavaDoc2HTMLTextReaderTester.class);
    }

    private String getTransformedJavaDoc(String string) {
        Reader reader = new JavaDocCommentReader(new MockBuffer(string), 0, string.length());
        return HTMLPrinter.read(new JavaDoc2HTMLTextReader(reader));
    }

    private void verify(String string, String expected) {
        String result = getTransformedJavaDoc(string);
        if (DEBUG)
            //$NON-NLS-1$
            System.out.println("result:" + result);
        assertEquals(expected, result);
    }

    public void test0() {
        //$NON-NLS-1$
        String string = "/**@deprecated*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>@deprecated</dt><dd></dd></dl>";
        verify(string, expected);
    }

    public void test1() {
        //$NON-NLS-1$
        String string = "/**@author Foo Bar*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Author:</dt><dd>Foo Bar</dd></dl>";
        verify(string, expected);
    }

    public void test2() {
        //test for bug 14658
        //$NON-NLS-1$
        String string = "/**@author Foo Bar<a href=\"mailto:foobar@eclipse.org\">foobar@eclipse.org</a>*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Author:</dt><dd>Foo Bar<a href=\"mailto:foobar@eclipse.org\">foobar@eclipse.org</a></dd></dl>";
        verify(string, expected);
    }

    public void test3() {
        //test for bug 14658
        //$NON-NLS-1$
        String string = "/**@author Foo Bar<a href=\"mailto:foobar@eclipse.org\">foobar@eclipse.org</a>\n *@deprecated*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Author:</dt><dd>Foo Bar<a href=\"mailto:foobar@eclipse.org\">foobar@eclipse.org</a></dd><dt>@deprecated</dt><dd></dd></dl>";
        verify(string, expected);
    }

    public void test4() {
        //$NON-NLS-1$
        String string = "/**@author Foo Bar\n * @deprecated*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Author:</dt><dd>Foo Bar</dd><dt>@deprecated</dt><dd></dd></dl>";
        verify(string, expected);
    }

    public void test5() {
        //$NON-NLS-1$
        String string = "/**@author Foo Bar\n * @author Baz Fred*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Author:</dt><dd>Foo Bar</dd><dd>Baz Fred</dd></dl>";
        verify(string, expected);
    }

    public void test6() {
        //$NON-NLS-1$
        String string = "/**@author Foo Bar\n * @since 2.0*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Author:</dt><dd>Foo Bar</dd><dt>Since:</dt><dd>2.0</dd></dl>";
        verify(string, expected);
    }

    public void test7() {
        if (DEBUG) {
            //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println(getClass().getName() + "::" + getName() + " disabled(corner case - @see tag inside <a> tag)");
            return;
        }
        //$NON-NLS-1$
        String string = "/**@author Foo Bar<a href=\"mailto:foobar@see.org\">foobar@see.org</a>*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Author:</dt><dd>Foo Bar<a href=\"mailto:foobar@see.org\">foobar@see.org</a></dd></dl>";
        verify(string, expected);
    }

    public void test8() {
        if (DEBUG) {
            //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println(getClass().getName() + "::" + getName() + " disabled(corner case - @see tag inside <a> tag)");
            return;
        }
        //$NON-NLS-1$
        String string = "/**@author Foo Bar<a href=\"mailto:foobar@see.org\">foobar@eclipse.org</a>*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Author:</dt><dd>Foo Bar<a href=\"mailto:foobar@see.org\">foobar@eclipse.org</a></dd></dl>";
        verify(string, expected);
    }

    public void test9() {
        //$NON-NLS-1$
        String string = "/**@throws NullPointerException*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Throws:</dt><dd>NullPointerException</dd></dl>";
        verify(string, expected);
    }

    public void test10() {
        //test for bug 8131
        //$NON-NLS-1$
        String string = "/**@exception NullPointerException*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Throws:</dt><dd>NullPointerException</dd></dl>";
        verify(string, expected);
    }

    public void test11() {
        //test for bug 8132
        //$NON-NLS-1$
        String string = "/**@exception NullPointerException \n * @throws java.lang.Exception*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Throws:</dt><dd>NullPointerException</dd><dd>java.lang.Exception</dd></dl>";
        verify(string, expected);
    }

    public void test12() {
        //$NON-NLS-1$
        String string = "/** \n *@param i fred or <code>null</code> \n*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Parameters:</dt><dd><b>i</b> fred or <code>null</code></dd></dl>";
        verify(string, expected);
    }

    public void test13_withText() {
        //$NON-NLS-1$
        String string = "/**\n * This is a {@linkplain Foo#bar(String, int) test link}. End.*/";
        //$NON-NLS-1$
        String expected = " This is a test link. End.";
        verify(string, expected);
    }

    public void test13_withoutText() {
        //$NON-NLS-1$
        String string = "/**\n * This is a {@linkplain Foo#bar(String, int)}. End.*/";
        //$NON-NLS-1$
        String expected = " This is a Foo.bar(String, int). End.";
        verify(string, expected);
    }

    public void test14_withText() {
        //$NON-NLS-1$
        String string = "/**\n * This is a {@link Foo#bar(String, int) test link}. End.*/";
        //$NON-NLS-1$
        String expected = " This is a <code>test link</code>. End.";
        verify(string, expected);
    }

    public void test14_withoutText() {
        //$NON-NLS-1$
        String string = "/**\n * This is a {@link Foo#bar(String, int)}. End.*/";
        //$NON-NLS-1$
        String expected = " This is a <code>Foo.bar(String, int)</code>. End.";
        verify(string, expected);
    }

    public void test15() {
        //$NON-NLS-1$
        String string = "/**\n * This is a <a href=\"{@docRoot}/test.html\">test link</a>. End.*/";
        //$NON-NLS-1$
        String expected = " This is a <a href=\"/test.html\">test link</a>. End.";
        verify(string, expected);
    }

    public void test16() {
        //$NON-NLS-1$
        String string = "/**\n *@param foo {@link Bar bar}*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>Parameters:</dt><dd><b>foo</b> <code>bar</code></dd></dl>";
        verify(string, expected);
    }

    public void test17() {
        // $NON-NLS-1$
        String string = "/**\n @model name='abc' value='@'\n * @generated*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>@model</dt><dd>name='abc' value='@'</dd><dt>@generated</dt><dd></dd></dl>";
        verify(string, expected);
    }

    public void test18() {
        // $NON-NLS-1$
        String string = "/**\r\n* Method foo.\r\n* @param bar\t\n* @custom fooBar\t\n*/";
        //$NON-NLS-1$
        String expected = " Method foo.\r\n <dl><dt>Parameters:</dt><dd><b>bar</b></dd><dt>@custom</dt><dd>fooBar</dd></dl>";
        verify(string, expected);
    }

    public void test19() {
        // $NON-NLS-1$
        String string = "/**\n @model\n * @generated*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>@model</dt><dd></dd><dt>@generated</dt><dd></dd></dl>";
        verify(string, expected);
    }

    public void test20() {
        //test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=94189
        // $NON-NLS-1$
        String string = "/**This is {@code}{@literal <literal>} {@code & code}.*/";
        //$NON-NLS-1$
        String expected = "This is <code></code>&lt;literal&gt; <code>&amp; code</code>.";
        verify(string, expected);
    }

    public void test21() {
        //test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=70870
        //$NON-NLS-1$
        String string = "/**@see <a href=\"http://foo.bar#baz\">foo</a>*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>See Also:</dt><dd><a href=\"http://foo.bar#baz\">foo</a></dd></dl>";
        verify(string, expected);
    }

    public void test22() {
        //test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=70870
        //$NON-NLS-1$
        String string = "/**@see <a href=\"http://foo.bar#baz\">foo</a> and {@link Foo#bar(String, int)} and <a href=\"http://foo.bar#baz\">foo</a>*/";
        //$NON-NLS-1$
        String expected = "<dl><dt>See Also:</dt><dd><a href=\"http://foo.bar#baz\">foo</a> and <code>Foo.bar(String, int)</code> and <a href=\"http://foo.bar#baz\">foo</a></dd></dl>";
        verify(string, expected);
    }
}

class MockBuffer implements IBuffer {

    private StringBuffer fStringBuffer;

     MockBuffer(String string) {
        fStringBuffer = new StringBuffer(string);
    }

    @Override
    public void addBufferChangedListener(IBufferChangedListener listener) {
    }

    @Override
    public void append(char[] text) {
        fStringBuffer.append(text);
    }

    @Override
    public void append(String text) {
        fStringBuffer.append(text);
    }

    @Override
    public void close() {
    }

    @Override
    public char getChar(int position) {
        return fStringBuffer.charAt(position);
    }

    @Override
    public char[] getCharacters() {
        return fStringBuffer.toString().toCharArray();
    }

    @Override
    public String getContents() {
        return fStringBuffer.toString();
    }

    @Override
    public int getLength() {
        return fStringBuffer.length();
    }

    @Override
    public IOpenable getOwner() {
        return null;
    }

    @Override
    public String getText(int offset, int length) {
        return fStringBuffer.toString().substring(offset, offset + length);
    }

    @Override
    public IResource getUnderlyingResource() {
        return null;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void removeBufferChangedListener(IBufferChangedListener listener) {
    }

    @Override
    public void replace(int position, int length, char[] text) {
    }

    @Override
    public void replace(int position, int length, String text) {
    }

    @Override
    public void save(IProgressMonitor progress, boolean force) throws JavaModelException {
    }

    @Override
    public void setContents(char[] contents) {
    }

    @Override
    public void setContents(String contents) {
        fStringBuffer = new StringBuffer(contents);
    }
}
