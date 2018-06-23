/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.builders;

import java.util.*;
import java.util.Map.Entry;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDECoreMessages;
import org.eclipse.pde.internal.core.project.PDEProject;

public class SourceEntryErrorReporter extends BuildErrorReporter {

    private static final String DEF_OUTPUT_ENTRY = PROPERTY_OUTPUT_PREFIX + '.';

    public  SourceEntryErrorReporter(IFile file, IBuild model) {
        super(file);
        fBuild = model;
    }

    class ProjectFolder {

        IPath fPath;

        String fToken;

        ArrayList<String> fLibs = new ArrayList(1);

        String dupeLibName = null;

        public  ProjectFolder(IPath path) {
            fPath = path;
        }

        public IPath getPath() {
            return fPath;
        }

        void setToken(String token) {
            fToken = token;
        }

        public String getToken() {
            if (fToken == null) {
                return fPath.toString();
            }
            return fToken;
        }

        public void addLib(String libName) {
            if (fLibs.contains(libName)) {
                dupeLibName = libName;
            } else
                fLibs.add(libName);
        }

        public ArrayList<String> getLibs() {
            return fLibs;
        }

        public String getDupeLibName() {
            return dupeLibName;
        }
    }

    class SourceFolder extends ProjectFolder {

        OutputFolder fOutputFolder;

        /**
		 * Constructs a source folder with the given project relative path.
		 *
		 * @param path source folder path
		 * @param outputFolder associated output folder
		 */
        public  SourceFolder(IPath path, OutputFolder outputFolder) {
            super(path);
            fOutputFolder = outputFolder;
        }

        public OutputFolder getOutputLocation() {
            return fOutputFolder;
        }
    }

    class OutputFolder extends ProjectFolder {

        private ArrayList<SourceFolder> fSourceFolders = new ArrayList();

        /**
		 * True when there is no corresponding source - i.e. a class file folder or library
		 */
        private boolean fIsLibrary = false;

        /**
		 * Creates an output folder with the given relative path (relative to the project).
		 *
		 * @param path project relative path
		 */
        public  OutputFolder(IPath path) {
            super(path);
        }

        /**
		 * Creates an output folder with the given relative path (relative to the project).
		 *
		 * @param path project relative path
		 * @param isLibrary whether this output folder is a binary location that has no corresponding
		 *  source folder
		 */
        public  OutputFolder(IPath path, boolean isLibrary) {
            this(path);
            fIsLibrary = isLibrary;
        }

        public void addSourceFolder(SourceFolder sourceFolder) {
            if (!fSourceFolders.contains(sourceFolder))
                fSourceFolders.add(sourceFolder);
        }

        public boolean isLibrary() {
            return fIsLibrary;
        }

        public ArrayList<SourceFolder> getSourceFolders() {
            return fSourceFolders;
        }
    }

    /**
	 * Represents a default or custom encoding property for a resource
	 * within a library.
	 */
    class EncodingEntry {

        private String fEncoding;

        private IResource fResource;

        /**
		 * Constructs an encoding entry for the given resource.
		 *
		 * @param resource resource
		 * @param encoding the encoding identifier
		 */
         EncodingEntry(IResource resource, String encoding) {
            fEncoding = encoding;
            fResource = resource;
        }

        /**
		 * Returns the explicit encoding for this entry.
		 *
		 * @return explicit encoding
		 */
        public String getEncoding() {
            return fEncoding;
        }

        /**
		 * Returns the resource this encoding is associated with.
		 *
		 * @return associated resource
		 */
        public IResource getResource() {
            return fResource;
        }

        @Override
        public String toString() {
            return getValue();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EncodingEntry) {
                EncodingEntry other = (EncodingEntry) obj;
                return other.fEncoding.equals(fEncoding) && other.fResource.equals(fResource);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return fEncoding.hashCode() + fResource.hashCode();
        }

        /**
		 * Returns the generated value of this entry for the build.properties file.
		 *
		 * @return value to enter into build.properties
		 */
        String getValue() {
            StringBuffer buf = new StringBuffer();
            IContainer root = PDEProject.getBundleRoot(fResource.getProject());
            buf.append(fResource.getFullPath().makeRelativeTo(root.getFullPath()).makeAbsolute());
            buf.append('[');
            buf.append(fEncoding);
            buf.append(']');
            return buf.toString();
        }
    }

