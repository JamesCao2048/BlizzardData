/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.ExecutionEnvironmentDescription;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.api.tools.internal.FilterStore;
import org.eclipse.pde.api.tools.internal.IApiCoreConstants;
import org.eclipse.pde.api.tools.internal.builder.BuildState;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.Factory;
import org.eclipse.pde.api.tools.internal.provisional.IApiMarkerConstants;
import org.eclipse.pde.api.tools.internal.provisional.IRequiredComponentDescription;
import org.eclipse.pde.api.tools.internal.provisional.VisibilityModifiers;
import org.eclipse.pde.api.tools.internal.provisional.comparator.DeltaVisitor;
import org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IReferenceTypeDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiTypeRoot;
import org.eclipse.pde.api.tools.internal.search.SkippedComponent;
import org.objectweb.asm.Opcodes;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Utility class to use for API tools
 *
 * @since 1.0.0
 */
public final class Util {

    //$NON-NLS-1$
    public static final String DOT_TGZ = ".tgz";

    //$NON-NLS-1$
    public static final String DOT_TAR_GZ = ".tar.gz";

    //$NON-NLS-1$
    public static final String DOT_JAR = ".jar";

    //$NON-NLS-1$
    public static final String DOT_ZIP = ".zip";

    public static final char VERSION_SEPARATOR = '(';

    /**
	 * Class that runs a build in the workspace or the given project
	 */
    private static final class BuildJob extends Job {

        private final IProject[] fProjects;

        private int fBuildType;

        /**
		 * Constructor
		 *
		 * @param name
		 * @param project
		 */
         BuildJob(String name, IProject[] projects) {
            this(name, projects, IncrementalProjectBuilder.FULL_BUILD);
        }

         BuildJob(String name, IProject[] projects, int buildType) {
            super(name);
            fProjects = projects;
            this.fBuildType = buildType;
        }

        @Override
        public boolean belongsTo(Object family) {
            return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
        }

