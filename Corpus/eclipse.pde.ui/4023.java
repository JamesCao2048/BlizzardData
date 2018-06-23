/*******************************************************************************
 * Copyright (c) 2015 OPCoach
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olivier Prouvost <olivier.prouvost@opcoach.com> - initial API and implementation (bug #481340)
 *******************************************************************************/
package org.eclipse.pde.internal.ui.templates.e4;

import org.eclipse.pde.internal.ui.templates.PDETemplateMessages;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.ITemplateSection;

public class E4HandlerNewWizard extends AbstractE4NewPluginTemplateWizard {

    @Override
    public void init(IFieldData data) {
        super.init(data);
        setWindowTitle(PDETemplateMessages.E4HandlerNewWizard_wtitle);
    }

    @Override
    public ITemplateSection[] createTemplateSections() {
        return new ITemplateSection[] { new E4HandlerTemplate() };
    }

    @Override
    protected String getFilenameToEdit() {
        return E4HandlerTemplate.E4_FRAGMENT_FILE;
    }

    @Override
    public String[] getImportPackages() {
        //$NON-NLS-1$
        return new String[] { "javax.annotation;version=\"1.2.0\"" };
    }
}
