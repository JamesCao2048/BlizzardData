/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;

import java.io.IOException;
import com.sun.jdi.connect.Transport;
import com.sun.jdi.connect.spi.Connection;
import com.sun.jdi.connect.spi.TransportService.ListenKey;

public class SocketTransportImpl implements Transport {

    //$NON-NLS-1$
    public static final String TRANSPORT_NAME = "dt_socket";

    public static final int MIN_PORTNR = 0;

    public static final int MAX_PORTNR = 65535;

    SocketTransportService service;

    private ListenKey fListenKey;

    /**
	 * Constructs new SocketTransportImpl.
	 */
    public  SocketTransportImpl() {
        service = new SocketTransportService();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.Transport#name()
	 */
    @Override
    public String name() {
        return TRANSPORT_NAME;
    }

    public Connection attach(String hostname, int port, long attachTimeout, long handshakeTimeout) throws IOException {
        return service.attach(hostname, port, attachTimeout, handshakeTimeout);
    }

    public String startListening(int port) throws IOException {
        //$NON-NLS-1$
        fListenKey = service.startListening(port + "");
        return fListenKey.address();
    }

    public void stopListening() throws IOException {
        service.stopListening(fListenKey);
    }

    public Connection accept(long attachTimeout, long handshakeTimeout) throws IOException {
        return service.accept(fListenKey, attachTimeout, handshakeTimeout);
    }
}
