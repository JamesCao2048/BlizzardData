/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla <b.muskalla@gmx.net> - [nls tooling] Externalize Strings Wizard should not touch annotation arguments - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102132
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.refactoring.nls;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.internal.corext.refactoring.nls.NLSElement;
import org.eclipse.jdt.internal.corext.refactoring.nls.NLSLine;
import org.eclipse.jdt.internal.corext.refactoring.nls.NLSScanner;

public class NLSScannerTester extends TestCase {

    public  NLSScannerTester(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(NLSScannerTester.class);
    }

    public void test0() throws Exception {
        //$NON-NLS-1$
        String text = "fred";
        NLSLine[] l = NLSScanner.scan(text);
        //$NON-NLS-1$
        assertEquals("empty", true, l.length == 0);
    }

    public void test1() throws Exception {
        String text = "fred\"x\"";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals("1 line", 1, l.length);
    }

    public void test1a() throws Exception {
        //$NON-NLS-1$
        String text = "fred\n\"x\"";
        NLSLine[] l = NLSScanner.scan(text);
        //$NON-NLS-1$
        assertEquals("1 line", 1, l.length);
    }

    public void test2() throws Exception {
        String text = "fred\"x\"\n\"xx\"";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals("2 line", 2, l.length);
    }

    public void test2a() throws Exception {
        //$NON-NLS-1$
        String text = "fred\n\"x\" \"xx\"";
        NLSLine[] l = NLSScanner.scan(text);
        //$NON-NLS-1$
        assertEquals("1 lines", 1, l.length);
    }

    public void test3() throws Exception {
        String text = "fred\"x\"\n \"xx\"";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals("2 lines", 2, l.length);
    }

    public void test4() throws Exception {
        //$NON-NLS-1$
        String text = "fred\n \"xx\"";
        NLSLine[] l = NLSScanner.scan(text);
        //$NON-NLS-1$
        assertEquals("1 line", 1, l.length);
    }

    public void test5() throws Exception {
        //$NON-NLS-1$
        String text = "\n \"xx\"";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("1 string", 1, line.size());
    }

    public void test6() throws Exception {
        //$NON-NLS-1$
        String text = "\n \"xx\" \"dff\"";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("2 string", 2, line.size());
    }

    public void test7() throws Exception {
        //$NON-NLS-1$
        String text = "\n \"xx\" \n\"dff\"";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("1 string A", 1, line.size());
        line = l[1];
        //$NON-NLS-1$
        assertEquals("1 string B", 1, line.size());
    }

    public void test8() throws Exception {
        //$NON-NLS-1$
        String text = "\n \"xx\" \n\"dff\" \"ccc\"";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("1 string A", 1, line.size());
        line = l[1];
        //$NON-NLS-1$
        assertEquals("2 strings B", 2, line.size());
    }

    public void test9() throws Exception {
        //$NON-NLS-1$ //$NON-NLS-2$
        String text = "fred\n \"xx\"" + NLSElement.createTagText(1) + "\n";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        NLSElement el = line.get(0);
        //$NON-NLS-1$
        assertEquals("has tag", true, el.hasTag());
    }

    public void test10() throws Exception {
        //$NON-NLS-1$
        String text = "fred\n \"xx\"\n";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        NLSElement el = line.get(0);
        //$NON-NLS-1$
        assertEquals("has tag", false, el.hasTag());
    }

    public void test11() throws Exception {
        String text = "\n\"x\" \"y\"" + NLSElement.createTagText(2) + NLSElement.createTagText(1) + //$NON-NLS-1$
        "\n";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("2 strings", 2, line.size());
        NLSElement el = line.get(0);
        //$NON-NLS-1$
        assertEquals("0 has tag", true, el.hasTag());
        el = line.get(1);
        //$NON-NLS-1$
        assertEquals("1 has tag", true, el.hasTag());
    }

    public void test12() throws Exception {
        String text = "\n\"x\" \"y\"" + NLSElement.createTagText(1) + NLSElement.createTagText(2) + //$NON-NLS-1$
        "\n";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("2 strings", 2, line.size());
        NLSElement el = line.get(0);
        //$NON-NLS-1$
        assertEquals("0 has tag", true, el.hasTag());
        el = line.get(1);
        //$NON-NLS-1$
        assertEquals("1 has tag", true, el.hasTag());
    }

    public void test13() throws Exception {
        String text = "\n\"x\" \"y\"" + NLSElement.createTagText(1) + //$NON-NLS-1$
        "\n";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("2 strings", 2, line.size());
        NLSElement el = line.get(0);
        //$NON-NLS-1$
        assertEquals("0 has tag", true, el.hasTag());
        el = line.get(1);
        //$NON-NLS-1$
        assertEquals("1 has no tag", false, el.hasTag());
    }

    public void test14() throws Exception {
        String text = "\n\"x\" \"y\"" + NLSElement.createTagText(2) + //$NON-NLS-1$
        "\n";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("2 strings", 2, line.size());
        NLSElement el = line.get(0);
        //$NON-NLS-1$
        assertEquals("0 has no tag", false, el.hasTag());
        el = line.get(1);
        //$NON-NLS-1$
        assertEquals("1 has tag", true, el.hasTag());
    }

