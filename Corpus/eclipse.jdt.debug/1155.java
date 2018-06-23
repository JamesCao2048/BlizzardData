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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdi.internal.VirtualMachineManagerImpl;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

public class SocketListeningConnectorImpl extends ConnectorImpl implements ListeningConnector {

    /** Port to which is attached. */
    private int fPort;

    /** Timeout before accept returns. */
    private int fTimeout;

    /**
	 * Creates new SocketAttachingConnectorImpl.
	 */
    public  SocketListeningConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
        super(virtualMachineManager);
        // Create communication protocol specific transport.
        SocketTransportImpl transport = new SocketTransportImpl();
        setTransport(transport);
    }

    /**
	 * @return Returns the default arguments.
	 */
    @Override
    public Map<String, Connector.Argument> defaultArguments() {
        HashMap<String, Connector.Argument> arguments = new HashMap<String, Connector.Argument>(1);
        // Port
        IntegerArgumentImpl intArg = new IntegerArgumentImpl("port", ConnectMessages.SocketListeningConnectorImpl_Port_number_at_which_to_listen_for_VM_connections_1, ConnectMessages.SocketListeningConnectorImpl_Port_2, true, SocketTransportImpl.MIN_PORTNR, //$NON-NLS-1$  
        SocketTransportImpl.MAX_PORTNR);
        arguments.put(intArg.name(), intArg);
        // Timeout
        intArg = new IntegerArgumentImpl("timeout", ConnectMessages.SocketListeningConnectorImpl_Timeout_before_accept_returns_3, ConnectMessages.SocketListeningConnectorImpl_Timeout_4, false, 0, //$NON-NLS-1$  
        Integer.MAX_VALUE);
        arguments.put(intArg.name(), intArg);
        // FIXME: connectionLimit is not actually used in this class, but in the higher-level controller, SocketListenConnector.  
        // But IntegerArgumentImpl is package restricted so we must put it here.
        //$NON-NLS-1$
        intArg = new IntegerArgumentImpl("connectionLimit", ConnectMessages.SocketListeningConnectorImpl_Limit_incoming_connections, ConnectMessages.SocketListeningConnectorImpl_Limit, false, 0, Integer.MAX_VALUE);
        // mimics previous behaviour, allowing a single connection
        intArg.setValue(1);
        arguments.put(intArg.name(), intArg);
        return arguments;
    }

    /**
	 * @return Returns a short identifier for the connector.
	 */
    @Override
    public String name() {
        //$NON-NLS-1$
        return "com.sun.jdi.SocketListen";
    }

    /**
	 * @return Returns a human-readable description of this connector and its
	 *         purpose.
	 */
    @Override
    public String description() {
        return ConnectMessages.SocketListeningConnectorImpl_Accepts_socket_connections_initiated_by_other_VMs_5;
    }

    /**
	 * Retrieves connection arguments.
	 */
    private void getConnectionArguments(Map<String, ? extends Connector.Argument> connectionArgs) throws IllegalConnectorArgumentsException {
        //$NON-NLS-1$
        String attribute = "port";
        try {
            // If listening port is not specified, use port 0
            IntegerArgument argument = (IntegerArgument) connectionArgs.get(attribute);
            if (argument != null && argument.value() != null) {
                fPort = argument.intValue();
            } else {
                fPort = 0;
            }
            // Note that timeout is not used in SUN's ListeningConnector, but is
            // used by our
            // LaunchingConnector.
            //$NON-NLS-1$
            attribute = "timeout";
            argument = (IntegerArgument) connectionArgs.get(attribute);
            if (argument != null && argument.value() != null) {
                fTimeout = argument.intValue();
            } else {
                fTimeout = 0;
            }
        } catch (ClassCastException e) {
            throw new IllegalConnectorArgumentsException(ConnectMessages.SocketListeningConnectorImpl_Connection_argument_is_not_of_the_right_type_6, attribute);
        } catch (NullPointerException e) {
            throw new IllegalConnectorArgumentsException(ConnectMessages.SocketListeningConnectorImpl_Necessary_connection_argument_is_null_7, attribute);
        } catch (NumberFormatException e) {
            throw new IllegalConnectorArgumentsException(ConnectMessages.SocketListeningConnectorImpl_Connection_argument_is_not_a_number_8, attribute);
        }
    }

    /**
	 * Listens for one or more connections initiated by target VMs.
	 * 
	 * @return Returns the address at which the connector is listening for a
	 *         connection.
	 */
    @Override
    public String startListening(Map<String, ? extends Connector.Argument> connectionArgs) throws IOException, IllegalConnectorArgumentsException {
        getConnectionArguments(connectionArgs);
        String result = null;
        try {
            result = ((SocketTransportImpl) fTransport).startListening(fPort);
        } catch (IllegalArgumentException e) {
            throw new IllegalConnectorArgumentsException(ConnectMessages.SocketListeningConnectorImpl_ListeningConnector_Socket_Port, "port");
        }
        return result;
    }

    /* (non-Javadoc)
	 * @see com.sun.jdi.connect.ListeningConnector#stopListening(java.util.Map)
	 */
    @Override
    public void stopListening(Map<String, ? extends Connector.Argument> connectionArgs) throws IOException {
        ((SocketTransportImpl) fTransport).stopListening();
    }

    /**
	 * Waits for a target VM to attach to this connector.
	 * 
	 * @return Returns a connected Virtual Machine.
	 */
    @Override
    public VirtualMachine accept(Map<String, ? extends Connector.Argument> connectionArgs) throws IOException, IllegalConnectorArgumentsException {
        getConnectionArguments(connectionArgs);
        SocketConnection connection = (SocketConnection) ((SocketTransportImpl) fTransport).accept(fTimeout, 0);
        return establishedConnection(connection);
    }

    /**
	 * @return Returns whether this listening connector supports multiple
	 *         connections for a single argument map.
	 */
    @Override
    public boolean supportsMultipleConnections() {
        return true;
    }

    /**
	 * @return Returns port number that is listened to.
	 */
    public int listeningPort() {
        return fPort;
    }
}
