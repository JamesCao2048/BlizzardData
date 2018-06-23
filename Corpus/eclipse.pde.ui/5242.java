/*******************************************************************************
* Copyright (c) 2009, 2015 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*   IBM Corporation - Renamed from FeaturesPage
******************************************************************************/
package org.eclipse.pde.internal.ui.editor.category;

import org.eclipse.pde.internal.core.isite.ISiteCategoryDefinition;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class IUsPage extends PDEFormPage {

    //$NON-NLS-1$
    public static final String PAGE_ID = "ius";

    private CategorySection fCategorySection;

    private SiteFeaturesBlock fBlock;

    public class SiteFeaturesBlock extends PDEMasterDetailsBlock {

        public  SiteFeaturesBlock() {
            super(IUsPage.this);
        }

        @Override
        protected PDESection createMasterSection(IManagedForm managedForm, Composite parent) {
            fCategorySection = new CategorySection(getPage(), parent);
            return fCategorySection;
        }

        @Override
        protected void registerPages(DetailsPart detailsPart) {
            detailsPart.setPageProvider(new IDetailsPageProvider() {

                @Override
                public Object getPageKey(Object object) {
                    if (object instanceof SiteFeatureAdapter)
                        return SiteFeatureAdapter.class;
                    if (object instanceof SiteBundleAdapter) {
                        return SiteBundleAdapter.class;
                    }
                    if (object instanceof ISiteCategoryDefinition)
                        return ISiteCategoryDefinition.class;
                    return object.getClass();
                }

                @Override
                public IDetailsPage getPage(Object key) {
                    if (key.equals(ISiteCategoryDefinition.class))
                        return createCategoryDetails();
                    return null;
                }
            });
        }
    }

    public  IUsPage(PDEFormEditor editor) {
        super(editor, PAGE_ID, PDEUIMessages.CategoryPage_header);
        fBlock = new SiteFeaturesBlock();
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        form.setText(PDEUIMessages.CategoryPage_header);
        form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_CATEGORY_OBJ));
        fBlock.createContent(managedForm);
        fCategorySection.fireSelection();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(), IHelpContextIds.CATEGORY_EDITOR);
    }

    private IDetailsPage createCategoryDetails() {
        return new PDEDetailsSections() {

            @Override
            protected PDESection[] createSections(PDEFormPage page, Composite parent) {
                return new PDESection[] { new CategoryDetailsSection(getPage(), parent) };
            }

            @Override
            public String getContextId() {
                return CategoryInputContext.CONTEXT_ID;
            }
        };
    }

    @Override
    protected String getHelpResource() {
        return IHelpContextIds.CATEGORY_EDITOR;
    }
}
