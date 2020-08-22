/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.ajde.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.tools.ajc.AjcTests;
import org.aspectj.util.FileUtil;

/**
 * Test implementation of ICompilerConfiguration. Allows users to configure the settings via setter methods. By default returns null
 * for all options except getClasspath(), getJavaOptionsMap() (by default returns that it's 1.3 compliant),
 * getOutputLocationManager(), getSourcePathResources() (it recursively looks for them) and getProjectSourceFiles(). If no source
 * files are specified by the user, then getProjectSourceFiles() returns an empty list.
 */
public class TestCompilerConfiguration implements ICompilerConfiguration {

	private String projectPath;

	private Set<File> aspectpath;
	private Set<File> inpath;
	private String outjar;
	private Map<String, String> javaOptions;
	private String nonStandardOptions;
	private List<String> projectSourceFiles = new ArrayList<String>();
	private Map<String, File> sourcePathResources;

	private String srcDirName = "src";

	private IOutputLocationManager outputLoc;

	public TestCompilerConfiguration(String projectPath) {
		this.projectPath = projectPath;
	}

	public Set<File> getAspectPath() {
		return aspectpath;
	}

	public List<String> getProjectXmlConfigFiles() {
		return Collections.emptyList();
	}

	public String getClasspath() {
		return projectPath + File.pathSeparator + System.getProperty("sun.boot.class.path") + File.pathSeparator
				+ AjcTests.aspectjrtClasspath();
	}

	public Set<File> getInpath() {
		return inpath;
	}

	public Map<String, String> getJavaOptionsMap() {
		if (javaOptions == null) {
			javaOptions = new Hashtable<String, String>();
			javaOptions.put(JavaOptions.COMPLIANCE_LEVEL, JavaOptions.VERSION_13);
			javaOptions.put(JavaOptions.SOURCE_COMPATIBILITY_LEVEL, JavaOptions.VERSION_13);
		}
		return javaOptions;
	}

	public String getNonStandardOptions() {
		return nonStandardOptions;
	}

	public String getOutJar() {
		return outjar;
	}

	public IOutputLocationManager getOutputLocationManager() {
		if (outputLoc == null) {
			outputLoc = new TestOutputLocationManager(projectPath);
		}
		return outputLoc;
	}

	public List<String> getProjectSourceFiles() {
		return projectSourceFiles;
	}

	public List getProjectSourceFilesChanged() {
		return null;
	}

	public void configurationRead() {
	}

	public Map<String, File> getSourcePathResources() {
		if (sourcePathResources == null) {
			sourcePathResources = new HashMap<String, File>();

			/* Allow the user to override the testProjectPath by using sourceRoots */
			File[] srcBase = new File[] { new File(projectPath + File.separator + srcDirName) };

			for (int j = 0; j < srcBase.length; j++) {
				File[] fromResources = FileUtil.listFiles(srcBase[j], new FileFilter() {
					public boolean accept(File pathname) {
						String name = pathname.getName().toLowerCase();
						return !name.endsWith(".class") && !name.endsWith(".java") && !name.endsWith(".aj")
								&& !name.endsWith(".lst") && !name.endsWith(".jar");
					}
				});
				for (int i = 0; i < fromResources.length; i++) {
					String normPath = FileUtil.normalizedPath(fromResources[i], srcBase[j]);
					sourcePathResources.put(normPath, fromResources[i]);

				}
			}
		}
		return sourcePathResources;
	}

	// -------------------- setter methods useful for testing ---------------
	public void setAspectPath(Set<File> aspectPath) {
		this.aspectpath = aspectPath;
	}

	public void setInpath(Set<File> inpath) {
		this.inpath = inpath;
	}

	public void setOutjar(String outjar) {
		this.outjar = outjar;
	}

	public void setJavaOptions(Map javaOptions) {
		this.javaOptions = javaOptions;
	}

	public void setNonStandardOptions(String options) {
		this.nonStandardOptions = options;
	}

	public void setProjectSourceFiles(List<String> projectSourceFiles) {
		this.projectSourceFiles = projectSourceFiles;
	}

	public void setSourcePathResources(Map<String, File> sourcePathResources) {
		this.sourcePathResources = sourcePathResources;
	}

	public void setSourceDir(String srcDirName) {
		this.srcDirName = srcDirName;
	}

	public int getConfigurationChanges() {
		return ICompilerConfiguration.EVERYTHING;
	}

	public List getClasspathElementsWithModifiedContents() {
		return null;
	}

	public String getProjectEncoding() {
		return null;
	}

}
