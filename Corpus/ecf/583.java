/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/cookie/TestCookiePolicy.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
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
 *
 */
package org.apache.commons.httpclient.cookie;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for Cookie Policy
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 1.1 $
 */
public class TestCookiePolicy extends TestCookieBase {

    public  TestCookiePolicy(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(TestCookiePolicy.class);
    }

    public void testRegisterNullPolicyId() {
        try {
            CookiePolicy.registerCookieSpec(null, null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRegisterNullPolicy() {
        try {
            CookiePolicy.registerCookieSpec("whatever", null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testUnregisterNullPolicy() {
        try {
            CookiePolicy.unregisterCookieSpec(null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetPolicyNullId() {
        try {
            CookiePolicy.getCookieSpec(null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRegisterUnregister() {
        CookiePolicy.registerCookieSpec("whatever", CookieSpecBase.class);
        CookiePolicy.unregisterCookieSpec("whatever");
        try {
            CookiePolicy.getCookieSpec("whatever");
            fail("IllegalStateException must have been thrown");
        } catch (IllegalStateException expected) {
        }
    }

    public void testGetDefaultPolicy() {
        assertNotNull(CookiePolicy.getDefaultSpec());
    }
}
