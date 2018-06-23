/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ivan Popov - Bug 184211: JDI connectors throw NullPointerException if used separately
 *     			from Eclipse
 *     Google Inc - add support for accepting multiple connections
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import org.eclipse.jdi.TimeoutException;
import com.sun.jdi.connect.TransportTimeoutException;
import com.sun.jdi.connect.spi.ClosedConnectionException;
import com.sun.jdi.connect.spi.Connection;
import com.sun.jdi.connect.spi.TransportService;

public class SocketTransportService extends TransportService {

    /** Handshake bytes used just after connecting VM. */
    //$NON-NLS-1$
    private static final byte[] handshakeBytes = "JDWP-Handshake".getBytes();

    private Capabilities fCapabilities = new Capabilities() {

        @Override
        public boolean supportsAcceptTimeout() {
            return true;
        }

        @Override
        public boolean supportsAttachTimeout() {
            return true;
        }

        @Override
        public boolean supportsHandshakeTimeout() {
            return true;
        }

        @Override
        public boolean supportsMultipleConnections() {
            return false;
        }
    };

    private class SocketListenKey extends ListenKey {

        private String fAddress;

         SocketListenKey(String address) {
            fAddress = address;
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.jdi.connect.spi.TransportService.ListenKey#address()
		 */
        @Override
        public String address() {
            return fAddress;
        }
    }

    // for listening or accepting connectors
    private ServerSocket fServerSocket;

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.jdi.connect.spi.TransportService#accept(com.sun.jdi.connect.spi
	 * .TransportService.ListenKey, long, long)
	 */
    @Override
    public Connection accept(ListenKey listenKey, long attachTimeout, long handshakeTimeout) throws IOException {
        if (attachTimeout > 0) {
            if (attachTimeout > Integer.MAX_VALUE) {
                // approx 25 days!
                attachTimeout = Integer.MAX_VALUE;
            }
            fServerSocket.setSoTimeout((int) attachTimeout);
        }
        Socket socket;
        try {
            socket = fServerSocket.accept();
        } catch (SocketTimeoutException e) {
            throw new TransportTimeoutException();
        }
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        performHandshake(input, output, handshakeTimeout);
        return new SocketConnection(socket, input, output);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.TransportService#attach(java.lang.String,
	 * long, long)
	 */
    @Override
    public Connection attach(String address, long attachTimeout, long handshakeTimeout) throws IOException {
        //$NON-NLS-1$
        String[] strings = address.split(":");
        //$NON-NLS-1$
        String host = "localhost";
        int port = 0;
        if (strings.length == 2) {
            host = strings[0];
            port = Integer.parseInt(strings[1]);
        } else {
            port = Integer.parseInt(strings[0]);
        }
        return attach(host, port, attachTimeout, handshakeTimeout);
    }

    public Connection attach(final String host, final int port, long attachTimeout, final long handshakeTimeout) throws IOException {
        if (attachTimeout > 0) {
            if (attachTimeout > Integer.MAX_VALUE) {
                // approx 25 days!
                attachTimeout = Integer.MAX_VALUE;
            }
        }
        final IOException[] ex = new IOException[1];
        final SocketConnection[] result = new SocketConnection[1];
        Thread attachThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Socket socket = new Socket(host, port);
                    InputStream input = socket.getInputStream();
                    OutputStream output = socket.getOutputStream();
                    performHandshake(input, output, handshakeTimeout);
                    result[0] = new SocketConnection(socket, input, output);
                } catch (IOException e) {
                    ex[0] = e;
                }
            }
        }, ConnectMessages.SocketTransportService_0);
        attachThread.setDaemon(true);
        attachThread.start();
        try {
            attachThread.join(attachTimeout);
            if (attachThread.isAlive()) {
                attachThread.interrupt();
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
        }
        if (ex[0] != null) {
            throw ex[0];
        }
        return result[0];
    }

    void performHandshake(final InputStream in, final OutputStream out, final long timeout) throws IOException {
        final IOException[] ex = new IOException[1];
        final boolean[] handshakeCompleted = new boolean[1];
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    writeHandshake(out);
                    readHandshake(in);
                    handshakeCompleted[0] = true;
                } catch (IOException e) {
                    ex[0] = e;
                }
            }
        }, ConnectMessages.SocketTransportService_1);
        t.setDaemon(true);
        t.start();
        try {
            t.join(timeout);
        } catch (InterruptedException e1) {
        }
        if (handshakeCompleted[0])
            return;
        try {
            in.close();
            out.close();
        } catch (IOException e) {
        }
        if (ex[0] != null)
            throw ex[0];
        throw new TransportTimeoutException();
    }

    private void readHandshake(InputStream input) throws IOException {
        try {
            DataInputStream in = new DataInputStream(input);
            byte[] handshakeInput = new byte[handshakeBytes.length];
            in.readFully(handshakeInput);
            if (!Arrays.equals(handshakeInput, handshakeBytes))
                throw new //$NON-NLS-1$
                IOException(//$NON-NLS-1$
                "Received invalid handshake");
        } catch (EOFException e) {
            throw new ClosedConnectionException();
        }
    }

    private void writeHandshake(OutputStream out) throws IOException {
        out.write(handshakeBytes);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.TransportService#capabilities()
	 */
    @Override
    public Capabilities capabilities() {
        return fCapabilities;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.TransportService#description()
	 */
    @Override
    public String description() {
        //$NON-NLS-1$
        return "org.eclipse.jdt.debug: Socket Implementation of TransportService";
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.TransportService#name()
	 */
    @Override
    public String name() {
        //$NON-NLS-1$
        return "org.eclipse.jdt.debug_SocketTransportService";
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.TransportService#startListening()
	 */
    @Override
    public ListenKey startListening() throws IOException {
        // not used by jdt debug.
        return startListening(null);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.jdi.connect.spi.TransportService#startListening(java.lang.String)
	 */
    @Override
    public ListenKey startListening(String address) throws IOException {
        String host = null;
        // jdt debugger will always specify an address in
        int port = 0;
        // the form localhost:port
        if (address != null) {
            //$NON-NLS-1$
            String[] strings = address.split(":");
            //$NON-NLS-1$
            host = "localhost";
            if (strings.length == 2) {
                host = strings[0];
                port = Integer.parseInt(strings[1]);
            } else {
                port = Integer.parseInt(strings[0]);
            }
        }
        if (host == null) {
            //$NON-NLS-1$
            host = "localhost";
        }
        fServerSocket = new ServerSocket(port);
        port = fServerSocket.getLocalPort();
        //$NON-NLS-1$
        ListenKey listenKey = new SocketListenKey(host + ":" + port);
        return listenKey;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.jdi.connect.spi.TransportService#stopListening(com.sun.jdi.connect
	 * .spi.TransportService.ListenKey)
	 */
    @Override
    public void stopListening(ListenKey arg1) throws IOException {
        if (fServerSocket != null) {
            try {
                fServerSocket.close();
            } catch (IOException e) {
            }
        }
        fServerSocket = null;
    }
}
