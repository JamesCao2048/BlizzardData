/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.api.tools.internal.ApiDescription;
import org.eclipse.pde.api.tools.internal.ApiDescriptionXmlCreator;
import org.eclipse.pde.api.tools.internal.CompilationUnit;
import org.eclipse.pde.api.tools.internal.IApiCoreConstants;
import org.eclipse.pde.api.tools.internal.model.ArchiveApiTypeContainer;
import org.eclipse.pde.api.tools.internal.model.CompositeApiTypeContainer;
import org.eclipse.pde.api.tools.internal.model.DirectoryApiTypeContainer;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiTypeContainer;
import org.eclipse.pde.api.tools.internal.provisional.scanner.TagScanner;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Ant task to generate the .api_description file during the Eclipse build.
 */
public class ApiFileGenerationTask extends Task {

    static class APIToolsNatureDefaultHandler extends DefaultHandler {

        //$NON-NLS-1$
        private static final String NATURE_ELEMENT_NAME = "nature";

        boolean isAPIToolsNature = false;

        boolean insideNature = false;

        StringBuffer buffer;

        @Override
        public void error(SAXParseException e) throws SAXException {
            e.printStackTrace();
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if (this.isAPIToolsNature) {
                return;
            }
            this.insideNature = NATURE_ELEMENT_NAME.equals(name);
            if (this.insideNature) {
                this.buffer = new StringBuffer();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (this.insideNature) {
                this.buffer.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (this.insideNature) {
                // check the contents of the characters
                String natureName = String.valueOf(this.buffer).trim();
                this.isAPIToolsNature = ApiPlugin.NATURE_ID.equals(natureName);
            }
            this.insideNature = false;
        }

        public boolean isAPIToolsNature() {
            return this.isAPIToolsNature;
        }
    }

    boolean debug;

    String projectName;

    String projectLocation;

    String targetFolder;

    String binaryLocations;

    String manifests;

    String sourceLocations;

    boolean allowNonApiProject = false;

    /**
	 * The encoding to read the source files with
	 *
	 * @since 1.0.600
	 */
    String encoding;

    Set<String> apiPackages = new HashSet(0);

    /**
	 * Set the project name.
	 *
	 * @param projectName the given project name
	 */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
	 * Set the project location.
	 *
	 * <br>
	 * <br>
	 * This is the folder that contains all the source files for the given
	 * project. <br>
	 * <br>
	 * The location is set using an absolute path.</p>
	 *
	 * @param projectLocation the given project location
	 */
    public void setProject(String projectLocation) {
        this.projectLocation = projectLocation;
    }

    /**
	 * Set the target location.
	 *
	 * <br>
	 * <br>
	 * This is the folder in which the generated files are generated. <br>
	 * <br>
	 * The location is set using an absolute path.</p>
	 *
	 * @param targetLocation the given target location
	 */
    public void setTarget(String targetLocation) {
        this.targetFolder = targetLocation;
    }

    /**
	 * Set the binary locations.
	 *
	 * <br>
	 * <br>
	 * This is a list of folders or jar files that contain all the .class files
	 * for the given project. They are separated by the platform path separator.
	 * Each entry must exist. <br>
	 * <br>
	 * They should be specified using absolute paths.
	 *
	 * @param binaryLocations the given binary locations
	 */
    public void setBinary(String binaryLocations) {
        this.binaryLocations = binaryLocations;
    }

    /**
	 * Set if the task should scan the project even if it is not API tools
	 * enabled.
	 * <p>
	 * The possible values are: <code>true</code> or <code>false</code>
	 * </p>
	 * <p>
	 * Default is: <code>false</code>.
	 * </p>
	 *
	 * @since 1.2
	 * @param allow
	 */
    public void setAllowNonApiProject(String allow) {
        this.allowNonApiProject = Boolean.valueOf(allow).booleanValue();
    }

    /**
	 * Sets the encoding the task should use when reading text streams
	 *
	 * @param encoding
	 * @since 1.0.600
	 */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
	 * Set the debug value.
	 * <p>
	 * The possible values are: <code>true</code>, <code>false</code>
	 * </p>
	 * <p>
	 * Default is <code>false</code>.
	 * </p>
	 *
	 * @param debugValue the given debug value
	 */
    public void setDebug(String debugValue) {
        this.debug = Boolean.toString(true).equals(debugValue);
    }

    /**
	 * Set the extra manifest files' locations.
	 *
	 * <p>
	 * This is a list of extra MANIFEST.MF files' locations that can be set to
	 * provide more API packages to scan. They are separated by the platform
	 * path separator. Each entry must exist.
	 * </p>
	 * <p>
	 * If the path is not absolute, it will be resolved relative to the current
	 * working directory.
	 * </p>
	 * <p>
	 * Jar files can be specified instead of MANIFEST.MF file. If a jar file is
	 * specified, its MANIFEST.MF file will be read if it exists.
	 * </p>
	 *
	 * @param manifests the given extra manifest files' locations
	 */
    public void setExtraManifests(String manifests) {
        this.manifests = manifests;
    }

    /**
	 * Set the extra source locations.
	 *
	 * <br>
	 * <br>
	 * This is a list of locations for source files that will be scanned. They
	 * are separated by the platform path separator. Each entry must exist. <br>
	 * <br>
	 * They should be specified using absolute paths.
	 *
	 * @param manifests the given extra source locations
	 */
    public void setExtraSourceLocations(String sourceLocations) {
        this.sourceLocations = sourceLocations;
    }

    /**
	 * Execute the ant task
	 */
    @Override
    public void execute() throws BuildException {
        if (this.binaryLocations == null || this.projectName == null || this.projectLocation == null || this.targetFolder == null) {
            StringWriter out = new StringWriter();
            PrintWriter writer = new PrintWriter(out);
            writer.println(NLS.bind(Messages.api_generation_printArguments, new String[] { this.projectName, this.projectLocation, this.binaryLocations, this.targetFolder }));
            writer.flush();
            writer.close();
            throw new BuildException(String.valueOf(out.getBuffer()));
        }
        if (this.debug) {
            //$NON-NLS-1$
            System.out.println("Project name : " + this.projectName);
            //$NON-NLS-1$
            System.out.println("Encoding: " + this.encoding);
            //$NON-NLS-1$
            System.out.println("Project location : " + this.projectLocation);
            //$NON-NLS-1$
            System.out.println("Binary locations : " + this.binaryLocations);
            //$NON-NLS-1$
            System.out.println("Target folder : " + this.targetFolder);
            if (this.manifests != null) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "Extra manifest entries : " + this.manifests);
            }
            if (this.sourceLocations != null) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "Extra source locations entries : " + this.sourceLocations);
            }
        }
        // collect all compilation units
        File root = new File(this.projectLocation);
        if (!root.exists() || !root.isDirectory()) {
            if (this.debug) {
                //$NON-NLS-1$
                System.err.println(//$NON-NLS-1$
                "Must be a directory : " + this.projectLocation);
            }
            throw new BuildException(NLS.bind(Messages.api_generation_projectLocationNotADirectory, this.projectLocation));
        }
        // check if the project contains the API tools nature
        //$NON-NLS-1$
        File dotProjectFile = new File(root, ".project");
        if (!this.allowNonApiProject && !isAPIToolsNature(dotProjectFile)) {
            //$NON-NLS-1$
            System.err.println("The project does not have an API Tools nature so a api_description file will not be generated");
            return;
        }
        // check if the .api_description file exists
        File targetProjectFolder = new File(this.targetFolder);
        if (!targetProjectFolder.exists()) {
            targetProjectFolder.mkdirs();
        } else if (!targetProjectFolder.isDirectory()) {
            if (this.debug) {
                //$NON-NLS-1$
                System.err.println(//$NON-NLS-1$
                "Must be a directory : " + this.targetFolder);
            }
            throw new BuildException(NLS.bind(Messages.api_generation_targetFolderNotADirectory, this.targetFolder));
        }
        File apiDescriptionFile = new File(targetProjectFolder, IApiCoreConstants.API_DESCRIPTION_XML_NAME);
        if (apiDescriptionFile.exists()) {
            // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=414053
            if (this.debug) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "Existing api description file deleted");
            }
            apiDescriptionFile.delete();
        }
        File[] allFiles = null;
        Map<String, String> manifestMap = null;
        IApiTypeContainer classFileContainer = null;
        if (!this.projectLocation.endsWith(Util.ORG_ECLIPSE_SWT)) {
            // create the directory class file container used to resolve
            // signatures during tag scanning
            String[] allBinaryLocations = this.binaryLocations.split(File.pathSeparator);
            List<IApiTypeContainer> allContainers = new ArrayList();
            IApiTypeContainer container = null;
            for (String allBinaryLocation : allBinaryLocations) {
                container = getContainer(allBinaryLocation);
                if (container == null) {
                    throw new BuildException(NLS.bind(Messages.api_generation_invalidBinaryLocation, allBinaryLocation));
                }
                allContainers.add(container);
            }
            classFileContainer = new CompositeApiTypeContainer(null, allContainers);
            File manifestFile = null;
            //$NON-NLS-1$
            File manifestDir = new File(root, "META-INF");
            if (manifestDir.exists() && manifestDir.isDirectory()) {
                manifestFile = new //$NON-NLS-1$
                File(//$NON-NLS-1$
                manifestDir, //$NON-NLS-1$
                "MANIFEST.MF");
            }
            if (manifestFile != null && manifestFile.exists()) {
                BufferedInputStream inputStream = null;
                try {
                    inputStream = new BufferedInputStream(new FileInputStream(manifestFile));
                    manifestMap = ManifestElement.parseBundleManifest(inputStream, null);
                    this.apiPackages = collectApiPackageNames(manifestMap);
                } catch (FileNotFoundException e) {
                    ApiPlugin.log(e);
                } catch (IOException e) {
                    ApiPlugin.log(e);
                } catch (BundleException e) {
                    ApiPlugin.log(e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            if (this.manifests != null) {
                String[] allManifestFiles = this.manifests.split(File.pathSeparator);
                for (String allManifestFile : allManifestFiles) {
                    File currentManifest = new File(allManifestFile);
                    Set<String> currentApiPackages = null;
                    if (currentManifest.exists()) {
                        BufferedInputStream inputStream = null;
                        ZipFile zipFile = null;
                        try {
                            if (isZipJarFile(currentManifest.getName())) {
                                zipFile = new ZipFile(currentManifest);
                                final ZipEntry entry = //$NON-NLS-1$
                                zipFile.getEntry(//$NON-NLS-1$
                                "META-INF/MANIFEST.MF");
                                if (entry != null) {
                                    inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                                }
                            } else {
                                inputStream = new BufferedInputStream(new FileInputStream(currentManifest));
                            }
                            if (inputStream != null) {
                                manifestMap = ManifestElement.parseBundleManifest(inputStream, null);
                                currentApiPackages = collectApiPackageNames(manifestMap);
                            }
                        } catch (FileNotFoundException e) {
                            ApiPlugin.log(e);
                        } catch (IOException e) {
                            ApiPlugin.log(e);
                        } catch (BundleException e) {
                            ApiPlugin.log(e);
                        } finally {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                }
                            }
                            if (zipFile != null) {
                                try {
                                    zipFile.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    }
                    if (currentApiPackages != null) {
                        if (this.apiPackages == null) {
                            this.apiPackages = currentApiPackages;
                        } else {
                            this.apiPackages.addAll(currentApiPackages);
                        }
                    }
                }
            }
            FileFilter fileFilter = new FileFilter() {

                @Override
                public boolean accept(File path) {
                    return (path.isFile() && Util.isJavaFileName(path.getName()) && isApi(path.getParent())) || path.isDirectory();
                }
            };
            allFiles = Util.getAllFiles(root, fileFilter);
            if (this.sourceLocations != null) {
                String[] allSourceLocations = this.sourceLocations.split(File.pathSeparator);
                for (String currentSourceLocation : allSourceLocations) {
                    File[] allFiles2 = Util.getAllFiles(new File(currentSourceLocation), fileFilter);
                    if (allFiles2 != null) {
                        if (allFiles == null) {
                            allFiles = allFiles2;
                        } else {
                            int length = allFiles.length;
                            int length2 = allFiles2.length;
                            System.arraycopy(allFiles, 0, (allFiles = new File[length + length2]), 0, length);
                            System.arraycopy(allFiles2, 0, allFiles, length, length2);
                        }
                    }
                }
            }
        }
        ApiDescription apiDescription = new ApiDescription(this.projectName);
        TagScanner tagScanner = TagScanner.newScanner();
        if (allFiles != null && allFiles.length != 0) {
            Map<String, String> options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_COMPLIANCE, resolveCompliance(manifestMap));
            CompilationUnit unit = null;
            for (int i = 0, max = allFiles.length; i < max; i++) {
                unit = new CompilationUnit(allFiles[i].getAbsolutePath(), this.encoding);
                if (this.debug) {
                    //$NON-NLS-1$ //$NON-NLS-2$
                    System.out.println("Unit name[" + i + "] : " + unit.getName());
                }
                try {
                    tagScanner.scan(unit, apiDescription, classFileContainer, options, null);
                } catch (CoreException e) {
                    ApiPlugin.log(e);
                } finally {
                    try {
                        if (classFileContainer != null) {
                            classFileContainer.close();
                        }
                    } catch (CoreException e) {
                    }
                }
            }
        }
        try {
            ApiDescriptionXmlCreator xmlVisitor = new ApiDescriptionXmlCreator(this.projectName, this.projectName);
            apiDescription.accept(xmlVisitor, null);
            String xml = xmlVisitor.getXML();
            Util.saveFile(apiDescriptionFile, xml);
        } catch (CoreException e) {
            ApiPlugin.log(e);
        } catch (IOException e) {
            ApiPlugin.log(e);
        }
    }

    /**
	 * Returns if the given path ends with one of the collected API path names
	 *
	 * @param path
	 * @return true if the given path name ends with one of the collected API
	 *         package names
	 */
    boolean isApi(String path) {
        for (String pkg : apiPackages) {
            if (path.endsWith(pkg.replace('.', File.separatorChar))) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Collects the names of the packages that are API for the bundle the API
	 * description is being created for
	 *
	 * @param manifestmap
	 * @return the names of the packages that are API for the bundle the API
	 *         description is being created for
	 * @throws BundleException if parsing the manifest map to get API package
	 *             names fail for some reason
	 */
    private Set<String> collectApiPackageNames(Map<String, String> manifestmap) throws BundleException {
        HashSet<String> set = new HashSet();
        ManifestElement[] packages = ManifestElement.parseHeader(Constants.EXPORT_PACKAGE, manifestmap.get(Constants.EXPORT_PACKAGE));
        if (packages != null) {
            for (int i = 0; i < packages.length; i++) {
                ManifestElement packageName = packages[i];
                Enumeration<String> directiveKeys = packageName.getDirectiveKeys();
                if (directiveKeys == null) {
                    set.add(packageName.getValue());
                } else {
                    boolean include = true;
                    loop: for (; directiveKeys.hasMoreElements(); ) {
                        Object directive = directiveKeys.nextElement();
                        if (//$NON-NLS-1$
                        "x-internal".equals(//$NON-NLS-1$
                        directive)) {
                            String value = packageName.getDirective((String) directive);
                            if (Boolean.valueOf(value).booleanValue()) {
                                include = false;
                                break loop;
                            }
                        }
                        if (//$NON-NLS-1$
                        "x-friends".equals(//$NON-NLS-1$
                        directive)) {
                            include = false;
                            break loop;
                        }
                    }
                    if (include) {
                        set.add(packageName.getValue());
                    }
                }
            }
        }
        return set;
    }

    private IApiTypeContainer getContainer(String location) {
        File f = new File(location);
        if (!f.exists()) {
            return null;
        }
        if (isZipJarFile(location)) {
            return new ArchiveApiTypeContainer(null, location);
        } else {
            return new DirectoryApiTypeContainer(null, location);
        }
    }

    /**
	 * Resolves the compiler compliance based on the BREE entry in the
	 * MANIFEST.MF file
	 *
	 * @param manifestmap
	 * @return The derived {@link JavaCore#COMPILER_COMPLIANCE} from the BREE in
	 *         the manifest map, or {@link JavaCore#VERSION_1_3} if there is no
	 *         BREE entry in the map or if the BREE entry does not directly map
	 *         to one of {"1.3", "1.4", "1.5", "1.6", "1.7","1.8"}.
	 */
    private String resolveCompliance(Map<String, String> manifestmap) {
        if (manifestmap != null) {
            String eename = manifestmap.get(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
            if (eename != null) {
                if (//$NON-NLS-1$
                "J2SE-1.4".equals(eename)) {
                    return JavaCore.VERSION_1_4;
                }
                if (//$NON-NLS-1$
                "J2SE-1.5".equals(eename)) {
                    return JavaCore.VERSION_1_5;
                }
                if (//$NON-NLS-1$
                "JavaSE-1.6".equals(eename)) {
                    return JavaCore.VERSION_1_6;
                }
                if (//$NON-NLS-1$
                "JavaSE-1.7".equals(eename)) {
                    return JavaCore.VERSION_1_7;
                }
                if (//$NON-NLS-1$
                "JavaSE-1.8".equals(eename)) {
                    return JavaCore.VERSION_1_8;
                }
            }
        }
        return JavaCore.VERSION_1_3;
    }

    /**
	 * Resolves if the '.project' file belongs to an API enabled project or not
	 *
	 * @param dotProjectFile
	 * @return true if the '.project' file is for an API enabled project, false
	 *         otherwise
	 */
    private boolean isAPIToolsNature(File dotProjectFile) {
        if (!dotProjectFile.exists()) {
            return false;
        }
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(dotProjectFile));
            //$NON-NLS-1$
            String contents = new String(Util.getInputStreamAsCharArray(stream, -1, "UTF-8"));
            return containsAPIToolsNature(contents);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    private static boolean isZipJarFile(String fileName) {
        String normalizedFileName = fileName.toLowerCase();
        return //$NON-NLS-1$
        normalizedFileName.endsWith(".zip") || //$NON-NLS-1$
        normalizedFileName.endsWith(//$NON-NLS-1$
        ".jar");
    }

    /**
	 * Check if the given source contains an source extension point.
	 *
	 * @param pluginXMLContents the given file contents
	 * @return true if it contains a source extension point, false otherwise
	 */
    private boolean containsAPIToolsNature(String pluginXMLContents) {
        SAXParserFactory factory = null;
        try {
            factory = SAXParserFactory.newInstance();
        } catch (FactoryConfigurationError e) {
            return false;
        }
        SAXParser saxParser = null;
        try {
            saxParser = factory.newSAXParser();
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        }
        if (saxParser == null) {
            return false;
        }
        // Parse
        InputSource inputSource = new InputSource(new BufferedReader(new StringReader(pluginXMLContents)));
        try {
            APIToolsNatureDefaultHandler defaultHandler = new APIToolsNatureDefaultHandler();
            saxParser.parse(inputSource, defaultHandler);
            return defaultHandler.isAPIToolsNature();
        } catch (SAXException e) {
        } catch (IOException e) {
        }
        return false;
    }
}
