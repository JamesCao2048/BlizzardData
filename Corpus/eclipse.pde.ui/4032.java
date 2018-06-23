/*******************************************************************************
 * Copyright (c) April 5, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package x.y.z;

import m.MethodReferenceAnnotation;
import m.MethodReferenceInterface;
import m.ConstructorReferenceAnnotation;
import m.ConstructorReferenceInterface;
import m.ConstructorReferenceInterfaceArg;
import m.ConstructorReferenceInterfaceParamArg;
import java.util.ArrayList;

/**
 * Tests illegal use of method accessed by method reference
 */
public class testMethodReferenceAnnotation {

    public void m1() {
        MethodReferenceInterface met = MethodReferenceAnnotation::<>method1;
        met.process();
        MethodReferenceAnnotation m = new MethodReferenceAnnotation();
        met = m::<>method2;
        met.process();
        ConstructorReferenceInterface<ConstructorReferenceAnnotation> con = ConstructorReferenceAnnotation::<>new;
        con.create1().getString();
        ConstructorReferenceInterfaceArg<ConstructorReferenceAnnotation, String> conWithArg = ConstructorReferenceAnnotation::<>new;
        conWithArg.create2("test").getString();
        ConstructorReferenceInterfaceParamArg<ConstructorReferenceAnnotation, String> conParamArg = ConstructorReferenceAnnotation::<String>new;
        conParamArg.create3(new ArrayList<String>()).getStrings().size();
    }
}
