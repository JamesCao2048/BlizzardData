/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/cookie/TestCookieBase.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
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

import junit.framework.TestCase;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;

/**
 * Test cases for Cookie
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @version $Revision: 1.1 $
 */
public class TestCookieBase extends TestCase {

    public  TestCookieBase(String name) {
        super(name);
    }

    public static Cookie[] cookieParse(final CookieSpec parser, String host, int port, String path, boolean isSecure, Header setHeader) throws MalformedCookieException {
        Cookie[] cookies = parser.parse(host, port, path, isSecure, setHeader);
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                parser.validate(host, port, path, isSecure, cookies[i]);
            }
        }
        return cookies;
    }
}
