/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.ArrayList;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.Util;

public class SignatureExt {

    public static char[][] getTypeSuperClassInterfaces(char[] typeSignature) throws IllegalArgumentException {
        try {
            int length = typeSignature.length;
            if (length == 0)
                return CharOperation.NO_CHAR_CHAR;
            int i = 0;
            if (typeSignature[0] == Signature.C_GENERIC_START) {
                // leading '<'
                i++;
                while (i < length && typeSignature[i] != Signature.C_GENERIC_END) {
                    i = CharOperation.indexOf(Signature.C_COLON, typeSignature, i);
                    if (i < 0 || i >= length)
                        throw new IllegalArgumentException();
                    // iterate over bounds
                    nextBound: while (typeSignature[i] == ':') {
                        // skip colon
                        i++;
                        if (typeSignature[i] == ':') {
                            // empty bound
                            continue nextBound;
                        }
                        i = Util.scanTypeSignature(typeSignature, i);
                        // position at start of next param if any
                        i++;
                    }
                }
                if (i < 0 || i >= length)
                    throw new IllegalArgumentException();
                // trailing '>'
                i++;
            }
            ArrayList<char[]> superList = new ArrayList<char[]>();
            while (i < length) {
                int superStart = i;
                i = Util.scanTypeSignature(typeSignature, i);
                i++;
                superList.add(CharOperation.subarray(typeSignature, superStart, i));
            }
            char[][] result;
            superList.toArray(result = new char[superList.size()][]);
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        throw new IllegalArgumentException();
    }

    public static String[] getTypeSuperClassInterfaces(String typeSignature) throws IllegalArgumentException {
        char[][] params = getTypeSuperClassInterfaces(typeSignature.toCharArray());
        return CharOperation.toStrings(params);
    }
}
