/*******************************************************************************
 *  Copyright (c) 2007, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Chris Aniszczyk <zx@us.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.runtime.spy.sections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.wizard.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.runtime.PDERuntimeMessages;
import org.eclipse.pde.internal.runtime.spy.SpyFormToolkit;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @since 3.4
 */
public class ActiveWizardSection implements ISpySection {

    @Override
    public void build(ScrolledForm form, SpyFormToolkit toolkit, ExecutionEvent event) {
        final Shell shell = HandlerUtil.getActiveShell(event);
        Object object = shell.getData();
        if (object == null)
            return;
        Class clazz = object.getClass();
        if (object instanceof WizardDialog) {
            WizardDialog dialog = (WizardDialog) object;
            IWizardPage page = dialog.getCurrentPage();
            IWizard wizard = page.getWizard();
            clazz = wizard.getClass();
            Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR);
            section.clientVerticalSpacing = 9;
            // the active wizard
            FormText text = toolkit.createFormText(section, true);
            section.setClient(text);
            TableWrapData td = new TableWrapData();
            td.align = TableWrapData.FILL;
            td.grabHorizontal = true;
            section.setLayoutData(td);
            StringBuffer buffer = new StringBuffer();
            //$NON-NLS-1$
            buffer.append("<form>");
            section.setText(NLS.bind(PDERuntimeMessages.SpyDialog_activeWizard_title, wizard.getWindowTitle()));
            buffer.append(toolkit.createClassSection(text, PDERuntimeMessages.SpyDialog_activeWizard_desc, new Class[] { clazz }));
            Bundle bundle = FrameworkUtil.getBundle(clazz);
            //$NON-NLS-1$
            toolkit.generatePluginDetailsText(bundle, null, "wizard", buffer, text);
            //$NON-NLS-1$
            buffer.append("</form>");
            text.setText(buffer.toString(), true, false);
        }
    }
}