        /**
		 * Returns if this build job is covered by another build job
		 *
		 * @param other
		 * @return true if covered by another build job, false otherwise
		 */
        public boolean isCoveredBy(BuildJob other) {
            if (other.fProjects == null) {
                return true;
            }
            if (this.fProjects != null) {
                for (int i = 0, max = this.fProjects.length; i < max; i++) {
                    if (!other.contains(this.fProjects[i])) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public boolean contains(IProject project) {
            if (project == null) {
                return false;
            }
            for (IProject fProject : this.fProjects) {
                if (project.equals(fProject)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            synchronized (getClass()) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                // cancelBuild(ResourcesPlugin.FAMILY_AUTO_BUILD);
                cancelBuild(ResourcesPlugin.FAMILY_MANUAL_BUILD);
            }
            try {
                if (fProjects != null) {
                    SubMonitor localmonitor = SubMonitor.convert(monitor, UtilMessages.Util_0, fProjects.length);
                    for (IProject currentProject : fProjects) {
                        if (this.fBuildType == IncrementalProjectBuilder.FULL_BUILD) {
                            BuildState.setLastBuiltState(currentProject, null);
                        }
                        localmonitor.subTask(NLS.bind(UtilMessages.Util_5, currentProject.getName()));
                        if (ResourcesPlugin.getWorkspace().isAutoBuilding()) {
                            currentProject.touch(null);
                        } else {
                            currentProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, localmonitor.split(1));
                        }
                    }
                }
            } catch (CoreException e) {
                return new Status(e.getStatus().getSeverity(), ApiPlugin.PLUGIN_ID, ApiPlugin.INTERNAL_ERROR, UtilMessages.Util_builder_errorMessage, e);
            } catch (OperationCanceledException e) {
                return Status.CANCEL_STATUS;
            } finally {
                monitor.done();
            }
            return Status.OK_STATUS;
        }

        private void cancelBuild(Object jobfamily) {
            Job[] buildJobs = Job.getJobManager().find(jobfamily);
            for (Job curr : buildJobs) {
                if (curr != this && curr instanceof BuildJob) {
                    BuildJob job = (BuildJob) curr;
                    if (job.isCoveredBy(this)) {
                        // cancel all other build jobs of our
                        curr.cancel();
                    // kind
                    }
                }
            }
        }
    }

    //$NON-NLS-1$
    public static final String EMPTY_STRING = "";

    public static final String DEFAULT_PACKAGE_NAME = EMPTY_STRING;

    //$NON-NLS-1$
    public static final String MANIFEST_NAME = "MANIFEST.MF";

    //$NON-NLS-1$
    public static final String DOT_CLASS_SUFFIX = ".class";

    //$NON-NLS-1$
    public static final String DOT_JAVA_SUFFIX = ".java";

    /**
	 * Constant representing the default size to read from an input stream
	 */
    private static final int DEFAULT_READING_SIZE = 8192;

    //$NON-NLS-1$
    private static final String JAVA_LANG_OBJECT = "java.lang.Object";

    //$NON-NLS-1$
    private static final String JAVA_LANG_RUNTIMEEXCEPTION = "java.lang.RuntimeException";

    //$NON-NLS-1$
    public static final String LINE_DELIMITER = System.getProperty("line.separator");

    //$NON-NLS-1$
    public static final String UNKNOWN_ELEMENT_KIND = "UNKNOWN_ELEMENT_KIND";

    //$NON-NLS-1$
    public static final String UNKNOWN_FLAGS = "UNKNOWN_FLAGS";

    //$NON-NLS-1$
    public static final String UNKNOWN_KIND = "UNKNOWN_KIND";

    //$NON-NLS-1$
    public static final String UNKNOWN_VISIBILITY = "UNKNOWN_VISIBILITY";

    //$NON-NLS-1$
    public static final String ISO_8859_1 = "ISO-8859-1";

    //$NON-NLS-1$
    public static final String REGULAR_EXPRESSION_START = "R:";

    // Trace for delete operation
    /*
	 * Maximum time wasted repeating delete operations while running JDT/Core
	 * tests.
	 */
    private static int DELETE_MAX_TIME = 0;

    /**
	 * Trace deletion operations while running JDT/Core tests.
	 */
    private static boolean DELETE_DEBUG = false;

    /**
	 * Maximum of time in milliseconds to wait in deletion operation while
	 * running JDT/Core tests. Default is 10 seconds. This number cannot exceed
	 * 1 minute (i.e. 60000). <br>
	 * To avoid too many loops while waiting, the ten first ones are done
	 * waiting 10ms before repeating, the ten loops after are done waiting 100ms
	 * and the other loops are done waiting 1s...
	 */
    private static int DELETE_MAX_WAIT = 10000;

    public static final IPath MANIFEST_PROJECT_RELATIVE_PATH = new Path(JarFile.MANIFEST_NAME);

    //$NON-NLS-1$
    public static final String ORG_ECLIPSE_SWT = "org.eclipse.swt";

    /**
	 * Throws an exception with the given message and underlying exception.
	 *
	 * @param message error message
	 * @param exception underlying exception, or <code>null</code>
	 * @throws CoreException
	 */
    private static void abort(String message, Throwable exception) throws CoreException {
        IStatus status = new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, message, exception);
        throw new CoreException(status);
    }

    /**
	 * Appends a property to the given string buffer with the given key and
	 * value in the format "key=value\n".
	 *
	 * @param buffer buffer to append to
	 * @param key key
	 * @param value value
	 */
    private static void appendProperty(StringBuffer buffer, String key, String value) {
        buffer.append(key);
        buffer.append('=');
        buffer.append(value);
        buffer.append('\n');
    }

    /**
	 * Collects all of the deltas from the given parent delta
	 *
	 * @param delta
	 * @return
	 */
    public static List<IDelta> collectAllDeltas(IDelta delta) {
        final List<IDelta> list = new ArrayList();
        delta.accept(new DeltaVisitor() {

            @Override
            public void endVisit(IDelta localDelta) {
                if (localDelta.getChildren().length == 0) {
                    list.add(localDelta);
                }
                super.endVisit(localDelta);
            }
        });
        return list;
    }

    /**
	 * Collects files into the collector array list
	 *
	 * @param root the root to collect the files from
	 * @param collector the collector to place matches into
	 * @param fileFilter the filter for files or <code>null</code> to accept all
	 *            files
	 */
    private static void collectAllFiles(File root, ArrayList<File> collector, FileFilter fileFilter) {
        File[] files = root.listFiles(fileFilter);
        for (final File currentFile : files) {
            if (currentFile.isDirectory()) {
                collectAllFiles(currentFile, collector, fileFilter);
            } else {
                collector.add(currentFile);
            }
        }
    }

    /**
	 * Returns all of the API projects in the workspace
	 *
	 * @return all of the API projects in the workspace or <code>null</code> if
	 *         there are none.
	 */
    public static IProject[] getApiProjects() {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        ArrayList<IProject> temp = new ArrayList();
        IProject project = null;
        for (IProject allProject : allProjects) {
            project = allProject;
            if (project.isAccessible()) {
                try {
                    if (project.hasNature(org.eclipse.pde.api.tools.internal.provisional.ApiPlugin.NATURE_ID)) {
                        temp.add(project);
                    }
                } catch (CoreException e) {
                }
            }
        }
        IProject[] projects = null;
        if (temp.size() != 0) {
            projects = new IProject[temp.size()];
            temp.toArray(projects);
        }
        return projects;
    }

    /**
	 * Returns all of the API projects in the workspace
	 *
	 * @param sourcelevel
	 * @return all of the API projects in the workspace or <code>null</code> if
	 *         there are none.
	 */
    public static IProject[] getApiProjectsMinSourceLevel(String sourcelevel) {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        ArrayList<IProject> temp = new ArrayList();
        IProject project = null;
        for (IProject allProject : allProjects) {
            project = allProject;
            if (project.isAccessible()) {
                try {
                    if (project.hasNature(org.eclipse.pde.api.tools.internal.provisional.ApiPlugin.NATURE_ID)) {
                        IJavaProject jp = JavaCore.create(project);
                        String src = jp.getOption(JavaCore.COMPILER_SOURCE, true);
                        if (src != null && src.compareTo(sourcelevel) >= 0) {
                            temp.add(project);
                        }
                    }
                } catch (CoreException e) {
                }
            }
        }
        IProject[] projects = null;
        if (temp.size() != 0) {
            projects = new IProject[temp.size()];
            temp.toArray(projects);
        }
        return projects;
    }

    /**
	 * Copies the given file to the new file
	 *
	 * @param file
	 * @param newFile
	 * @return if the copy succeeded
	 */
    public static boolean copy(File file, File newFile) {
        byte[] bytes = null;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            bytes = Util.getInputStreamAsByteArray(inputStream, -1);
        } catch (FileNotFoundException e) {
            ApiPlugin.log(e);
        } catch (IOException e) {
            ApiPlugin.log(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    ApiPlugin.log(e);
                }
            }
        }
        if (bytes != null) {
            BufferedOutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(newFile));
                outputStream.write(bytes);
                outputStream.flush();
            } catch (FileNotFoundException e) {
                ApiPlugin.log(e);
            } catch (IOException e) {
                ApiPlugin.log(e);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
	 * Creates an EE file for the given JRE and specified EE id
	 *
	 * @param jre
	 * @param eeid
	 * @return
	 * @throws IOException
	 */
    public static File createEEFile(IVMInstall jre, String eeid) throws IOException {
        String string = Util.generateEEContents(jre, eeid);
        //$NON-NLS-1$ //$NON-NLS-2$
        File eeFile = createTempFile("eed", ".ee");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(eeFile);
            outputStream.write(string.getBytes(IApiCoreConstants.UTF_8));
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return eeFile;
    }

    /**
	 * Returns whether the objects are equal, accounting for either one being
	 * <code>null</code>.
	 *
	 * @param o1
	 * @param o2
	 * @return whether the objects are equal, or both are <code>null</code>
	 */
    public static boolean equalsOrNull(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        return o1.equals(o2);
    }

    /**
	 * Returns an execution environment description for the given VM.
	 *
	 * @param vm JRE to create an definition for
	 * @return an execution environment description for the given VM
	 * @throws IOException if unable to generate description
	 */
    public static String generateEEContents(IVMInstall vm, String eeId) throws IOException {
        StringBuffer buffer = new StringBuffer();
        appendProperty(buffer, ExecutionEnvironmentDescription.JAVA_HOME, vm.getInstallLocation().getCanonicalPath());
        StringBuffer paths = new StringBuffer();
        LibraryLocation[] libraryLocations = JavaRuntime.getLibraryLocations(vm);
        for (int i = 0; i < libraryLocations.length; i++) {
            LibraryLocation lib = libraryLocations[i];
            paths.append(lib.getSystemLibraryPath().toOSString());
            if (i < (libraryLocations.length - 1)) {
                paths.append(File.pathSeparatorChar);
            }
        }
        appendProperty(buffer, ExecutionEnvironmentDescription.BOOT_CLASS_PATH, paths.toString());
        appendProperty(buffer, ExecutionEnvironmentDescription.CLASS_LIB_LEVEL, eeId);
        return buffer.toString();
    }

    /**
	 * Returns an array of all of the files from the given root that are
	 * accepted by the given file filter. If the file filter is null all files
	 * within the given root are returned.
	 *
	 * @param root
	 * @param fileFilter
	 * @return the list of files from within the given root
	 */
    public static File[] getAllFiles(File root, FileFilter fileFilter) {
        ArrayList<File> files = new ArrayList();
        if (root.isDirectory()) {
            collectAllFiles(root, files, fileFilter);
            File[] result = new File[files.size()];
            files.toArray(result);
            return result;
        }
        return null;
    }

    /**
	 * Returns a build job that will perform a full build on the given projects.
	 *
	 * If <code>projects</code> are null, then an AssertionFailedException is
	 * thrown
	 *
	 * @param projects the projects to build
	 * @return the build job
	 * @throws AssertionFailedException if the given projects are null
	 */
    public static Job getBuildJob(final IProject[] projects) {
        Assert.isNotNull(projects);
        Job buildJob = new BuildJob(UtilMessages.Util_4, projects);
        buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
        buildJob.setUser(true);
        return buildJob;
    }

    /**
	 * Returns a build job that will return the build that corresponds to the
	 * given build kind on the given projects.
	 *
	 * If <code>projects</code> are null, then an AssertionFailedException is
	 * thrown
	 *
	 * @param projects the projects to build
	 * @param buildKind the given build kind
	 * @return the build job
	 * @throws AssertionFailedException if the given projects are null
	 */
    public static Job getBuildJob(final IProject[] projects, int buildKind) {
        Assert.isNotNull(projects);
        Job buildJob = new BuildJob(UtilMessages.Util_4, projects, buildKind);
        buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
        buildJob.setUser(true);
        return buildJob;
    }

    /**
	 * Returns a result of searching the given components for class file with
	 * the given type name.
	 *
	 * @param components API components to search or <code>null</code> if none
	 * @param typeName type to search for
	 * @return class file or <code>null</code> if none found
	 */
    public static IApiTypeRoot getClassFile(IApiComponent[] components, String typeName) {
        if (components == null) {
            return null;
        }
        for (IApiComponent apiComponent : components) {
            if (apiComponent != null) {
                try {
                    IApiTypeRoot classFile = apiComponent.findTypeRoot(typeName);
                    if (classFile != null) {
                        return classFile;
                    }
                } catch (CoreException e) {
                }
            }
        }
        return null;
    }

    /**
	 * Return a string that represents the element type of the given delta.
	 * Returns {@link #UNKNOWN_ELEMENT_KIND} if the element type cannot be
	 * determined.
	 *
	 * @param delta the given delta
	 * @return a string that represents the element type of the given delta.
	 */
    public static String getDeltaElementType(IDelta delta) {
        return getDeltaElementType(delta.getElementType());
    }

    /**
	 * Returns a text representation of a marker severity level
	 *
	 * @param severity
	 * @return text of a marker severity level
	 */
    public static String getSeverity(int severity) {
        switch(severity) {
            case IMarker.SEVERITY_ERROR:
                {
                    return //$NON-NLS-1$
                    "ERROR";
                }
            case IMarker.SEVERITY_INFO:
                {
                    //$NON-NLS-1$
                    return "INFO";
                }
            case IMarker.SEVERITY_WARNING:
                {
                    return //$NON-NLS-1$
                    "WARNING";
                }
            default:
                {
                    return "UNKNOWN_SEVERITY";
                }
        }
    }

    /**
	 * Return an int value that represents the given element type Returns -1 if
	 * the element type cannot be determined.
	 *
	 * @param elementType the given element type
	 * @return an int that represents the given element type constant.
	 */
    public static int getDeltaElementTypeValue(String elementType) {
        Class<IDelta> IDeltaClass = IDelta.class;
        try {
            Field field = IDeltaClass.getField(elementType);
            return field.getInt(null);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return -1;
    }

    /**
	 * Return a string that represents the given element type Returns
	 * {@link #UNKNOWN_ELEMENT_KIND} if the element type cannot be determined.
	 *
	 * @param elementType the given element type
	 * @return a string that represents the given element type.
	 */
    public static String getDeltaElementType(int elementType) {
        switch(elementType) {
            case IDelta.ANNOTATION_ELEMENT_TYPE:
                return "ANNOTATION_ELEMENT_TYPE";
            case IDelta.INTERFACE_ELEMENT_TYPE:
                return "INTERFACE_ELEMENT_TYPE";
            case IDelta.ENUM_ELEMENT_TYPE:
                return "ENUM_ELEMENT_TYPE";
            case IDelta.API_COMPONENT_ELEMENT_TYPE:
                return "API_COMPONENT_ELEMENT_TYPE";
            case IDelta.API_BASELINE_ELEMENT_TYPE:
                return "API_BASELINE_ELEMENT_TYPE";
            case IDelta.CONSTRUCTOR_ELEMENT_TYPE:
                return "CONSTRUCTOR_ELEMENT_TYPE";
            case IDelta.METHOD_ELEMENT_TYPE:
                return "METHOD_ELEMENT_TYPE";
            case IDelta.FIELD_ELEMENT_TYPE:
                return "FIELD_ELEMENT_TYPE";
            case IDelta.CLASS_ELEMENT_TYPE:
                return "CLASS_ELEMENT_TYPE";
            case IDelta.TYPE_PARAMETER_ELEMENT_TYPE:
                return "TYPE_PARAMETER_ELEMENT_TYPE";
            default:
                break;
        }
        return UNKNOWN_ELEMENT_KIND;
    }

    /**
	 * Return a string that represents the given flags Returns
	 * {@link #UNKNOWN_FLAGS} if the flags cannot be determined.
	 *
	 * @param flags the given delta's flags
	 * @return a string that represents the given flags.
	 */
    public static String getDeltaFlagsName(int flags) {
        switch(flags) {
            case IDelta.ABSTRACT_TO_NON_ABSTRACT:
                return "ABSTRACT_TO_NON_ABSTRACT";
            case IDelta.ANNOTATION_DEFAULT_VALUE:
                return "ANNOTATION_DEFAULT_VALUE";
            case IDelta.API_COMPONENT:
                return "API_COMPONENT";
            case IDelta.ARRAY_TO_VARARGS:
                return "ARRAY_TO_VARARGS";
            case IDelta.CHECKED_EXCEPTION:
                return "CHECKED_EXCEPTION";
            case IDelta.CLASS_BOUND:
                return //$NON-NLS-1$
                "CLASS_BOUND";
            case IDelta.CLINIT:
                return //$NON-NLS-1$
                "CLINIT";
            case IDelta.CONSTRUCTOR:
                return //$NON-NLS-1$
                "CONSTRUCTOR";
            case IDelta.CONTRACTED_SUPERINTERFACES_SET:
                return "CONTRACTED_SUPERINTERFACES_SET";
            case IDelta.DECREASE_ACCESS:
                return "DECREASE_ACCESS";
            case IDelta.ENUM_CONSTANT:
                return "ENUM_CONSTANT";
            case IDelta.EXECUTION_ENVIRONMENT:
                return "EXECUTION_ENVIRONMENT";
            case IDelta.EXPANDED_SUPERINTERFACES_SET:
                return "EXPANDED_SUPERINTERFACES_SET";
            case IDelta.FIELD:
                return //$NON-NLS-1$
                "FIELD";
            case IDelta.FIELD_MOVED_UP:
                return "FIELD_MOVED_UP";
            case IDelta.FINAL_TO_NON_FINAL:
                return "FINAL_TO_NON_FINAL";
            case IDelta.FINAL_TO_NON_FINAL_NON_STATIC:
                return "FINAL_TO_NON_FINAL_NON_STATIC";
            case IDelta.FINAL_TO_NON_FINAL_STATIC_CONSTANT:
                return "FINAL_TO_NON_FINAL_STATIC_CONSTANT";
            case IDelta.FINAL_TO_NON_FINAL_STATIC_NON_CONSTANT:
                return "FINAL_TO_NON_FINAL_STATIC_NON_CONSTANT";
            case IDelta.INCREASE_ACCESS:
                return "INCREASE_ACCESS";
            case IDelta.INTERFACE_BOUND:
                return "INTERFACE_BOUND";
            case IDelta.METHOD:
            case IDelta.DEFAULT_METHOD:
                return //$NON-NLS-1$
                "METHOD";
            case IDelta.METHOD_MOVED_UP:
                return "METHOD_MOVED_UP";
            case IDelta.METHOD_WITH_DEFAULT_VALUE:
                return "METHOD_WITH_DEFAULT_VALUE";
            case IDelta.METHOD_WITHOUT_DEFAULT_VALUE:
                return "METHOD_WITHOUT_DEFAULT_VALUE";
            case IDelta.NATIVE_TO_NON_NATIVE:
                return "NATIVE_TO_NON_NATIVE";
            case IDelta.NON_ABSTRACT_TO_ABSTRACT:
                return "NON_ABSTRACT_TO_ABSTRACT";
            case IDelta.NON_FINAL_TO_FINAL:
                return "NON_FINAL_TO_FINAL";
            case IDelta.NON_NATIVE_TO_NATIVE:
                return "NON_NATIVE_TO_NATIVE";
            case IDelta.NON_STATIC_TO_STATIC:
                return "NON_STATIC_TO_STATIC";
            case IDelta.NON_SYNCHRONIZED_TO_SYNCHRONIZED:
                return "NON_SYNCHRONIZED_TO_SYNCHRONIZED";
            case IDelta.NON_TRANSIENT_TO_TRANSIENT:
                return "NON_TRANSIENT_TO_TRANSIENT";
            case IDelta.OVERRIDEN_METHOD:
                return "OVERRIDEN_METHOD";
            case IDelta.STATIC_TO_NON_STATIC:
                return "STATIC_TO_NON_STATIC";
            case IDelta.SUPERCLASS:
                return //$NON-NLS-1$
                "SUPERCLASS";
            case IDelta.SYNCHRONIZED_TO_NON_SYNCHRONIZED:
                return "SYNCHRONIZED_TO_NON_SYNCHRONIZED";
            case IDelta.TYPE_CONVERSION:
                return "TYPE_CONVERSION";
            case IDelta.TRANSIENT_TO_NON_TRANSIENT:
                return "TRANSIENT_TO_NON_TRANSIENT";
            case IDelta.TYPE:
                //$NON-NLS-1$
                return "TYPE";
            case IDelta.TYPE_ARGUMENTS:
                return "TYPE_ARGUMENTS";
            case IDelta.TYPE_MEMBER:
                return //$NON-NLS-1$
                "TYPE_MEMBER";
            case IDelta.TYPE_PARAMETER:
                return "TYPE_PARAMETER";
            case IDelta.TYPE_PARAMETER_NAME:
                return "TYPE_PARAMETER_NAME";
            case IDelta.TYPE_PARAMETERS:
                return "TYPE_PARAMETERS";
            case IDelta.TYPE_VISIBILITY:
                return "TYPE_VISIBILITY";
            case IDelta.UNCHECKED_EXCEPTION:
                return "UNCHECKED_EXCEPTION";
            case IDelta.VALUE:
                return //$NON-NLS-1$
                "VALUE";
            case IDelta.VARARGS_TO_ARRAY:
                return "VARARGS_TO_ARRAY";
            case IDelta.RESTRICTIONS:
                return "RESTRICTIONS";
            case IDelta.API_TYPE:
                return //$NON-NLS-1$
                "API_TYPE";
            case IDelta.NON_VOLATILE_TO_VOLATILE:
                return "NON_VOLATILE_TO_VOLATILE";
            case IDelta.VOLATILE_TO_NON_VOLATILE:
                return "VOLATILE_TO_NON_VOLATILE";
            case IDelta.MINOR_VERSION:
                return "MINOR_VERSION";
            case IDelta.MAJOR_VERSION:
                return "MAJOR_VERSION";
            case IDelta.API_FIELD:
                return //$NON-NLS-1$
                "API_FIELD";
            case IDelta.API_METHOD:
                return //$NON-NLS-1$
                "API_METHOD";
            case IDelta.API_CONSTRUCTOR:
                return "API_CONSTRUCTOR";
            case IDelta.API_ENUM_CONSTANT:
                return "API_ENUM_CONSTANT";
            case IDelta.API_METHOD_WITH_DEFAULT_VALUE:
                return "API_METHOD_WITH_DEFAULT_VALUE";
            case IDelta.API_METHOD_WITHOUT_DEFAULT_VALUE:
                return "API_METHOD_WITHOUT_DEFAULT_VALUE";
            case IDelta.TYPE_ARGUMENT:
                return "TYPE_ARGUMENT";
            case IDelta.SUPER_INTERFACE_WITH_METHODS:
                return "SUPER_INTERFACE_WITH_METHODS";
            case IDelta.REEXPORTED_API_TYPE:
                return "REEXPORTED_API_TYPE";
            case IDelta.REEXPORTED_TYPE:
                return "REEXPORTED_TYPE";
            case IDelta.METHOD_MOVED_DOWN:
                return "METHOD_MOVED_DOWN";
            case IDelta.DEPRECATION:
                return //$NON-NLS-1$
                "DEPRECATION";
            default:
                break;
        }
        return UNKNOWN_FLAGS;
    }

    /**
	 * Return a string that represents the kind of the given delta. Returns
	 * {@link #UNKNOWN_KIND} if the kind cannot be determined.
	 *
	 * @param delta the given delta
	 * @return a string that represents the kind of the given delta.
	 */
    public static String getDeltaKindName(IDelta delta) {
        return getDeltaKindName(delta.getKind());
    }

    /**
	 * Return a string that represents the given kind. Returns
	 * {@link #UNKNOWN_KIND} if the kind cannot be determined.
	 *
	 * @param delta the given kind
	 * @return a string that represents the given kind.
	 */
    public static String getDeltaKindName(int kind) {
        switch(kind) {
            case IDelta.ADDED:
                return //$NON-NLS-1$
                "ADDED";
            case IDelta.CHANGED:
                return //$NON-NLS-1$
                "CHANGED";
            case IDelta.REMOVED:
                return //$NON-NLS-1$
                "REMOVED";
            default:
                break;
        }
        return UNKNOWN_KIND;
    }

    /**
	 * Returns the preference key for the given element type, the given kind and
	 * the given flags.
	 *
	 * @param elementType the given element type (retrieved using
	 *            {@link IDelta#getElementType()}
	 * @param kind the given kind (retrieved using {@link IDelta#getKind()}
	 * @param flags the given flags (retrieved using {@link IDelta#getFlags()}
	 * @return the preference key for the given element type, the given kind and
	 *         the given flags.
	 */
    public static String getDeltaPrefererenceKey(int elementType, int kind, int flags) {
        StringBuffer buffer = new StringBuffer(Util.getDeltaElementType(elementType));
        buffer.append('_').append(Util.getDeltaKindName(kind));
        if (flags != -1) {
            buffer.append('_');
            switch(flags) {
                case IDelta.API_FIELD:
                    buffer.append(Util.getDeltaFlagsName(IDelta.FIELD));
                    break;
                case IDelta.API_ENUM_CONSTANT:
                    buffer.append(Util.getDeltaFlagsName(IDelta.ENUM_CONSTANT));
                    break;
                case IDelta.API_CONSTRUCTOR:
                    buffer.append(Util.getDeltaFlagsName(IDelta.CONSTRUCTOR));
                    break;
                case IDelta.API_METHOD:
                    buffer.append(Util.getDeltaFlagsName(IDelta.METHOD));
                    break;
                case IDelta.API_METHOD_WITH_DEFAULT_VALUE:
                    if (kind == IDelta.REMOVED) {
                        buffer.append(Util.getDeltaFlagsName(IDelta.METHOD));
                    } else {
                        buffer.append(Util.getDeltaFlagsName(IDelta.METHOD_WITH_DEFAULT_VALUE));
                    }
                    break;
                case IDelta.API_METHOD_WITHOUT_DEFAULT_VALUE:
                    if (kind == IDelta.REMOVED) {
                        buffer.append(Util.getDeltaFlagsName(IDelta.METHOD));
                    } else {
                        buffer.append(Util.getDeltaFlagsName(IDelta.METHOD_WITHOUT_DEFAULT_VALUE));
                    }
                    break;
                case IDelta.METHOD_WITH_DEFAULT_VALUE:
                    if (kind == IDelta.REMOVED) {
                        buffer.append(Util.getDeltaFlagsName(IDelta.METHOD));
                    } else {
                        buffer.append(Util.getDeltaFlagsName(IDelta.METHOD_WITH_DEFAULT_VALUE));
                    }
                    break;
                case IDelta.METHOD_WITHOUT_DEFAULT_VALUE:
                    if (kind == IDelta.REMOVED) {
                        buffer.append(Util.getDeltaFlagsName(IDelta.METHOD));
                    } else {
                        buffer.append(Util.getDeltaFlagsName(IDelta.METHOD_WITHOUT_DEFAULT_VALUE));
                    }
                    break;
                default:
                    buffer.append(Util.getDeltaFlagsName(flags));
            }
        }
        return String.valueOf(buffer);
    }

    /**
	 * Returns the details of the API delta as a string
	 *
	 * @param delta
	 * @return the details of the delta as a string
	 */
    public static String getDetail(IDelta delta) {
        StringBuffer buffer = new StringBuffer();
        switch(delta.getElementType()) {
            case IDelta.CLASS_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "class");
                break;
            case IDelta.ANNOTATION_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "annotation");
                break;
            case IDelta.INTERFACE_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "interface");
                break;
            case IDelta.API_COMPONENT_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "api component");
                break;
            case IDelta.API_BASELINE_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "api baseline");
                break;
            case IDelta.METHOD_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "method");
                break;
            case IDelta.CONSTRUCTOR_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "constructor");
                break;
            case IDelta.ENUM_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "enum");
                break;
            case IDelta.FIELD_ELEMENT_TYPE:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "field");
                break;
            default:
                break;
        }
        buffer.append(' ');
        switch(delta.getKind()) {
            case IDelta.ADDED:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "added");
                break;
            case IDelta.REMOVED:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "removed");
                break;
            case IDelta.CHANGED:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "changed");
                break;
            default:
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "unknown kind");
                break;
        }
        //$NON-NLS-1$
        buffer.append(' ').append(getDeltaFlagsName(delta.getFlags())).append(' ').append(delta.getTypeName()).append("#").append(delta.getKey());
        return String.valueOf(buffer);
    }

    /**
	 * Returns the {@link IDocument} for the specified {@link ICompilationUnit}
	 *
	 * @param cu
	 * @return the {@link IDocument} for the specified {@link ICompilationUnit}
	 * @throws CoreException
	 */
    public static IDocument getDocument(ICompilationUnit cu) throws CoreException {
        if (cu.getOwner() == null) {
            IFile file = (IFile) cu.getResource();
            if (file.exists()) {
                ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
                IPath path = cu.getPath();
                bufferManager.connect(path, LocationKind.IFILE, new NullProgressMonitor());
                try {
                    return bufferManager.getTextFileBuffer(path, LocationKind.IFILE).getDocument();
                } finally {
                    bufferManager.disconnect(path, LocationKind.IFILE, null);
                }
            }
        }
        return new org.eclipse.jface.text.Document(cu.getSource());
    }

    /**
	 * Returns the OSGi profile properties corresponding to the given execution
	 * environment id, or <code>null</code> if none.
	 *
	 * @param eeId OSGi profile identifier
	 *
	 * @return the corresponding properties or <code>null</code> if none
	 */
    public static Properties getEEProfile(String eeId) {
        //$NON-NLS-1$
        String profileName = eeId + ".profile";
        //$NON-NLS-1$
        InputStream stream = Util.class.getResourceAsStream("profiles/" + profileName);
        if (stream != null) {
            try {
                Properties profile = new Properties();
                profile.load(stream);
                return profile;
            } catch (IOException e) {
                ApiPlugin.log(e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    ApiPlugin.log(e);
                }
            }
        }
        return null;
    }

    /**
	 * Returns the number of fragments for the given version value, -1 if the
	 * format is unknown. The version is formed like: [optional plug-in name]
	 * major.minor.micro.qualifier.
	 *
	 * @param version the given version value
	 * @return the number of fragments for the given version value or -1 if the
	 *         format is unknown
	 * @throws IllegalArgumentException if version is null
	 */
    public static final int getFragmentNumber(String version) {
        if (version == null) {
            //$NON-NLS-1$
            throw new IllegalArgumentException("The given version should not be null");
        }
        int index = version.indexOf(' ');
        char[] charArray = version.toCharArray();
        int length = charArray.length;
        if (index + 1 >= length) {
            return -1;
        }
        int counter = 1;
        for (int i = index + 1; i < length; i++) {
            switch(charArray[i]) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    continue;
                case '.':
                    counter++;
                    break;
                default:
                    return -1;
            }
        }
        return counter;
    }

    public static IMember getIMember(IDelta delta, IJavaProject javaProject) {
        String typeName = delta.getTypeName();
        if (typeName == null) {
            return null;
        }
        IType type = null;
        try {
            type = javaProject.findType(typeName.replace('$', '.'));
        } catch (JavaModelException e) {
        }
        if (type == null) {
            return null;
        }
        String key = delta.getKey();
        switch(delta.getElementType()) {
            case IDelta.FIELD_ELEMENT_TYPE:
                {
                    IField field = type.getField(key);
                    if (field.exists()) {
                        return field;
                    }
                }
                break;
            case IDelta.CLASS_ELEMENT_TYPE:
            case IDelta.ANNOTATION_ELEMENT_TYPE:
            case IDelta.INTERFACE_ELEMENT_TYPE:
            case IDelta.ENUM_ELEMENT_TYPE:
                // we report the marker on the type
                switch(delta.getKind()) {
                    case IDelta.ADDED:
                        switch(delta.getFlags()) {
                            case IDelta.FIELD:
                            case IDelta.ENUM_CONSTANT:
                                IField field = type.getField(key);
                                if (field.exists()) {
                                    return field;
                                }
                                break;
                            case IDelta.METHOD_WITH_DEFAULT_VALUE:
                            case IDelta.METHOD_WITHOUT_DEFAULT_VALUE:
                            case IDelta.METHOD:
                            case IDelta.DEFAULT_METHOD:
                            case IDelta.CONSTRUCTOR:
                                return getMethod(type, key);
                            case IDelta.TYPE_MEMBER:
                                IType type2 = type.getType(key);
                                if (type2.exists()) {
                                    return type2;
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case IDelta.REMOVED:
                        switch(delta.getFlags()) {
                            case IDelta.API_FIELD:
                            case IDelta.API_ENUM_CONSTANT:
                                IField field = type.getField(key);
                                if (field.exists()) {
                                    return field;
                                }
                                break;
                            case IDelta.API_METHOD_WITH_DEFAULT_VALUE:
                            case IDelta.API_METHOD_WITHOUT_DEFAULT_VALUE:
                            case IDelta.API_METHOD:
                            case IDelta.API_CONSTRUCTOR:
                                return getMethod(type, key);
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
                return type;
            case IDelta.METHOD_ELEMENT_TYPE:
            case IDelta.CONSTRUCTOR_ELEMENT_TYPE:
                {
                    return getMethod(type, key);
                }
            case IDelta.API_COMPONENT_ELEMENT_TYPE:
                return type;
            default:
                break;
        }
        return null;
    }

    private static IMember getMethod(IType type, String key) {
        boolean isGeneric = false;
        int indexOfTypeVariable = key.indexOf('<');
        int index = 0;
        if (indexOfTypeVariable == -1) {
            int indexOfParen = key.indexOf('(');
            if (indexOfParen == -1) {
                return null;
            }
            index = indexOfParen;
        } else {
            int indexOfParen = key.indexOf('(');
            if (indexOfParen == -1) {
                return null;
            }
            if (indexOfParen < indexOfTypeVariable) {
                index = indexOfParen;
            } else {
                index = indexOfTypeVariable;
                isGeneric = true;
            }
        }
        String selector = key.substring(0, index);
        String descriptor = key.substring(index, key.length());
        IMethod method = null;
        String signature = descriptor.replace('/', '.');
        String[] parameterTypes = null;
        if (isGeneric) {
            // remove all type variables first
            signature = signature.substring(signature.indexOf('('));
            parameterTypes = Signature.getParameterTypes(signature);
        } else {
            parameterTypes = Signature.getParameterTypes(signature);
        }
        try {
            method = type.getMethod(selector, parameterTypes);
        } catch (IllegalArgumentException e) {
            ApiPlugin.log(e);
        }
        if (method == null) {
            return null;
        }
        if (method.exists()) {
            return method;
        }
        // default constructor or a constructor in inner type
        if (selector.equals(type.getElementName())) {
            if (parameterTypes.length == 0) {
                return null;
            }
            // Perhaps a constructor on an inner type?
            IJavaElement parent = type.getParent();
            if (parent instanceof IType) {
                String parentTypeSig = Signature.createTypeSignature(((IType) parent).getFullyQualifiedName(), true);
                if (Signatures.matches(parentTypeSig, parameterTypes[0])) {
                    IMethod constructor = type.getMethod(selector, Arrays.copyOfRange(parameterTypes, 1, parameterTypes.length));
                    try {
                        if (constructor.exists() && constructor.isConstructor()) {
                            return constructor;
                        }
                        String contructorSig = Signature.createMethodSignature(Arrays.copyOfRange(parameterTypes, 1, parameterTypes.length), Signature.getReturnType(signature));
                        IMethod[] methods = type.findMethods(constructor);
                        if (methods != null) {
                            if (methods.length == 1 && methods[0].isConstructor()) {
                                return methods[0];
                            }
                            // different package names
                            for (IMethod m : methods) {
                                try {
                                    if (m.isConstructor() && m.getNumberOfParameters() == parameterTypes.length - 1 && Signatures.matchesSignatures(generateBinarySignature(m), contructorSig)) {
                                        return m;
                                    }
                                } catch (JavaModelException e) {
                                }
                            }
                        }
                    } catch (JavaModelException e) {
                    }
                }
            }
        }
        // Let JDT have a go
        IMethod[] methods = type.findMethods(method);
        if (methods != null && methods.length == 1) {
            /* exact match found */
            return methods[0];
        }
        if (methods == null || methods.length == 0) {
            /* no methods found: may be due to type erasure */
            try {
                methods = type.getMethods();
            } catch (JavaModelException e) {
                ApiPlugin.log(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, NLS.bind("Unable to retrieve methods for {0}", type.getFullyQualifiedName()), e));
                return null;
            }
        }
        /*
		 * findMethods() checks simple type names, so it's possible to have
		 * multiple matches with different package names. Or we may need to
		 * check with type erasure.
		 */
        for (IMethod m : methods) {
            try {
                if (!m.getElementName().equals(selector) || m.getNumberOfParameters() != parameterTypes.length) {
                    continue;
                }
                if (Signatures.matchesSignatures(generateBinarySignature(m), signature)) {
                    return m;
                }
            } catch (JavaModelException e) {
            }
        }
        /*
		 * Unclear what circumstances that this could happen, so provide more
		 * information to help understand why
		 */
        StringBuilder sb = new StringBuilder();
        for (IMethod m : methods) {
            sb.append('\n').append(m.getHandleIdentifier());
        }
        ApiPlugin.log(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, NLS.bind(UtilMessages.Util_6, new String[] { selector, descriptor }) + sb.toString()));
        // do not default to the enclosing type - see bug 224713
        return null;
    }

    /**
	 * Generate the binary signature for the provided method. This is the
	 * type-erased signature written out to the .class file.
	 *
	 * @param method the method
	 * @return the method signature as would be encoded in a .class file
	 * @throws JavaModelException
	 */
    private static String generateBinarySignature(IMethod method) throws JavaModelException {
        ITypeParameter[] typeTPs = method.getDeclaringType().getTypeParameters();
        ITypeParameter[] methodTPs = method.getTypeParameters();
        if (typeTPs.length == 0 && methodTPs.length == 0) {
            return method.getSignature();
        }
        Map<String, String> lookup = new HashMap();
        Stream.concat(Stream.of(typeTPs), Stream.of(methodTPs)).forEach( tp -> {
            try {
                String sigs[] = tp.getBoundsSignatures();
                lookup.put(tp.getElementName(), sigs.length == 1 ? sigs[0] : "Ljava.lang.Object;");
            } catch (JavaModelException e) {
            }
        });
        String[] parameterTypes = Stream.of(method.getParameterTypes()).map( p -> expandParameterType(p, lookup)).toArray(String[]::<>new);
        return Signature.createMethodSignature(parameterTypes, expandParameterType(method.getReturnType(), lookup));
    }

    /**
	 * Rewrite a parameter type signature with type erasure and using the
	 * parameterized type bounds lookup table. For example:
	 * 
	 * <pre>
	 *     expand("QList&lt;QE;&gt;;", {"E" &rarr; "Ljava.lang.Object;"}) = "QList;"
	 *     expand("QE;", {"E" &rarr; "Ljava.lang.Object;"}) = "Ljava.lang.Object;"
	 * </pre>
	 *
	 * @param parameterTypeSig the type signature for a parameter
	 * @param bounds the type bounds as expressed on the method and class
	 * @return a rewritten parameter type signature as would be found in the .class file
	 */
    private static String expandParameterType(String parameterTypeSig, Map<String, String> bounds) {
        String erased = Signature.getTypeErasure(parameterTypeSig);
        if (erased.charAt(0) == Signature.C_UNRESOLVED || erased.charAt(0) == Signature.C_TYPE_VARIABLE) {
            String repl = bounds.get(Signature.getSignatureSimpleName(erased));
            if (repl != null) {
                return repl;
            }
        }
        return erased;
    }

    /**
	 * Returns the given input stream as a byte array
	 *
	 * @param stream the stream to get as a byte array
	 * @param length the length to read from the stream or -1 for unknown
	 * @return the given input stream as a byte array
	 * @throws IOException
	 */
    public static byte[] getInputStreamAsByteArray(InputStream stream, int length) throws IOException {
        byte[] contents;
        if (length == -1) {
            contents = new byte[0];
            int contentsLength = 0;
            int amountRead = -1;
            do {
                // read at least 8K
                int amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE);
                // resize contents if needed
                if (contentsLength + amountRequested > contents.length) {
                    System.arraycopy(contents, 0, contents = new byte[contentsLength + amountRequested], 0, contentsLength);
                }
                // read as many bytes as possible
                amountRead = stream.read(contents, contentsLength, amountRequested);
                if (amountRead > 0) {
                    // remember length of contents
                    contentsLength += amountRead;
                }
            } while (amountRead != -1);
            // resize contents if necessary
            if (contentsLength < contents.length) {
                System.arraycopy(contents, 0, contents = new byte[contentsLength], 0, contentsLength);
            }
        } else {
            contents = new byte[length];
            int len = 0;
            int readSize = 0;
            while ((readSize != -1) && (len != length)) {
                // See PR 1FMS89U
                // We record first the read size. In this case length is the
                // actual
                // read size.
                len += readSize;
                readSize = stream.read(contents, len, length - len);
            }
        }
        return contents;
    }

    /**
	 * Returns the given input stream's contents as a character array. If a
	 * length is specified (i.e. if length != -1), this represents the number of
	 * bytes in the stream. Note the specified stream is not closed in this
	 * method
	 *
	 * @param stream the stream to get convert to the char array
	 * @param length the length of the input stream, or -1 if unknown
	 * @param encoding the encoding to use when reading the stream
	 * @return the given input stream's contents as a character array.
	 * @throws IOException if a problem occurred reading the stream.
	 */
    public static char[] getInputStreamAsCharArray(InputStream stream, int length, String encoding) throws IOException {
        Charset charset = null;
        try {
            charset = Charset.forName(encoding);
        } catch (IllegalCharsetNameException e) {
            System.err.println("Illegal charset name : " + encoding);
            return null;
        } catch (UnsupportedCharsetException e) {
            System.err.println("Unsupported charset : " + encoding);
            return null;
        }
        CharsetDecoder charsetDecoder = charset.newDecoder();
        charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        byte[] contents = getInputStreamAsByteArray(stream, length);
        ByteBuffer byteBuffer = ByteBuffer.allocate(contents.length);
        byteBuffer.put(contents);
        byteBuffer.flip();
        CharBuffer charBuffer = charsetDecoder.decode(byteBuffer);
        // ensure pay-load starting at 0
        charBuffer.compact();
        char[] array = charBuffer.array();
        int lengthToBe = charBuffer.position();
        if (array.length > lengthToBe) {
            System.arraycopy(array, 0, (array = new char[lengthToBe]), 0, lengthToBe);
        }
        return array;
    }

    /**
	 * Tries to find the 'MANIFEST.MF' file with in the given project in the
	 * 'META-INF folder'.
	 *
	 * @param currentProject
	 * @return a handle to the manifest file or <code>null</code> if not found
	 */
    public static IResource getManifestFile(IProject currentProject) {
        //$NON-NLS-1$
        return currentProject.findMember("META-INF/MANIFEST.MF");
    }

    /**
	 * Returns if the given {@link IMarker} is representing an
	 * {@link org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem}
	 * or not
	 *
	 * @param marker the marker to check
	 * @return true if the marker is for an
	 *         {@link org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem}
	 *         false otherwise
	 * @throws CoreException
	 */
    public static boolean isApiProblemMarker(IMarker marker) {
        return marker.getAttribute(IApiMarkerConstants.API_MARKER_ATTR_ID, -1) > 0;
    }

    /**
	 * Returns a reference type for the given fully qualified type name.
	 *
	 * @param fullyQualifiedName type name
	 * @return reference type
	 */
    public static IReferenceTypeDescriptor getType(String fullyQualifiedName) {
        int index = fullyQualifiedName.lastIndexOf('.');
        String pkg = index == -1 ? DEFAULT_PACKAGE_NAME : fullyQualifiedName.substring(0, index);
        String type = index == -1 ? fullyQualifiedName : fullyQualifiedName.substring(index + 1);
        return Factory.packageDescriptor(pkg).getType(type);
    }

    /**
	 * Returns if the given project is API enabled
	 *
	 * @param project the given project
	 * @return true if the project is API enabled, false otherwise
	 */
    public static boolean isApiProject(IProject project) {
        try {
            return project.hasNature(ApiPlugin.NATURE_ID);
        } catch (CoreException e) {
            return false;
        }
    }

    /**
	 * Returns if the given project is a java project
	 *
	 * @param project the given project
	 * @return <code>true</code> if the project is a java project,
	 *         <code>false</code> otherwise
	 */
    public static boolean isJavaProject(IProject project) {
        try {
            return project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            return false;
        }
    }

    /**
	 * Returns if the given project is API enabled
	 *
	 * @param project the given project
	 * @return <code>true</code> if the project is API enabled,
	 *         <code>false</code> otherwise
	 */
    public static boolean isApiProject(IJavaProject project) {
        if (project != null) {
            return isApiProject(project.getProject());
        }
        return false;
    }

    /**
	 * Returns if the given {@link IApiComponent} is a valid
	 * {@link IApiComponent}
	 *
	 * @param apiComponent the given component
	 * @return true if the given {@link IApiComponent} is valid, false otherwise
	 */
    public static boolean isApiToolsComponent(IApiComponent apiComponent) {
        File file = new File(apiComponent.getLocation());
        if (file.exists()) {
            if (file.isDirectory()) {
                // directory binary bundle
                File apiDescription = new File(file, IApiCoreConstants.API_DESCRIPTION_XML_NAME);
                return apiDescription.exists();
            }
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(file);
                return zipFile.getEntry(IApiCoreConstants.API_DESCRIPTION_XML_NAME) != null;
            } catch (ZipException e) {
            } catch (IOException e) {
            } finally {
                try {
                    if (zipFile != null) {
                        zipFile.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    /**
	 * Returns if the specified file name is an archive name. A name is
	 * considered to be an archive name if it ends with either '.zip' or '.jar'
	 *
	 * @param fileName
	 * @return true if the file name is an archive name false otherwise
	 */
    public static boolean isArchive(String fileName) {
        return isZipJarFile(fileName) || isTGZFile(fileName);
    }

    /**
	 * Returns if the given file name represents a 'standard' archive, where the
	 * name has an extension of *.zip or *.jar
	 *
	 * @param fileName
	 * @return true if the given file name is that of a 'standard' archive,
	 *         false otherwise
	 */
    public static boolean isZipJarFile(String fileName) {
        String normalizedFileName = fileName.toLowerCase();
        return normalizedFileName.endsWith(DOT_ZIP) || normalizedFileName.endsWith(DOT_JAR);
    }

    /**
	 * Returns if the given file name represents a G-zip file name, where the
	 * name has an extension of *.tar.gz or *.tgz
	 *
	 * @param fileName
	 * @return true if the given file name is that of a G-zip archive, false
	 *         otherwise
	 */
    public static boolean isTGZFile(String fileName) {
        String normalizedFileName = fileName.toLowerCase();
        return normalizedFileName.endsWith(DOT_TAR_GZ) || normalizedFileName.endsWith(DOT_TGZ);
    }

    /**
	 * Returns if the flags are for a class
	 *
	 * @param accessFlags the given access flags
	 * @return
	 */
    public static boolean isClass(int accessFlags) {
        return (accessFlags & (Opcodes.ACC_ENUM | Opcodes.ACC_ANNOTATION | Opcodes.ACC_INTERFACE)) == 0;
    }

    /**
	 * Returns if the specified file name is for a class file. A name is
	 * considered to be a class file if it ends in '.class'
	 *
	 * @param fileName
	 * @return true if the name is for a class file false otherwise
	 */
    public static boolean isClassFile(String fileName) {
        return fileName.toLowerCase().endsWith(DOT_CLASS_SUFFIX);
    }

    public static boolean isDefault(int accessFlags) {
        // none of the private, protected or public bit is set
        return (accessFlags & (Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC)) == 0;
    }

    public static final boolean isDifferentVersion(String versionToBeChecked, String referenceVersion) {
        SinceTagVersion sinceTagVersion1 = null;
        SinceTagVersion sinceTagVersion2 = null;
        try {
            sinceTagVersion1 = new SinceTagVersion(versionToBeChecked);
            sinceTagVersion2 = new SinceTagVersion(referenceVersion);
        } catch (IllegalArgumentException e) {
            return false;
        }
        Version version1 = sinceTagVersion1.getVersion();
        Version version2 = sinceTagVersion2.getVersion();
        if (version1.getMajor() != version2.getMajor()) {
            return true;
        }
        if (version1.getMinor() != version2.getMinor()) {
            return true;
        }
        if (version1.getMicro() != version2.getMicro()) {
            return true;
        }
        return false;
    }

    /**
	 * Returns if the specified file name is for a java source file. A name is
	 * considered to be a java source file if it ends in '.java'
	 *
	 * @param fileName
	 * @return true if the name is for a java source file, false otherwise
	 */
    public static boolean isJavaFileName(String fileName) {
        return fileName.toLowerCase().endsWith(DOT_JAVA_SUFFIX);
    }

    /**
	 * Returns if the given name is {@link java.lang.Object}
	 *
	 * @param name
	 * @return true if the name is java.lang.Object, false otherwise
	 */
    public static boolean isJavaLangObject(String name) {
        return name != null && name.equals(JAVA_LANG_OBJECT);
    }

    /**
	 * Return if the name is {@link java.lang.RuntimeException}
	 *
	 * @param name
	 * @return true if the name is java.lang.RuntimeException, false otherwise
	 */
    public static boolean isJavaLangRuntimeException(String name) {
        return name != null && name.equals(JAVA_LANG_RUNTIMEEXCEPTION);
    }

    public static boolean isVisible(int modifiers) {
        return Flags.isProtected(modifiers) || Flags.isPublic(modifiers);
    }

    public static boolean isBinaryProject(IProject project) {
        return org.eclipse.pde.internal.core.WorkspaceModelManager.isBinaryProject(project);
    }

    /**
	 * Returns a new XML document.
	 *
	 * @return document
	 * @throws CoreException if unable to create a new document
	 */
    public static Document newDocument() throws CoreException {
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dfactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            abort("Unable to create new XML document.", e);
        }
        Document doc = docBuilder.newDocument();
        return doc;
    }

    /**
	 * Parses the given string representing an XML document, returning its root
	 * element.
	 *
	 * @param document XML document as a string
	 * @return the document's root element
	 * @throws CoreException if unable to parse the document
	 */
    public static Element parseDocument(String document) throws CoreException {
        Element root = null;
        InputStream stream = null;
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            parser.setErrorHandler(new DefaultHandler());
            stream = new ByteArrayInputStream(document.getBytes(IApiCoreConstants.UTF_8));
            root = parser.parse(stream).getDocumentElement();
        } catch (ParserConfigurationException e) {
            abort("Unable to parse XML document.", e);
        } catch (FactoryConfigurationError e) {
            abort("Unable to parse XML document.", e);
        } catch (SAXException e) {
            abort("Unable to parse XML document.", e);
        } catch (IOException e) {
            abort("Unable to parse XML document.", e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                abort("Unable to parse XML document.", e);
            }
        }
        return root;
    }

    /**
	 * Save the given contents into the given file. The file parent folder must
	 * exist.
	 *
	 * @param file the given file target
	 * @param contents the given contents
	 * @throws IOException if an IOException occurs while saving the file
	 */
    public static void saveFile(File file, String contents) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(contents);
            writer.flush();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
	 * Returns the contents of the given file as a string, or <code>null</code>
	 *
	 * @param file the file to get the contents for
	 * @return the contents of the file as a {@link String} or <code>null</code>
	 */
    public static String getFileContentAsString(File file) {
        String contents = null;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            char[] array = getInputStreamAsCharArray(stream, -1, IApiCoreConstants.UTF_8);
            contents = new String(array);
        } catch (IOException ioe) {
            ApiPlugin.log(ioe);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return contents;
    }

    /**
	 * Returns the given string as an {@link InputStream}. It is up to the
	 * caller to close the new stream.
	 *
	 * @param string the string to convert
	 * @return the {@link InputStream} for the given string
	 */
    public static InputStream getInputStreamFromString(String string) {
        try {
            return new ByteArrayInputStream(string.getBytes(IApiCoreConstants.UTF_8));
        } catch (UnsupportedEncodingException uee) {
            ApiPlugin.log(uee);
        }
        return null;
    }

    /**
	 * Serializes the given XML document into a UTF-8 string.
	 *
	 * @param document XML document to serialize
	 * @return a string representing the given document
	 * @throws CoreException if unable to serialize the document
	 */
    public static String serializeDocument(Document document) throws CoreException {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //$NON-NLS-1$ //$NON-NLS-2$
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(document);
            StreamResult outputTarget = new StreamResult(s);
            transformer.transform(source, outputTarget);
            return s.toString(IApiCoreConstants.UTF_8);
        } catch (TransformerException e) {
            abort("Unable to serialize XML document.", e);
        } catch (IOException e) {
            abort("Unable to serialize XML document.", e);
        }
        return null;
    }

    /**
	 * Unzip the contents of the given zip in the given directory (create it if
	 * it doesn't exist)
	 */
    public static void unzip(String zipPath, String destDirPath) throws IOException {
        InputStream zipIn = new FileInputStream(zipPath);
        byte[] buf = new byte[8192];
        File destDir = new File(destDirPath);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipIn));
        BufferedOutputStream outputStream = null;
        try {
            ZipEntry zEntry;
            while ((zEntry = zis.getNextEntry()) != null) {
                // if it is empty directory, create it
                if (zEntry.isDirectory()) {
                    new File(destDir, zEntry.getName()).mkdirs();
                    continue;
                }
                // if it is a file, extract it
                String filePath = zEntry.getName();
                int lastSeparator = //$NON-NLS-1$
                filePath.lastIndexOf(//$NON-NLS-1$
                "/");
                //$NON-NLS-1$
                String //$NON-NLS-1$
                fileDir = "";
                if (lastSeparator >= 0) {
                    fileDir = filePath.substring(0, lastSeparator);
                }
                // create directory for a file
                new File(destDir, fileDir).mkdirs();
                // write file
                File outFile = new File(destDir, filePath);
                outputStream = new BufferedOutputStream(new FileOutputStream(outFile));
                int n = 0;
                while ((n = zis.read(buf)) >= 0) {
                    outputStream.write(buf, 0, n);
                }
                outputStream.close();
            }
        } catch (IOException ioe) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ioe2) {
                }
            }
        } finally {
            try {
                zipIn.close();
                zis.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
	 * Unzip the contents of the given zip in the given directory (create it if
	 * it doesn't exist)
	 */
    public static void guntar(String zipPath, String destDirPath) throws TarException, IOException {
        TarFile tarFile = new TarFile(zipPath);
        Enumeration<?> entries = tarFile.entries();
        byte[] buf = new byte[8192];
        for (; entries.hasMoreElements(); ) {
            TarEntry zEntry;
            while ((zEntry = (TarEntry) entries.nextElement()) != null) {
                // if it is empty directory, create it
                if (zEntry.getFileType() == TarEntry.DIRECTORY) {
                    new File(destDirPath, zEntry.getName()).mkdirs();
                    continue;
                }
                // if it is a file, extract it
                String filePath = zEntry.getName();
                int lastSeparator = //$NON-NLS-1$
                filePath.lastIndexOf(//$NON-NLS-1$
                "/");
                //$NON-NLS-1$
                String //$NON-NLS-1$
                fileDir = "";
                if (lastSeparator >= 0) {
                    fileDir = filePath.substring(0, lastSeparator);
                }
                // create directory for a file
                new File(destDirPath, fileDir).mkdirs();
                // write file
                File outFile = new File(destDirPath, filePath);
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile));
                int n = 0;
                InputStream inputStream = tarFile.getInputStream(zEntry);
                BufferedInputStream stream = new BufferedInputStream(inputStream);
                while ((n = stream.read(buf)) >= 0) {
                    outputStream.write(buf, 0, n);
                }
                outputStream.close();
                stream.close();
            }
        }
    }

    /**
	 * Gets the .ee file supplied to run tests based on system property.
	 *
	 * @return
	 */
    public static File getEEDescriptionFile() {
        // generate a fake 1.6 ee file
        File fakeEEFile = null;
        PrintWriter writer = null;
        try {
            //$NON-NLS-1$ //$NON-NLS-2$
            fakeEEFile = createTempFile("eefile", ".ee");
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fakeEEFile)));
            //$NON-NLS-1$
            writer.print("-Djava.home=");
            //$NON-NLS-1$
            writer.println(System.getProperty("java.home"));
            //$NON-NLS-1$
            writer.print("-Dee.bootclasspath=");
            writer.println(getJavaClassLibsAsString());
            //$NON-NLS-1$
            writer.println("-Dee.language.level=1.6");
            //$NON-NLS-1$
            writer.println("-Dee.class.library.level=JavaSE-1.6");
            writer.flush();
        } catch (IOException e) {
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return fakeEEFile;
    }

    /**
	 * Creates a new file in the users' <code>temp</code> directory
	 *
	 * @param prefix
	 * @param suffix
	 * @return a new temp file
	 * @throws IOException
	 * @since 1.1
	 */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();
        FileManager.getManager().recordTempFileRoot(file.getCanonicalPath());
        return file;
    }

    /**
	 * @return a string representation of all of the libraries from the bootpath
	 *         of the current default system VM.
	 */
    public static String getJavaClassLibsAsString() {
        String[] libs = Util.getJavaClassLibs();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, max = libs.length; i < max; i++) {
            if (i > 0) {
                buffer.append(File.pathSeparatorChar);
            }
            buffer.append(libs[i]);
        }
        return String.valueOf(buffer);
    }

    /**
	 * @return an array of the library names from the bootpath of the current
	 *         default system VM
	 */
    public static String[] getJavaClassLibs() {
        // check bootclasspath properties for Sun, JRockit and Harmony VMs
        //$NON-NLS-1$
        String bootclasspathProperty = System.getProperty("sun.boot.class.path");
        if ((bootclasspathProperty == null) || (bootclasspathProperty.length() == 0)) {
            // IBM J9 VMs
            //$NON-NLS-1$
            bootclasspathProperty = System.getProperty("vm.boot.class.path");
            if ((bootclasspathProperty == null) || (bootclasspathProperty.length() == 0)) {
                // Harmony using IBM VME
                bootclasspathProperty = //$NON-NLS-1$
                System.getProperty(//$NON-NLS-1$
                "org.apache.harmony.boot.class.path");
            }
        }
        String[] jars = null;
        if ((bootclasspathProperty != null) && (bootclasspathProperty.length() != 0)) {
            StringTokenizer tokenizer = new StringTokenizer(bootclasspathProperty, File.pathSeparator);
            final int size = tokenizer.countTokens();
            jars = new String[size];
            int i = 0;
            while (tokenizer.hasMoreTokens()) {
                final String fileName = toNativePath(tokenizer.nextToken());
                if (new File(fileName).exists()) {
                    jars[i] = fileName;
                    i++;
                }
            }
            if (size != i) {
                // resize
                System.arraycopy(jars, 0, (jars = new String[i]), 0, i);
            }
        } else {
            //$NON-NLS-1$
            String jreDir = System.getProperty("java.home");
            //$NON-NLS-1$
            final String osName = System.getProperty("os.name");
            if (jreDir == null) {
                return new String[] {};
            }
            if (//$NON-NLS-1$
            osName.startsWith("Mac")) {
                return new String[] { toNativePath(jreDir + "/../Classes/classes.jar") };
            }
            //$NON-NLS-1$
            final String vmName = System.getProperty("java.vm.name");
            if (//$NON-NLS-1$
            "J9".equals(vmName)) {
                return new String[] { toNativePath(jreDir + "/lib/jclMax/classes.zip") };
            }
            String[] jarsNames = null;
            ArrayList<String> paths = new ArrayList();
            if (//$NON-NLS-1$
            "DRLVM".equals(vmName)) {
                FilenameFilter jarFilter = ( dir,  name) -> name.endsWith(DOT_JAR) & !//$NON-NLS-1$
                name.endsWith(//$NON-NLS-1$
                "-src.jar");
                jarsNames = //$NON-NLS-1$
                new File(jreDir + "/lib/boot/").list(//$NON-NLS-1$
                jarFilter);
                addJarEntries(//$NON-NLS-1$
                jreDir + "/lib/boot/", //$NON-NLS-1$
                jarsNames, //$NON-NLS-1$
                paths);
            } else {
                jarsNames = new String[] { //$NON-NLS-1$
                "/lib/vm.jar", //$NON-NLS-1$
                "/lib/rt.jar", //$NON-NLS-1$
                "/lib/core.jar", //$NON-NLS-1$
                "/lib/security.jar", //$NON-NLS-1$
                "/lib/xml.jar", //$NON-NLS-1$
                "/lib/graphics.jar" };
                addJarEntries(jreDir, jarsNames, paths);
            }
            jars = new String[paths.size()];
            paths.toArray(jars);
        }
        return jars;
    }

    /**
	 * Makes the given path a path using native path separators as returned by
	 * File.getPath() and trimming any extra slash.
	 */
    public static String toNativePath(String path) {
        String nativePath = path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        return nativePath.endsWith("/") || nativePath.endsWith("\\") ? nativePath.substring(0, nativePath.length() - 1) : nativePath;
    }

    private static void addJarEntries(String jreDir, String[] jarNames, ArrayList<String> paths) {
        for (String jarName : jarNames) {
            final String currentName = jreDir + jarName;
            File f = new File(currentName);
            if (f.exists()) {
                paths.add(toNativePath(currentName));
            }
        }
    }

    public static boolean delete(File file) {
        if (!file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            flushDirectoryContent(file);
        }
        file.delete();
        if (isFileDeleted(file)) {
            return true;
        }
        return waitUntilFileDeleted(file);
    }

    public static void flushDirectoryContent(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            delete(file);
        }
    }

    private static boolean waitUntilFileDeleted(File file) {
        int count = 0;
        int delay = 10;
        int maxRetry = DELETE_MAX_WAIT / delay;
        int time = 0;
        while (count < maxRetry) {
            try {
                count++;
                Thread.sleep(delay);
                time += delay;
                if (time > DELETE_MAX_TIME) {
                    DELETE_MAX_TIME = time;
                }
                if (DELETE_DEBUG) {
                    System.out.print('.');
                }
                if (file.exists()) {
                    if (file.delete()) {
                        return true;
                    }
                }
                if (isFileDeleted(file)) {
                    return true;
                }
                if (count >= 10 && delay <= 100) {
                    count = 1;
                    delay *= 10;
                    maxRetry = DELETE_MAX_WAIT / delay;
                    if ((DELETE_MAX_WAIT % delay) != 0) {
                        maxRetry++;
                    }
                }
            } catch (InterruptedException ie) {
                break;
            }
        }
        System.err.println();
        System.err.println("	!!! ERROR: " + file + " was never deleted even after having waited " + DELETE_MAX_TIME + "ms!!!");
        System.err.println();
        return false;
    }

    public static boolean isFileDeleted(File file) {
        return !file.exists() && getParentChildFile(file) == null;
    }

    /**
	 * Returns the parent's child file matching the given file or null if not
	 * found.
	 *
	 * @param file The searched file in parent
	 * @return The parent's child matching the given file or null if not found.
	 */
    private static File getParentChildFile(File file) {
        File parent = file.getParentFile();
        if (parent == null || !parent.exists()) {
            return null;
        }
        File[] files = parent.listFiles();
        if (files == null) {
            return null;
        }
        int length = files == null ? 0 : files.length;
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (files[i] == file) {
                    return files[i];
                } else if (files[i].equals(file)) {
                    return files[i];
                } else if (files[i].getPath().equals(file.getPath())) {
                    return files[i];
                }
            }
        }
        return null;
    }

    /**
	 * Turns the given array of strings into a {@link HashSet}
	 *
	 * @param values
	 * @return a new {@link HashSet} of the string array
	 */
    public static Set<String> convertAsSet(String[] values) {
        Set<String> set = new HashSet();
        if (values != null && values.length != 0) {
            for (String value : values) {
                set.add(value);
            }
        }
        return set;
    }

    /**
	 * Returns an identifier for the given API component including its version
	 * identifier (component id + '(' + major + . + minor + . + micro + ')' )
	 *
	 * @param component API component
	 * @return API component + version identifier
	 */
    public static String getDeltaComponentVersionsId(IApiComponent component) {
        StringBuffer buffer = new StringBuffer(component.getSymbolicName());
        String version = component.getVersion();
        // remove the qualifier part
        if (version != null) {
            buffer.append(Util.VERSION_SEPARATOR);
            try {
                Version version2 = new Version(version);
                buffer.append(version2.getMajor()).append('.').append(version2.getMinor()).append('.').append(version2.getMicro());
            } catch (IllegalArgumentException e) {
                buffer.append(version);
            }
            buffer.append(')');
        }
        return String.valueOf(buffer);
    }

    /**
	 * Returns an identifier for the given API component including its version
	 * identifier (component id + _ + major + _ + minor + _ + micro)
	 *
	 * @param component API component
	 * @return API component + version identifier
	 */
    public static String getComponentVersionsId(IApiComponent component) {
        StringBuffer buffer = new StringBuffer(component.getSymbolicName());
        String version = component.getVersion();
        // remove the qualifier part
        if (version != null) {
            buffer.append('_');
            try {
                Version version2 = new Version(version);
                buffer.append(version2.getMajor()).append('.').append(version2.getMinor()).append('.').append(version2.getMicro());
            } catch (IllegalArgumentException e) {
                buffer.append(version);
            }
        }
        return String.valueOf(buffer);
    }

    public static String getDescriptorName(IApiType descriptor) {
        String typeName = descriptor.getName();
        int index = typeName.lastIndexOf('$');
        if (index != -1) {
            return typeName.replace('$', '.');
        }
        return typeName;
    }

    public static String getDeltaArgumentString(IDelta delta) {
        String[] arguments = delta.getArguments();
        switch(delta.getFlags()) {
            case IDelta.TYPE_MEMBER:
            case IDelta.TYPE:
                return arguments[0];
            case IDelta.METHOD:
            case IDelta.DEFAULT_METHOD:
            case IDelta.CONSTRUCTOR:
            case IDelta.ENUM_CONSTANT:
            case IDelta.METHOD_WITH_DEFAULT_VALUE:
            case IDelta.METHOD_WITHOUT_DEFAULT_VALUE:
            case IDelta.FIELD:
                return arguments[1];
            case IDelta.INCREASE_ACCESS:
                switch(delta.getElementType()) {
                    case IDelta.FIELD_ELEMENT_TYPE:
                    case IDelta.METHOD_ELEMENT_TYPE:
                    case IDelta.CONSTRUCTOR_ELEMENT_TYPE:
                        return arguments[1];
                    default:
                        return arguments[0];
                }
            default:
                break;
        }
        return EMPTY_STRING;
    }

    /**
	 * Returns the string representation of the {@link IApiElement} type
	 *
	 * @param type
	 * @return the string of the {@link IApiElement} type
	 */
    public static String getApiElementType(int type) {
        switch(type) {
            case IApiElement.API_TYPE_CONTAINER:
                return "API_TYPE_CONTAINER";
            case IApiElement.API_TYPE_ROOT:
                return "API_TYPE_ROOT";
            case IApiElement.BASELINE:
                return //$NON-NLS-1$
                "BASELINE";
            case IApiElement.COMPONENT:
                return //$NON-NLS-1$
                "COMPONENT";
            case IApiElement.FIELD:
                return //$NON-NLS-1$
                "FIELD";
            case IApiElement.METHOD:
                return //$NON-NLS-1$
                "METHOD";
            case IApiElement.TYPE:
                //$NON-NLS-1$
                return "TYPE";
            default:
                return //$NON-NLS-1$
                "UNKNOWN";
        }
    }

    public static boolean isConstructor(String referenceMemberName) {
        return Arrays.equals(ConstantPool.Init, referenceMemberName.toCharArray());
    }

    public static boolean isManifest(IPath path) {
        return MANIFEST_PROJECT_RELATIVE_PATH.equals(path);
    }

    public static void touchCorrespondingResource(IProject project, IResource resource, String typeName) {
        if (typeName != null && typeName != FilterStore.GLOBAL) {
            if (Util.isManifest(resource.getProjectRelativePath())) {
                try {
                    IJavaProject javaProject = JavaCore.create(project);
                    IType findType = javaProject.findType(typeName);
                    if (findType != null) {
                        ICompilationUnit compilationUnit = findType.getCompilationUnit();
                        if (compilationUnit != null) {
                            IResource cuResource = compilationUnit.getResource();
                            if (cuResource != null) {
                                cuResource.touch(null);
                            }
                        }
                    }
                } catch (JavaModelException e) {
                    ApiPlugin.log(e);
                } catch (CoreException e) {
                    ApiPlugin.log(e);
                }
            } else {
                try {
                    resource.touch(null);
                } catch (CoreException e) {
                    ApiPlugin.log(e);
                }
            }
        }
    }

    public static String getTypeNameFromMarker(IMarker marker) {
        return marker.getAttribute(IApiMarkerConstants.MARKER_ATTR_PROBLEM_TYPE_NAME, null);
    }

    public static IApiComponent[] getReexportedComponents(IApiComponent component) {
        try {
            IRequiredComponentDescription[] requiredComponents = component.getRequiredComponents();
            int length = requiredComponents.length;
            if (length != 0) {
                List<IApiComponent> reexportedComponents = null;
                IApiBaseline baseline = component.getBaseline();
                for (int i = 0; i < length; i++) {
                    IRequiredComponentDescription description = requiredComponents[i];
                    if (description.isExported()) {
                        String id = description.getId();
                        IApiComponent reexportedComponent = baseline.getApiComponent(id);
                        if (reexportedComponent != null) {
                            if (reexportedComponents == null) {
                                reexportedComponents = new ArrayList();
                            }
                            reexportedComponents.add(reexportedComponent);
                        }
                    }
                }
                if (reexportedComponents == null || reexportedComponents.size() == 0) {
                    return null;
                }
                return reexportedComponents.toArray(new IApiComponent[reexportedComponents.size()]);
            }
        } catch (CoreException e) {
            ApiPlugin.log(e);
        }
        return null;
    }

    /**
	 * Returns the {@link IResource} to create markers on when building. If the
	 * {@link IType} is <code>null</code> or the type cannot be located (does
	 * not exist) than the MANIFEST.MF will be returned. <code>null</code> can
	 * be returned in the case that the project does not have a manifest file.
	 *
	 * @param project the project to look in for the {@link IResource}
	 * @param type the type we are looking for the resource for, or
	 *            <code>null</code>
	 * @return the {@link IResource} associated with the given {@link IType} or
	 *         the MANIFEST.MF file, or <code>null</code> if the project does
	 *         not have a manifest
	 */
    public static IResource getResource(IProject project, IType type) {
        try {
            if (type != null) {
                ICompilationUnit unit = type.getCompilationUnit();
                if (unit != null) {
                    IResource resource = unit.getCorrespondingResource();
                    if (resource != null && resource.exists()) {
                        return resource;
                    }
                }
            }
        } catch (JavaModelException e) {
            ApiPlugin.log(e);
        }
        return getManifestFile(project);
    }

    /**
	 * Default comparator that orders {@link IApiComponent} by their ID
	 */
    public static final Comparator<Object> componentsorter = ( o1,  o2) -> {
        if (o1 instanceof IApiComponent && o2 instanceof IApiComponent) {
            return ((IApiComponent) o1).getSymbolicName().compareTo(((IApiComponent) o2).getSymbolicName());
        }
        if (o1 instanceof SkippedComponent && o2 instanceof SkippedComponent) {
            return ((SkippedComponent) o1).getComponentId().compareTo(((SkippedComponent) o2).getComponentId());
        }
        if (o1 instanceof String && o2 instanceof String) {
            return ((String) o1).compareTo((String) o2);
        }
        return -1;
    };

    /**
	 * Initializes the exclude set with regex support. The API baseline is used
	 * to determine which bundles should be added to the list when processing
	 * regex expressions.
	 *
	 * @param location
	 * @param baseline
	 * @return the list of bundles to be excluded
	 * @throws CoreException if the location does not describe a includes file
	 *             or an IOException occurs
	 */
    public static FilteredElements initializeRegexFilterList(String location, IApiBaseline baseline, boolean debug) throws CoreException {
        FilteredElements excludedElements = new FilteredElements();
        if (location != null) {
            File file = new File(location);
            InputStream stream = null;
            char[] contents = null;
            try {
                stream = new BufferedInputStream(new FileInputStream(file));
                contents = getInputStreamAsCharArray(stream, -1, ISO_8859_1);
            } catch (FileNotFoundException e) {
                abort(NLS.bind(UtilMessages.Util_couldNotFindFilterFile, location), e);
            } catch (IOException e) {
                abort(NLS.bind(UtilMessages.Util_problemWithFilterFile, location), e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            }
            if (contents != null) {
                LineNumberReader reader = new LineNumberReader(new StringReader(new String(contents)));
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (//$NON-NLS-1$
                        line.startsWith("#") || //$NON-NLS-1$
                        line.length() == 0) {
                            continue;
                        }
                        if (line.startsWith(REGULAR_EXPRESSION_START)) {
                            if (baseline != null) {
                                Util.collectRegexIds(line, excludedElements, baseline.getApiComponents(), debug);
                            }
                        } else {
                            excludedElements.addExactMatch(line);
                        }
                    }
                } catch (IOException e) {
                    abort(NLS.bind(UtilMessages.Util_problemWithFilterFile, location), e);
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return excludedElements;
    }

    /**
	 * Collects the set of component ids that match a given regex in the exclude
	 * file
	 *
	 * @param line
	 * @param list
	 * @param components
	 */
    public static void collectRegexIds(String line, FilteredElements excludedElements, IApiComponent[] components, boolean debug) throws CoreException {
        if (line.startsWith(REGULAR_EXPRESSION_START)) {
            String componentname = line;
            // regular expression
            componentname = componentname.substring(2);
            Pattern pattern = null;
            try {
                if (debug) {
                    System.out.println(//$NON-NLS-1$
                    "Pattern to match : " + //$NON-NLS-1$
                    componentname);
                }
                pattern = Pattern.compile(componentname);
                String componentid = null;
                for (IApiComponent component : components) {
                    componentid = component.getSymbolicName();
                    if (pattern.matcher(componentid).matches()) {
                        if (debug) {
                            System.out.println(//$NON-NLS-1$
                            componentid + " matched the pattern " + //$NON-NLS-1$
                            componentname);
                        }
                        excludedElements.addPartialMatch(componentid);
                    } else if (debug) {
                        System.out.println(//$NON-NLS-1$
                        componentid + " didn't match the pattern " + //$NON-NLS-1$
                        componentname);
                    }
                }
            } catch (PatternSyntaxException e) {
                abort(NLS.bind(UtilMessages.comparison_invalidRegularExpression, componentname), e);
            }
        }
    }

    /**
	 * Default comparator that orders {@link File}s by their name
	 */
    public static final Comparator<Object> filesorter = ( o1,  o2) -> {
        if (o1 instanceof File && o2 instanceof File) {
            return ((File) o1).getName().compareTo(((File) o2).getName());
        }
        return 0;
    };

    /**
	 * Returns true if the given {@link IApiType} is API or not, where API is
	 * defined as having API visibility in an API description and having either
	 * the public of protected Java flag set
	 *
	 * @param visibility
	 * @param typeDescriptor
	 * @return true if the given type is API, false otherwise
	 */
    public static boolean isAPI(int visibility, IApiType typeDescriptor) {
        int access = typeDescriptor.getModifiers();
        return VisibilityModifiers.isAPI(visibility) && (Flags.isPublic(access) || Flags.isProtected(access));
    }

    /**
	 * Simple method to walk an array and call <code>toString()</code> on each
	 * of the entries. Does not descend into sub-collections.
	 *
	 * @param array the array
	 * @return the comma-separated string representation of the the array
	 * @since 1.0.3
	 */
    public static String deepToString(Object[] array) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i].toString());
            if (i < array.length - 1) {
                buffer.append(',');
            }
        }
        return buffer.toString();
    }
}
