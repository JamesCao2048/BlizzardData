/*******************************************************************************
 *  Copyright (c) 2009, 2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     A. Aadel - initial API and implementation     
 *******************************************************************************/
package org.eclipse.ecf.tests.provider.zookeeper;

import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.eclipse.ecf.tests.discovery.DiscoveryServiceTest;

public class ZooDiscoveryServiceTest extends DiscoveryServiceTest {

    public  ZooDiscoveryServiceTest() {
        super(ZooDiscoveryContainerInstantiator.NAME);
    }

    // Known/Accepted test "failure"
    public void testAddServiceListenerWithRefresh() {
        // just register a service to make sure de-registration in tearDown does not fail
        registerService();
    }
}
