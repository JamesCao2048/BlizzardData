/*******************************************************************************
 *  Copyright (c) 2008, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.runtime;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.internal.runtime.registry.model.LocalRegistryBackend;
import org.eclipse.pde.internal.runtime.registry.model.RegistryModel;

public class LocalModelTest extends AbstractRegistryModelTest {

    @Override
    protected RegistryModel createModel() {
        return new RegistryModel(new LocalRegistryBackend() {

            @Override
            public void connect(IProgressMonitor monitor) {
                mockFramework.setListener(this);
            }

            @Override
            public void disconnect() {
            // empty
            }
        });
    }

    public static Test suite() {
        return new TestSuite(LocalModelTest.class);
    }
}
