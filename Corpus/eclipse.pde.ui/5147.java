/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.api.tools.internal.ApiBaselineManager;
import org.eclipse.pde.api.tools.internal.ApiDescription;
import org.eclipse.pde.api.tools.internal.ApiDescriptionProcessor;
import org.eclipse.pde.api.tools.internal.BundleVersionRange;
import org.eclipse.pde.api.tools.internal.CompositeApiDescription;
import org.eclipse.pde.api.tools.internal.FilterStore;
import org.eclipse.pde.api.tools.internal.IApiCoreConstants;
import org.eclipse.pde.api.tools.internal.RequiredComponentDescription;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.Factory;
import org.eclipse.pde.api.tools.internal.provisional.IApiAccess;
import org.eclipse.pde.api.tools.internal.provisional.IApiDescription;
import org.eclipse.pde.api.tools.internal.provisional.IApiFilterStore;
import org.eclipse.pde.api.tools.internal.provisional.IRequiredComponentDescription;
import org.eclipse.pde.api.tools.internal.provisional.ProfileModifiers;
import org.eclipse.pde.api.tools.internal.provisional.VisibilityModifiers;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IPackageDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiTypeContainer;
import org.eclipse.pde.api.tools.internal.util.FileManager;
import org.eclipse.pde.api.tools.internal.util.SourceDefaultHandler;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.eclipse.pde.internal.core.TargetWeaver;
import org.eclipse.pde.internal.core.util.ManifestUtils;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of an API component based on a bundle in the file system.
 *
 * @since 1.0.0
 */
public class BundleComponent extends Component {

    //$NON-NLS-1$
    static final String TMP_API_FILE_PREFIX = "api";

    //$NON-NLS-1$
    static final String TMP_API_FILE_POSTFIX = "tmp";

    /**
	 * Dictionary parsed from MANIFEST.MF
	 */
    private Map<String, String> fManifest;

    /**
	 * Manifest headers that are maintained after {@link BundleDescription}
	 * creation. Only these headers are maintained in the manifest dictionary to
	 * reduce footprint.
	 */
    private static final String[] MANIFEST_HEADERS = new String[] { IApiCoreConstants.ECLIPSE_SOURCE_BUNDLE, Constants.BUNDLE_CLASSPATH, Constants.BUNDLE_NAME, Constants.BUNDLE_VERSION };

    /**
	 * Whether there is an underlying .api_description file
	 */
    private boolean fHasApiDescription = false;

    /**
	 * Root location of component in the file system
	 */
    private String fLocation;

    /**
	 * Underlying bundle description (OSGi model of a bundle)
	 */
    private BundleDescription fBundleDescription;

    /**
	 * Symbolic name of this bundle
	 */
    private String fSymbolicName = null;

    /**
	 * Bundle version
	 */
    private Version fVersion = null;

    /**
	 * Cached value for the lowest EEs
	 */
    private String[] lowestEEs;

    /**
	 * Flag to know if this component is a binary bundle in the workspace i.e.
	 * an imported binary bundle
	 */
    private boolean fWorkspaceBinary = false;

    /**
	 * The id of this component
	 */
    private long fBundleId = 0L;

