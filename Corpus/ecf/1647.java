/*******************************************************************************
* Copyright (c) 2010 IBM, and others. 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   IBM Corporation - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.provider.filetransfer.util;

import java.net.URI;
import java.net.URL;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.ProxyAddress;
import org.eclipse.ecf.internal.provider.filetransfer.Activator;

/**
 * Proxy setup utilities.  
 *
 * NOTE:  Use of this class implies the presence of the core.net.proxy
 * API...this class will not load (NoClassDefFoundError will be thrown if load/use is attempted)
 * if the core.net.proxy bundle is not present in the runtime.  
 * 
 * @noextend This class is not intended to be extended by clients.
 * @since 3.1
 */
public class ProxySetupHelper {

    public static Proxy getProxy(String url) {
        Proxy proxy = null;
        try {
            IProxyService proxyService = Activator.getDefault().getProxyService();
            // Only do this if platform service exists
            if (proxyService != null && proxyService.isProxiesEnabled()) {
                // Setup via proxyService entry
                URI uri = new URI(url);
                final IProxyData[] proxies = proxyService.select(uri);
                IProxyData selectedProxy = selectProxyFromProxies(uri.getScheme(), proxies);
                if (selectedProxy != null) {
                    proxy = new Proxy(((selectedProxy.getType().equalsIgnoreCase(IProxyData.SOCKS_PROXY_TYPE)) ? Proxy.Type.SOCKS : Proxy.Type.HTTP), new ProxyAddress(selectedProxy.getHost(), selectedProxy.getPort()), selectedProxy.getUserId(), selectedProxy.getPassword());
                }
            }
        } catch (Exception e) {
            Activator.logNoProxyWarning(e);
        } catch (NoClassDefFoundError e) {
            Activator.logNoProxyWarning(e);
        }
        return proxy;
    }

    public static Proxy getSocksProxy(URL url) {
        String host = url.getHost();
        int port = url.getPort();
        //$NON-NLS-1$
        String strURL = IProxyData.SOCKS_PROXY_TYPE + "://" + host;
        if (port != -1) {
            //$NON-NLS-1$
            strURL += ":" + port;
        }
        return ProxySetupHelper.getProxy(strURL);
    }

    /**
	 * Select a single proxy from a set of proxies available for the given host.  This implementation
	 * selects in the following manner:  1) If proxies provided is null or array of 0 length, null 
	 * is returned.  If only one proxy is available (array of length 1) then the entry is returned.
	 * If proxies provided is length greater than 1, then if the type of a proxy in the array matches the given
	 * protocol (e.g. http, https), then the first matching proxy is returned.  If the protocol does
	 * not match any of the proxies, then the *first* proxy (i.e. proxies[0]) is returned.  
	 * 
	 * @param protocol the target protocol (e.g. http, https, scp, etc).  Will not be <code>null</code>.
	 * @param proxies the proxies to select from.  May be <code>null</code> or array of length 0.
	 * @return proxy data selected from the proxies provided.  
	 */
    public static IProxyData selectProxyFromProxies(String protocol, IProxyData[] proxies) {
        if (proxies == null || proxies.length == 0)
            return null;
        // If only one proxy is available, then use that
        if (proxies.length == 1)
            return proxies[0];
        // one...if not found then use first
        if (//$NON-NLS-1$
        protocol.equalsIgnoreCase("http")) {
            for (int i = 0; i < proxies.length; i++) {
                if (proxies[i].getType().equals(IProxyData.HTTP_PROXY_TYPE))
                    return proxies[i];
            }
        } else if (//$NON-NLS-1$
        protocol.equalsIgnoreCase("https")) {
            for (int i = 0; i < proxies.length; i++) {
                if (proxies[i].getType().equals(IProxyData.HTTPS_PROXY_TYPE))
                    return proxies[i];
            }
        }
        // If we haven't found it yet, then return the first one.
        return proxies[0];
    }
}
