/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public @interface Annot {

    String[] name() default { "" };

    Class<? extends Object>[] getClazz() default { Object.class };

    ElementType[] getElementType() default { ElementType.FIELD };

    Target[] getTarget() default { @Target({ ElementType.METHOD }) };

    float[] getF() default { 0.0f };

    double[] getD() default { -0.0 };

    short[] getS() default { 0 };

    boolean[] getB() default { false };

    char[] getC() default { ' ' };

    long[] getL() default { Long.MAX_VALUE };

    byte[] getByte() default { 0 };

    int[] getInts() default { 0 };
}
