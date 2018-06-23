/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.Date;

public class InstanceVariablesTests {

    public void nop() {
    }

    public  InstanceVariablesTests() {
        // should see this.*Str with correct values
        nop();
    }

    public static void main(String[] args) {
        InstanceVariablesTests ivt = new InstanceVariablesTests();
        ivt.run();
    }

    public void run() {
        // should see this
        nop();
        InstanceVariablesTests ivt = new IVTSubclass();
        ivt.run();
    }

    public String pubStr = "public";

    protected String protStr = "protected";

    /* default */
    String defStr = "default";

    private String privStr = "private";

    protected String nullStr = null;

    protected Date date = new Date();

    protected Date nullDate = null;
}

class IVTSubclass extends InstanceVariablesTests {

    public void run() {
        nop();
    }

    public String pubStr = "redefined public";

    protected String protStr = "redefined protected";

    /* default */
    String defStr = "redefined default";
}
