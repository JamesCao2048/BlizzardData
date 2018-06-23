/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java6;

import java.util.ArrayList;

/**
 * Class to test instance retrieval features
 */
public class AllInstancesTests {

    class RefObject {

        private int fNum = -1;

        public  RefObject(int number) {
            fNum = number;
        }

        public String toString() {
            return "RefObject" + fNum;
        }
    }

    class RefClass {

        public ArrayList makeReferences(int number) {
            ArrayList list = new ArrayList(number);
            for (int i = 0; i < number; i++) {
                list.add(new RefObject(i));
            }
            return list;
        }
    }

    public ArrayList makeRefObjectReferences(int number) {
        ArrayList list = new ArrayList(number);
        for (int i = 0; i < number; i++) {
            list.add(new RefObject(i));
        }
        return list;
    }

    public ArrayList makeRefClassReferences(int number) {
        ArrayList list = new ArrayList(number);
        for (int i = 0; i < number; i++) {
            list.add(new RefClass());
        }
        return list;
    }

    public static void main(String[] args) {
        AllInstancesTests ait = new AllInstancesTests();
        ArrayList list = ait.makeRefObjectReferences(12);
        RefObject ro = ait.new RefObject(-1);
        list = ait.makeRefClassReferences(1001);
        RefClass rc = ait.new RefClass();
        //TODO breakpoint
        System.out.println("end");
    }
}
