/*******************************************************************************
 * Copyright (c) 2004,2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.data.engine.regre.db;

import testutil.ConfigText;



/**
 * 
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ConnectionSQLServerTest extends ConnectionTest
{

	/*
	 * @see ConnectionTest#setUp()
	 */
	protected void setUp( ) throws Exception
	{
		DriverClass = ConfigText.getString( "Regre.SQLServer.DriverClass" );
		URL = ConfigText.getString( "Regre.SQLServer.URL" );
		User = ConfigText.getString( "Regre.SQLServer.User" );
		Password = ConfigText.getString( "Regre.SQLServer.Password" );
		super.setUp( );
	}

}
