/*******************************************************************************
 *  Copyright (c) 2006, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Peter Nehrer <pnehrer@eclipticalsoftware.com> - bug 490771
 *******************************************************************************/
package org.eclipse.pde.internal.ui.templates.osgi;

import org.eclipse.pde.internal.ui.templates.PDETemplateMessages;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.pde.ui.templates.NewPluginTemplateWizard;

public class OSGiSimpleLogServiceNewWizard extends NewPluginTemplateWizard {

    @Override
    public void init(IFieldData data) {
        super.init(data);
        setWindowTitle(PDETemplateMessages.OSGiSimpleLogServiceNewWizard_title);
    }

    @Override
    public ITemplateSection[] createTemplateSections() {
        return new ITemplateSection[] { new OSGiSimpleLogServiceTemplate() };
    }

    @Override
    public String[] getImportPackages() {
        return new String[] { //$NON-NLS-1$
        "org.osgi.framework;version=\"1.3.0\"", "org.osgi.util.tracker;version=\"1.3.1\"", //$NON-NLS-1$
        "org.osgi.service.component.annotations;version=\"1.2.0\";resolution:=\"optional\"" };
    }
}
