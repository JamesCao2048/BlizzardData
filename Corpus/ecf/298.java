/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/TestMultipartPost.java,v 1.1 2009/02/13 18:07:49 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:49 $
 *
 * ====================================================================
 *
 *  Copyright 2003-2004 The Apache Software Foundation
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.apache.commons.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * Webapp tests specific to the MultiPostMethod.
 *
 * @author <a href="oleg@ural.ru">Oleg Kalnichevski</a>
 */
public class TestMultipartPost extends HttpClientTestBase {

    public  TestMultipartPost(final String testName) throws IOException {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestMultipartPost.class);
        ProxyTestDecorator.addTests(suite);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestMultipartPost.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests
    /**
     * Test that the body consisting of a string part can be posted.
     */
    public void testPostStringPart() throws Exception {
        this.server.setHttpService(new EchoService());
        PostMethod method = new PostMethod();
        MultipartRequestEntity entity = new MultipartRequestEntity(new Part[] { new StringPart("param", "Hello", "ISO-8859-1") }, method.getParams());
        method.setRequestEntity(entity);
        client.executeMethod(method);
        assertEquals(200, method.getStatusCode());
        String body = method.getResponseBodyAsString();
        assertTrue(body.indexOf("Content-Disposition: form-data; name=\"param\"") >= 0);
        assertTrue(body.indexOf("Content-Type: text/plain; charset=ISO-8859-1") >= 0);
        assertTrue(body.indexOf("Content-Transfer-Encoding: 8bit") >= 0);
        assertTrue(body.indexOf("Hello") >= 0);
    }

    /**
     * Test that the body consisting of a file part can be posted.
     */
    public void testPostFilePart() throws Exception {
        this.server.setHttpService(new EchoService());
        PostMethod method = new PostMethod();
        byte[] content = "Hello".getBytes();
        MultipartRequestEntity entity = new MultipartRequestEntity(new Part[] { new FilePart("param1", new ByteArrayPartSource("filename.txt", content), "text/plain", "ISO-8859-1") }, method.getParams());
        method.setRequestEntity(entity);
        client.executeMethod(method);
        assertEquals(200, method.getStatusCode());
        String body = method.getResponseBodyAsString();
        assertTrue(body.indexOf("Content-Disposition: form-data; name=\"param1\"; filename=\"filename.txt\"") >= 0);
        assertTrue(body.indexOf("Content-Type: text/plain; charset=ISO-8859-1") >= 0);
        assertTrue(body.indexOf("Content-Transfer-Encoding: binary") >= 0);
        assertTrue(body.indexOf("Hello") >= 0);
    }

    public class TestPartSource implements PartSource {

        private String fileName;

        private byte[] data;

        public  TestPartSource(String fileName, byte[] data) {
            this.fileName = fileName;
            this.data = data;
        }

        public long getLength() {
            return -1;
        }

        public String getFileName() {
            return fileName;
        }

        public InputStream createInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }
    }

    public void testPostFilePartUnknownLength() throws Exception {
        this.server.setHttpService(new EchoService());
        String enc = "ISO-8859-1";
        PostMethod method = new PostMethod();
        byte[] content = "Hello".getBytes(enc);
        MultipartRequestEntity entity = new MultipartRequestEntity(new Part[] { new FilePart("param1", new TestPartSource("filename.txt", content), "text/plain", enc) }, method.getParams());
        method.setRequestEntity(entity);
        client.executeMethod(method);
        assertEquals(200, method.getStatusCode());
        String body = method.getResponseBodyAsString();
        assertTrue(body.indexOf("Content-Disposition: form-data; name=\"param1\"; filename=\"filename.txt\"") >= 0);
        assertTrue(body.indexOf("Content-Type: text/plain; charset=" + enc) >= 0);
        assertTrue(body.indexOf("Content-Transfer-Encoding: binary") >= 0);
        assertTrue(body.indexOf("Hello") >= 0);
    }
}
