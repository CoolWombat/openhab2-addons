/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.socket;

import com.google.gson.JsonObject;

/**
 * Interface for a listener on the {@link XiaomiBridgeSocket}.
 * When it is registered on the socket, it gets called back each time, the {@link XiaomiBridgeSocket} receives data.
 *
 * @author Patrick Boos - Initial
 */
public interface XiaomiSocketListener {
    /**
     * Callback method for the {@link XiaomiSocketListener}
     *
     * @param message - The received message in JSON format
     */
    void onDataReceived(JsonObject message);
}
