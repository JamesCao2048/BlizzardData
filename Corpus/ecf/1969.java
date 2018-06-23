/****************************************************************************
 * Copyright (c) 2008 Versant Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Versant Corp. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.discovery.ui.model;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.ecf.discovery.ui.model.ModelPackage
 * @generated
 */
public interface ModelFactory extends EFactory {

    /**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
    ModelFactory eINSTANCE = org.eclipse.ecf.discovery.ui.model.impl.ModelFactoryImpl.init();

    /**
	 * Returns a new object of class '<em>IService Info</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>IService Info</em>'.
	 * @generated
	 */
    IServiceInfo createIServiceInfo();

    /**
	 * Returns a new object of class '<em>INetwork</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>INetwork</em>'.
	 * @generated
	 */
    INetwork createINetwork();

    /**
	 * Returns a new object of class '<em>IHost</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>IHost</em>'.
	 * @generated
	 */
    IHost createIHost();

    /**
	 * Returns a new object of class '<em>IService ID</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>IService ID</em>'.
	 * @generated
	 */
    IServiceID createIServiceID();

    /**
	 * Returns a new object of class '<em>IService Type ID</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>IService Type ID</em>'.
	 * @generated
	 */
    IServiceTypeID createIServiceTypeID();

    /**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
    ModelPackage getModelPackage();
}
//ModelFactory
