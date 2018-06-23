/*******************************************************************************
 *  Copyright (c) 2007, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Chris Aniszczyk <zx@us.ibm.com> - initial API and implementation
 *     Kevin Doyle <kjdoyle@ca.ibm.com> - bug 207868, 207904
 *******************************************************************************/
package org.eclipse.pde.internal.runtime.spy.sections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IPageChangeProvider;
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
public class ActiveDialogPageSection implements ISpySection {

    @Override
    public void build(ScrolledForm form, SpyFormToolkit toolkit, ExecutionEvent event) {
        final Shell shell = HandlerUtil.getActiveShell(event);
        Object object = shell.getData();
        if (object == null)
            return;
        Class clazz = object.getClass();
        if (object instanceof IPageChangeProvider) {
            IPageChangeProvider pageChangeProvider = (IPageChangeProvider) object;
            Object selectedPage = pageChangeProvider.getSelectedPage();
            if (selectedPage != null) {
                Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR);
                section.clientVerticalSpacing = 9;
                if (selectedPage instanceof IDialogPage) {
                    IDialogPage page = (IDialogPage) selectedPage;
                    clazz = page.getClass();
                    section.setText(NLS.bind(PDERuntimeMessages.SpyDialog_activeDialogPageSection_title, page.getTitle()));
                } else {
                    clazz = selectedPage.getClass();
                    section.setText(PDERuntimeMessages.SpyDialog_activeDialogPageSection_title2);
                }
                // the active page
                FormText text = toolkit.createFormText(section, true);
                section.setClient(text);
                TableWrapData td = new TableWrapData();
                td.align = TableWrapData.FILL;
                td.grabHorizontal = true;
                section.setLayoutData(td);
                StringBuffer buffer = new StringBuffer();
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<form>");
                buffer.append(toolkit.createClassSection(text, PDERuntimeMessages.SpyDialog_activeDialogPageSection_desc, new Class[] { clazz }));
                Bundle bundle = FrameworkUtil.getBundle(clazz);
                //$NON-NLS-1$
                toolkit.generatePluginDetailsText(//$NON-NLS-1$
                bundle, //$NON-NLS-1$
                null, //$NON-NLS-1$
                "dialog page", //$NON-NLS-1$
                buffer, //$NON-NLS-1$
                text);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "</form>");
                text.setText(buffer.toString(), true, false);
            }
        }
    }
}