    // test for bug 102132
    public void test15() throws Exception {
        String text = "\nfoo\n@Annotation(\"bar\")\n\"baz\"";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(1, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(3, line.getLineNumber());
        assertEquals("\"baz\"", line.get(0).getValue());
    }

    // test for bug 102132
    public void test16() throws Exception {
        String text = "\nfoo\n@Annotation(\n{\"bar\",\n\"baz\"})\n\"baz\"";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(1, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(5, line.getLineNumber());
        assertEquals("\"baz\"", line.get(0).getValue());
    }

    // test for bug 102132
    public void test17() throws Exception {
        String text = "\n@Annotation(a= @Nested(\"Hello\"), b= \"World\")\n@Annotation2(a= (1 + 2) * 3, b= \"xx\")";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(0, l.length);
    }

    // test for bug 102132
    public void test18() throws Exception {
        String text = "@interface Annotation { String a= \"translate me\"; }";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(1, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(0, line.getLineNumber());
        assertEquals("\"translate me\"", line.get(0).getValue());
    }

    // test for bug 102132
    public void test19() throws Exception {
        String text = "@interface Annotation {\r\n" + "	String a() default \"a\" + \"b\";\r\n" + "	String b() default \"bee\";\r\n" + "	String c() default true ? \"x\" : \"y\";\r\n" + "}\r\n";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(0, l.length);
    }

    // test for bug 102132
    public void test20() throws Exception {
        String text = "class C {\r\n" + "    void m() {\r\n" + "        switch (42) {\r\n" + "            default: String s= \"x\";\r\n" + "        }\r\n" + "        switch (1) {\r\n" + "            default /*standard*/: String s= \"x\";\r\n" + "        }\r\n" + "    }\r\n" + "}";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(2, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(3, line.getLineNumber());
        assertEquals("\"x\"", line.get(0).getValue());
        line = l[1];
        assertEquals(1, line.size());
        assertEquals(6, line.getLineNumber());
        assertEquals("\"x\"", line.get(0).getValue());
    }

    // test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=227482
    public void test21() throws Exception {
        String text = "class C {\r\n" + "    void m() {\r\n" + "        System.out.println(new Object() {\r\n" + "            @Override\r\n" + "            public String toString() {\r\n" + "                return \"me\";\r\n" + "            };\r\n" + "        });\r\n" + "    }\r\n" + "}";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(1, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(5, line.getLineNumber());
        assertEquals("\"me\"", line.get(0).getValue());
    }

    // test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=227482
    public void test22() throws Exception {
        String text = "class C {\r\n" + "    void m() {\r\n" + "        Object var= ((((new Object() {\r\n" + "            @Override\r\n" + "            public String toString() {\r\n" + "                return \"me\";\r\n" + "            };\r\n" + "        }))));\r\n" + "    }\r\n" + "}";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(1, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(5, line.getLineNumber());
        assertEquals("\"me\"", line.get(0).getValue());
    }

    // test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=227482
    public void test23() throws Exception {
        String text = "class C {\r\n" + "    Object field= (new Object() {\r\n" + "        @java.lang.Override\r\n" + "        public String toString() {\r\n" + "            return \"me\";\r\n" + "        };\r\n" + "    });\r\n" + "}";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(1, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(4, line.getLineNumber());
        assertEquals("\"me\"", line.get(0).getValue());
    }

    // test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=227482
    public void test24() throws Exception {
        String text = "class C {\r\n" + "    @java.lang.Deprecated int field2= (\"me\").length();\r\n" + "}";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(1, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(1, line.getLineNumber());
        assertEquals("\"me\"", line.get(0).getValue());
    }

    // test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=233905
    public void test25() throws Exception {
        String text = "@SuppressWarnings(\"unchecked\") //$NON-NLS-1$\r\n" + "public class B {}\r\n" + "\r\n";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(0, l.length);
    }

    // test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=xxx
    public void test26() throws Exception {
        String text = "@interface Ann {\n" + "	String[] strings() default {\"a\", \"2\"};\n" + "	String string() default \"s\";\n" + "	String string2() default ((true) ? \"t\" : \"f\");\n" + "}\n" + "\n" + "public interface Intf {\n" + "	default void foo() {\n" + "		System.out.println(\"Hello\");\n" + "	}\n" + "}";
        NLSLine[] l = NLSScanner.scan(text);
        assertEquals(1, l.length);
        NLSLine line = l[0];
        assertEquals(1, line.size());
        assertEquals(8, line.getLineNumber());
        assertEquals("\"Hello\"", line.get(0).getValue());
    }

    //regression test for bug 12600
    public void test54() throws Exception {
        String text = //$NON-NLS-1$
        "\n\"x\"" + NLSElement.TAG_PREFIX + 1 + //$NON-NLS-1$
        "\n";
        NLSLine[] l = NLSScanner.scan(text);
        NLSLine line = l[0];
        //$NON-NLS-1$
        assertEquals("1 strings", 1, line.size());
        NLSElement el = line.get(0);
        //$NON-NLS-1$
        assertEquals("0 has no tag", false, el.hasTag());
    }
}
