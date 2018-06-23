/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.presence.bot.kosmos;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private static Activator instance;

    private Bundle bundle;

    public  Activator() {
        instance = this;
    }

    public void start(BundleContext context) throws Exception {
        bundle = context.getBundle();
    }

    public void stop(BundleContext context) throws Exception {
        bundle = null;
        instance = null;
    }

    static Bundle getBundle() {
        return instance.bundle;
    }
}
