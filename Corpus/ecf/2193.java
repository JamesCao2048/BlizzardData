/*******************************************************************************
 * Copyright (c) 2005, 2006 Erkki Lindpere and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Erkki Lindpere - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.provider.phpbb.identity;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.ecf.internal.bulletinboard.commons.util.IDUtil;

public class MemberID extends PHPBBID {

    private static final long serialVersionUID = 6005970691318023633L;

    public  MemberID(PHPBBNamespace namespace, URI uri) throws URISyntaxException {
        super(namespace, uri);
    }

    public  MemberID(PHPBBNamespace namespace, URL baseURL, long longValue) throws URISyntaxException {
        super(namespace, IDUtil.composeURI(baseURL, "profile.php?mode=viewprofile&u=" + longValue));
    }

    public long getLongValue() {
        Matcher m = Pattern.compile("profile.php\\?mode=viewprofile(?:.*?)u=([0-9]+)").matcher(uri.toString());
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return -1;
    }
}
