/* *******************************************************************
 * Copyright (c) 2009 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *	Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Unknown;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverStateInfo;

public class ReadingAttributes extends TestCase {

	public void testWeaverStateInfo() throws ClassNotFoundException, IOException {

		JavaClass jc = getClassFrom(new File("n:/temp"), "com.springsource.petclinic.domain.Visit");
		assertNotNull(jc);
		Attribute[] attrs = jc.getAttributes();
		for (int i = 0; i < attrs.length; i++) {
			System.out.println(attrs[i].getName());
			if (attrs[i].getName().endsWith("WeaverState")) {
				Unknown u = (Unknown) attrs[i];
				VersionedDataInputStream vdis = new VersionedDataInputStream(new ByteArrayInputStream(u.getBytes()), null);
				// WeaverStateInfo wsi =
				WeaverStateInfo.read(vdis, null);
				// System.out.println(wsi);
			}
		}
		// Method[] meths = jc.getMethods();
		// Method oneWeWant = null;
		// for (int i = 0; i < meths.length && oneWeWant == null; i++) {
		// Method method = meths[i];
		// if (method.getName().equals("main")) {
		// oneWeWant = meths[i];
		// }
		// }
	}

	public SyntheticRepository createRepos(File cpentry) {
		ClassPath cp = new ClassPath(cpentry + File.pathSeparator + System.getProperty("java.class.path"));
		return SyntheticRepository.getInstance(cp);
	}

	protected JavaClass getClassFrom(File where, String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos(where);
		return repos.loadClass(clazzname);
	}
}
