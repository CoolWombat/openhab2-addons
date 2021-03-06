/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Takes care of the communication with MiHome devices.
 *
 *
 * @author Patrick Boos - Initial contribution
 * @author Dimalo - JavaDoc and clean code
 *
 */
public abstract class XiaomiSocket {

    static final String MCAST_ADDR = "224.0.0.50";
    private static final int BUFFER_LENGTH = 1024;
    private static DatagramPacket datagramPacket = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);

    protected Thread socketReceiveThread;
    private static List<XiaomiSocketListener> listeners = new CopyOnWriteArrayList<>();
    private static final JsonParser parser = new JsonParser();
    protected static Logger logger = LoggerFactory.getLogger(XiaomiSocket.class);
    static ArrayList<XiaomiSocket> openSockets = new ArrayList<XiaomiSocket>();

    int port = 0;
    DatagramSocket socket;

    /**
     * Sets up an {@link XiaomiSocket} with the MiHome multicast address and a random port
     *
     */
    public XiaomiSocket() {
        this(0);
    }

    /**
     * Sets up an {@link XiaomiSocket} with the MiHome multicast address and a specific port
     *
     * @param port - the socket will be bound to this port
     */
    public XiaomiSocket(int port) {
        this.port = port;

        setupSocket();
    }

    abstract void setupSocket();

    /**
     * Interrupts the {@link ReceiverThread} and closes the {@link XiaomiSocket}.
     */
    private void closeSocket() {
        synchronized (XiaomiSocket.class) {
            if (socketReceiveThread != null) {
                socketReceiveThread.interrupt();
            }
            if (socket != null) {
                socket.close();
                logger.debug("Socket closed");
                socket = null;
            }
        }
    }

    /**
     * Registers a {@link XiaomiSocketListener} to be called back, when data is received.
     * If no {@link XiaomiSocket} exists, when the method is called, it is being set up.
     *
     * @param listener - {@link XiaomiSocketListener} to be called back
     */
    public synchronized void registerListener(XiaomiSocketListener listener) {
        if (socket == null) {
            setupSocket();
        }
        if (!listeners.contains(listener)) {
            logger.trace("Adding socket listener {}", listener.toString());
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a {@link XiaomiSocketListener}. If there are no listeners left,
     * the {@link XiaomiSocket} is being closed.
     *
     * @param listener - {@link XiaomiSocketListener} to be unregistered
     */
    public synchronized void unregisterListener(XiaomiSocketListener listener) {
        listeners.remove(listener);

        if (listeners.isEmpty()) {
            closeSocket();
        }
    }

    /**
     * Sends a message through the {@link XiaomiSocket} to a specific address and port
     *
     * @param message - Message to be sent
     * @param address - Address, to which the message shall be sent
     * @param port - - Port, through which the message shall be sent
     */
    public void sendMessage(String message, InetAddress address, int port) {
        try {
            byte[] sendData = message.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            logger.trace("Sending message: {} to {}", message, address.toString());
            socket.send(sendPacket);
        } catch (IOException e) {
            logger.error("Sending error", e);
        }
    }

    /**
     * @return - the port number of this {@link XiaomiSocket}
     */
    public int getPort() {
        return port;
    }

    /**
     * @return - a list of already open sockets
     */
    public static ArrayList<XiaomiSocket> getOpenSockets() {
        logger.debug("Open Sockets are {}", openSockets.toString());
        return openSockets;
    }

    /**
     * The thread, which waits for data on the {@link XiaomiSocket} and handles it, when received
     *
     * @author Patrick Boos - Initial contribution
     * @author Dimalo - comments and synchronized block for callback instead of copy
     *
     */
    class ReceiverThread extends Thread {
        @Override
        public void run() {
            logger.trace("Staring reveicer thread for socket on port {}", socket.getLocalPort());
            receiveData(socket, datagramPacket);
        }

        /**
         * This method is the main method of the {@link ReceiverThread} for the {@link XiaomiBridgeSocket}.
         * If the socket has data, it parses the data to a json object and calls all
         * {@link XiaomiSocketListener} and passes the data to them.
         *
         * @param socket - The multicast socket to listen to
         * @param dgram - The datagram to receive
         */
        private void receiveData(DatagramSocket socket, DatagramPacket dgram) {
            try {
                while (true) {
                    logger.trace("Thread waiting for data on port {}", port);
                    socket.receive(dgram);
                    String sentence = new String(dgram.getData(), 0, dgram.getLength());
                    JsonObject message = parser.parse(sentence).getAsJsonObject();
                    notifyAll(listeners, message);
                    logger.trace("Data received and notified {} listeners", listeners.size());
                }
            } catch (IOException e) {
                if (!isInterrupted()) {
                    logger.error("Error while receiving", e);
                } else {
                    logger.trace("Receiver thread was interrupted");
                }
            }
            logger.debug("Receiver thread ended");
        }

        /**
         * Notifies all {@link XiaomiSocketListener} on the parent {@link XiaomiSocket}
         *
         * @param listeners - a list of all {@link XiaomiSocketListener} to notify
         * @param message - the data message as {@link JsonObject}
         */
        synchronized void notifyAll(List<XiaomiSocketListener> listeners, JsonObject message) {
            for (XiaomiSocketListener listener : listeners) {
                listener.onDataReceived(message);
            }
        }
    }
}