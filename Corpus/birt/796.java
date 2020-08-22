/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Actuate Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.report.designer.ui.preview.parameter;

import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.ui.preview.BaseTestCase;

/**
 * Tests <code>ParameterFactory</code>
 *
 */

public class ParameterCreationTest extends BaseTestCase
{
	/**
	 * Test getRootChildren method in ParameterFactory.
	 * 
	 * @throws Exception
	 */

	public void testGetRootChildren( ) throws Exception
	{
		ParameterFactory factory = new ParameterFactory( engineTask );

		List children = factory.getRootChildren( );
		assertEquals( 3, children.size( ) );

		Iterator iterator = children.iterator( );
		while ( iterator.hasNext( ) )
		{
			Object obj = iterator.next( );
			if ( obj instanceof ScalarParam )
			{
				ScalarParam param = (ScalarParam) obj;
				assertEquals( 1, param.getValueList( ).size( ) );
			}
			else if ( obj instanceof ParamGroup )
			{
				ParamGroup group = (ParamGroup) obj;
				if ( group.getHandle( ).getName( ).equals( "NewParameterGroup" ) ) //$NON-NLS-1$
				{
					assertEquals( 2, group.getChildren( ).size( ) );
				}
				else if ( group.getHandle( ).getName( ).equals(
						"NewCascadingParameterGroup" ) )//$NON-NLS-1$
				{
					assertEquals( 3, group.getChildren( ).size( ) );
				}
			}
		}
	}
	
	
}