    /**
	 * Constructs a new API component from the specified location in the file
	 * system in the given baseline.
	 *
	 * @param baseline owning API baseline
	 * @param location directory or jar file
	 * @param bundleid
	 * @exception CoreException if unable to create a component from the
	 *                specified location
	 */
    public  BundleComponent(IApiBaseline baseline, String location, long bundleid) throws CoreException {
        super(baseline);
        fLocation = location;
        fBundleId = bundleid;
        fWorkspaceBinary = isBinary() && ApiBaselineManager.WORKSPACE_API_BASELINE_ID.equals(baseline.getName()) && /*
		 * Workaround for bad architecture, see bug 488694: We don't know if the
		 * bundle is from the workspace , from a host workspace, or from a
		 * target platform. If it's a file, then it's certainly not from a
		 * workspace project, and hence can't be an imported binary bundle.
		 */
        !new File(location).isFile();
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
        } finally {
            synchronized (this) {
                fManifest = null;
                fBundleDescription = null;
            }
        }
    }

    /**
	 * Returns this bundle's manifest as a dictionary or <code>null</code> if no
	 * manifest was found.
	 *
	 * @return manifest dictionary or <code>null</code>
	 * @exception CoreException if something goes terribly wrong
	 */
    protected synchronized Map<String, String> getManifest() throws CoreException {
        if (fManifest == null) {
            try {
                fManifest = ManifestUtils.loadManifest(new File(fLocation));
            } catch (CoreException e) {
                if (e.getStatus().getCode() == ManifestUtils.STATUS_CODE_PLUGIN_CONVERTER_UNAVAILABLE) {
                    System.err.println(e.getMessage());
                    return null;
                } else if (e.getStatus().getCode() == ManifestUtils.STATUS_CODE_NOT_A_BUNDLE_MANIFEST) {
                    return null;
                } else {
                    throw e;
                }
            }
            if (isWorkspaceBinary()) {
                // must account for bundles in development mode - look for class
                // files in output
                // folders rather than jars
                TargetWeaver.weaveManifest(fManifest);
            }
        }
        return fManifest;
    }

    /**
	 * Reduce the manifest to only contain required headers after
	 * {@link BundleDescription} creation.
	 */
    protected synchronized void doManifestCompaction() {
        Map<String, String> temp = fManifest;
        fManifest = new Hashtable(MANIFEST_HEADERS.length, 1);
        for (String header : MANIFEST_HEADERS) {
            String value = temp.get(header);
            if (value != null) {
                fManifest.put(header, value);
            }
        }
    }

    /**
	 * Returns if the bundle at the specified location is a valid bundle or not.
	 * Validity is determined via the existence of a readable manifest file
	 *
	 * @param location
	 * @return true if the bundle at the given location is valid false otherwise
	 * @throws IOException
	 */
    public boolean isValidBundle() throws CoreException {
        Map<String, String> manifest = getManifest();
        return manifest != null && (manifest.get(Constants.BUNDLE_NAME) != null && manifest.get(Constants.BUNDLE_VERSION) != null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BundleComponent) {
            BundleComponent comp = (BundleComponent) obj;
            return getName().equals(comp.getName()) && getSymbolicName().equals(comp.getSymbolicName()) && getVersion().equals(comp.getVersion());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getSymbolicName().hashCode() + getVersion().hashCode();
    }

    /**
	 * Initializes the component
	 *
	 * @throws CoreException on failure
	 */
    protected synchronized void init() {
        if (fBundleDescription != null) {
            return;
        }
        try {
            Map<String, String> manifest = getManifest();
            if (manifest == null) {
                ApiPlugin.log(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, //$NON-NLS-1$
                "Unable to find a manifest for the component from: " + //$NON-NLS-1$
                fLocation, null));
                return;
            }
            fBundleDescription = getBundleDescription(manifest, fLocation, fBundleId);
            fSymbolicName = fBundleDescription.getSymbolicName();
            fVersion = fBundleDescription.getVersion();
            setName(manifest.get(Constants.BUNDLE_NAME));
        } catch (BundleException e) {
            ApiPlugin.log(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, "Unable to create API component from specified location: " + fLocation, e));
        } catch (CoreException ce) {
            ApiPlugin.log(ce);
        }
        // compact manifest after initialization - only keep used headers
        doManifestCompaction();
    }

    /**
	 * Returns if this component is a a binary bundle in the workspace i.e. an
	 * imported binary bundle
	 *
	 * @return true if the component is a binary bundle in the workspace, false
	 *         otherwise
	 */
    public boolean isWorkspaceBinary() {
        return fWorkspaceBinary;
    }

    /**
	 * Returns the {@link State} from the backing baseline
	 *
	 * @return the state from the backing {@link ApiBaseline}
	 */
    protected State getState() {
        return ((ApiBaseline) getBaseline()).getState();
    }

    /**
	 * Returns the {@link BundleDescription} for the given manifest + state or
	 * throws an exception, never returns <code>null</code>
	 *
	 * @param manifest
	 * @param location
	 * @param id
	 * @return the {@link BundleDescription} or throws an exception
	 * @throws BundleException
	 */
    protected BundleDescription getBundleDescription(Map<String, String> manifest, String location, long id) throws BundleException {
        State state = getState();
        BundleDescription bundle = lookupBundle(state, manifest);
        if (bundle != null) {
            return bundle;
        }
        StateObjectFactory factory = StateObjectFactory.defaultFactory;
        Hashtable<String, String> dictionaryManifest = new Hashtable(manifest);
        bundle = factory.createBundleDescription(state, dictionaryManifest, fLocation, id);
        state.addBundle(bundle);
        return bundle;
    }

    /**
	 * Tries to look up the bundle described by the given manifest in the given
	 * state
	 *
	 * @param manifest
	 * @return the bundle for the given manifest, <code>null</code> otherwise
	 * @throws BundleException
	 */
    protected BundleDescription lookupBundle(State state, Map<String, String> manifest) throws BundleException {
        Version version = null;
        try {
            // just in case the version is not a number
            String ver = manifest.get(Constants.BUNDLE_VERSION);
            version = ver != null ? new Version(ver) : null;
        } catch (NumberFormatException nfe) {
        }
        ManifestElement[] name = ManifestElement.parseHeader(Constants.BUNDLE_SYMBOLICNAME, manifest.get(Constants.BUNDLE_SYMBOLICNAME));
        if (name.length < 1) {
            return null;
        }
        return state.getBundle(name[0].getValue(), version);
    }

    /**
	 * Returns whether this API component represents a binary bundle versus a
	 * project bundle.
	 *
	 * @return whether this API component represents a binary bundle
	 */
    protected boolean isBinary() {
        return true;
    }

    @Override
    protected IApiDescription createApiDescription() throws CoreException {
        BundleDescription[] fragments = getBundleDescription().getFragments();
        if (fragments.length == 0) {
            return createLocalApiDescription();
        }
        // build a composite description
        ArrayList<IApiDescription> descriptions = new ArrayList(fragments.length);
        descriptions.add(createLocalApiDescription());
        IApiComponent component = null;
        for (int i = 0; i < fragments.length; i++) {
            if (!fragments[i].isResolved()) {
                continue;
            }
            component = getBaseline().getApiComponent(fragments[i].getSymbolicName());
            if (component != null) {
                descriptions.add(component.getApiDescription());
            } else {
                ApiPlugin.log(new Status(IStatus.WARNING, ApiPlugin.PLUGIN_ID, NLS.bind(Messages.BundleComponent_failed_to_lookup_fragment, fragments[i].getSymbolicName())));
            }
        }
        return new CompositeApiDescription(descriptions.toArray(new IApiDescription[descriptions.size()]));
    }

    /**
	 * Creates and returns this component's API description based on packages
	 * supplied by this component, exported packages, and associated directives.
	 *
	 * @return API description
	 * @throws CoreException if unable to initialize
	 */
    protected IApiDescription createLocalApiDescription() throws CoreException {
        IApiDescription apiDesc = new ApiDescription(getSymbolicName());
        // first mark all packages as internal
        initializeApiDescription(apiDesc, getBundleDescription(), getLocalPackageNames());
        try {
            String xml = loadApiDescription(new File(fLocation));
            setHasApiDescription(xml != null);
            if (xml != null) {
                ApiDescriptionProcessor.annotateApiSettings(null, apiDesc, xml);
            }
        } catch (IOException e) {
            abort("Unable to load .api_description file ", e);
        }
        return apiDesc;
    }

    /**
	 * Returns the names of all packages that originate from this bundle. Does
	 * not include packages that originate from fragments or a host.
	 *
	 * @return local package names
	 * @throws CoreException
	 */
    protected Set<String> getLocalPackageNames() throws CoreException {
        Set<String> names = new HashSet();
        IApiTypeContainer[] containers = getApiTypeContainers();
        IApiComponent comp = null;
        for (IApiTypeContainer container : containers) {
            comp = (IApiComponent) container.getAncestor(IApiElement.COMPONENT);
            if (comp != null && comp.getSymbolicName().equals(getSymbolicName())) {
                String[] packageNames = container.getPackageNames();
                for (String packageName : packageNames) {
                    names.add(packageName);
                }
            }
        }
        return names;
    }

    /**
	 * Initializes the given API description based on package exports in the
	 * manifest. The API description for a bundle only contains packages that
	 * originate from this bundle (so a host will not contain API descriptions
	 * for packages that originate from fragments). However, a host's API
	 * description will be represented by a proxy that delegates to the host and
	 * all of its fragments to provide a complete description of the host.
	 *
	 * @param apiDesc API description to initialize
	 * @param bundle the bundle to load from
	 * @param packages the complete set of packages names originating from the
	 *            backing component
	 * @throws CoreException if an error occurs
	 */
    public static void initializeApiDescription(IApiDescription apiDesc, BundleDescription bundle, Set<String> packages) throws CoreException {
        for (String name : packages) {
            apiDesc.setVisibility(Factory.packageDescriptor(name), VisibilityModifiers.PRIVATE);
        }
        // then process exported packages that originate from this bundle
        // considering host and fragment package exports
        List<ExportPackageDescription> supplied = new ArrayList();
        ExportPackageDescription[] exportPackages = bundle.getExportPackages();
        addSuppliedPackages(packages, supplied, exportPackages);
        HostSpecification host = bundle.getHost();
        if (host != null) {
            BundleDescription[] hosts = host.getHosts();
            for (BundleDescription bundleDescription : hosts) {
                addSuppliedPackages(packages, supplied, bundleDescription.getExportPackages());
            }
        }
        BundleDescription[] fragments = bundle.getFragments();
        for (int i = 0; i < fragments.length; i++) {
            if (!fragments[i].isResolved()) {
                continue;
            }
            addSuppliedPackages(packages, supplied, fragments[i].getExportPackages());
        }
        annotateExportedPackages(apiDesc, supplied.toArray(new ExportPackageDescription[supplied.size()]));
    }

    /**
	 * Adds package exports to the given list if the associated package
	 * originates from this bundle.
	 *
	 * @param packages names of packages supplied by this bundle
	 * @param supplied list to append package exports to
	 * @param exportPackages package exports to consider
	 */
    protected static void addSuppliedPackages(Set<String> packages, List<ExportPackageDescription> supplied, ExportPackageDescription[] exportPackages) {
        for (ExportPackageDescription pkg : exportPackages) {
            String name = pkg.getName();
            if (//$NON-NLS-1$
            name.equals(".")) {
                // translate . to default package
                name = Util.DEFAULT_PACKAGE_NAME;
            }
            if (packages.contains(name)) {
                supplied.add(pkg);
            }
        }
    }

    /**
	 * Annotates the API description with exported packages.
	 *
	 * @param apiDesc description to annotate
	 * @param exportedPackages packages that are exported
	 */
    protected static void annotateExportedPackages(IApiDescription apiDesc, ExportPackageDescription[] exportedPackages) {
        for (ExportPackageDescription pkg : exportedPackages) {
            //$NON-NLS-1$
            boolean internal = ((Boolean) pkg.getDirective("x-internal")).booleanValue();
            //$NON-NLS-1$
            String[] friends = (String[]) pkg.getDirective("x-friends");
            String pkgName = pkg.getName();
            if (//$NON-NLS-1$
            pkgName.equals(".")) {
                // default package
                //$NON-NLS-1$
                pkgName = "";
            }
            IPackageDescriptor pkgDesc = Factory.packageDescriptor(pkgName);
            if (internal) {
                apiDesc.setVisibility(pkgDesc, VisibilityModifiers.PRIVATE);
            }
            if (friends != null) {
                apiDesc.setVisibility(pkgDesc, VisibilityModifiers.PRIVATE);
                for (String friend : friends) {
                    // annotate the api description for x-friends access levels
                    apiDesc.setAccessLevel(Factory.componentDescriptor(friend), Factory.packageDescriptor(pkgName), IApiAccess.FRIEND);
                }
            }
            if (!internal && friends == null) {
                // there could have been directives that have nothing to do with
                // visibility, so we need to add the package as API in that case
                apiDesc.setVisibility(pkgDesc, VisibilityModifiers.API);
            }
        }
    }

    @Override
    protected IApiFilterStore createApiFilterStore() throws CoreException {
        return new FilterStore(this);
    }

    /**
	 * @see org.eclipse.pde.api.tools.internal.AbstractApiTypeContainer#createApiTypeContainers()
	 */
    @Override
    protected synchronized List<IApiTypeContainer> createApiTypeContainers() throws CoreException {
        List<IApiTypeContainer> containers = new ArrayList(5);
        List<IApiComponent> all = new ArrayList();
        // build the classpath from bundle and all fragments
        all.add(this);
        boolean considerFragments = true;
        if (Util.ORG_ECLIPSE_SWT.equals(getSymbolicName())) {
            // if SWT is a project to be built/analyzed don't consider its
            // fragments
            considerFragments = !isApiEnabled();
        }
        if (considerFragments) {
            BundleDescription[] fragments = getBundleDescription().getFragments();
            IApiComponent component = null;
            for (int i = 0; i < fragments.length; i++) {
                if (!fragments[i].isResolved()) {
                    continue;
                }
                component = getBaseline().getApiComponent(fragments[i].getSymbolicName());
                if (component != null) {
                    // force initialization of the fragment so we can
                    // retrieve its class file containers
                    component.getApiTypeContainers();
                    all.add(component);
                }
            }
        }
        Iterator<IApiComponent> iterator = all.iterator();
        Set<String> entryNames = new HashSet(5);
        BundleComponent other = null;
        while (iterator.hasNext()) {
            BundleComponent component = (BundleComponent) iterator.next();
            Map<String, String> manifest = component.getManifest();
            if (manifest != null) {
                try {
                    String[] paths = getClasspathEntries(manifest);
                    for (String path : paths) {
                        // entries ".")
                        if (//$NON-NLS-1$
                        !(".".equals(path))) {
                            if (entryNames.contains(path)) {
                                continue;
                            }
                        }
                        IApiTypeContainer container = component.createApiTypeContainer(path);
                        if (container == null) {
                            for (IApiComponent iApiComponent : all) {
                                other = (BundleComponent) iApiComponent;
                                if (other != component) {
                                    container = other.createApiTypeContainer(path);
                                }
                            }
                        }
                        if (container != null) {
                            containers.add(container);
                            //$NON-NLS-1$
                            if (!(".".equals(path))) {
                                entryNames.add(path);
                            }
                        }
                    }
                } catch (BundleException e) {
                    abort("Unable to parse bundle-classpath entry of manifest for bundle at " + component.getLocation(), e);
                }
            }
        }
        return containers;
    }

    /**
	 * Returns whether this API component is enabled for API analysis by the API
	 * builder.
	 *
	 * @return whether this API component is enabled for API analysis by the API
	 *         builder.
	 */
    protected boolean isApiEnabled() {
        return false;
    }

    /**
	 * Returns classpath entries defined in the given manifest.
	 *
	 * @param manifest
	 * @return classpath entries as bundle relative paths
	 * @throws BundleException
	 */
    protected String[] getClasspathEntries(Map<String, String> manifest) throws BundleException {
        ManifestElement[] classpath = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH, manifest.get(Constants.BUNDLE_CLASSPATH));
        String elements[] = null;
        if (classpath == null) {
            // default classpath is '.'
            //$NON-NLS-1$
            elements = new String[] { "." };
        } else {
            elements = new String[classpath.length];
            for (int i = 0; i < classpath.length; i++) {
                elements[i] = classpath[i].getValue();
            }
        }
        return elements;
    }

    /**
	 * Creates and returns an {@link IApiTypeContainer} at the specified path in
	 * this bundle, or <code>null</code> if the {@link IApiTypeContainer} does
	 * not exist. The path is the name (path) of entries specified by the
	 * <code>Bundle-ClassPath:</code> header.
	 *
	 * @param path relative path to a class file container in this bundle
	 * @return {@link IApiTypeContainer} or <code>null</code>
	 * @throws IOException
	 * @throws CoreException if something goes wrong while creating the
	 *             container
	 */
    protected IApiTypeContainer createApiTypeContainer(String path) throws CoreException {
        try {
            File bundle = new File(fLocation);
            if (bundle.isDirectory()) {
                // bundle is folder
                File entry = new File(bundle, path);
                if (entry.exists()) {
                    if (entry.isFile()) {
                        return new ArchiveApiTypeContainer(this, entry.getCanonicalPath());
                    } else {
                        return new DirectoryApiTypeContainer(this, entry.getCanonicalPath());
                    }
                }
            } else {
                // bundle is jar'd
                ZipFile zip = null;
                try {
                    if (//$NON-NLS-1$
                    path.equals(//$NON-NLS-1$
                    ".")) {
                        return new ArchiveApiTypeContainer(this, fLocation);
                    } else {
                        // classpath element can be jar or folder
                        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=279729
                        zip = new ZipFile(fLocation);
                        ZipEntry entry = zip.getEntry(path);
                        if (entry != null) {
                            File tmpfolder = new File(//$NON-NLS-1$
                            System.getProperty(//$NON-NLS-1$
                            "java.io.tmpdir"));
                            if (entry.isDirectory()) {
                                // extract the dir and all children
                                File dir = Util.createTempFile(TMP_API_FILE_PREFIX, TMP_API_FILE_POSTFIX);
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4735419
                                if (dir.delete()) {
                                    dir.mkdir();
                                    FileManager.getManager().recordTempFileRoot(dir.getCanonicalPath());
                                }
                                extractDirectory(zip, entry.getName(), dir);
                                if (dir.isDirectory() && dir.exists()) {
                                    return new DirectoryApiTypeContainer(this, dir.getCanonicalPath());
                                }
                            } else {
                                File file = extractEntry(zip, entry, tmpfolder);
                                if (Util.isArchive(file.getName())) {
                                    File parent = file.getParentFile();
                                    if (!parent.equals(tmpfolder)) {
                                        FileManager.getManager().recordTempFileRoot(parent.getCanonicalPath());
                                    } else {
                                        FileManager.getManager().recordTempFileRoot(file.getCanonicalPath());
                                    }
                                    return new ArchiveApiTypeContainer(this, file.getCanonicalPath());
                                }
                            }
                        }
                    }
                } finally {
                    if (zip != null) {
                        zip.close();
                    }
                }
            }
        } catch (IOException e) {
            abort("Problem creating api type container for path " + path + " in bundle at " + fLocation, e);
        }
        return null;
    }

    /**
	 * Extracts a directory from the archive given a path prefix for entries to
	 * retrieve. <code>null</code> can be passed in as a prefix, causing all
	 * entries to be be extracted from the archive.
	 *
	 * @param zip the {@link ZipFile} to extract from
	 * @param pathprefix the prefix'ing path to include for extraction
	 * @param parent the parent directory to extract to
	 * @throws IOException if the {@link ZipFile} cannot be read or extraction
	 *             fails to write the file(s)
	 */
    void extractDirectory(ZipFile zip, String pathprefix, File parent) throws IOException {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        String prefix = (pathprefix == null ? Util.EMPTY_STRING : pathprefix);
        ZipEntry entry = null;
        File file = null;
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (entry.getName().startsWith(prefix)) {
                file = new File(parent, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdir();
                    continue;
                }
                extractEntry(zip, entry, parent);
            }
        }
    }

    /**
	 * Extracts a non-directory entry from a zip file and returns the File
	 * handle
	 *
	 * @param zip the zip to extract from
	 * @param entry the entry to extract
	 * @param parent the parent directory to add the extracted entry to
	 * @return the file handle to the extracted entry, <code>null</code>
	 *         otherwise
	 * @throws IOException
	 */
    File extractEntry(ZipFile zip, ZipEntry entry, File parent) throws IOException {
        InputStream inputStream = null;
        File file;
        FileOutputStream outputStream = null;
        try {
            inputStream = zip.getInputStream(entry);
            file = new File(parent, entry.getName());
            File lparent = file.getParentFile();
            if (!lparent.exists()) {
                lparent.mkdirs();
            }
            outputStream = new FileOutputStream(file);
            byte[] bytes = new byte[8096];
            while (inputStream.available() > 0) {
                int read = inputStream.read(bytes);
                if (read > 0) {
                    outputStream.write(bytes, 0, read);
                }
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    ApiPlugin.log(e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    ApiPlugin.log(e);
                }
            }
        }
        return file;
    }

    public void closingZipFileAndStream(InputStream stream, ZipFile jarFile) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            ApiPlugin.log(e);
        }
        try {
            if (jarFile != null) {
                jarFile.close();
            }
        } catch (IOException e) {
            ApiPlugin.log(e);
        }
    }

    /**
	 * Reads and returns the file contents corresponding to the given file name.
	 * The bundle may be in a jar or in a directory at the specified location.
	 *
	 * @param xmlFileName the given file name
	 * @param bundleLocation the root location of the bundle
	 * @return the file contents or <code>null</code> if not present
	 */
    protected String readFileContents(String xmlFileName, File bundleLocation) {
        ZipFile jarFile = null;
        InputStream stream = null;
        try {
            String extension = new Path(bundleLocation.getName()).getFileExtension();
            if (//$NON-NLS-1$
            extension != null && extension.equals("jar") && bundleLocation.isFile()) {
                jarFile = new ZipFile(bundleLocation, ZipFile.OPEN_READ);
                ZipEntry manifestEntry = jarFile.getEntry(xmlFileName);
                if (manifestEntry != null) {
                    stream = jarFile.getInputStream(manifestEntry);
                }
            } else {
                File file = new File(bundleLocation, xmlFileName);
                if (file.exists()) {
                    stream = new FileInputStream(file);
                }
            }
            if (stream == null) {
                return null;
            }
            return new String(Util.getInputStreamAsCharArray(stream, -1, IApiCoreConstants.UTF_8));
        } catch (IOException e) {
            ApiPlugin.log(e);
        } finally {
            closingZipFileAndStream(stream, jarFile);
        }
        return null;
    }

    /**
	 * Parses a bundle's .api_description XML into a string. The file may be in
	 * a jar or in a directory at the specified location.
	 *
	 * @param bundleLocation root location of the bundle
	 * @return API description XML as a string or <code>null</code> if none
	 * @throws IOException if unable to parse
	 */
    protected String loadApiDescription(File bundleLocation) throws IOException {
        ZipFile jarFile = null;
        InputStream stream = null;
        String contents = null;
        try {
            String extension = new Path(bundleLocation.getName()).getFileExtension();
            if (//$NON-NLS-1$
            extension != null && extension.equals("jar") && bundleLocation.isFile()) {
                jarFile = new ZipFile(bundleLocation, ZipFile.OPEN_READ);
                ZipEntry manifestEntry = jarFile.getEntry(IApiCoreConstants.API_DESCRIPTION_XML_NAME);
                if (manifestEntry != null) {
                    // new file is present
                    stream = jarFile.getInputStream(manifestEntry);
                }
            } else {
                File file = new File(bundleLocation, IApiCoreConstants.API_DESCRIPTION_XML_NAME);
                if (file.exists()) {
                    // use new file
                    stream = new FileInputStream(file);
                }
            }
            if (stream == null) {
                return null;
            }
            char[] charArray = Util.getInputStreamAsCharArray(stream, -1, IApiCoreConstants.UTF_8);
            contents = new String(charArray);
        } finally {
            closingZipFileAndStream(stream, jarFile);
        }
        return contents;
    }

    /**
	 * Returns a URL describing a file inside a bundle.
	 *
	 * @param bundleLocation root location of the bundle. May be a directory or
	 *            a file (jar)
	 * @param filePath bundle relative path to desired file
	 * @return URL to the file
	 * @throws MalformedURLException
	 */
    protected URL getFileInBundle(File bundleLocation, String filePath) throws MalformedURLException {
        String extension = new Path(bundleLocation.getName()).getFileExtension();
        StringBuffer urlSt = new StringBuffer();
        if (//$NON-NLS-1$
        extension != null && extension.equals("jar") && bundleLocation.isFile()) {
            //$NON-NLS-1$
            urlSt.append("jar:file:");
            urlSt.append(bundleLocation.getAbsolutePath());
            //$NON-NLS-1$
            urlSt.append("!/");
            urlSt.append(filePath);
        } else {
            //$NON-NLS-1$
            urlSt.append("file:");
            urlSt.append(bundleLocation.getAbsolutePath());
            urlSt.append(File.separatorChar);
            urlSt.append(filePath);
        }
        return new URL(urlSt.toString());
    }

    @Override
    public synchronized String[] getExecutionEnvironments() throws CoreException {
        if (fBundleDescription == null) {
            baselineDisposed(getBaseline());
        }
        return fBundleDescription.getExecutionEnvironments();
    }

    @Override
    public final String getSymbolicName() {
        init();
        return fSymbolicName;
    }

    @Override
    public synchronized IRequiredComponentDescription[] getRequiredComponents() throws CoreException {
        if (fBundleDescription == null) {
            baselineDisposed(getBaseline());
        }
        BundleSpecification[] requiredBundles = fBundleDescription.getRequiredBundles();
        IRequiredComponentDescription[] req = new IRequiredComponentDescription[requiredBundles.length];
        for (int i = 0; i < requiredBundles.length; i++) {
            BundleSpecification bundle = requiredBundles[i];
            req[i] = new RequiredComponentDescription(bundle.getName(), new BundleVersionRange(bundle.getVersionRange()), bundle.isOptional(), bundle.isExported());
        }
        return req;
    }

    @Override
    public synchronized String getVersion() {
        init();
        // remove the qualifier
        StringBuffer buffer = new StringBuffer();
        buffer.append(fVersion.getMajor()).append('.').append(fVersion.getMinor()).append('.').append(fVersion.getMicro());
        return String.valueOf(buffer);
    }

    @Override
    public String getName() {
        init();
        return super.getName();
    }

    /**
	 * Returns this component's bundle description.
	 *
	 * @return bundle description
	 */
    public synchronized BundleDescription getBundleDescription() throws CoreException {
        init();
        if (fBundleDescription == null) {
            baselineDisposed(getBaseline());
        }
        return fBundleDescription;
    }

    @Override
    public String toString() {
        if (fBundleDescription != null) {
            try {
                StringBuffer buffer = new StringBuffer();
                buffer.append(fBundleDescription.toString());
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                " - ");
                //$NON-NLS-1$ //$NON-NLS-2$
                buffer.append("[fragment: ").append(isFragment()).append("] ");
                //$NON-NLS-1$ //$NON-NLS-2$
                buffer.append("[host: ").append(fBundleDescription.getFragments().length > 0).append("] ");
                //$NON-NLS-1$ //$NON-NLS-2$
                buffer.append("[system bundle: ").append(isSystemComponent()).append("] ");
                //$NON-NLS-1$ //$NON-NLS-2$
                buffer.append("[source bundle: ").append(isSourceComponent()).append("] ");
                //$NON-NLS-1$ //$NON-NLS-2$
                buffer.append("[dev bundle: ").append(fWorkspaceBinary).append("]");
                return buffer.toString();
            } catch (CoreException ce) {
            }
        } else {
            StringBuffer buffer = new StringBuffer();
            //$NON-NLS-1$
            buffer.append("Un-initialized Bundle Component");
            //$NON-NLS-1$ //$NON-NLS-2$
            buffer.append("[location: ").append(fLocation).append("]");
            //$NON-NLS-1$ //$NON-NLS-2$
            buffer.append("[dev bundle: ").append(fWorkspaceBinary).append("]");
            return buffer.toString();
        }
        return super.toString();
    }

    @Override
    public String getLocation() {
        return fLocation;
    }

    @Override
    public boolean isSystemComponent() {
        return false;
    }

    @Override
    public synchronized boolean isSourceComponent() throws CoreException {
        Map<String, String> manifest = getManifest();
        if (manifest == null) {
            baselineDisposed(getBaseline());
        }
        ManifestElement[] sourceBundle = null;
        try {
            sourceBundle = ManifestElement.parseHeader(IApiCoreConstants.ECLIPSE_SOURCE_BUNDLE, manifest.get(IApiCoreConstants.ECLIPSE_SOURCE_BUNDLE));
        } catch (BundleException e) {
        }
        if (sourceBundle != null) {
            // this is a source bundle with the new format
            return true;
        }
        // check for the old format
        String pluginXMLContents = readFileContents(IApiCoreConstants.PLUGIN_XML_NAME, new File(getLocation()));
        if (pluginXMLContents != null) {
            if (containsSourceExtensionPoint(pluginXMLContents)) {
                return true;
            }
        }
        // check if it contains a fragment.xml with the appropriate extension
        // point
        pluginXMLContents = readFileContents(IApiCoreConstants.FRAGMENT_XML_NAME, new File(getLocation()));
        if (pluginXMLContents != null) {
            if (containsSourceExtensionPoint(pluginXMLContents)) {
                return true;
            }
        }
        // parse XML contents to find extension points
        return false;
    }

    /**
	 * Check if the given source contains an source extension point.
	 *
	 * @param pluginXMLContents the given file contents
	 * @return true if it contains a source extension point, false otherwise
	 */
    private boolean containsSourceExtensionPoint(String pluginXMLContents) {
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
            SourceDefaultHandler defaultHandler = new SourceDefaultHandler();
            saxParser.parse(inputSource, defaultHandler);
            return defaultHandler.isSource();
        } catch (SAXException e) {
        } catch (IOException e) {
        }
        return false;
    }

    @Override
    public synchronized boolean isFragment() throws CoreException {
        init();
        if (fBundleDescription == null) {
            baselineDisposed(getBaseline());
        }
        return fBundleDescription.getHost() != null;
    }

    @Override
    public synchronized IApiComponent getHost() throws CoreException {
        init();
        if (fBundleDescription == null) {
            baselineDisposed(getBaseline());
        }
        HostSpecification host = fBundleDescription.getHost();
        if (host != null) {
            return getBaseline().getApiComponent(host.getName());
        }
        return null;
    }

    @Override
    public synchronized boolean hasFragments() throws CoreException {
        return getBundleDescription().getFragments().length != 0;
    }

    /**
	 * Sets whether this bundle has an underlying API description file.
	 *
	 * @param hasApiDescription whether this bundle has an underlying API
	 *            description file
	 */
    protected void setHasApiDescription(boolean hasApiDescription) {
        fHasApiDescription = hasApiDescription;
    }

    @Override
    public boolean hasApiDescription() {
        // ensure initialized
        try {
            getApiDescription();
        } catch (CoreException e) {
        }
        return fHasApiDescription;
    }

    @Override
    public String[] getLowestEEs() throws CoreException {
        if (lowestEEs != null) {
            return lowestEEs;
        }
        String[] temp = null;
        String[] executionEnvironments = getExecutionEnvironments();
        int length = executionEnvironments.length;
        switch(length) {
            case 0:
                return null;
            case 1:
                temp = new String[] { executionEnvironments[0] };
                break;
            default:
                int values = ProfileModifiers.NO_PROFILE_VALUE;
                for (int i = 0; i < length; i++) {
                    values |= ProfileModifiers.getValue(executionEnvironments[i]);
                }
                if (ProfileModifiers.isJRE(values)) {
                    if (ProfileModifiers.isJRE_1_1(values)) {
                        temp = new String[] { ProfileModifiers.JRE_1_1_NAME };
                    } else if (ProfileModifiers.isJ2SE_1_2(values)) {
                        temp = new String[] { ProfileModifiers.J2SE_1_2_NAME };
                    } else if (ProfileModifiers.isJ2SE_1_3(values)) {
                        temp = new String[] { ProfileModifiers.J2SE_1_3_NAME };
                    } else if (ProfileModifiers.isJ2SE_1_4(values)) {
                        temp = new String[] { ProfileModifiers.J2SE_1_4_NAME };
                    } else if (ProfileModifiers.isJ2SE_1_5(values)) {
                        temp = new String[] { ProfileModifiers.J2SE_1_5_NAME };
                    } else if (ProfileModifiers.isJAVASE_1_6(values)) {
                        temp = new String[] { ProfileModifiers.JAVASE_1_6_NAME };
                    } else if (ProfileModifiers.isJAVASE_1_7(values)) {
                        temp = new String[] { ProfileModifiers.JAVASE_1_7_NAME };
                    } else {
                        // this is 1.8
                        temp = new String[] { ProfileModifiers.JAVASE_1_8_NAME };
                    }
                }
                if (ProfileModifiers.isCDC_Foundation(values)) {
                    if (ProfileModifiers.isCDC_1_0_FOUNDATION_1_0(values)) {
                        if (temp != null) {
                            temp = new String[] { temp[0], ProfileModifiers.CDC_1_0_FOUNDATION_1_0_NAME };
                        } else {
                            temp = new String[] { ProfileModifiers.CDC_1_0_FOUNDATION_1_0_NAME };
                        }
                    } else {
                        if (temp != null) {
                            temp = new String[] { temp[0], ProfileModifiers.CDC_1_1_FOUNDATION_1_1_NAME };
                        } else {
                            temp = new String[] { ProfileModifiers.CDC_1_1_FOUNDATION_1_1_NAME };
                        }
                    }
                }
                if (ProfileModifiers.isOSGi(values)) {
                    if (ProfileModifiers.isOSGI_MINIMUM_1_0(values)) {
                        if (temp != null) {
                            int tempLength = temp.length;
                            System.arraycopy(temp, 0, (temp = new String[tempLength + 1]), 0, tempLength);
                            temp[tempLength] = ProfileModifiers.OSGI_MINIMUM_1_0_NAME;
                        } else {
                            temp = new String[] { ProfileModifiers.OSGI_MINIMUM_1_0_NAME };
                        }
                    } else if (ProfileModifiers.isOSGI_MINIMUM_1_1(values)) {
                        if (temp != null) {
                            int tempLength = temp.length;
                            System.arraycopy(temp, 0, (temp = new String[tempLength + 1]), 0, tempLength);
                            temp[tempLength] = ProfileModifiers.OSGI_MINIMUM_1_1_NAME;
                        } else {
                            temp = new String[] { ProfileModifiers.OSGI_MINIMUM_1_1_NAME };
                        }
                    } else {
                        // OSGI_MINIMUM_1_2
                        if (temp != null) {
                            int tempLength = temp.length;
                            System.arraycopy(temp, 0, (temp = new String[tempLength + 1]), 0, tempLength);
                            temp[tempLength] = ProfileModifiers.OSGI_MINIMUM_1_2_NAME;
                        } else {
                            temp = new String[] { ProfileModifiers.OSGI_MINIMUM_1_2_NAME };
                        }
                    }
                }
        }
        lowestEEs = temp;
        return temp;
    }

    @Override
    public synchronized ResolverError[] getErrors() throws CoreException {
        init();
        ApiBaseline baseline = (ApiBaseline) getBaseline();
        if (fBundleDescription == null) {
            baselineDisposed(baseline);
        }
        if (baseline != null) {
            ResolverError[] resolverErrors = baseline.getState().getResolverErrors(fBundleDescription);
            if (resolverErrors.length == 0) {
                return null;
            }
            return resolverErrors;
        }
        return null;
    }

    /**
	 * @param baseline the baseline that is disposed
	 * @throws CoreException with the baseline disposed information
	 */
    protected void baselineDisposed(IApiBaseline baseline) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, ApiPlugin.PLUGIN_ID, ApiPlugin.REPORT_BASELINE_IS_DISPOSED, NLS.bind(Messages.BundleApiComponent_baseline_disposed, baseline.getName()), null));
    }
}
