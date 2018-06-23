/****************************************************************************
 * Copyright (c) 2006, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.PlatformHelper;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowser;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowserFactory;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;
import org.eclipse.ecf.filetransfer.service.ISendFileTransfer;
import org.eclipse.ecf.filetransfer.service.ISendFileTransferFactory;
import org.eclipse.ecf.provider.filetransfer.IFileTransferProtocolToFactoryMapper;
import org.eclipse.ecf.provider.filetransfer.retrieve.MultiProtocolRetrieveAdapter;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator, IFileTransferProtocolToFactoryMapper {

    // This is the name of a system property 'org.eclipse.ecf.provider.filetransfer.excludeContributors' 
    // that allows people to defeat the priority system for the browse, send, and retrieve factory contributions.
    // If a plugin (symbolic id) is given in this property (multiples separated by comma), then
    // that plugin's contributions will *not* be added to the browse, send, and retrieve protocol factories
    //$NON-NLS-1$
    public static final String PLUGIN_EXCLUDED_SYS_PROP_NAME = Activator.PLUGIN_ID + ".excludeContributors";

    //$NON-NLS-1$
    private static final String CLASS_ATTR = "class";

    //$NON-NLS-1$
    private static final String PRIORITY_ATTR = "priority";

    private static final int DEFAULT_PRIORITY = 100;

    //$NON-NLS-1$
    private static final String PROTOCOL_ATTR = "protocol";

    //$NON-NLS-1$
    private static final String URI_ATTR = "uri";

    private static final String[] jvmSchemes = new String[] { Messages.FileTransferNamespace_Http_Protocol, Messages.FileTransferNamespace_Ftp_Protocol, Messages.FileTransferNamespace_File_Protocol, Messages.FileTransferNamespace_Jar_Protocol, Messages.FileTransferNamespace_Https_Protocol, Messages.FileTransferNamespace_Mailto_Protocol, Messages.FileTransferNamespace_Gopher_Protocol };

    //$NON-NLS-1$
    private static final String URL_HANDLER_PROTOCOL_NAME = "url.handler.protocol";

    //$NON-NLS-1$
    private static final String URLSTREAM_HANDLER_SERVICE_NAME = "org.osgi.service.url.URLStreamHandlerService";

    // The plug-in ID
    //$NON-NLS-1$
    public static final String PLUGIN_ID = "org.eclipse.ecf.provider.filetransfer";

    //$NON-NLS-1$
    private static final String RETRIEVE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME = "retrieveFileTransferProtocolFactory";

    private static final String RETRIEVE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT = //$NON-NLS-1$
    PLUGIN_ID + "." + RETRIEVE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME;

    //$NON-NLS-1$
    private static final String SEND_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME = "sendFileTransferProtocolFactory";

    private static final String SEND_FILETRANSFER_PROTOCOL_FACTORY_EPOINT = //$NON-NLS-1$
    PLUGIN_ID + "." + SEND_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME;

    //$NON-NLS-1$
    private static final String BROWSE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME = "browseFileTransferProtocolFactory";

    private static final String BROWSE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT = //$NON-NLS-1$
    PLUGIN_ID + "." + BROWSE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME;

    // The shared instance
    private static Activator plugin;

    private BundleContext context = null;

    private ServiceRegistration fileTransferServiceRegistration;

    private ServiceTracker logServiceTracker = null;

    private ServiceTracker extensionRegistryTracker = null;

    private Map retrieveFileTransferProtocolMap;

    private Map sendFileTransferProtocolMap;

    private Map browseFileTransferProtocolMap;

    private ServiceTracker adapterManagerTracker = null;

    private ServiceTracker proxyServiceTracker = null;

    private IURLConnectionModifier urlConnectionModifier = null;

    private String[] excludedPlugins = null;

    private ServiceRegistration protocolMapperRegistration;

    private IRegistryChangeListener registryChangeListener = new IRegistryChangeListener() {

        public void registryChanged(IRegistryChangeEvent event) {
            final IExtensionDelta retrieveDelta[] = event.getExtensionDeltas(PLUGIN_ID, RETRIEVE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME);
            for (int i = 0; i < retrieveDelta.length; i++) {
                switch(retrieveDelta[i].getKind()) {
                    case IExtensionDelta.ADDED:
                        addRetrieveExtensions(retrieveDelta[i].getExtension().getConfigurationElements());
                        break;
                    case IExtensionDelta.REMOVED:
                        removeRetrieveExtensions(retrieveDelta[i].getExtension().getConfigurationElements());
                        break;
                }
            }
            final IExtensionDelta sendDelta[] = event.getExtensionDeltas(PLUGIN_ID, SEND_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME);
            for (int i = 0; i < sendDelta.length; i++) {
                switch(sendDelta[i].getKind()) {
                    case IExtensionDelta.ADDED:
                        addSendExtensions(sendDelta[i].getExtension().getConfigurationElements());
                        break;
                    case IExtensionDelta.REMOVED:
                        removeSendExtensions(sendDelta[i].getExtension().getConfigurationElements());
                        break;
                }
            }
            final IExtensionDelta browseDelta[] = event.getExtensionDeltas(PLUGIN_ID, BROWSE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT_NAME);
            for (int i = 0; i < browseDelta.length; i++) {
                switch(browseDelta[i].getKind()) {
                    case IExtensionDelta.ADDED:
                        addBrowseExtensions(browseDelta[i].getExtension().getConfigurationElements());
                        break;
                    case IExtensionDelta.REMOVED:
                        removeBrowseExtensions(browseDelta[i].getExtension().getConfigurationElements());
                        break;
                }
            }
        }
    };

    private String[] parseExcludedPlugins() {
        String prop = System.getProperty(PLUGIN_EXCLUDED_SYS_PROP_NAME);
        if (prop == null)
            return new String[0];
        //$NON-NLS-1$
        StringTokenizer tok = new StringTokenizer(prop, ",");
        int count = tok.countTokens();
        String[] results = new String[count];
        for (int i = 0; i < count; i++) {
            results[i] = tok.nextToken();
        }
        return results;
    }

    /**
	 * The constructor
	 */
    public  Activator() {
    //
    }

    protected LogService getLogService() {
        synchronized (this) {
            if (this.context == null)
                return null;
            if (logServiceTracker == null) {
                logServiceTracker = new ServiceTracker(this.context, LogService.class.getName(), null);
                logServiceTracker.open();
            }
            return (LogService) logServiceTracker.getService();
        }
    }

    public IProxyService getProxyService() {
        try {
            if (proxyServiceTracker == null) {
                proxyServiceTracker = new ServiceTracker(this.context, IProxyService.class.getName(), null);
                proxyServiceTracker.open();
            }
            return (IProxyService) proxyServiceTracker.getService();
        } catch (Exception e) {
            logNoProxyWarning(e);
        } catch (NoClassDefFoundError e) {
            logNoProxyWarning(e);
        }
        return null;
    }

    public static void logNoProxyWarning(Throwable e) {
        Activator a = getDefault();
        if (a != null) {
            //$NON-NLS-1$
            a.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.ERROR, "Warning: Platform proxy API not available", e));
        }
    }

    public void log(IStatus status) {
        if (this.context == null)
            return;
        final LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogHelper.getLogCode(status), LogHelper.getLogMessage(status), status.getException());
        }
    }

    public Bundle getBundle() {
        if (context == null)
            return null;
        return context.getBundle();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
    public void start(BundleContext ctxt) throws Exception {
        plugin = this;
        this.context = ctxt;
        this.retrieveFileTransferProtocolMap = new HashMap(3);
        this.sendFileTransferProtocolMap = new HashMap(3);
        this.browseFileTransferProtocolMap = new HashMap(3);
        // initialize the default url connection modifier for ssl
        try {
            //$NON-NLS-1$
            Class urlConnectionModifierClass = Class.forName("org.eclipse.ecf.internal.provider.filetransfer.ssl.ECFURLConnectionModifier");
            urlConnectionModifier = (IURLConnectionModifier) urlConnectionModifierClass.newInstance();
            urlConnectionModifier.init(ctxt);
        } catch (ClassNotFoundException e) {
        } catch (Throwable t) {
            log(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), "Unexpected Error in Activator.start", t));
        }
        fileTransferServiceRegistration = ctxt.registerService(IRetrieveFileTransferFactory.class.getName(), new IRetrieveFileTransferFactory() {

            public IRetrieveFileTransfer newInstance() {
                return new MultiProtocolRetrieveAdapter();
            }
        }, null);
        this.extensionRegistryTracker = new ServiceTracker(ctxt, IExtensionRegistry.class.getName(), null);
        this.extensionRegistryTracker.open();
        final IExtensionRegistry registry = getExtensionRegistry();
        if (registry != null) {
            registry.addRegistryChangeListener(registryChangeListener);
        }
        // Can't be lazy about this, as schemes need to be registered with
        // platform
        loadProtocolHandlers();
        // Finally, register this object as a IFileTransferProtocolToFactoryMapper service
        protocolMapperRegistration = context.registerService(IFileTransferProtocolToFactoryMapper.class.getName(), this, null);
    }

    public boolean reinitialize() {
        try {
            loadProtocolHandlers();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
    public void stop(BundleContext ctxt) throws Exception {
        final IExtensionRegistry registry = getExtensionRegistry();
        if (registry != null) {
            registry.removeRegistryChangeListener(registryChangeListener);
        }
        if (urlConnectionModifier != null) {
            urlConnectionModifier.dispose();
            urlConnectionModifier = null;
        }
        if (extensionRegistryTracker != null) {
            extensionRegistryTracker.close();
            extensionRegistryTracker = null;
        }
        if (fileTransferServiceRegistration != null) {
            fileTransferServiceRegistration.unregister();
            fileTransferServiceRegistration = null;
        }
        if (adapterManagerTracker != null) {
            adapterManagerTracker.close();
            adapterManagerTracker = null;
        }
        if (proxyServiceTracker != null) {
            proxyServiceTracker.close();
            proxyServiceTracker = null;
        }
        if (this.retrieveFileTransferProtocolMap != null) {
            this.retrieveFileTransferProtocolMap.clear();
            this.retrieveFileTransferProtocolMap = null;
        }
        if (this.sendFileTransferProtocolMap != null) {
            this.sendFileTransferProtocolMap.clear();
            this.sendFileTransferProtocolMap = null;
        }
        if (this.browseFileTransferProtocolMap != null) {
            this.browseFileTransferProtocolMap.clear();
            this.browseFileTransferProtocolMap = null;
        }
        if (this.protocolMapperRegistration != null) {
            this.protocolMapperRegistration.unregister();
            this.protocolMapperRegistration = null;
        }
        synchronized (this) {
            this.context = null;
        }
        plugin = null;
    }

    /**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
    public static synchronized Activator getDefault() {
        if (plugin == null) {
            plugin = new Activator();
        }
        return plugin;
    }

    public String[] getPlatformSupportedSchemes() {
        final ServiceTracker handlers = new ServiceTracker(context, URLSTREAM_HANDLER_SERVICE_NAME, null);
        handlers.open();
        final ServiceReference[] refs = handlers.getServiceReferences();
        final Set protocols = new HashSet();
        if (refs != null)
            for (int i = 0; i < refs.length; i++) {
                final Object protocol = refs[i].getProperty(URL_HANDLER_PROTOCOL_NAME);
                if (protocol instanceof String)
                    protocols.add(protocol);
                else if (protocol instanceof String[]) {
                    final String[] ps = (String[]) protocol;
                    for (int j = 0; j < ps.length; j++) protocols.add(ps[j]);
                }
            }
        handlers.close();
        for (int i = 0; i < jvmSchemes.length; i++) protocols.add(jvmSchemes[i]);
        return (String[]) protocols.toArray(new String[] {});
    }

    public IExtensionRegistry getExtensionRegistry() {
        if (extensionRegistryTracker == null) {
            this.extensionRegistryTracker = new ServiceTracker(context, IExtensionRegistry.class.getName(), null);
            this.extensionRegistryTracker.open();
        }
        return (IExtensionRegistry) extensionRegistryTracker.getService();
    }

    static class ProtocolFactory implements Comparable {

        Object factory;

        int priority = 0;

        String id;

        public  ProtocolFactory(Object factory, int priority, String id) {
            this.factory = factory;
            this.priority = priority;
            this.id = id;
        }

        public Object getFactory() {
            return factory;
        }

        public String getID() {
            return id;
        }

        public int getPriority() {
            return priority;
        }

        /* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
        public int compareTo(Object another) {
            if (!(another instanceof ProtocolFactory))
                return -1;
            ProtocolFactory other = (ProtocolFactory) another;
            if (this.priority == other.priority)
                return 0;
            return (this.priority < other.priority) ? -1 : 1;
        }
    }

    private int getPriority(IConfigurationElement configElement, String warning, String protocol) {
        // Get priority for new entry, if optional priority attribute specified
        final String priorityString = configElement.getAttribute(PRIORITY_ATTR);
        int priority = DEFAULT_PRIORITY;
        if (priorityString != null) {
            try {
                priority = new Integer(priorityString).intValue();
                // Make sure that any negative values are reset to 0 (highest priority)
                priority = (priority < 0) ? 0 : priority;
            } catch (NumberFormatException e) {
                Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, NLS.bind("{0} for {1} from {2} has invalid priority {3}. Priority will be set to {4}", new Object[] { warning, protocol, configElement.getDeclaringExtension().getContributor().getName(), priorityString, String.valueOf(DEFAULT_PRIORITY) }), null));
            }
        }
        return priority;
    }

    boolean pluginExcluded(String pluginId) {
        if (excludedPlugins == null) {
            excludedPlugins = parseExcludedPlugins();
        }
        List l = Arrays.asList(excludedPlugins);
        return l.contains(pluginId);
    }

    void addRetrieveExtensions(IConfigurationElement[] configElements) {
        for (int i = 0; i < configElements.length; i++) {
            final String protocol = configElements[i].getAttribute(PROTOCOL_ATTR);
            if (//$NON-NLS-1$
            protocol == null || "".equals(protocol))
                return;
            String uriStr = configElements[i].getAttribute(URI_ATTR);
            boolean uri = (uriStr == null) ? false : Boolean.valueOf(uriStr).booleanValue();
            //$NON-NLS-1$
            String CONTRIBUTION_WARNING = "File retrieve contribution";
            try {
                String pluginId = configElements[i].getDeclaringExtension().getContributor().getName();
                // Only add the factories if the contributor plugin has not been excluded
                if (!pluginExcluded(pluginId)) {
                    // First create factory clazz 
                    final IRetrieveFileTransferFactory retrieveFactory = (IRetrieveFileTransferFactory) configElements[i].createExecutableExtension(CLASS_ATTR);
                    // Get priority for new entry, if optional priority attribute specified
                    int priority = getPriority(configElements[i], CONTRIBUTION_WARNING, protocol);
                    String contributorName = configElements[i].getDeclaringExtension().getContributor().getName();
                    // Now add new ProtocolFactory
                    setRetrieveFileTransferFactory(protocol, contributorName, retrieveFactory, priority, uri);
                } else {
                    //$NON-NLS-1$ //$NON-NLS-2$
                    Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, "Plugin " + pluginId + " excluded from contributing retrieve factory", null));
                }
            } catch (final CoreException e) {
                Activator.getDefault().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, NLS.bind("Error loading from {0} extension point", RETRIEVE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT), e));
            }
        }
    }

    void removeRetrieveExtensions(IConfigurationElement[] configElements) {
        for (int i = 0; i < configElements.length; i++) {
            final String protocol = configElements[i].getAttribute(PROTOCOL_ATTR);
            if (//$NON-NLS-1$
            protocol == null || "".equals(protocol))
                return;
            String id = getRetrieveFileTransferFactoryId(protocol);
            if (id != null)
                removeRetrieveFileTransferFactory(id);
        }
    }

    void addSendExtensions(IConfigurationElement[] configElements) {
        for (int i = 0; i < configElements.length; i++) {
            final String protocol = configElements[i].getAttribute(PROTOCOL_ATTR);
            if (//$NON-NLS-1$
            protocol == null || "".equals(protocol))
                return;
            String uriStr = configElements[i].getAttribute(URI_ATTR);
            boolean uri = (uriStr == null) ? false : Boolean.valueOf(uriStr).booleanValue();
            //$NON-NLS-1$
            String CONTRIBUTION_WARNING = "File send contribution";
            try {
                String pluginId = configElements[i].getDeclaringExtension().getContributor().getName();
                // Only add the factories if the contributor plugin has not been excluded
                if (!pluginExcluded(pluginId)) {
                    // First create factory clazz 
                    final ISendFileTransferFactory clazz = (ISendFileTransferFactory) configElements[i].createExecutableExtension(CLASS_ATTR);
                    // Get priority for new entry, if optional priority attribute specified
                    int priority = getPriority(configElements[i], CONTRIBUTION_WARNING, protocol);
                    setSendFileTransferFactory(protocol, pluginId, clazz, priority, uri);
                } else {
                    //$NON-NLS-1$ //$NON-NLS-2$
                    Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, "Plugin " + pluginId + " excluded from contributing send factory", null));
                }
            } catch (final CoreException e) {
                Activator.getDefault().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, NLS.bind("Error loading from {0} extension point", SEND_FILETRANSFER_PROTOCOL_FACTORY_EPOINT), e));
            }
        }
    }

    void removeSendExtensions(IConfigurationElement[] configElements) {
        for (int i = 0; i < configElements.length; i++) {
            final String protocol = configElements[i].getAttribute(PROTOCOL_ATTR);
            if (//$NON-NLS-1$
            protocol == null || "".equals(protocol))
                return;
            String id = getSendFileTransferFactoryId(protocol);
            if (id != null)
                removeSendFileTransferFactory(id);
        }
    }

    void addBrowseExtensions(IConfigurationElement[] configElements) {
        for (int i = 0; i < configElements.length; i++) {
            final String protocol = configElements[i].getAttribute(PROTOCOL_ATTR);
            if (//$NON-NLS-1$
            protocol == null || "".equals(protocol))
                return;
            String uriStr = configElements[i].getAttribute(URI_ATTR);
            boolean uri = (uriStr == null) ? false : Boolean.valueOf(uriStr).booleanValue();
            //$NON-NLS-1$
            String CONTRIBUTION_WARNING = "File browse contribution";
            try {
                String pluginId = configElements[i].getDeclaringExtension().getContributor().getName();
                // Only add the factories if the contributor plugin has not been excluded
                if (!pluginExcluded(pluginId)) {
                    // First create factory clazz 
                    final IRemoteFileSystemBrowserFactory clazz = (IRemoteFileSystemBrowserFactory) configElements[i].createExecutableExtension(CLASS_ATTR);
                    // Get priority for new entry, if optional priority attribute specified
                    int priority = getPriority(configElements[i], CONTRIBUTION_WARNING, protocol);
                    setBrowseFileTransferFactory(protocol, pluginId, clazz, priority, uri);
                } else {
                    //$NON-NLS-1$ //$NON-NLS-2$
                    Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, "Plugin " + pluginId + " excluded from contributing browse factory", null));
                }
            } catch (final CoreException e) {
                Activator.getDefault().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, NLS.bind("Error loading from {0} extension point", BROWSE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT), e));
            }
        }
    }

    void removeBrowseExtensions(IConfigurationElement[] configElements) {
        for (int i = 0; i < configElements.length; i++) {
            final String protocol = configElements[i].getAttribute(PROTOCOL_ATTR);
            if (//$NON-NLS-1$
            protocol == null || "".equals(protocol))
                return;
            String id = getBrowseFileTransferFactoryId(protocol);
            if (id != null)
                removeBrowseFileTransferFactory(id);
        }
    }

    private void loadProtocolHandlers() {
        final IExtensionRegistry reg = getExtensionRegistry();
        if (reg != null) {
            final IExtensionPoint retrieveExtensionPoint = reg.getExtensionPoint(RETRIEVE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT);
            if (retrieveExtensionPoint != null)
                addRetrieveExtensions(retrieveExtensionPoint.getConfigurationElements());
            // Now do it with send
            final IExtensionPoint sendExtensionPoint = reg.getExtensionPoint(SEND_FILETRANSFER_PROTOCOL_FACTORY_EPOINT);
            if (sendExtensionPoint != null)
                addSendExtensions(sendExtensionPoint.getConfigurationElements());
            // Now for browse
            final IExtensionPoint browseExtensionPoint = reg.getExtensionPoint(BROWSE_FILETRANSFER_PROTOCOL_FACTORY_EPOINT);
            if (browseExtensionPoint != null)
                addBrowseExtensions(browseExtensionPoint.getConfigurationElements());
        }
    }

    private boolean isSchemeRegistered(String protocol, String[] schemes) {
        for (int i = 0; i < schemes.length; i++) {
            if (protocol.equals(schemes[i]))
                return true;
        }
        return false;
    }

    class DummyURLStreamHandlerService extends AbstractURLStreamHandlerService {

        /*
		 * (non-Javadoc)
		 * 
		 * @see org.osgi.service.url.AbstractURLStreamHandlerService#openConnection(java.net.URL)
		 */
        public URLConnection openConnection(URL u) throws IOException {
            //$NON-NLS-1$
            throw new IOException(NLS.bind("URLConnection cannot be created for {0}", u.toExternalForm()));
        }
    }

    private final DummyURLStreamHandlerService dummyService = new DummyURLStreamHandlerService();

    private void registerScheme(String protocol) {
        final Hashtable properties = new Hashtable();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { protocol });
        context.registerService(URLStreamHandlerService.class.getName(), dummyService, properties);
    }

    public IRetrieveFileTransfer getFileTransfer(String protocol) {
        ProtocolFactory protocolFactory = null;
        synchronized (retrieveFileTransferProtocolMap) {
            protocolFactory = (ProtocolFactory) retrieveFileTransferProtocolMap.get(protocol);
        }
        if (protocolFactory == null)
            return null;
        final IRetrieveFileTransferFactory factory = (IRetrieveFileTransferFactory) protocolFactory.getFactory();
        if (factory != null)
            return factory.newInstance();
        return null;
    }

    public ISendFileTransfer getSendFileTransfer(String protocol) {
        ProtocolFactory protocolFactory = null;
        synchronized (sendFileTransferProtocolMap) {
            protocolFactory = (ProtocolFactory) sendFileTransferProtocolMap.get(protocol);
        }
        if (protocolFactory == null)
            return null;
        final ISendFileTransferFactory factory = (ISendFileTransferFactory) protocolFactory.getFactory();
        if (factory != null)
            return factory.newInstance();
        return null;
    }

    public IRemoteFileSystemBrowser getBrowseFileTransfer(String protocol) {
        ProtocolFactory protocolFactory = null;
        synchronized (browseFileTransferProtocolMap) {
            protocolFactory = (ProtocolFactory) browseFileTransferProtocolMap.get(protocol);
        }
        if (protocolFactory == null)
            return null;
        final IRemoteFileSystemBrowserFactory factory = (IRemoteFileSystemBrowserFactory) protocolFactory.getFactory();
        if (factory != null)
            return factory.newInstance();
        return null;
    }

    public IAdapterManager getAdapterManager() {
        // First, try to get the adapter manager via
        if (adapterManagerTracker == null) {
            adapterManagerTracker = new ServiceTracker(this.context, IAdapterManager.class.getName(), null);
            adapterManagerTracker.open();
        }
        IAdapterManager adapterManager = (IAdapterManager) adapterManagerTracker.getService();
        // PlatformHelper class
        if (adapterManager == null)
            adapterManager = PlatformHelper.getPlatformAdapterManager();
        return adapterManager;
    }

    public IURLConnectionModifier getURLConnectionModifier() {
        return urlConnectionModifier;
    }

    public boolean setRetrieveFileTransferFactory(String protocol, String id, IRetrieveFileTransferFactory factory, int priority) {
        return setRetrieveFileTransferFactory(protocol, id, factory, priority, false);
    }

    public boolean setRetrieveFileTransferFactory(String protocol, String id, IRetrieveFileTransferFactory factory, int priority, boolean uri) {
        if (//$NON-NLS-1$
        protocol == null || "".equals(protocol))
            return false;
        if (id == null)
            return false;
        if (factory == null)
            return false;
        if (!pluginExcluded(id)) {
            // Now create new ProtocolFactory
            ProtocolFactory newProtocolFactory = new ProtocolFactory(factory, priority, id);
            synchronized (retrieveFileTransferProtocolMap) {
                ProtocolFactory oldProtocolFactory = (ProtocolFactory) retrieveFileTransferProtocolMap.get(protocol);
                // If found, choose between them based upon comparing their priority
                if (oldProtocolFactory != null) {
                    // Now, compare priorities and pick winner
                    String CONTRIBUTION_WARNING = "File retrieve contribution";
                    int result = oldProtocolFactory.compareTo(newProtocolFactory);
                    if (result < 0) {
                        // Existing one has higher priority, so we provide warning and return (leaving existing one as the handler)
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be ignored.  Existing protocol factory has higher priority.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id }), null));
                        return false;
                    } else if (result == 0) {
                        // Warn that we are using new one because they have the same priority.
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be used in preference to existing handler.  Both have same priority={3}.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id, new Integer(priority) }), null));
                    } else if (result > 0) {
                        // Warn that we are using new one because it has higher priority.
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be used in preference to existing handler.  New handler has higher priority={3}<{4}.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id, new Integer(priority), new Integer(oldProtocolFactory.priority) }), null));
                    }
                }
                // If !uri, then check/register protocol as URLStreamHandlerService
                if (!uri) {
                    String[] existingSchemes = getPlatformSupportedSchemes();
                    if (!isSchemeRegistered(protocol, existingSchemes))
                        registerScheme(protocol);
                }
                // Finally, put protocol factory in map with protocol as key
                retrieveFileTransferProtocolMap.put(protocol, newProtocolFactory);
                return true;
            }
        }
        return false;
    }

    public String getRetrieveFileTransferFactoryId(String protocol) {
        if (protocol == null)
            return null;
        synchronized (retrieveFileTransferProtocolMap) {
            ProtocolFactory oldProtocolFactory = (ProtocolFactory) retrieveFileTransferProtocolMap.get(protocol);
            if (oldProtocolFactory == null)
                return null;
            return oldProtocolFactory.getID();
        }
    }

    public int getRetrieveFileTransferPriority(String protocol) {
        if (protocol == null)
            return -1;
        synchronized (retrieveFileTransferProtocolMap) {
            ProtocolFactory oldProtocolFactory = (ProtocolFactory) retrieveFileTransferProtocolMap.get(protocol);
            if (oldProtocolFactory == null)
                return -1;
            return oldProtocolFactory.getPriority();
        }
    }

    public boolean removeRetrieveFileTransferFactory(String id) {
        if (id == null)
            return false;
        boolean removed = false;
        synchronized (retrieveFileTransferProtocolMap) {
            for (Iterator i = retrieveFileTransferProtocolMap.keySet().iterator(); i.hasNext(); ) {
                ProtocolFactory oldProtocolFactory = (ProtocolFactory) retrieveFileTransferProtocolMap.get(i.next());
                if (oldProtocolFactory == null)
                    continue;
                if (id.equals(oldProtocolFactory.getID())) {
                    i.remove();
                    removed = true;
                }
            }
            return removed;
        }
    }

    public boolean setBrowseFileTransferFactory(String protocol, String id, IRemoteFileSystemBrowserFactory factory, int priority) {
        return setBrowseFileTransferFactory(protocol, id, factory, priority, false);
    }

    public boolean setBrowseFileTransferFactory(String protocol, String id, IRemoteFileSystemBrowserFactory factory, int priority, boolean uri) {
        if (//$NON-NLS-1$
        protocol == null || "".equals(protocol))
            return false;
        if (id == null)
            return false;
        if (factory == null)
            return false;
        if (!pluginExcluded(id)) {
            // Now create new ProtocolFactory
            ProtocolFactory newProtocolFactory = new ProtocolFactory(factory, priority, id);
            synchronized (browseFileTransferProtocolMap) {
                ProtocolFactory oldProtocolFactory = (ProtocolFactory) browseFileTransferProtocolMap.get(protocol);
                // If found, choose between them based upon comparing their priority
                if (oldProtocolFactory != null) {
                    // Now, compare priorities and pick winner
                    String CONTRIBUTION_WARNING = "File browse contribution";
                    int result = oldProtocolFactory.compareTo(newProtocolFactory);
                    if (result < 0) {
                        // Existing one has higher priority, so we provide warning and return (leaving existing one as the handler)
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be ignored.  Existing protocol factory has higher priority.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id }), null));
                        return false;
                    } else if (result == 0) {
                        // Warn that we are using new one because they have the same priority.
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be used in preference to existing handler.  Both have same priority={3}.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id, new Integer(priority) }), null));
                    } else if (result > 0) {
                        // Warn that we are using new one because it has higher priority.
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be used in preference to existing handler.  New handler has higher priority={3}<{4}.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id, new Integer(priority), new Integer(oldProtocolFactory.priority) }), null));
                    }
                }
                // If !uri, then check/register protocol as URLStreamHandlerService
                if (!uri) {
                    String[] existingSchemes = getPlatformSupportedSchemes();
                    if (!isSchemeRegistered(protocol, existingSchemes))
                        registerScheme(protocol);
                }
                // Finally, put protocol factory in map with protocol as key
                browseFileTransferProtocolMap.put(protocol, newProtocolFactory);
                return true;
            }
        }
        return false;
    }

    public String getBrowseFileTransferFactoryId(String protocol) {
        if (protocol == null)
            return null;
        synchronized (browseFileTransferProtocolMap) {
            ProtocolFactory oldProtocolFactory = (ProtocolFactory) browseFileTransferProtocolMap.get(protocol);
            if (oldProtocolFactory == null)
                return null;
            return oldProtocolFactory.getID();
        }
    }

    public int getBrowseFileTransferPriority(String protocol) {
        if (protocol == null)
            return -1;
        synchronized (browseFileTransferProtocolMap) {
            ProtocolFactory oldProtocolFactory = (ProtocolFactory) browseFileTransferProtocolMap.get(protocol);
            if (oldProtocolFactory == null)
                return -1;
            return oldProtocolFactory.getPriority();
        }
    }

    public boolean removeBrowseFileTransferFactory(String id) {
        if (id == null)
            return false;
        boolean removed = false;
        synchronized (browseFileTransferProtocolMap) {
            for (Iterator i = browseFileTransferProtocolMap.keySet().iterator(); i.hasNext(); ) {
                ProtocolFactory oldProtocolFactory = (ProtocolFactory) browseFileTransferProtocolMap.get(i.next());
                if (oldProtocolFactory == null)
                    continue;
                if (id.equals(oldProtocolFactory.getID())) {
                    i.remove();
                    removed = true;
                }
            }
            return removed;
        }
    }

    public boolean setSendFileTransferFactory(String protocol, String id, ISendFileTransferFactory factory, int priority) {
        return setSendFileTransferFactory(protocol, id, factory, priority, false);
    }

    public boolean setSendFileTransferFactory(String protocol, String id, ISendFileTransferFactory factory, int priority, boolean uri) {
        if (//$NON-NLS-1$
        protocol == null || "".equals(protocol))
            return false;
        if (id == null)
            return false;
        if (factory == null)
            return false;
        if (!pluginExcluded(id)) {
            // Now create new ProtocolFactory
            ProtocolFactory newProtocolFactory = new ProtocolFactory(factory, priority, id);
            synchronized (sendFileTransferProtocolMap) {
                ProtocolFactory oldProtocolFactory = (ProtocolFactory) sendFileTransferProtocolMap.get(protocol);
                // If found, choose between them based upon comparing their priority
                if (oldProtocolFactory != null) {
                    // Now, compare priorities and pick winner
                    String CONTRIBUTION_WARNING = "File send contribution";
                    int result = oldProtocolFactory.compareTo(newProtocolFactory);
                    if (result < 0) {
                        // Existing one has higher priority, so we provide warning and return (leaving existing one as the handler)
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be ignored.  Existing protocol factory has higher priority.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id }), null));
                        return false;
                    } else if (result == 0) {
                        // Warn that we are using new one because they have the same priority.
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be used in preference to existing handler.  Both have same priority={3}.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id, new Integer(priority) }), null));
                    } else if (result > 0) {
                        // Warn that we are using new one because it has higher priority.
                        Activator.getDefault().log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, //$NON-NLS-1$
                        NLS.bind(//$NON-NLS-1$
                        "{0} for protocol {1} from {2} will be used in preference to existing handler.  New handler has higher priority={3}<{4}.", //$NON-NLS-1$
                        new Object[] { CONTRIBUTION_WARNING, protocol, id, new Integer(priority), new Integer(oldProtocolFactory.priority) }), null));
                    }
                }
                // If !uri, then check/register protocol as URLStreamHandlerService
                if (!uri) {
                    String[] existingSchemes = getPlatformSupportedSchemes();
                    if (!isSchemeRegistered(protocol, existingSchemes))
                        registerScheme(protocol);
                }
                // Finally, put protocol factory in map with protocol as key
                sendFileTransferProtocolMap.put(protocol, newProtocolFactory);
                return true;
            }
        }
        return false;
    }

    public String getSendFileTransferFactoryId(String protocol) {
        if (protocol == null)
            return null;
        synchronized (sendFileTransferProtocolMap) {
            ProtocolFactory oldProtocolFactory = (ProtocolFactory) sendFileTransferProtocolMap.get(protocol);
            if (oldProtocolFactory == null)
                return null;
            return oldProtocolFactory.getID();
        }
    }

    public int getSendFileTransferPriority(String protocol) {
        if (protocol == null)
            return -1;
        synchronized (sendFileTransferProtocolMap) {
            ProtocolFactory oldProtocolFactory = (ProtocolFactory) sendFileTransferProtocolMap.get(protocol);
            if (oldProtocolFactory == null)
                return -1;
            return oldProtocolFactory.getPriority();
        }
    }

    public boolean removeSendFileTransferFactory(String id) {
        if (id == null)
            return false;
        boolean removed = false;
        synchronized (sendFileTransferProtocolMap) {
            for (Iterator i = sendFileTransferProtocolMap.keySet().iterator(); i.hasNext(); ) {
                ProtocolFactory oldProtocolFactory = (ProtocolFactory) sendFileTransferProtocolMap.get(i.next());
                if (oldProtocolFactory == null)
                    continue;
                if (id.equals(oldProtocolFactory.getID())) {
                    i.remove();
                    removed = true;
                }
            }
            return removed;
        }
    }
}
