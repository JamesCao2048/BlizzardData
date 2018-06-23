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
package org.eclipse.ecf.internal.provider.vbulletin.identity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.bulletinboard.commons.PatternFactoryPair;
import org.eclipse.ecf.internal.provider.vbulletin.ThreadFactory;
import org.eclipse.ecf.internal.provider.vbulletin.internal.ForumFactory;
import org.eclipse.ecf.internal.provider.vbulletin.internal.MemberFactory;
import org.eclipse.ecf.internal.provider.vbulletin.internal.MemberGroupFactory;
import org.eclipse.ecf.internal.provider.vbulletin.internal.ThreadMessageFactory;

public class VBNamespace extends Namespace {

    private static final long serialVersionUID = 5509243089799844682L;

    private static final String PROTOCOL = "http://";

    private static final PatternFactoryPair[] ID_FACTORIES = { new PatternFactoryPair(Pattern.compile("(.*?)forumdisplay.php\\?(?:.*?)f=([0-9]+)"), new ForumFactory()), new PatternFactoryPair(Pattern.compile("(.*?)showthread.php\\?(?:.*?)t=([0-9]+)"), new ThreadFactory()), new PatternFactoryPair(Pattern.compile("(.*?)showpost.php\\?(?:.*?)p=([0-9]+)"), new ThreadMessageFactory()), new PatternFactoryPair(Pattern.compile("(.*?)member.php\\?(?:.*?)u=([0-9]+)"), new MemberFactory()), new PatternFactoryPair(Pattern.compile("(.*?)profile.php\\?do=editusergroups(?:.*?)usergroupid=([0-9]+)"), new MemberGroupFactory()) };

    @Override
    public ID createInstance(Object[] args) throws IDCreateException {
        try {
            ID id = null;
            for (PatternFactoryPair creator : ID_FACTORIES) {
                Matcher m = creator.getPattern().matcher((CharSequence) args[0]);
                if (m.find()) {
                    id = creator.getFactory().createBBObjectId(this, new URL(m.group(1)), m.group(2));
                    return id;
                }
            }
            return new VBID(this, new URI((String) args[0]));
        } catch (URISyntaxException e) {
            throw new IDCreateException("VB ID creation exception", e);
        } catch (NumberFormatException e) {
            throw new IDCreateException("VB ID creation exception", e);
        } catch (MalformedURLException e) {
            throw new IDCreateException("VB ID creation exception", e);
        }
    }

    @Override
    public String getScheme() {
        return PROTOCOL;
    }
}
