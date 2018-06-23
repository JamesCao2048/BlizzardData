/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.product;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;

public class ProductIntroWizard extends Wizard {

    private ProductDefinitonWizardPage fProductDefinitionPage;

    private ProductIntroWizardPage fNewIntroPage;

    private boolean fNeedNewProduct;

    private String fIntroId;

    private String fProductId;

    private String fPluginId;

    private String fApplication;

    private IProduct fProduct;

    public  ProductIntroWizard(IProduct product, boolean needNewProduct) {
        setDefaultPageImageDescriptor(PDEPluginImages.DESC_DEFCON_WIZ);
        setNeedsProgressMonitor(true);
        fProduct = product;
        fNeedNewProduct = needNewProduct;
        setWindowTitle(PDEUIMessages.ProductIntroWizard_title);
    }

    @Override
    public void addPages() {
        if (fNeedNewProduct) {
            //$NON-NLS-1$
            fProductDefinitionPage = new ProductDefinitonWizardPage("product", fProduct);
            addPage(fProductDefinitionPage);
        }
        //$NON-NLS-1$
        fNewIntroPage = new ProductIntroWizardPage("intro", fProduct);
        addPage(fNewIntroPage);
    }

    @Override
    public boolean performFinish() {
        try {
            if (fNeedNewProduct) {
                fProductId = fProductDefinitionPage.getProductId();
                fPluginId = fProductDefinitionPage.getDefiningPlugin();
                fApplication = fProductDefinitionPage.getApplication();
                String newProductName = fProductDefinitionPage.getProductName();
                if (newProductName != null)
                    fProduct.setName(newProductName);
                fProduct.setProductId(getProductId());
                fProduct.setApplication(fApplication);
                getContainer().run(false, true, new ProductDefinitionOperation(fProduct, fPluginId, fProductId, fApplication, getContainer().getShell()));
            }
            fIntroId = fNewIntroPage.getIntroId();
            if (fPluginId == null)
                fPluginId = fNewIntroPage.getDefiningPlugin();
            getContainer().run(false, true, new ProductIntroOperation(fProduct, fPluginId, fIntroId, getContainer().getShell()));
        } catch (InvocationTargetException e) {
            MessageDialog.openError(getContainer().getShell(), PDEUIMessages.ProductDefinitionWizard_error, e.getTargetException().getMessage());
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    public String getIntroId() {
        return fIntroId;
    }

    public String getProductId() {
        //$NON-NLS-1$
        return fPluginId + "." + fProductId;
    }

    public String getApplication() {
        return fApplication;
    }
}
