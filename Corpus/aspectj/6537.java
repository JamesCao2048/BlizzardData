/* *******************************************************************
 * Copyright (c) 2013 VMware
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation {date}
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataInputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ClassVisitor;

public class RuntimeInvisTypeAnnos extends RuntimeTypeAnnos {

	public RuntimeInvisTypeAnnos(int nameIdx, int len, DataInputStream dis,
			ConstantPool cpool) throws IOException {
		this(nameIdx, len, cpool);
		readTypeAnnotations(dis, cpool);
	}

	public RuntimeInvisTypeAnnos(int nameIdx, int len, ConstantPool cpool) {
		super(Constants.ATTR_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS, true, nameIdx, len, cpool);
	}

	public void accept(ClassVisitor v) {
		v.visitRuntimeInvisibleTypeAnnotations(this);
	}
	
}