    /**
	 * Visits a source folder gathering encodings.
	 */
    class Visitor implements IResourceVisitor {

        String[] fLibs = null;

         Visitor(SourceFolder folder) {
            ArrayList<String> list = folder.getLibs();
            fLibs = list.toArray(new String[list.size()]);
        }

        @Override
        public boolean visit(IResource resource) throws CoreException {
            String encoding = null;
            switch(resource.getType()) {
                case IResource.FOLDER:
                    encoding = ((IFolder) resource).getDefaultCharset(false);
                    break;
                case IResource.FILE:
                    IFile file = (IFile) resource;
                    // only worry about .java files
                    if (file.getFileExtension() != null && //$NON-NLS-1$
                    file.getFileExtension().equals(//$NON-NLS-1$
                    "java")) {
                        encoding = file.getCharset(false);
                    }
                    break;
            }
            if (encoding != null) {
                EncodingEntry entry = new EncodingEntry(resource, encoding);
                for (int i = 0; i < fLibs.length; i++) {
                    String lib = fLibs[i];
                    List<EncodingEntry> encodings = fCustomEncodings.get(lib);
                    if (encodings == null) {
                        encodings = new ArrayList();
                        fCustomEncodings.put(lib, encodings);
                    }
                    encodings.add(entry);
                }
            }
            return true;
        }
    }

    private HashMap<IPath, SourceFolder> fSourceFolderMap = new HashMap(4);

    private HashMap<IPath, OutputFolder> fOutputFolderMap = new HashMap(4);

    private IBuild fBuild = null;

    /**
	 * Maps library name to default encoding for that library (or not present if there is no
	 * explicit default encoding specified).
	 */
    Map<String, String> fDefaultLibraryEncodings = new HashMap();

    /**
	 * Maps library name to custom {@link EncodingEntry}'s for this library.
	 */
    Map<String, List<EncodingEntry>> fCustomEncodings = new HashMap();

