/*
 * $HeadURL: 
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:49 $
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
package org.apache.commons.httpclient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests that don't require any external host.
 * I.e., that run entirely within this JVM.
 *
 * (True unit tests, by some definitions.)
 *
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @version $Revision: 1.1 $ $Date: 2009/02/13 18:07:49 $
 * 
 * @deprecated Use TestAll
 */
public class TestNoHost extends TestCase {

    public  TestNoHost(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(TestAll.suite());
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestNoHost.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
}
