/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.targetdefinition;

import org.eclipse.pde.core.target.ITargetDefinition;
import java.lang.reflect.InvocationTargetException;
import java.util.TreeSet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.parts.ComboPart;
import org.eclipse.pde.internal.ui.util.LocaleUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.*;

/**
 * Section for editing OS, WS, Arch and NL settings in the target definition editor
 * @see EnvironmentPage
 * @see TargetEditor
 */
public class EnvironmentSection extends SectionPart {

    private ComboPart fOSCombo;

    private ComboPart fWSCombo;

    private ComboPart fNLCombo;

    private ComboPart fArchCombo;

    private TreeSet<String> fNLChoices;

    private TreeSet<String> fOSChoices;

    private TreeSet<String> fWSChoices;

    private TreeSet<String> fArchChoices;

    private boolean LOCALES_INITIALIZED = false;

    private TargetEditor fEditor;

    public  EnvironmentSection(FormPage page, Composite parent) {
        super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
        fEditor = (TargetEditor) page.getEditor();
        createClient(getSection(), page.getEditor().getToolkit());
    }

    /**
	 * @return The target model backing this editor
	 */
    private ITargetDefinition getTarget() {
        return fEditor.getTarget();
    }

    /**
	 * Creates the UI for this section.
	 *
	 * @param section section the UI is being added to
	 * @param toolkit form toolkit used to create the widgets
	 */
    protected void createClient(Section section, FormToolkit toolkit) {
        section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
        section.setText(PDEUIMessages.EnvironmentBlock_targetEnv);
        section.setDescription(PDEUIMessages.EnvironmentSection_description);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.verticalAlignment = SWT.TOP;
        data.horizontalSpan = 2;
        section.setLayoutData(data);
        Composite client = toolkit.createComposite(section);
        client.setLayout(FormLayoutFactory.createSectionClientGridLayout(true, 2));
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Composite left = toolkit.createComposite(client);
        left.setLayout(new GridLayout(2, false));
        GridLayout layout = FormLayoutFactory.createClearGridLayout(false, 2);
        layout.horizontalSpacing = layout.verticalSpacing = 5;
        left.setLayout(layout);
        left.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        initializeChoices();
        Label label = toolkit.createLabel(left, PDEUIMessages.EnvironmentSection_operationSystem);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        fOSCombo = new ComboPart();
        fOSCombo.createControl(left, toolkit, SWT.SINGLE | SWT.BORDER);
        fOSCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fOSCombo.setItems(fOSChoices.toArray(new String[fOSChoices.size()]));
        fOSCombo.setVisibleItemCount(30);
        label = toolkit.createLabel(left, PDEUIMessages.EnvironmentSection_windowingSystem);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        fWSCombo = new ComboPart();
        fWSCombo.createControl(left, toolkit, SWT.SINGLE | SWT.BORDER);
        fWSCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fWSCombo.setItems(fWSChoices.toArray(new String[fWSChoices.size()]));
        fWSCombo.setVisibleItemCount(30);
        Composite right = toolkit.createComposite(client);
        layout = FormLayoutFactory.createClearGridLayout(false, 2);
        layout.verticalSpacing = layout.horizontalSpacing = 5;
        right.setLayout(layout);
        right.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = toolkit.createLabel(right, PDEUIMessages.EnvironmentSection_architecture);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        fArchCombo = new ComboPart();
        fArchCombo.createControl(right, toolkit, SWT.SINGLE | SWT.BORDER);
        fArchCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fArchCombo.setItems(fArchChoices.toArray(new String[fArchChoices.size()]));
        fArchCombo.setVisibleItemCount(30);
        label = toolkit.createLabel(right, PDEUIMessages.EnvironmentSection_locale);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        fNLCombo = new ComboPart();
        fNLCombo.createControl(right, toolkit, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
        fNLCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fNLCombo.setItems(fNLChoices.toArray(new String[fNLChoices.size()]));
        fNLCombo.setVisibleItemCount(30);
        refresh();
        fOSCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                markDirty();
                getTarget().setOS(getText(fOSCombo));
            }
        });
        fWSCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                markDirty();
                getTarget().setWS(getText(fWSCombo));
            }
        });
        fArchCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                markDirty();
                getTarget().setArch(getText(fArchCombo));
            }
        });
        fNLCombo.getControl().addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent event) {
                // if we haven't gotten all the values for the NL's, display a busy cursor to the user while we find them.
                if (!LOCALES_INITIALIZED) {
                    try {
                        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

                            @Override
                            public void run(IProgressMonitor monitor) {
                                initializeAllLocales();
                                LOCALES_INITIALIZED = true;
                            }
                        });
                    } catch (InvocationTargetException e) {
                        PDEPlugin.log(e);
                    } catch (InterruptedException e) {
                        PDEPlugin.log(e);
                    }
                }
                // If we attached the listener initially, when we call setItems(..), it would make the editor dirty (when the user didn't change anything)
                if (fNLCombo.getItemCount() < 3) {
                    String current = fNLCombo.getSelection();
                    if (!fNLCombo.getControl().isDisposed()) {
                        fNLCombo.setItems(fNLChoices.toArray(new String[fNLChoices.size()]));
                        fNLCombo.setText(current);
                    }
                    fNLCombo.addModifyListener(new ModifyListener() {

                        @Override
                        public void modifyText(ModifyEvent e) {
                            String value = getText(fNLCombo);
                            if (value == null) {
                                getTarget().setNL(null);
                            } else {
                                //$NON-NLS-1$
                                int //$NON-NLS-1$
                                index = value.indexOf("-");
                                if (index > 0)
                                    value = value.substring(0, index);
                                getTarget().setNL(value.trim());
                            }
                            markDirty();
                        }
                    });
                }
            }
        });
        toolkit.paintBordersFor(client);
        section.setClient(client);
    }

    private void initializeChoices() {
        ITargetDefinition target = getTarget();
        fOSChoices = new TreeSet();
        String[] os = Platform.knownOSValues();
        for (int i = 0; i < os.length; i++) fOSChoices.add(os[i]);
        //$NON-NLS-1$
        fOSChoices.add("");
        String fileValue = target.getOS();
        if (fileValue != null)
            fOSChoices.add(fileValue);
        fWSChoices = new TreeSet();
        String[] ws = Platform.knownWSValues();
        for (int i = 0; i < ws.length; i++) fWSChoices.add(ws[i]);
        //$NON-NLS-1$
        fWSChoices.add("");
        fileValue = target.getWS();
        if (fileValue != null)
            fWSChoices.add(fileValue);
        fArchChoices = new TreeSet();
        String[] arch = Platform.knownOSArchValues();
        for (int i = 0; i < arch.length; i++) fArchChoices.add(arch[i]);
        //$NON-NLS-1$
        fArchChoices.add("");
        fileValue = target.getArch();
        if (fileValue != null)
            fArchChoices.add(fileValue);
        fNLChoices = new TreeSet();
        //$NON-NLS-1$
        fNLChoices.add("");
    }

    private void initializeAllLocales() {
        String[] nl = LocaleUtil.getLocales();
        for (int i = 0; i < nl.length; i++) fNLChoices.add(nl[i]);
        String fileValue = getTarget().getNL();
        if (fileValue != null)
            fNLChoices.add(LocaleUtil.expandLocaleName(fileValue));
        LOCALES_INITIALIZED = true;
    }

    /**
	 * Returns the text of the widget or null if it is empty
	 * @param combo
	 * @return text of the widget or <code>null</code>
	 */
    private String getText(ComboPart combo) {
        String text;
        Control control = combo.getControl();
        if (control instanceof Combo) {
            text = ((Combo) control).getText();
        } else {
            text = ((CCombo) control).getText();
        }
        text = text.trim();
        if (text.length() == 0) {
            return null;
        }
        return text;
    }

    @Override
    public void refresh() {
        ITargetDefinition target = getTarget();
        //$NON-NLS-1$
        String presetValue = (target.getOS() == null) ? "" : target.getOS();
        fOSCombo.setText(presetValue);
        //$NON-NLS-1$
        presetValue = (target.getWS() == null) ? "" : target.getWS();
        fWSCombo.setText(presetValue);
        //$NON-NLS-1$
        presetValue = (target.getArch() == null) ? "" : target.getArch();
        fArchCombo.setText(presetValue);
        //$NON-NLS-1$
        presetValue = (target.getNL() == null) ? "" : LocaleUtil.expandLocaleName(target.getNL());
        fNLCombo.setText(presetValue);
        super.refresh();
    }

    protected void updateChoices() {
        if (LOCALES_INITIALIZED)
            return;
        // kick off thread in background to find the NL values
        new Thread(new Runnable() {

            @Override
            public void run() {
                initializeAllLocales();
            }
        }).start();
    }
}