    public void initialize(ArrayList<?> sourceEntries, ArrayList<?> outputEntries, IClasspathEntry[] cpes, IProject project) {
        fProject = project;
        IPath defaultOutputLocation = null;
        IJavaProject javaProject = JavaCore.create(fProject);
        try {
            defaultOutputLocation = javaProject.getOutputLocation();
        } catch (JavaModelException e) {
        }
        List<String> pluginLibraryNames = new ArrayList(1);
        IPluginModelBase pluginModel = PluginRegistry.findModel(fProject);
        if (pluginModel != null) {
            IPluginLibrary[] pluginLibraries = pluginModel.getPluginBase().getLibraries();
            for (int i = 0; i < pluginLibraries.length; i++) {
                pluginLibraryNames.add(pluginLibraries[i].getName());
            }
        }
        if (//$NON-NLS-1$
        !pluginLibraryNames.contains(".")) {
            //$NON-NLS-1$)
            pluginLibraryNames.add(".");
        }
        for (int i = 0; i < cpes.length; i++) {
            if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IPath sourcePath = getPath(cpes[i]);
                if (sourcePath == null)
                    continue;
                IPath outputLocation = cpes[i].getOutputLocation();
                if (outputLocation == null)
                    outputLocation = defaultOutputLocation;
                IPath outputPath = getPath(outputLocation);
                OutputFolder outputFolder = fOutputFolderMap.get(outputPath);
                if (outputFolder == null) {
                    outputFolder = new OutputFolder(outputPath);
                }
                SourceFolder sourceFolder = fSourceFolderMap.get(sourcePath);
                if (sourceFolder == null) {
                    sourceFolder = new SourceFolder(sourcePath, outputFolder);
                }
                outputFolder.addSourceFolder(sourceFolder);
                fOutputFolderMap.put(outputPath, outputFolder);
                fSourceFolderMap.put(sourcePath, sourceFolder);
            } else if (cpes[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                IClasspathEntry entry = cpes[i];
                IPackageFragmentRoot[] roots = javaProject.findPackageFragmentRoots(entry);
                IPath outputPath = null;
                if (// should only be one entry for a library
                roots.length == 1) {
                    if (!roots[0].isArchive()) {
                        outputPath = getPath(entry);
                        OutputFolder outputFolder = new OutputFolder(outputPath, true);
                        fOutputFolderMap.put(outputPath, outputFolder);
                    }
                }
            }
        }
        for (Iterator<?> iterator = sourceEntries.iterator(); iterator.hasNext(); ) {
            IBuildEntry sourceEntry = (IBuildEntry) iterator.next();
            String libName = sourceEntry.getName().substring(PROPERTY_SOURCE_PREFIX.length());
            if (!pluginLibraryNames.contains(libName)) {
                prepareError(sourceEntry.getName(), null, NLS.bind(PDECoreMessages.SourceEntryErrorReporter_MissingLibrary, libName), PDEMarkerFactory.B_REMOVAL, fSrcLibSeverity, PDEMarkerFactory.CAT_OTHER);
            }
            String[] tokens = sourceEntry.getTokens();
            for (int i = 0; i < tokens.length; i++) {
                IPath path = new Path(tokens[i]).addTrailingSeparator();
                SourceFolder sourceFolder = fSourceFolderMap.get(path);
                if (sourceFolder == null) {
                    sourceFolder = new SourceFolder(path, null);
                    fSourceFolderMap.put(path, sourceFolder);
                }
                sourceFolder.setToken(tokens[i]);
                sourceFolder.addLib(libName);
            }
        }
        for (Iterator<?> iterator = outputEntries.iterator(); iterator.hasNext(); ) {
            IBuildEntry outputEntry = (IBuildEntry) iterator.next();
            String libName = outputEntry.getName().substring(PROPERTY_OUTPUT_PREFIX.length());
            if (!pluginLibraryNames.contains(libName)) {
                prepareError(outputEntry.getName(), null, NLS.bind(PDECoreMessages.SourceEntryErrorReporter_MissingLibrary, libName), PDEMarkerFactory.B_REMOVAL, fOututLibSeverity, PDEMarkerFactory.CAT_OTHER);
            }
            String[] tokens = outputEntry.getTokens();
            for (int i = 0; i < tokens.length; i++) {
                IPath path = new Path(tokens[i]).addTrailingSeparator();
                if (//$NON-NLS-1$
                path.segmentCount() == 1 && path.segment(0).equals(".")) {
                    // translate "." to root path
                    path = Path.ROOT;
                }
                OutputFolder outputFolder = fOutputFolderMap.get(path);
                if (outputFolder == null) {
                    outputFolder = new OutputFolder(path);
                    fOutputFolderMap.put(path, outputFolder);
                }
                outputFolder.setToken(tokens[i]);
                outputFolder.addLib(libName);
            }
        }
    }

    private IPath getPath(Object entry) {
        IPath path = null;
        if (entry instanceof IClasspathEntry) {
            IClasspathEntry cpes = (IClasspathEntry) entry;
            path = cpes.getPath();
        } else if (entry instanceof IPath) {
            path = (IPath) entry;
        }
        if (path != null && path.matchingFirstSegments(fProject.getFullPath()) > 0) {
            path = path.removeFirstSegments(1);
        }
        if (path != null) {
            return path.addTrailingSeparator();
        }
        return null;
    }

