/*
 *************************************************************************
 * Copyright (c) 2009, 2010 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation - initial API and implementation
 *  
 *************************************************************************
 */

package org.eclipse.birt.report.data.oda.jdbc.dbprofile.ui.internal.datasource;

import java.util.Properties;

import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSourceDesign;
import org.eclipse.datatools.connectivity.oda.design.ui.profile.db.wizards.DbProfileEditorPage;

/**
 *  Extends ODA UI framework property page class for this custom ODA designer.
 *  @since 2.5.2
 */
public class CustomDbProfileEditorPage extends DbProfileEditorPage
{

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.design.internal.ui.profile.db.DbProfilePropertyPage#createTransientProfile(java.util.Properties)
     */
    @Override
    protected IConnectionProfile createTransientProfile( Properties connProperties )
    {
        Properties profileProps = DbProfilePropertyProvider.adaptToDbProfileProperties( connProperties );
        
        return super.createTransientProfile( profileProps );
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.design.internal.ui.profile.db.DbProfilePropertyPage#setDataSourceDesignProperties(org.eclipse.datatools.connectivity.oda.design.DataSourceDesign, java.util.Properties)
     */
    @Override
    protected void setDataSourceDesignProperties( DataSourceDesign design, Properties propertyValuePairs ) 
        throws OdaException
    {
        Properties dataSourceProps = 
            DbProfilePropertyProvider.adaptToDataSourceProperties( propertyValuePairs );
        
        super.setDataSourceDesignProperties( design, dataSourceProps );
    }

}
