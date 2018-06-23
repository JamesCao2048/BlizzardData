/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/cookie/TestCookieAll.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:50 $
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.httpclient.cookie;

import junit.framework.*;

/**
 * @author oleg Kalnichevski 
 * 
 * @version $Id: TestCookieAll.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
 */
public class TestCookieAll extends TestCase {

    public  TestCookieAll(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(TestCookie.suite());
        suite.addTest(TestCookieCompatibilitySpec.suite());
        suite.addTest(TestCookieRFC2109Spec.suite());
        suite.addTest(TestCookieNetscapeDraft.suite());
        suite.addTest(TestCookieIgnoreSpec.suite());
        suite.addTest(TestCookiePolicy.suite());
        suite.addTest(TestDateParser.suite());
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestCookieAll.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
}
