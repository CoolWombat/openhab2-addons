/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.CHANNEL_BUTTON;

import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author Patrick Boos - Initial contribution
 */
public class XiaomiSensorSwitchHandler extends XiaomiSensorBaseHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public XiaomiSensorSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    void parseReport(JsonObject data) {
        if (data.has("status")) {
            triggerChannel(CHANNEL_BUTTON, data.get("status").getAsString().toUpperCase());
        }
    }
}