    public void validate() {
        for (Iterator<IPath> iterator = fOutputFolderMap.keySet().iterator(); iterator.hasNext(); ) {
            IPath outputPath = iterator.next();
            OutputFolder outputFolder = fOutputFolderMap.get(outputPath);
            ArrayList<SourceFolder> sourceFolders = outputFolder.getSourceFolders();
            ArrayList<String> outputFolderLibs = new ArrayList(outputFolder.getLibs());
            if (sourceFolders.size() == 0) {
                if (!outputFolder.isLibrary()) {
                    // report error - invalid output folder
                    for (Iterator<String> libNameiterator = outputFolderLibs.iterator(); libNameiterator.hasNext(); ) {
                        String libName = libNameiterator.next();
                        IResource folderEntry = fProject.findMember(outputPath);
                        String message;
                        if (folderEntry == null || !folderEntry.exists() || !(folderEntry instanceof IContainer))
                            message = NLS.bind(PDECoreMessages.BuildErrorReporter_missingFolder, outputPath.toString());
                        else
                            message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_InvalidOutputFolder, outputPath.toString());
                        prepareError(PROPERTY_OUTPUT_PREFIX + libName, outputFolder.getToken(), message, PDEMarkerFactory.B_REMOVAL, fOututLibSeverity, PDEMarkerFactory.CAT_OTHER);
                    }
                } else {
                    if (outputFolderLibs.size() == 0) {
                        //class folder does not have an output.<library> entry, only continue if we have a plugin model for the project
                        IPluginModelBase model = PluginRegistry.findModel(fProject);
                        if (model != null) {
                            IPluginLibrary[] libs = model.getPluginBase().getLibraries();
                            String message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_MissingOutputLibForClassFolder, outputPath.toString());
                            if (libs.length > 0) {
                                prepareError(PROPERTY_OUTPUT_PREFIX, null, message, PDEMarkerFactory.NO_RESOLUTION, fOututLibSeverity, PDEMarkerFactory.CAT_OTHER);
                            } else {
                                prepareError(DEF_OUTPUT_ENTRY, outputPath.toString(), message, PDEMarkerFactory.B_ADDITION, fOututLibSeverity, PDEMarkerFactory.CAT_OTHER);
                            }
                        }
                    }
                }
            } else {
                String srcFolderLibName = null;
                for (int i = 0; i < sourceFolders.size(); i++) {
                    SourceFolder sourceFolder = sourceFolders.get(i);
                    ArrayList<String> srcFolderLibs = sourceFolder.getLibs();
                    outputFolderLibs.removeAll(srcFolderLibs);
                    switch(srcFolderLibs.size()) {
                        case 0:
                            //do nothing. already caught in super
                            break;
                        case 1:
                            if (srcFolderLibName == null) {
                                srcFolderLibName = srcFolderLibs.get(0);
                                break;
                            } else if (srcFolderLibName.equals(srcFolderLibs.get(0))) {
                                break;
                            }
                        default:
                            //error - targeted to diff libs
                            String erringSrcFolders = join(sourceFolders.toArray(new SourceFolder[sourceFolders.size()]));
                            for (int j = 0; j < sourceFolders.size(); j++) {
                                SourceFolder srcFolder = sourceFolders.get(j);
                                for (int k = 0; k < srcFolder.getLibs().size(); k++) {
                                    String libName = srcFolder.getLibs().get(k);
                                    String message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_DifferentTargetLibrary, erringSrcFolders);
                                    prepareError(PROPERTY_SOURCE_PREFIX + libName, srcFolder.getToken(), message, PDEMarkerFactory.NO_RESOLUTION, fSrcLibSeverity, PDEMarkerFactory.CAT_OTHER);
                                }
                            }
                    }
                }
                for (int i = 0; i < outputFolderLibs.size(); i++) {
                    String message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_ExtraOutputFolder, outputFolder.getPath().toString(), PROPERTY_SOURCE_PREFIX + outputFolderLibs.get(i));
                    prepareError(PROPERTY_OUTPUT_PREFIX + outputFolderLibs.get(i), outputFolder.getToken(), message, PDEMarkerFactory.B_REMOVAL, fOututLibSeverity, PDEMarkerFactory.CAT_OTHER);
                }
                if (outputFolder.getDupeLibName() != null) {
                    String message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_DupeOutputFolder, outputPath.toString(), PROPERTY_OUTPUT_PREFIX + outputFolder.getDupeLibName());
                    prepareError(PROPERTY_OUTPUT_PREFIX + outputFolder.getDupeLibName(), outputFolder.getToken(), message, PDEMarkerFactory.NO_RESOLUTION, fOututLibSeverity, PDEMarkerFactory.CAT_OTHER);
                }
            }
        }
        class MissingOutputEntry {

            private List<String> fSrcFolders = new ArrayList(1);

            private List<String> fOutputFolders = new ArrayList(1);

            public String getOutputList() {
                return generateList(fOutputFolders);
            }

            public String getSourceList() {
                return generateList(fSrcFolders);
            }

            private String generateList(List<String> strings) {
                StringBuffer buffer = new StringBuffer();
                Iterator<String> iterator = strings.iterator();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    buffer.append(next);
                    if (iterator.hasNext()) {
                        buffer.append(',');
                        buffer.append(' ');
                    }
                }
                return buffer.toString();
            }

            public void addSrcFolder(String sourcePath) {
                if (!fSrcFolders.contains(sourcePath)) {
                    fSrcFolders.add(sourcePath);
                }
            }

            public void addOutputFolder(String outputPath) {
                if (!fOutputFolders.contains(outputPath)) {
                    fOutputFolders.add(outputPath);
                }
            }
        }
        HashMap<String, MissingOutputEntry> missingOutputEntryErrors = new HashMap(4);
        // list of source folders to perform encoding validation on
        List<SourceFolder> toValidate = new ArrayList();
        for (Iterator<IPath> iterator = fSourceFolderMap.keySet().iterator(); iterator.hasNext(); ) {
            IPath sourcePath = iterator.next();
            SourceFolder sourceFolder = fSourceFolderMap.get(sourcePath);
            OutputFolder outputFolder = sourceFolder.getOutputLocation();
            if (outputFolder == null) {
                //error - not a src folder
                IResource folderEntry = fProject.findMember(sourcePath);
                String message;
                if (folderEntry == null || !folderEntry.exists() || !(folderEntry instanceof IContainer))
                    message = NLS.bind(PDECoreMessages.BuildErrorReporter_missingFolder, sourcePath.toString());
                else
                    message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_InvalidSourceFolder, sourcePath.toString());
                ArrayList<String> srcLibs = sourceFolder.getLibs();
                for (int i = 0; i < srcLibs.size(); i++) {
                    String libName = srcLibs.get(i);
                    prepareError(PROPERTY_SOURCE_PREFIX + libName, sourceFolder.getToken(), message, PDEMarkerFactory.B_REMOVAL, fSrcLibSeverity, PDEMarkerFactory.CAT_OTHER);
                }
            } else {
                if (outputFolder.getLibs().size() == 0 && sourceFolder.getLibs().size() == 1) {
                    //error - missing output folder
                    String libName = sourceFolder.getLibs().get(0);
                    MissingOutputEntry errorEntry = missingOutputEntryErrors.get(libName);
                    if (errorEntry == null)
                        errorEntry = new MissingOutputEntry();
                    errorEntry.addSrcFolder(sourcePath.toString());
                    errorEntry.addOutputFolder(outputFolder.getToken());
                    missingOutputEntryErrors.put(libName, errorEntry);
                }
                if (sourceFolder.getDupeLibName() != null) {
                    String message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_DupeSourceFolder, sourcePath.toString(), PROPERTY_SOURCE_PREFIX + sourceFolder.getDupeLibName());
                    prepareError(PROPERTY_SOURCE_PREFIX + sourceFolder.getDupeLibName(), sourceFolder.getToken(), message, PDEMarkerFactory.NO_RESOLUTION, fSrcLibSeverity, PDEMarkerFactory.CAT_OTHER);
                }
                toValidate.add(sourceFolder);
            }
        }
        for (Iterator<String> iter = missingOutputEntryErrors.keySet().iterator(); iter.hasNext(); ) {
            String libName = iter.next();
            MissingOutputEntry errorEntry = missingOutputEntryErrors.get(libName);
            String message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_MissingOutputEntry, errorEntry.getSourceList(), PROPERTY_OUTPUT_PREFIX + libName);
            prepareError(PROPERTY_OUTPUT_PREFIX + libName, errorEntry.getOutputList(), message, PDEMarkerFactory.B_ADDITION, fMissingOutputLibSeverity, PDEMarkerFactory.CAT_OTHER);
        }
        if (fEncodingSeverity == CompilerFlags.ERROR || fEncodingSeverity == CompilerFlags.WARNING) {
            // build map of expected encodings
            Iterator<SourceFolder> iterator = toValidate.iterator();
            while (iterator.hasNext()) {
                SourceFolder sourceFolder = iterator.next();
                IPath sourcePath = sourceFolder.getPath();
                IContainer container = fProject;
                if (!sourcePath.isEmpty() && !sourcePath.isRoot()) {
                    container = container.getFolder(sourcePath);
                }
                try {
                    ArrayList<String> list = sourceFolder.getLibs();
                    String[] libs = list.toArray(new String[list.size()]);
                    String encoding = getExplicitEncoding(container);
                    if (encoding != null) {
                        for (int i = 0; i < libs.length; i++) {
                            fDefaultLibraryEncodings.put(libs[i], encoding);
                        }
                    }
                    container.accept(new Visitor(sourceFolder));
                } catch (CoreException e) {
                    PDECore.log(e);
                }
            }
            // Compare to encodings specified in build.properties (if any)
            IBuildEntry[] entries = fBuild.getBuildEntries();
            for (int i = 0; i < entries.length; i++) {
                IBuildEntry entry = entries[i];
                String name = entry.getName();
                if (name.startsWith(PROPERTY_JAVAC_DEFAULT_ENCODING_PREFIX)) {
                    String lib = name.substring(PROPERTY_JAVAC_DEFAULT_ENCODING_PREFIX.length());
                    String[] tokens = entry.getTokens();
                    if (tokens.length > 0) {
                        if (tokens.length == 1) {
                            // compare
                            String specified = tokens[0];
                            String expected = fDefaultLibraryEncodings.remove(lib);
                            if (expected != null) {
                                if (!specified.equals(expected)) {
                                    prepareError(name, specified, NLS.bind(PDECoreMessages.SourceEntryErrorReporter_0, new String[] { expected, specified, lib }), PDEMarkerFactory.NO_RESOLUTION, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                                }
                            } else {
                                // encoding is specified, but workspace does not specify one
                                prepareError(name, null, NLS.bind(PDECoreMessages.SourceEntryErrorReporter_1, new String[] { specified, lib }), PDEMarkerFactory.B_REMOVAL, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                            }
                        } else {
                            // syntax error
                            fDefaultLibraryEncodings.remove(lib);
                            prepareError(name, null, NLS.bind(PDECoreMessages.SourceEntryErrorReporter_2, lib), PDEMarkerFactory.NO_RESOLUTION, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                        }
                    }
                } else if (name.startsWith(PROPERTY_JAVAC_CUSTOM_ENCODINGS_PREFIX)) {
                    IContainer bundleRoot = PDEProject.getBundleRoot(fProject);
                    String lib = name.substring(PROPERTY_JAVAC_CUSTOM_ENCODINGS_PREFIX.length());
                    String[] tokens = entry.getTokens();
                    if (tokens.length > 0) {
                        List<EncodingEntry> encodings = new ArrayList();
                        for (int j = 0; j < tokens.length; j++) {
                            String special = tokens[j];
                            int index = special.indexOf('[');
                            if (//$NON-NLS-1$
                            index >= 0 && //$NON-NLS-1$
                            special.endsWith("]")) {
                                String path = special.substring(0, index);
                                String encoding = special.substring(index + 1, special.length() - 1);
                                IResource member = bundleRoot.findMember(path);
                                if (member == null) {
                                    // error - missing resource
                                    String message = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_3, new String[] { encoding, path });
                                    prepareError(name, special, message, PDEMarkerFactory.B_REMOVAL, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                                } else {
                                    encodings.add(new EncodingEntry(member, encoding));
                                }
                            } else {
                                // syntax error - invalid
                                String message = PDECoreMessages.SourceEntryErrorReporter_4;
                                prepareError(name, special, message, PDEMarkerFactory.NO_RESOLUTION, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                            }
                        }
                        // compare with workspace encodings
                        List<EncodingEntry> workspace = fCustomEncodings.remove(lib);
                        if (workspace == null) {
                            prepareError(name, null, NLS.bind(PDECoreMessages.SourceEntryErrorReporter_5, lib), PDEMarkerFactory.B_REMOVAL, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                        } else {
                            Map<IResource, String> map = new HashMap();
                            Iterator<EncodingEntry> iter = workspace.iterator();
                            while (iter.hasNext()) {
                                EncodingEntry ee = iter.next();
                                map.put(ee.getResource(), ee.getEncoding());
                            }
                            iter = encodings.iterator();
                            while (iter.hasNext()) {
                                EncodingEntry ee = iter.next();
                                String specified = ee.getEncoding();
                                String expected = map.remove(ee.getResource());
                                if (expected == null) {
                                    prepareError(name, ee.getValue(), NLS.bind(PDECoreMessages.SourceEntryErrorReporter_6, new String[] { expected, ee.getResource().getProjectRelativePath().toString() }), PDEMarkerFactory.B_REMOVAL, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                                } else {
                                    if (!specified.equals(expected)) {
                                        prepareError(name, ee.getValue(), NLS.bind(PDECoreMessages.SourceEntryErrorReporter_7, new String[] { expected, ee.getResource().getProjectRelativePath().toString(), specified }), PDEMarkerFactory.NO_RESOLUTION, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                                    }
                                }
                            }
                            // anything left in the workspace map?
                            if (!map.isEmpty()) {
                                Iterator<Entry<IResource, String>> iter2 = map.entrySet().iterator();
                                while (iter2.hasNext()) {
                                    Entry<IResource, String> en = iter2.next();
                                    IResource res = en.getKey();
                                    String expected = en.getValue();
                                    EncodingEntry missing = new EncodingEntry(res, expected);
                                    String m = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_8, new String[] { expected, res.getProjectRelativePath().toString() });
                                    prepareError(name, missing.getValue(), m, PDEMarkerFactory.B_ADDITION, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                                }
                            }
                        }
                    }
                }
            }
            // check for unspecified default encodings
            Iterator<Entry<String, String>> iter = fDefaultLibraryEncodings.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, String> entry = iter.next();
                String lib = entry.getKey();
                String expected = entry.getValue();
                prepareError(PROPERTY_JAVAC_DEFAULT_ENCODING_PREFIX + lib, expected, NLS.bind(PDECoreMessages.SourceEntryErrorReporter_9, new String[] { expected, lib }), PDEMarkerFactory.B_ADDITION, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
            }
            // check for unspecified custom encodings
            Iterator<Entry<String, List<EncodingEntry>>> iter2 = fCustomEncodings.entrySet().iterator();
            while (iter2.hasNext()) {
                Entry<String, List<EncodingEntry>> entry = iter2.next();
                String lib = entry.getKey();
                List<EncodingEntry> encodings = entry.getValue();
                Iterator<EncodingEntry> iterator2 = encodings.iterator();
                while (iterator2.hasNext()) {
                    EncodingEntry encoding = iterator2.next();
                    String m = NLS.bind(PDECoreMessages.SourceEntryErrorReporter_10, new String[] { encoding.getEncoding(), encoding.getResource().getProjectRelativePath().toString() });
                    prepareError(PROPERTY_JAVAC_CUSTOM_ENCODINGS_PREFIX + lib, encoding.getValue(), m, PDEMarkerFactory.B_ADDITION, fEncodingSeverity, PDEMarkerFactory.CAT_OTHER);
                }
            }
        }
    }

    /**
	 * Returns any explicit encoding set on the given container or one of its parents
	 * up to and including its project.
	 *
	 * @param container container
	 * @return any explicit encoding or <code>null</code> if none
	 * @throws CoreException
	 */
    private String getExplicitEncoding(IContainer container) throws CoreException {
        String encoding = container.getDefaultCharset(false);
        if (encoding == null) {
            IContainer parent = container.getParent();
            if (parent != null) {
                switch(parent.getType()) {
                    case IResource.FOLDER:
                        return getExplicitEncoding(parent);
                    case IResource.PROJECT:
                        return getExplicitEncoding(parent);
                    default:
                        // don't consider workspace encoding
                        return null;
                }
            }
        }
        return encoding;
    }

    private String join(ProjectFolder[] folders) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < folders.length; i++) {
            String text = folders[i].getPath().toString().trim();
            if (text.length() > 0) {
                result.append(text);
                result.append(',');
            }
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    public ArrayList<BuildProblem> getProblemList() {
        return fProblemList;
    }
}
